# Android Native Code Implementation for KITT K2000 Interface

## Project Structure and Setup

### 1. AndroidManifest.xml Permissions
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

### 2. build.gradle Dependencies
```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.core:core:1.10.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // For FFT processing
    implementation 'org.apache.commons:commons-math3:3.6.1'
    
    // For advanced audio processing
    implementation 'com.github.wendykierp:JTransforms:3.1'
}
```

## Core Custom Views Implementation

### 3. KittButton.java - Custom Button Component
```java
package com.yourpackage.kitt;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class KittButton extends View {
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint textPaint;
    private String text = "";
    private int buttonColor = Color.RED;
    private boolean isPressed = false;
    private boolean isGlowing = false;
    private ValueAnimator glowAnimator;
    private float glowIntensity = 0.3f;
    
    public KittButton(Context context) {
        super(context);
        init();
    }
    
    public KittButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // Configure text paint
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(20);
        textPaint.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        
        // Configure border paint
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);
        
        setupGlowAnimation();
        setClickable(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // Enable shadow layer
    }
    
    private void setupGlowAnimation() {
        glowAnimator = ValueAnimator.ofFloat(0.3f, 1.0f);
        glowAnimator.setDuration(800);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.addUpdateListener(animation -> {
            glowIntensity = (Float) animation.getAnimatedValue();
            invalidate();
        });
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int padding = 8;
        
        // Draw button background
        backgroundPaint.setColor(buttonColor);
        
        if (isGlowing || isPressed) {
            float shadowRadius = 15 * glowIntensity;
            backgroundPaint.setShadowLayer(shadowRadius, 0, 0, buttonColor);
            backgroundPaint.setAlpha((int) (255 * glowIntensity));
        } else {
            backgroundPaint.setShadowLayer(8, 0, 0, buttonColor);
            backgroundPaint.setAlpha(180);
        }
        
        RectF rect = new RectF(padding, padding, width - padding, height - padding);
        canvas.drawRoundRect(rect, 12, 12, backgroundPaint);
        
        // Draw border
        if (isPressed) {
            borderPaint.setColor(Color.YELLOW);
            borderPaint.setStrokeWidth(4);
        } else {
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStrokeWidth(2);
        }
        canvas.drawRoundRect(rect, 12, 12, borderPaint);
        
        // Draw text
        float textY = height / 2f + textPaint.getTextSize() / 3f;
        canvas.drawText(text, width / 2f, textY, textPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                invalidate();
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
    
    public void setText(String text) {
        this.text = text;
        invalidate();
    }
    
    public void setButtonColor(int color) {
        this.buttonColor = color;
        invalidate();
    }
    
    public void startGlow() {
        isGlowing = true;
        glowAnimator.start();
    }
    
    public void stopGlow() {
        isGlowing = false;
        glowAnimator.cancel();
        glowIntensity = 0.3f;
        invalidate();
    }
}
```

### 4. KittScannerView.java - Oscillating Scanner
```java
package com.yourpackage.kitt;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class KittScannerView extends View {
    private Paint ledPaint;
    private float scannerPosition = 0;
    private ValueAnimator scannerAnimator;
    private int numLeds = 24;
    private float ledWidth;
    private float ledHeight;
    private boolean isScanning = false;
    
    public KittScannerView(Context context) {
        super(context);
        init();
    }
    
    public KittScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        ledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setBackgroundColor(Color.parseColor("#0a0a0a"));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setupScannerAnimation();
    }
    
    private void setupScannerAnimation() {
        scannerAnimator = ValueAnimator.ofFloat(0, numLeds - 1);
        scannerAnimator.setDuration(1200);
        scannerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scannerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scannerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scannerAnimator.addUpdateListener(animation -> {
            scannerPosition = (Float) animation.getAnimatedValue();
            invalidate();
        });
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ledWidth = (float) (w - 20) / numLeds;
        ledHeight = h - 20;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isScanning) return;
        
        int centerY = getHeight() / 2;
        
        for (int i = 0; i < numLeds; i++) {
            float x = 10 + i * ledWidth + ledWidth / 2;
            float distance = Math.abs(i - scannerPosition);
            
            // Calculate LED intensity based on distance from scanner
            float intensity = Math.max(0, 1.0f - distance / 4.0f);
            
            if (intensity > 0) {
                int red = (int) (255 * intensity);
                int color = Color.rgb(red, 0, 0);
                ledPaint.setColor(color);
                
                // Add glow effect
                float shadowRadius = 12 * intensity;
                ledPaint.setShadowLayer(shadowRadius, 0, 0, color);
                
                RectF ledRect = new RectF(
                    x - ledWidth / 3,
                    centerY - ledHeight / 3,
                    x + ledWidth / 3,
                    centerY + ledHeight / 3
                );
                canvas.drawRoundRect(ledRect, 6, 6, ledPaint);
            }
        }
    }
    
    public void startScanning() {
        if (!isScanning) {
            isScanning = true;
            scannerAnimator.start();
        }
    }
    
    public void stopScanning() {
        if (isScanning) {
            isScanning = false;
            scannerAnimator.cancel();
            invalidate();
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 60; // Fixed height for scanner
        setMeasuredDimension(width, height);
    }
}
```

