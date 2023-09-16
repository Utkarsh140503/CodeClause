package com.utkarsh.securevisage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_FINGERPRINT_PERMISSION = 100;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newSingleThreadExecutor();

        // Initialize the BiometricPrompt
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                // Display the Toast message on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ImagesActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        // Configure the prompt
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate with your fingerprint or face")
                .setNegativeButtonText("Cancel")
                .build();
    }

    public void authenticateWithBiometrics(android.view.View view) {
        // Check if the device has a fingerprint sensor
        FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(this);
        if (!fingerprintManager.isHardwareDetected()) {
            // Handle devices without a fingerprint sensor
            Toast.makeText(this, "Fingerprint sensor not available", Toast.LENGTH_SHORT).show();
        } else {
            // Check for the fingerprint permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_BIOMETRIC}, REQUEST_FINGERPRINT_PERMISSION);
            } else {
                // Authenticate with BiometricPrompt
                biometricPrompt.authenticate(promptInfo);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINGERPRINT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, authenticate with BiometricPrompt
                biometricPrompt.authenticate(promptInfo);
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied for fingerprint authentication", Toast.LENGTH_SHORT).show();
            }
        }
    }
}