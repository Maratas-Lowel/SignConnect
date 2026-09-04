package com.example.signconnect_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.List;

public class HandLandmarkerOverlay extends View {
    private HandLandmarkerResult results;
    private Paint pointPaint;
    private Paint linePaint;

    private static final int[][] HAND_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4},       // Thumb
            {0, 5}, {5, 6}, {6, 7}, {7, 8},       // Index finger
            {5, 9}, {9, 10}, {10, 11}, {11, 12},  // Middle finger
            {9, 13}, {13, 14}, {14, 15}, {15, 16},// Ring finger
            {13, 17}, {17, 18}, {18, 19}, {19, 20},// Pinky
            {0, 17}                               // Palm base
    };

    public HandLandmarkerOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pointPaint = new Paint();
        pointPaint.setColor(Color.GREEN);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setStrokeWidth(10f);

        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#00E676"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setResults(HandLandmarkerResult handLandmarkerResult) {
        this.results = handLandmarkerResult;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (results == null || results.landmarks() == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        for (List<NormalizedLandmark> landmarkList : results.landmarks()) {
            for (int[] connection : HAND_CONNECTIONS) {
                NormalizedLandmark startPoint = landmarkList.get(connection[0]);
                NormalizedLandmark endPoint = landmarkList.get(connection[1]);

                float startX = (1.0f - startPoint.x()) * width;
                float startY = startPoint.y() * height;
                float endX = (1.0f - endPoint.x()) * width;
                float endY = endPoint.y() * height;

                canvas.drawLine(startX, startY, endX, endY, linePaint);
            }

            for (NormalizedLandmark landmark : landmarkList) {
                float x = (1.0f - landmark.x()) * width;
                float y = landmark.y() * height;
                canvas.drawCircle(x, y, 10f, pointPaint);
            }
        }
    }
}