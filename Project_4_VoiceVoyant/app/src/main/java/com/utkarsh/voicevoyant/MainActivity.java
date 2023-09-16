package com.utkarsh.voicevoyant;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppListAdapter appListAdapter;
    private TextToSpeech textToSpeech;
    private GestureDetector gestureDetector;
    private EditText searchEditText;
    TextView footerTextView;

    private String[] appNames = {"Youtube", "Contacts", "Camera", "Spotify"};
    private int[] appIcons = {R.drawable.yt, R.drawable.contact, R.drawable.camera, R.drawable.spotify};
    private int currentAppIndex = 0;

    private static final int LEFT_SWIPE_ACTION = 2; // Define a unique action code for left swipe
    private static final int SPEECH_REQUEST_CODE = 123;
    private static final String UTTERANCE_ID = "opening_youtube";
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 1;
    private static final String SPOTIFY_COMMAND = "play on Spotify";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        footerTextView = findViewById(R.id.footerTextView);
        footerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the LinkedIn profile in a web browser
                String linkedInProfileUrl = "https://utkarsh140503.github.io/Portfolio/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedInProfileUrl));
                startActivity(intent);
            }
        });

        // Initialize the TextToSpeech engine
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        showToast("Text-to-speech initialization failed.");
                    } else {
                        speakText("Welcome to Voice Voyant. Here are the instructions. Swipe down to go to the next application. Swipe up to go to the last application. Swipe right to open the application. Swipe left to View developers website. Current Selected App " + appNames[currentAppIndex]);
                    }
                } else {
                    showToast("Text-to-speech initialization failed.");
                }
            }
        });

        // Add an UtteranceProgressListener to detect when speech finishes
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speech started
            }

            @Override
            public void onDone(String utteranceId) {
                if (utteranceId.equals(UTTERANCE_ID)) {
                    // Speech finished, start voice recognition
                    startVoiceRecognition();
                }
            }

            @Override
            public void onError(String utteranceId) {
                // Speech error
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        appListAdapter = new AppListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(appListAdapter);

        searchEditText = new EditText(this);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 100) {
                    if (deltaX > 0) {
                        // Swipe right, open the selected app
                        openSelectedApp(currentAppIndex);
                    } else {
                        // Swipe left, open the website
                        openWebsite("https://utkarsh140503.github.io/Portfolio/");
                    }
                } else if (Math.abs(deltaY) > 100) {
                    if (deltaY > 0) {
                        // Swipe down, move to the next app
                        currentAppIndex = (currentAppIndex + 1) % appIcons.length;
                    } else {
                        // Swipe up, move to the previous app
                        currentAppIndex = (currentAppIndex - 1 + appIcons.length) % appIcons.length;
                    }

                    // Notify the adapter of the selected app
                    appListAdapter.setSelectedApp(currentAppIndex);

                    // Speak the selected app's name
                    speakText("Current Selected App " + appNames[currentAppIndex]);
                }
                return true;
            }
        });

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        // Check for and request the READ_CONTACTS permission at runtime
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACTS_PERMISSION_REQUEST_CODE);
        }
    }

    private void openWebsite(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        speakText("Opening developers website");
        try {
            startActivity(browserIntent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error opening the website.");
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, continue with your app initialization
                initializeApp();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                showToast("Permission denied to access contacts.");
            }
        }
    }

    // Initialize your app after permission is granted
    private void initializeApp() {
        // Your app initialization code here, including the code to access contacts
    }

    private void openSelectedApp(int position) {
        switch (position) {
            case 0:
                // Open YouTube
                // Use the UTTERANCE_ID to ensure speech completion before starting voice recognition
                speakText("Opening YouTube. What do you want to watch?", UTTERANCE_ID);
                break;
            case 1:
                // Open Contacts
                // Check if the READ_CONTACTS permission is granted before opening Contacts
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    openContactByName("John Doe"); // Replace with the desired contact name
                } else {
                    showToast("Permission denied to access contacts.");
                }
                break;
            case 2:
                // Open Camera
                Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                speakText("Opening Camera");
                try {
                    startActivity(cameraIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error opening the app.");
                }
                break;
            case 3: // Spotify
                // Check if the Spotify app is installed
                if (isAppInstalled("com.spotify.music")) {
                    // Open Spotify app and send a voice command
                    Intent spotifyIntent = new Intent(Intent.ACTION_SEND);
                    spotifyIntent.setPackage("com.spotify.music");
                    spotifyIntent.setType("text/plain");
                    spotifyIntent.putExtra(Intent.EXTRA_TEXT, SPOTIFY_COMMAND);
                    try {
                        startActivity(spotifyIntent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        showToast("Error opening Spotify.");
                    }
                } else {
                    // If Spotify app is not installed, open Spotify website
                    String spotifyUrl = "https://www.spotify.com/";
                    Intent spotifyBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl));
                    speakText("Opening Spotify");
                    try {
                        startActivity(spotifyBrowserIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Error opening Spotify.");
                    }
                }
                break;
            default:
                showToast("Invalid app selection.");
                break;
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Method to open a contact by name
    private void openContactByName(String contactName) {
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(contactName));
        Intent contactsIntent = new Intent(Intent.ACTION_VIEW, uri);
        speakText("Opening Contacts");
        try {
            startActivity(contactsIntent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error opening the app.");
        }
    }

    private void showToast(String message) {
        // Display a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void speakText(String text, String utteranceId) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private void startVoiceRecognition() {
        try {
            // Start voice recognition intent
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            showToast("Voice recognition not available on this device.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && results.size() > 0) {
                    String spokenText = results.get(0);
                    // Perform a YouTube search based on the spoken text
                    performYouTubeSearch(spokenText);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void performYouTubeSearch(String query) {
        // Construct a YouTube search URL and open it in a web browser
        String searchUrl = "https://www.youtube.com/results?search_query=" + Uri.encode(query);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
        startActivity(browserIntent);
    }

    private class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

        private int selectedItem = RecyclerView.NO_POSITION;

        public AppListAdapter() {
            // Constructor
            // Set the initially selected item here
            setSelectedApp(0);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_app, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.appIcon.setImageResource(appIcons[position]);

            // Set the application name with bold text style using HTML formatting
            holder.appName.setText(Html.fromHtml("<b>" + appNames[position] + "</b>"));

            // Set the text color to white
            holder.appName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));

            // Highlight the selected item with a custom background
            if (position == selectedItem) {
                holder.itemView.setBackgroundResource(R.drawable.blackborder); // Set your custom background here
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            }
        }

        @Override
        public int getItemCount() {
            return appNames.length;
        }

        public void setSelectedApp(int position) {
            selectedItem = position;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView appIcon;
            TextView appName;

            ViewHolder(View itemView) {
                super(itemView);
                appIcon = itemView.findViewById(R.id.appIcon);
                appName = itemView.findViewById(R.id.appName);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
