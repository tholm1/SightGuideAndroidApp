package com.example.sightguide;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import com.google.mlkit.vision.objects.ObjectDetection;


import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecondActivity extends AppCompatActivity
{
    private Bitmap selectedImage;
    private ImageView click_image_2;
    private TextView textView2;
    private static final int CAMERA_PERMISSION_CODE = 1001;
    private PreviewView previewView;
    private OverlayView overlayView;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        click_image_2 = findViewById(R.id.click_image2);
        textView2 = findViewById(R.id.textView2);
        Button selectImageButton2 = findViewById(R.id.selectImageButton2);

        selectImageButton2.setOnClickListener(v -> pickImage.launch("image/*"));

        requestCameraPermission();

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                (ListenableFuture<ProcessCameraProvider>) ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    analyzeImage(image);
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void requestCameraPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void analyzeImage(ImageProxy image)
    {
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = image.getImage();

        if (mediaImage != null)
        {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

            ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                            .enableMultipleObjects()
                            .enableClassification()
                            .build();

            ObjectDetector detector = ObjectDetection.getClient(options);

            detector.process(inputImage)
                    .addOnSuccessListener(detectedObjects -> {
                        for (DetectedObject detectedObject : detectedObjects)
                        {
                            // Get bounding box
                            Rect boundingBox = detectedObject.getBoundingBox();

                            // Get labels and confidence
                            for (DetectedObject.Label label : detectedObject.getLabels())
                            {
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                int index = label.getIndex(); // Not always available

                                // Pass this data to your drawing function
                                //drawBoundingBox(boundingBox, text, confidence);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Object detection failed", e));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startCamera();
            }
            else
            {
                Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImageSelected);

    // Prompts the user to pick an image for the object detection
    private void onImageSelected(Uri imageUri)
    {
        if (imageUri != null)
        {
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                click_image_2.setImageBitmap(selectedImage);
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

    // Draw bounding boxes on objects detected in regular mode
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
                for (DetectedObject.Label label : object.getLabels())
                {
                    String labelText = label.getText();
                    float confidence = label.getConfidence();
                    canvas.drawText(labelText + " (" + confidence + ")", object.getBoundingBox().left, object.getBoundingBox().top - 10, textPaint);
                    detectedText.append(labelText).append(" - Confidence: ").append(confidence).append("\n");
                }
            }
            else
            {
                detectedText.append("Unknown object detected\n");
            }
        }

        click_image_2.setImageBitmap(mutableBitmap);
        textView2.setText(detectedText.toString());
    }

    private void drawBoundingBox(List<DetectedObject> detectedObjects)
    {
        runOnUiThread(()->overlayView.setDetectedObjects(detectedObjects));
    }

    public void goBack(View view)
    {
        finish();
    }
}