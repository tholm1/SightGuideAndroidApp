package com.example.sightguide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.DetectedObject.Label;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    private Bitmap selectedImage;
    private ImageView imageView4;
    private TextView textView;
    private static final int pic_id = 123;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Button camera_open_id;
    private Button documentReadingButton;
    private ImageView click_image_id;
    private TextSpeechTranslation textSpeechTranslation;

    private static final String PI_BLUETOOTH_ADDRESS = "2C:CF:67:B1:16:F4"; // Raspberry Pi's MAC address
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

//    private final ActivityResultLauncher<String[]> activityResultLauncher =
//            registerForActivityResult(
//                    new ActivityResultContracts.RequestMultiplePermissions(),
//                    permissions -> {
//                        // Handle Permission granted/rejected
//                        boolean permissionGranted = true;
//                        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
//                            if (Arrays.asList(REQUIRED_PERMISSIONS).contains(entry.getKey()) && !entry.getValue()) {
//                                permissionGranted = false;
//                            }
//                        }
//                        if (!permissionGranted) {
//                            Toast.makeText(getBaseContext(),
//                                    "Permission request denied",
//                                    Toast.LENGTH_SHORT).show();
//                        } else {
//                            startCamera();
//                        }
//                    }
//            );



    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        // Bluetooth connection to Raspberry Pi
//        new Thread(() -> {
//            try {
//                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(PI_BLUETOOTH_ADDRESS);
//                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
//
//                bluetoothAdapter.cancelDiscovery();
//                socket.connect();
//
//                // Receive image data
//                InputStream inputStream = socket.getInputStream();
//                byte[] imageData = inputStream.readAllBytes();
//
//                // Display image in ImageView (convert bytes to Bitmap)
//                runOnUiThread(() -> {
//                    ImageView imageView = findViewById(R.id.imageView);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
//                    imageView.setImageBitmap(bitmap);
//                });
//
//                socket.close();
//            } catch (Exception e) {
//                Log.e("Bluetooth", "Error: " + e.getMessage());
//            }
//        }).start();
    }

    // Open Camera
    private void openCamera()
    {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, pic_id);
    }

    // Handle Permission Request Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
            }
            else
            {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle Camera Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == pic_id && resultCode == RESULT_OK && data != null)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            click_image_id.setImageBitmap(photo);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (textSpeechTranslation != null)
        {
            textSpeechTranslation.shutdown();
        }
    }

    public void goToSecondActivity(View view)
    {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    public void goToCrosswalkActivity(View view)
    {
        Intent intent = new Intent(this, CrosswalkActivity.class);
        startActivity(intent);
    }
}