### 5. KittSpectrumView.java - Audio Spectrum Analyzer
```java
package com.yourpackage.kitt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class KittSpectrumView extends View {
    private Paint barPaint;
    private float[] magnitudes;
    private int numBars = 32;
    private float barWidth;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private FastFourierTransformer fft;
    private int sampleRate = 44100;
    private int bufferSize;
    
    public KittSpectrumView(Context context) {
        super(context);
        init();
    }
    
    public KittSpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        magnitudes = new float[numBars];
        fft = new FastFourierTransformer(DftNormalization.STANDARD);
        setBackgroundColor(Color.parseColor("#0a0a0a"));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        
        bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        barWidth = (float) w / numBars;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int height = getHeight();
        float maxHeight = height - 30;
        
        for (int i = 0; i < numBars; i++) {
            float x = i * barWidth;
            float barHeight = magnitudes[i] * maxHeight;
            
            // KITT-style color gradient (red to yellow based on intensity)
            float intensity = magnitudes[i];
            int red = 255;
            int green = (int) (255 * Math.min(1.0f, intensity * 2));
            int blue = 0;
            
            int color = Color.rgb(red, green, blue);
            barPaint.setColor(color);
            
            // Add glow effect
            float shadowRadius = 10 * intensity;
            barPaint.setShadowLayer(shadowRadius, 0, 0, color);
            
            RectF barRect = new RectF(
                x + 2,
                height - barHeight - 15,
                x + barWidth - 2,
                height - 15
            );
            canvas.drawRoundRect(barRect, 3, 3, barPaint);
        }
    }
    
    public void startVisualization() {
        if (!isRecording && checkAudioPermission()) {
            isRecording = true;
            startAudioCapture();
        }
    }
    
    public void stopVisualization() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
    
    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(getContext(), 
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void startAudioCapture() {
        try {
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            );
            
            audioRecord.startRecording();
            
            recordingThread = new Thread(() -> {
                short[] buffer = new short[1024];
                double[] fftBuffer = new double[1024];
                
                while (isRecording && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    
                    if (read > 0) {
                        // Convert to double for FFT
                        for (int i = 0; i < buffer.length; i++) {
                            fftBuffer[i] = buffer[i] / 32768.0;
                        }
                        
                        // Perform FFT
                        try {
                            org.apache.commons.math3.complex.Complex[] fftResult = 
                                fft.transform(fftBuffer, TransformType.FORWARD);
                            updateMagnitudes(fftResult);
                        } catch (Exception e) {
                            // Handle FFT errors gracefully
                        }
                        
                        // Update UI on main thread
                        post(this::invalidate);
                    }
                    
                    try {
                        Thread.sleep(50); // ~20 FPS update rate
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            recordingThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateMagnitudes(org.apache.commons.math3.complex.Complex[] fftData) {
        int step = fftData.length / 2 / numBars;
        
        for (int i = 0; i < numBars; i++) {
            double sum = 0;
            int startIndex = i * step;
            int endIndex = Math.min((i + 1) * step, fftData.length / 2);
            
            for (int j = startIndex; j < endIndex; j++) {
                if (j < fftData.length) {
                    double magnitude = fftData[j].abs();
                    sum += magnitude;
                }
            }
            
            float avgMagnitude = (float) (sum / (endIndex - startIndex));
            
            // Smooth the values and normalize
            magnitudes[i] = (magnitudes[i] * 0.7f) + (Math.min(1.0f, avgMagnitude * 15) * 0.3f);
        }
    }
}
```

