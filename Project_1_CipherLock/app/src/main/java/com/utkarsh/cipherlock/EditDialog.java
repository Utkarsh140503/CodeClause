package com.utkarsh.cipherlock;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditDialog extends Dialog implements View.OnClickListener {
    private EditText editTextUsername, editTextWebsite, editTextPassword;
    private Button buttonUpdate;
    private long selectedPasswordId;
    private boolean isLocallySaved;

    public EditDialog(Context context, String selectedPassword, long selectedPasswordId) {
        super(context);
        this.selectedPasswordId = selectedPasswordId;
        this.isLocallySaved = isLocallySaved;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_dialog);

        editTextUsername = findViewById(R.id.editTextEditUsername);
        editTextWebsite = findViewById(R.id.editTextEditWebsite);
        editTextPassword = findViewById(R.id.editTextEditPassword);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        buttonUpdate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonUpdate) {
            // Handle the update operation here using the data in the EditText fields
            String username = editTextUsername.getText().toString().trim();
            String website = editTextWebsite.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (isLocallySaved) {
                // Update data in the SQLite database
                DatabaseHelper dbHelper = new DatabaseHelper(getContext());
                SQLiteDatabase database = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_USERNAME, username);
                values.put(DatabaseHelper.COLUMN_WEBSITE, website);
                values.put(DatabaseHelper.COLUMN_PASSWORD, password);

                String selection = DatabaseHelper.COLUMN_ID + " = ?";
                String[] selectionArgs = {String.valueOf(selectedPasswordId)};

                int rowCount = database.update(DatabaseHelper.TABLE_NAME, values, selection, selectionArgs);
                database.close();

                if (rowCount > 0) {
                    Toast.makeText(getContext(), "Data updated locally", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error updating data locally", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Update data in the Firebase Realtime Database
                DatabaseReference passwordRef = FirebaseDatabase.getInstance().getReference("Passwords").child(String.valueOf(selectedPasswordId));
                passwordRef.child("Username").setValue(username);
                passwordRef.child("Website").setValue(website);
                passwordRef.child("Password").setValue(password);

                Toast.makeText(getContext(), "Data updated in Firebase", Toast.LENGTH_SHORT).show();
            }

            // Notify your MainActivity to update the UI
            if (getContext() instanceof MainActivity) {
                ((MainActivity) getContext()).displayLocalPasswords();
                ((MainActivity) getContext()).displayFirebasePasswords();
                ((MainActivity) getContext()).displayPasswords();
            }
        }

        dismiss(); // Close the dialog after handling the operation
    }
}
