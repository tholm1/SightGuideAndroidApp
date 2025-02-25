package com.example.sightguide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.objects.DetectedObject;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private final Paint boxPaint;
    private final Paint textPaint;
    private List<DetectedObject> detectedObjects = new ArrayList<>();

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        boxPaint = new Paint();
        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setDetectedObjects(List<DetectedObject> objects)
    {
        this.detectedObjects = objects;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DetectedObject obj : detectedObjects) {
            Rect box = obj.getBoundingBox();
            canvas.drawRect(box, boxPaint);
            for (DetectedObject.Label label : obj.getLabels()) {
                String text = label.getText() + " (" + label.getConfidence() + ")";
                canvas.drawText(text, box.left, box.top - 10, textPaint);
            }
        }
    }
}
