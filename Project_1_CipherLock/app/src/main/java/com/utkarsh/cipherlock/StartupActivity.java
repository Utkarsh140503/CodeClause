package com.utkarsh.cipherlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StartupActivity extends AppCompatActivity {

    private EditText editTextUsernameLogin;
    private EditText editTextPasswordLogin;
    private Button buttonLogin;
    private EditText editTextUsernameSignup;
    private EditText editTextPasswordSignup;
    private Button buttonSignup;
    private TextView welcomeMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        editTextUsernameLogin = findViewById(R.id.editTextUsernameLogin);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        editTextUsernameSignup = findViewById(R.id.editTextUsernameSignup);
        editTextPasswordSignup = findViewById(R.id.editTextPasswordSignup);
        buttonSignup = findViewById(R.id.buttonSignup);
        welcomeMessageTextView = findViewById(R.id.welcomeMessageTextView);

        // Check if a username and password are saved in SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String savedUsername = preferences.getString("username", "");
        final String savedPassword = preferences.getString("password", "");

        if (savedUsername.isEmpty() || savedPassword.isEmpty()) {
            // Username and/or password not found, show signup components
            showSignupForm();
        } else {
            // Username and password found, show login components
            showLoginForm();

            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check entered username and password
                    String enteredUsername = editTextUsernameLogin.getText().toString();
                    String enteredPassword = editTextPasswordLogin.getText().toString();

                    if (enteredUsername.equals(savedUsername) && enteredPassword.equals(savedPassword)) {
                        // Username and password match, redirect to MainActivity
                        // You can replace MainActivity.class with your desired destination activity
                        Toast.makeText(StartupActivity.this, "Welcome " + savedUsername + "!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(StartupActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Username or password does not match, show error
                        Toast.makeText(StartupActivity.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save username and password in SharedPreferences
                String newUsername = editTextUsernameSignup.getText().toString();
                String newPassword = editTextPasswordSignup.getText().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", newUsername);
                editor.putString("password", newPassword);
                editor.apply();

                Toast.makeText(StartupActivity.this, "Welcome! Start Adding your passwords.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(StartupActivity.this, MainActivity.class));
                finish();
            }
        });

        // Set welcome message based on whether password is saved or not
        if (savedPassword.isEmpty()) {
            welcomeMessageTextView.setText("Welcome to CipherLock. Sign Up");
        } else {
            welcomeMessageTextView.setText("Welcome Back. Login");
        }
    }

    private void showLoginForm() {
        editTextUsernameLogin.setVisibility(View.VISIBLE);
        editTextPasswordLogin.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);

        editTextUsernameSignup.setVisibility(View.GONE);
        editTextPasswordSignup.setVisibility(View.GONE);
        buttonSignup.setVisibility(View.GONE);
    }

    private void showSignupForm() {
        editTextUsernameLogin.setVisibility(View.GONE);
        editTextPasswordLogin.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.GONE);

        editTextUsernameSignup.setVisibility(View.VISIBLE);
        editTextPasswordSignup.setVisibility(View.VISIBLE);
        buttonSignup.setVisibility(View.VISIBLE);
    }
}
