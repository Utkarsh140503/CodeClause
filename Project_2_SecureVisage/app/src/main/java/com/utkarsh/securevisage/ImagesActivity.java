package com.utkarsh.securevisage;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ImagesActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY = 101;

    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<Pair<String, Bitmap>> imagesList;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        executor = Executors.newSingleThreadExecutor();
        imagesList = new ArrayList<>();

        // Initialize RecyclerView
        imageRecyclerView = findViewById(R.id.imageRecyclerView);
        imageAdapter = new ImageAdapter(imagesList);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageRecyclerView.setAdapter(imageAdapter);

        // Initialize BiometricPrompt
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                showToast("Authentication successful");
                openGallery();
            }
        });

        // Configure the BiometricPrompt
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate to access the gallery")
                .setNegativeButtonText("Cancel")
                .build();

        // Set up the long-press listener for the RecyclerView items
        imageAdapter.setOnImageLongClickListener(new ImageAdapter.OnImageLongClickListener() {
            @Override
            public void onImageLongClick(String filename, Bitmap imageBitmap) {
                showFullScreenPopup(filename, imageBitmap);
            }
        });

        // Set up the FAB to open the gallery
        ImageView fabAddImage = findViewById(R.id.ivAddImage);
        fabAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateWithBiometrics();
            }
        });

        // Load saved images from internal storage
        loadImagesFromInternalStorage();
    }

    private void authenticateWithBiometrics() {
        // Check for biometric authentication permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_BIOMETRIC}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Authenticate with BiometricPrompt
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void openGallery() {
        // Open the device's image gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            // Handle selected image from the gallery
            try {
                Uri selectedImageUri = data.getData();
                String filename = getFileNameFromUri(selectedImageUri);

                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imagesList.add(new Pair<>(filename, selectedImage));
                imageAdapter.notifyDataSetChanged();

                // Save the selected image with the extracted filename
                saveImageToInternalStorage(selectedImage, filename);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error loading image");
            }
        }
    }

    // Helper method to extract filename from URI
    private String getFileNameFromUri(Uri uri) {
        String displayName = null;
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            if (columnIndex != -1) {
                displayName = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return displayName;
    }

    private void saveImageToInternalStorage(Bitmap imageBitmap, String filename) {
        try {
            // Open an output stream to write the image data
            FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);

            // Compress the image to JPEG format and write it to the output stream
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // Close the output stream
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error saving image");
        }
    }

    private void loadImagesFromInternalStorage() {
        File internalStorageDir = getFilesDir();
        File[] imageFiles = internalStorageDir.listFiles();

        if (imageFiles != null) {
            for (File imageFile : imageFiles) {
                String filename = imageFile.getName();
                Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                imagesList.add(new Pair<>(filename, imageBitmap));
            }

            imageAdapter.notifyDataSetChanged();
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ImagesActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFullScreenPopup(final String filename, final Bitmap imageBitmap) {
        // Create a dialog for the full-screen image
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.popup_image);

        ImageView fullScreenImageView = dialog.findViewById(R.id.fullScreenImageView);
        fullScreenImageView.setImageBitmap(imageBitmap);

        ImageView removeImageButton = dialog.findViewById(R.id.removeImageButton);
        ImageView cancelImageButton = dialog.findViewById(R.id.cancelImageButton);
        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the image from internal storage and the list
                removeImageFromInternalStorage(filename);
                imagesList.remove(new Pair<>(filename, imageBitmap));
                imageAdapter.notifyDataSetChanged();

                // Dismiss the dialog
                dialog.dismiss();
            }
        });
        cancelImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void removeImageFromInternalStorage(String filename) {
        try {
            // Delete the file from internal storage
            File file = new File(getFilesDir(), filename);
            if (file.delete()) {
                showToast("Image removed successfully");
            } else {
                showToast("Failed to remove image");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error removing image");
        }
    }
}
