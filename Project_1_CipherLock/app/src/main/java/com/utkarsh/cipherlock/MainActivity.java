package com.utkarsh.cipherlock;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    EditText editTextUsername, editTextWebsite, editTextPassword;
    ImageView buttonSaveLocally, buttonSaveToFirebase;
    ListView listViewPasswords;
    ArrayAdapter<String> passwordAdapter;
    ArrayList<String> passwordList;
    ArrayList<String> firebasePasswordList;
    ArrayList<String> localPasswordList;
    ArrayList<Long> passwordIds; // Store password IDs
    DatabaseHelper dbHelper;
    DatabaseReference firebaseDatabase;
    ValueEventListener firebaseValueEventListener;
    RadioGroup radioGroup;
    ImageView emptyListViewImage; // ImageView for empty ListView
    RadioButton radioButtonAll, radioButtonLocal, radioButtonFirebase;
    FloatingActionButton fabAddPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radioGroup = findViewById(R.id.radioGroupFilter);
        radioButtonAll = findViewById(R.id.radioButtonAll);
        radioButtonLocal = findViewById(R.id.radioButtonLocal);
        radioButtonFirebase = findViewById(R.id.radioButtonFirebase);
        emptyListViewImage = findViewById(R.id.emptyListViewImage); // Initialize empty ListView image view
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextWebsite = findViewById(R.id.editTextWebsite);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSaveLocally = findViewById(R.id.buttonSaveLocally);
        buttonSaveToFirebase = findViewById(R.id.buttonSaveToFirebase);
        listViewPasswords = findViewById(R.id.listViewPasswords);

        dbHelper = new DatabaseHelper(this);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("Passwords");

        passwordList = new ArrayList<>();
        passwordAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, passwordList);
        listViewPasswords.setAdapter(passwordAdapter);

        firebasePasswordList = new ArrayList<>();
        localPasswordList = new ArrayList<>();
        passwordIds = new ArrayList<>(); // Initialize password IDs

        buttonSaveLocally.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocally();
            }
        });

        buttonSaveToFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFirebase();
            }
        });

        radioButtonAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPasswords();
            }
        });

        radioButtonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocalPasswords();
            }
        });

        radioButtonFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFirebasePasswords();
            }
        });

        listViewPasswords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the password details and ID of the selected password
                String selectedPassword = getPasswordFromList(position);
                long selectedPasswordId = getPasswordIdFromList(position);

                // Show the edit or delete dialog with password ID
                showEditDeleteDialog(selectedPassword, selectedPasswordId);
            }
        });

        // Initialize the ValueEventListener for Firebase updates
        firebaseValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                firebasePasswordList.clear();

                for (DataSnapshot passwordSnapshot : dataSnapshot.getChildren()) {
                    String idString = passwordSnapshot.getKey(); // Get the Firebase key as a string

                    String username = passwordSnapshot.child("Username").getValue(String.class);
                    String website = passwordSnapshot.child("Website").getValue(String.class);
                    String password = passwordSnapshot.child("Password").getValue(String.class);
                    String timestamp = passwordSnapshot.child("Timestamp").getValue(String.class);
                    Integer savedLocallyValue = passwordSnapshot.child("SavedLocally").getValue(Integer.class);
                    int savedLocally = (savedLocallyValue != null) ? savedLocallyValue.intValue() : 0;

                    String savedStatus = (savedLocally == 1) ? "Saved: Locally" : "Saved: Firebase";
                    firebasePasswordList.add("ID: " + idString + "\nUsername: " + username + "\nWebsite: " + website + "\nPassword: " + password + "\nTimestamp: " + timestamp + "\n" + savedStatus);
                }

                // Update the combined password list and notify the adapter
                displayPasswords();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error, if needed
            }
        };

        // Add the ValueEventListener to the Firebase reference
        firebaseDatabase.addValueEventListener(firebaseValueEventListener);

        // Fetch and display passwords when the activity starts
        displayLocalPasswords();
        displayFirebasePasswords();
        displayPasswords();
    }

    // Update the combined password list and notify the adapter
    void displayPasswords() {
        passwordList.clear();
        if (radioButtonAll.isChecked()) {
            passwordList.addAll(localPasswordList);
            passwordList.addAll(firebasePasswordList);
        } else if (radioButtonLocal.isChecked()) {
            passwordList.addAll(localPasswordList);
        } else if (radioButtonFirebase.isChecked()) {
            passwordList.addAll(firebasePasswordList);
        }
        // Update the visibility of the empty ListView image
        updateEmptyListViewImageVisibility();
        passwordAdapter.notifyDataSetChanged();
    }

    // Methods to display passwords based on radio button selection
    void displayLocalPasswords() {
        localPasswordList.clear();

        // Fetch and add passwords from the SQLite database
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") long passwordId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
                @SuppressLint("Range") String website = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_WEBSITE));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));
                @SuppressLint("Range") int savedLocally = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SAVED_LOCALLY));

                String savedStatus = (savedLocally == 1) ? "Saved: Locally" : "Saved: Firebase";
                localPasswordList.add("ID: " + passwordId + "\nUsername: " + username + "\nWebsite: " + website + "\nPassword: " + password + "\nTimestamp: " + timestamp + "\n" + savedStatus);
            }
            cursor.close();
        }

        // Update the combined password list and notify the adapter
        displayPasswords();
    }

    void displayFirebasePasswords() {
        // Firebase passwords are already fetched in onDataChange
        displayPasswords();
    }

    private void saveLocally() {
        String username = editTextUsername.getText().toString().trim();
        String website = editTextWebsite.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String timestamp = getCurrentTimestamp();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(website) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save data locally using SQLite database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_WEBSITE, website);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, timestamp);
        values.put(DatabaseHelper.COLUMN_SAVED_LOCALLY, 1); // Indicate that data is saved locally

        try {
            long newRowId = database.insert(DatabaseHelper.TABLE_NAME, null, values);
            if (newRowId != -1) {
                Toast.makeText(this, "Data saved locally with ID: " + newRowId, Toast.LENGTH_LONG).show();

                // Update the ListView to display the locally saved password
                displayLocalPasswords();
                editTextUsername.setText("");
                editTextWebsite.setText("");
                editTextPassword.setText("");
            } else {
                Toast.makeText(this, "Error saving data locally", Toast.LENGTH_LONG).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
    }

    private void saveToFirebase() {
        String username = editTextUsername.getText().toString().trim();
        String website = editTextWebsite.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String timestamp = getCurrentTimestamp();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(website) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference passwordRef = firebaseDatabase.push(); // Create a new child node in Firebase
        passwordRef.child("Username").setValue(username);
        passwordRef.child("Website").setValue(website);
        passwordRef.child("Password").setValue(password);
        passwordRef.child("Timestamp").setValue(timestamp);
        passwordRef.child("SavedLocally").setValue(0); // Indicate that data is saved to Firebase

        Toast.makeText(this, "Data saved to Firebase with Password ID: " + passwordRef.getKey(), Toast.LENGTH_LONG).show();

        // Clear input fields
        editTextUsername.setText("");
        editTextWebsite.setText("");
        editTextPassword.setText("");
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private String getPasswordFromList(int position) {
        if (position >= 0 && position < passwordList.size()) {
            return passwordList.get(position);
        }
        return null;
    }

    private long getPasswordIdFromList(int position) {
        if (position >= 0 && position < passwordIds.size()) {
            return passwordIds.get(position);
        }
        return -1; // Invalid ID
    }

    private void showEditDeleteDialog(String selectedPassword, long selectedPasswordId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password Info.");
        builder.setMessage(selectedPassword);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

//        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Show the edit dialog with password ID
//                showEditDialog(selectedPassword, selectedPasswordId);
//            }
//        });
//
//        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Show the delete dialog with password ID
//                showDeleteDialog(selectedPasswordId);
//            }
//        });

        builder.create().show();
    }

    private void showEditDialog(String selectedPassword, long selectedPasswordId) {
        EditDialog editDialog = new EditDialog(this, selectedPassword, selectedPasswordId);
        editDialog.show();
    }

    private void showDeleteDialog(long selectedPasswordId) {
        DeleteDialog deleteDialog = new DeleteDialog(this, selectedPasswordId);
        deleteDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close(); // Close the SQLite database when the activity is destroyed
        // Remove the ValueEventListener when the activity is destroyed
        firebaseDatabase.removeEventListener(firebaseValueEventListener);
    }

    private void updateEmptyListViewImageVisibility() {
        if (passwordList.isEmpty()) {
            emptyListViewImage.setVisibility(View.VISIBLE);
            listViewPasswords.setVisibility(View.GONE);
        } else {
            emptyListViewImage.setVisibility(View.GONE);
            listViewPasswords.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch and display passwords when the activity resumes
        displayLocalPasswords();
        displayFirebasePasswords();
        displayPasswords();
    }
}
