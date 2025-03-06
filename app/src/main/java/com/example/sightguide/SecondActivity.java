package com.example.sightguide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.IOException;
import java.util.List;

public class SecondActivity extends AppCompatActivity
{
    private Bitmap selectedImage;
    private ImageView click_image_2;
    private TextView textView2;

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

        click_image_2 = findViewById(R.id.click_image2);
        textView2 = findViewById(R.id.textView2);
        Button selectImageButton2 = findViewById(R.id.selectImageButton2);

        selectImageButton2.setOnClickListener(v -> pickImage.launch("image/*"));

    }

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImageSelected);

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

        click_image_2.setImageBitmap(mutableBitmap);
        textView2.setText(detectedText.toString());
    }

    public void goBack(View view)
    {
        finish();
    }
}