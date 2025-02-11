package com.example.sightguide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private static final int pic_id = 123;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Button camera_open_id;
    private Button documentReadingButton;
    private ImageView click_image_id;
    private TextSpeechTranslation textSpeechTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        camera_open_id = findViewById(R.id.camera_button);
        documentReadingButton = findViewById(R.id.documentReadingButton);
        click_image_id = findViewById(R.id.click_image);

        // Initialize Text-to-Speech
        textSpeechTranslation = new TextSpeechTranslation(this);

        // Camera Button Click Listener
        camera_open_id.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        });

        // Document Reading Button (Trigger TTS)
        documentReadingButton.setOnClickListener(v -> {
            String sampleText = "This is a sample document being read aloud.";
            textSpeechTranslation.speak(sampleText);
            Toast.makeText(MainActivity.this, "Reading Document...", Toast.LENGTH_SHORT).show();
        });
    }

    // Open Camera
    private void openCamera() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, pic_id);
    }

    // Handle Permission Request Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle Camera Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            click_image_id.setImageBitmap(photo);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textSpeechTranslation != null) {
            textSpeechTranslation.shutdown();
        }
    }
}
