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
import java.util.List;
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
        imageView4 = findViewById(R.id.imageView4);
        textView = findViewById(R.id.textView);
        Button selectImageButton = findViewById(R.id.selectImageButton);

        selectImageButton.setOnClickListener(v -> pickImage.launch("image/*"));

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
        new Thread(() -> {
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(PI_BLUETOOTH_ADDRESS);
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);

                bluetoothAdapter.cancelDiscovery();
                socket.connect();

                // Receive image data
                InputStream inputStream = socket.getInputStream();
                byte[] imageData = inputStream.readAllBytes();

                // Display image in ImageView (convert bytes to Bitmap)
                runOnUiThread(() -> {
                    ImageView imageView = findViewById(R.id.imageView);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    imageView.setImageBitmap(bitmap);
                });

                socket.close();
            } catch (Exception e) {
                Log.e("Bluetooth", "Error: " + e.getMessage());
            }
        }).start();
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

    // Checks to see if the image has been picked and launches
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImageSelected);

    // Prompts the user to pick an image for the object detection
    private void onImageSelected(Uri imageUri) {
        if (imageUri != null) {
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView4.setImageBitmap(selectedImage);
                runObjectDetection(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Runs Google ML Kit Object Detection
    private void runObjectDetection(Bitmap bitmap)
    {
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        ObjectDetector objectDetector = ObjectDetection.getClient(options);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        objectDetector.process(image)
                .addOnSuccessListener(detectedObjects -> drawBoundingBoxes(detectedObjects, bitmap))
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // Draw bounding boxes on objects detected in base configuration
    private void drawBoundingBoxes(List<DetectedObject> objects, Bitmap bitmap)
    {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(50);

        StringBuilder detectedText = new StringBuilder();
        for (DetectedObject object : objects)
        {
            if (object.getBoundingBox() != null)
            {
                canvas.drawRect(object.getBoundingBox(), paint);
            }
            if (!object.getLabels().isEmpty())
            {
                for (Label label : object.getLabels())
                {
                    String labelText = label.getText();
                    float confidence = label.getConfidence();
                    canvas.drawText(labelText + " (" + confidence + ")",
                            object.getBoundingBox().left, object.getBoundingBox().top - 10, textPaint);
                    detectedText.append(labelText).append(" - Confidence: ").append(confidence).append("\n");
                }
            }
            else
            {
                detectedText.append("Unknown object detected\n");
            }
        }

        imageView4.setImageBitmap(mutableBitmap);
        textView.setText(detectedText.toString());
    }
}
