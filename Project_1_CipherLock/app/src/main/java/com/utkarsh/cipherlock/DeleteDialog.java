package com.utkarsh.cipherlock;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DeleteDialog extends Dialog implements View.OnClickListener {
    private Button buttonDelete;

    public DeleteDialog(Context context, long selectedPassword) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_dialog);

        buttonDelete = findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonDelete) {
            // Handle the delete operation here
            // Delete the data from your database, either SQLite or Firebase
            // Notify your MainActivity to update the UI
        }

        dismiss(); // Close the dialog after handling the operation
    }
}
