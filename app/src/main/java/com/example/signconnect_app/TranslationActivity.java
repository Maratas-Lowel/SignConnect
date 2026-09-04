package com.example.signconnect_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Size;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

// MediaPipe Task Vision Imports
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslationActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private PreviewView viewFinder;
    private HandLandmarkerOverlay overlayView;
    private HandLandmarker handLandmarker = null;

    // Dedicated background thread executor to prevent UI thread lag
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        ImageButton btnSettings = findViewById(R.id.btnSettings);
        ImageButton btnTextToSpeech = findViewById(R.id.btnTextToSpeech);
        Button btnAudioToggle = findViewById(R.id.btnAudioToggle);
        Button btnHistory = findViewById(R.id.btnHistory);
        viewFinder = findViewById(R.id.viewFinder);
        overlayView = findViewById(R.id.overlayView);
        TextView tvTranslatedText = findViewById(R.id.tvTranslatedText);

        // Initialize background thread pool
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupHandLandmarker();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }

        btnSettings.setOnClickListener(v -> Toast.makeText(this, "Opening Settings", Toast.LENGTH_SHORT).show());
        btnTextToSpeech.setOnClickListener(v -> Toast.makeText(this, "Speaking...", Toast.LENGTH_SHORT).show());
        btnAudioToggle.setOnClickListener(v -> Toast.makeText(this, "Audio Toggled", Toast.LENGTH_SHORT).show());
        btnHistory.setOnClickListener(v -> Toast.makeText(this, "Opening History", Toast.LENGTH_SHORT).show());

        tvTranslatedText.setText(R.string.translation_sample);
    }

    private void setupHandLandmarker() {
        try {
            // GPU Delegate enabled with proper Delegate import
            BaseOptions baseOptions = BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task")
                    .setDelegate(Delegate.GPU)
                    .build();

            HandLandmarker.HandLandmarkerOptions options =
                    HandLandmarker.HandLandmarkerOptions.builder()
                            .setBaseOptions(baseOptions)
                            .setRunningMode(RunningMode.LIVE_STREAM)
                            .setNumHands(1) // Limited to 1 hand to cut overhead in half
                            .setMinHandPresenceConfidence(0.5f)
                            .setMinTrackingConfidence(0.5f)
                            .setResultListener((result, image) -> runOnUiThread(() -> {
                                if (overlayView != null) {
                                    overlayView.setResults(result);
                                }
                            }))
                            .setErrorListener(error -> {
                                // Handle optional runtime inference errors safely
                            })
                            .build();

            handLandmarker = HandLandmarker.createFromOptions(this, options);

        } catch (Exception e) {
            handLandmarker = null;
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // Updated to ResolutionSelector to avoid deprecation warnings
                ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                        .setResolutionStrategy(new ResolutionStrategy(
                                new Size(640, 480),
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Run analysis on background cameraExecutor
                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (handLandmarker != null) {
                        Bitmap bitmap = imageProxy.toBitmap();

                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees != 0) {
                            android.graphics.Matrix matrix = new android.graphics.Matrix();
                            matrix.postRotate(rotationDegrees);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        }

                        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
                        long frameTimestamp = imageProxy.getImageInfo().getTimestamp();

                        handLandmarker.detectAsync(mpImage, frameTimestamp);
                    }
                    imageProxy.close();
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (handLandmarker != null) {
            handLandmarker.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show();
            }
        }
    }
}