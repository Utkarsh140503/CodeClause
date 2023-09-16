package com.utkarsh.voicevoyant;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class AppFragment extends Fragment {

    private ImageView appIcon;
    private TextView appName;
    private TextToSpeech textToSpeech;

    private String appNameText;
    private int appIconResId;

    public AppFragment() {
        // Required empty public constructor
    }

    public static AppFragment newInstance(String appNameText, int appIconResId) {
        AppFragment fragment = new AppFragment();
        Bundle args = new Bundle();
        args.putString("appNameText", appNameText);
        args.putInt("appIconResId", appIconResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appNameText = getArguments().getString("appNameText");
            appIconResId = getArguments().getInt("appIconResId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app, container, false);

        appIcon = view.findViewById(R.id.appIcon);
        appName = view.findViewById(R.id.appName);

        appIcon.setImageResource(appIconResId);
        appName.setText(appNameText);

        textToSpeech = new TextToSpeech(requireContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Handle the error
                    } else {
                        // Speak the app's name when the fragment is created
                        speakText(appNameText);
                    }
                } else {
                    // Handle the error
                }
            }
        });

        return view;
    }

    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
