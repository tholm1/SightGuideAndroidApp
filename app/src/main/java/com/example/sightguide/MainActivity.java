package com.example.sightguide;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    private static final int pic_id = 123;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Button camera_open_id;
    private ImageView click_image_id;
    private Button documentReadingButton;

    private static final String PI_BLUETOOTH_ADDRESS = "2C:CF:67:B1:16:F4"; // Raspberry Pi's MAC address
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        camera_open_id = findViewById(R.id.camera_button);
        click_image_id = findViewById(R.id.click_image);
        documentReadingButton = findViewById(R.id.documentReadingButton);


        // Camera Button Click Listener
        camera_open_id.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
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

    public void goToTTSActivity(View view)
    {
        Intent intent = new Intent(this, TTSActivity.class);
        startActivity(intent);
    }

    public void goToSpeechIntoText(View view)
    {
        Intent intent = new Intent(this, SpeechIntoText.class);
        startActivity(intent);
    }

    public void goToPIImageUpload(View view)
    {
        Intent intent = new Intent(this, PI_ImageUpload.class);
        startActivity(intent);
    }

    public void goToAudioUpload(View view)
    {
        Intent intent = new Intent(this, AudioUpload.class);
        startActivity(intent);
    }
}