### 6. KittDashboardView.java - Main Dashboard Container
```java
package com.yourpackage.kitt;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class KittDashboardView extends LinearLayout {
    private KittScannerView scannerView;
    private KittSpectrumView spectrumView;
    private LinearLayout sideButtonsLayout;
    private KittButton[] buttons;
    
    public KittDashboardView(Context context) {
        super(context);
        init(context);
    }
    
    public KittDashboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setBackgroundColor(Color.BLACK);
        setGravity(Gravity.CENTER_VERTICAL);
        
        initSideButtons(context);
        initCenterPanel(context);
    }
    
    private void initSideButtons(Context context) {
        sideButtonsLayout = new LinearLayout(context);
        sideButtonsLayout.setOrientation(VERTICAL);
        sideButtonsLayout.setBackgroundColor(Color.parseColor("#1a1a1a"));
        sideButtonsLayout.setPadding(10, 10, 10, 10);
        
        String[] buttonLabels = {"AIR", "OIL", "P1", "P2", "S1", "S2"};
        int[] buttonColors = {
            Color.RED, 
            Color.YELLOW, 
            Color.GREEN, 
            Color.BLUE, 
            Color.CYAN, 
            Color.MAGENTA
        };
        
        buttons = new KittButton[buttonLabels.length];
        
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new KittButton(context);
            buttons[i].setText(buttonLabels[i]);
            buttons[i].setButtonColor(buttonColors[i]);
            
            final int buttonIndex = i;
            buttons[i].setOnClickListener(v -> onKittButtonClick(buttonLabels[buttonIndex]));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                120, 0, 1.0f);
            params.setMargins(5, 5, 5, 5);
            sideButtonsLayout.addView(buttons[i], params);
        }
        
        LinearLayout.LayoutParams sideParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.MATCH_PARENT);
        addView(sideButtonsLayout, sideParams);
    }
    
    private void initCenterPanel(Context context) {
        LinearLayout centerPanel = new LinearLayout(context);
        centerPanel.setOrientation(VERTICAL);
        centerPanel.setBackgroundColor(Color.BLACK);
        centerPanel.setPadding(20, 20, 20, 20);
        
        // KITT Scanner
        scannerView = new KittScannerView(context);
        LinearLayout.LayoutParams scannerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT);
        scannerParams.setMargins(0, 0, 0, 30);
        centerPanel.addView(scannerView, scannerParams);
        
        // Spectrum Analyzer
        spectrumView = new KittSpectrumView(context);
        LinearLayout.LayoutParams spectrumParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            200);
        centerPanel.addView(spectrumView, spectrumParams);
        
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.MATCH_PARENT, 2.0f);
        addView(centerPanel, centerParams);
    }
    
    private void onKittButtonClick(String buttonLabel) {
        Toast.makeText(getContext(), "KITT: " + buttonLabel + " activated", Toast.LENGTH_SHORT).show();
        
        // Add specific button functionality here
        switch (buttonLabel) {
            case "AIR":
                // Air conditioning control
                break;
            case "OIL":
                // Oil system check
                break;
            case "P1":
            case "P2":
                // Program buttons
                break;
            case "S1":
            case "S2":
                // Scanner controls
                break;
        }
    }
    
    public void startSystems() {
        scannerView.startScanning();
        spectrumView.startVisualization();
        
        // Optional: Start button glow effects
        for (KittButton button : buttons) {
            button.startGlow();
        }
    }
    
    public void stopSystems() {
        scannerView.stopScanning();
        spectrumView.stopVisualization();
        
        for (KittButton button : buttons) {
            button.stopGlow();
        }
    }
}
```

### 7. MainActivity.java - Main Activity Implementation
```java
package com.yourpackage.kitt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private KittDashboardView kittDashboard;
    private TextView titleText;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupFullScreen();
        initializeViews();
        checkPermissions();
    }
    
    private void setupFullScreen() {
        // Hide system UI for immersive experience
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        // Hide navigation bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
    
    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        kittDashboard = findViewById(R.id.kittDashboard);
        
        // Animate title text
        titleText.setAlpha(0f);
        titleText.animate()
                .alpha(1f)
                .setDuration(2000)
                .start();
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
        } else {
            initializeKitt();
        }
    }
    
    private void initializeKitt() {
        // Start KITT systems with delay for dramatic effect
        titleText.postDelayed(() -> {
            kittDashboard.startSystems();
            playStartupSound();
        }, 1000);
    }
    
    private void playStartupSound() {
        // Optional: Play KITT startup sound
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.kitt_startup);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            // Handle if sound file not found
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeKitt();
            } else {
                // Handle permission denial
                titleText.setText("AUDIO PERMISSION REQUIRED");
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kittDashboard != null) {
            kittDashboard.stopSystems();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (kittDashboard != null) {
            kittDashboard.stopSystems();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setupFullScreen();
        if (kittDashboard != null && 
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                == PackageManager.PERMISSION_GRANTED) {
            kittDashboard.startSystems();
        }
    }
}
```

### 8. activity_main.xml - Layout File
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000">

    <TextView
        android:id="@+id/titleText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="20dp"
        android:text="KNIGHT INDUSTRIES 2000"
        android:textColor="#FF0000"
        android:textSize="24sp"
        android:textStyle="bold"
        android:fontFamily="monospace"
        android:letterSpacing="0.1" />

    <com.yourpackage.kitt.KittDashboardView
        android:id="@+id/kittDashboard"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/titleText"
        android:layout_margin="20dp" />

</RelativeLayout>
```

## Additional Implementation Notes

### Performance Optimizations
- Use `setLayerType(LAYER_TYPE_SOFTWARE, null)` for shadow effects
- Implement proper view recycling in custom views
- Use background threads for audio processing
- Optimize FFT calculations with appropriate buffer sizes

### Error Handling
- Check audio permissions before starting recording
- Handle AudioRecord initialization failures gracefully
- Implement proper cleanup in lifecycle methods
- Add try-catch blocks around FFT operations

### Customization Options
- Adjustable scanner speed and LED count
- Configurable spectrum analyzer sensitivity
- Custom button colors and labels
- Theme switching capabilities

### Analysis Guidelines
- **Respect .gitignore Exclusions:** When analyzing the repository for code, documentation, or any other purpose, all tools and processes must respect the exclusions defined in the .gitignore files at the root and in subdirectories. This ensures that temporary, system-generated, or otherwise irrelevant files (such as .DS_Store) are not considered during analysis or included in any automated processes.

This implementation provides a complete KITT K2000 interface with all requested features including the 6 side buttons, oscillating scanner lights, and real-time audio spectrum visualization.
