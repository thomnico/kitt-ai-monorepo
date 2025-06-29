package com.kitt.android;

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
import android.util.Log;
import android.view.View;
import androidx.core.content.ContextCompat;

/**
 * KITT-style spectrum analyzer with symmetric LED bars matching the original dashboard
 */
public class KittSpectrumView extends View {
    private static final String TAG = "KittSpectrumView";
    private static final int TOTAL_COLUMNS = 7; // Match the image - 7 columns total
    private static final int MAX_SEGMENTS = 20; // Maximum segments per column for detailed VU meter (set to 20 as requested)
    private Paint ledPaint;
    private Paint offLedPaint;
    private int[][] columnHeights = new int[TOTAL_COLUMNS][1]; // Current height for each column
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
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

    public KittSpectrumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ledPaint.setColor(Color.RED);
        
        offLedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        offLedPaint.setColor(Color.parseColor("#330000")); // Very dark red for "off" LEDs
        
        setBackgroundColor(Color.parseColor("#000000"));

        bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );
        
        // Initialize with some demo pattern
        initDemoPattern();
    }
    
    private void initDemoPattern() {
        // Create a symmetric pattern like in the image
        columnHeights[0][0] = 4; // Left outer
        columnHeights[1][0] = 8; // Left middle
        columnHeights[2][0] = 10; // Left inner
        columnHeights[3][0] = 12; // Center (tallest)
        columnHeights[4][0] = 10; // Right inner
        columnHeights[5][0] = 8; // Right middle
        columnHeights[6][0] = 4; // Right outer
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) return;

        float columnWidth = (float) width / TOTAL_COLUMNS;
        float segmentHeight = (float) height / MAX_SEGMENTS;
        float ledWidth = columnWidth * 0.7f; // 70% of column width for LED
        float ledHeight = segmentHeight * 0.8f; // 80% of segment height for LED
        float ledSpacing = columnWidth * 0.15f; // Spacing between LEDs
        int maxSegmentsForDarkColumns = MAX_SEGMENTS - 2; // Shorter height for dark columns
        int middleSegment = MAX_SEGMENTS / 2; // Middle point for symmetry

        for (int col = 0; col < TOTAL_COLUMNS; col++) {
            float columnLeft = col * columnWidth + ledSpacing;
            int activeSegments = columnHeights[col][0];
            boolean isDarkColumn = (col == 0 || col == 2 || col == 4 || col == 6);

            for (int seg = 0; seg < MAX_SEGMENTS; seg++) {
                // Skip drawing segments beyond the shorter height for dark columns
                if (isDarkColumn && seg >= maxSegmentsForDarkColumns) continue;

                float segmentTop = height - (seg + 1) * segmentHeight + (segmentHeight - ledHeight) / 2;
                float segmentBottom = segmentTop + ledHeight;

                RectF ledRect = new RectF(columnLeft, segmentTop, columnLeft + ledWidth, segmentBottom);

                // Calculate the range for active segments to be centered vertically
                int halfActive = activeSegments / 2;
                int startSeg = middleSegment - halfActive;
                int endSeg = middleSegment + halfActive + (activeSegments % 2 == 0 ? 0 : 1);

                if (seg >= startSeg && seg < endSeg && !isDarkColumn) {
                    // LED is "on" - bright red for active columns within the centered range
                    // Apply transparency effect at the top and bottom of the active bar for the last 4 segments
                    if (seg >= endSeg - 4 && seg < endSeg) {
                        // Calculate transparency factor for the top 4 segments, most fade at the top
                        int alpha = (int) (255 * ((float)(endSeg - seg - 1) / 4.0f));
                        ledPaint.setAlpha(alpha);
                        canvas.drawRoundRect(ledRect, 2, 2, ledPaint);
                        ledPaint.setAlpha(255); // Reset alpha after drawing
                    } else if (seg >= startSeg && seg < startSeg + 4) {
                        // Calculate transparency factor for the bottom 4 segments
                        int alpha = (int) (255 * (1.0f - (float)(startSeg + 3 - seg) / 4.0f));
                        ledPaint.setAlpha(alpha);
                        canvas.drawRoundRect(ledRect, 2, 2, ledPaint);
                        ledPaint.setAlpha(255); // Reset alpha after drawing
                    } else {
                        canvas.drawRoundRect(ledRect, 2, 2, ledPaint);
                    }
                }
                // Do not draw anything for dark columns or inactive segments to make them transparent
            }
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
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping audio record", e);
            }
            audioRecord = null;
        }
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(getContext(),
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void startAudioCapture() {
        if (!checkAudioPermission()) {
            Log.w(TAG, "Audio permission not granted");
            return;
        }

        try {
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                return;
            }

            audioRecord.startRecording();

            recordingThread = new Thread(() -> {
                short[] buffer = new short[1024];

                while (isRecording && audioRecord != null &&
                       audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    try {
                        int read = audioRecord.read(buffer, 0, buffer.length);

                        if (read > 0) {
                            processAudioData(buffer, read);
                            post(this::invalidate);
                        } else {
                            applyDecayMechanism();
                            post(this::invalidate);
                        }

                        Thread.sleep(50); // ~20 FPS update rate
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in audio processing", e);
                        applyDecayMechanism();
                        post(this::invalidate);
                        break;
                    }
                }
            });
            recordingThread.start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting audio capture", e);
        }
    }

    private long lastUpdateTime = 0;
    private static final long DECAY_TIMEOUT_MS = 500; // Decay after 500ms of no update

    private void processAudioData(short[] buffer, int length) {
        // Simulate a basic frequency spectrum analysis by splitting the buffer into bands
        int bandSize = length / 3; // Divide into 3 bands for left, center, right active columns
        
        // Calculate RMS for each band
        float[] bandRms = new float[3];
        for (int band = 0; band < 3; band++) {
            double sum = 0;
            int start = band * bandSize;
            int end = Math.min((band + 1) * bandSize, length);
            for (int i = start; i < end; i++) {
                sum += buffer[i] * buffer[i];
            }
            bandRms[band] = (float) Math.sqrt(sum / (end - start));
        }
        
        // Log RMS levels for monitoring
        Log.d(TAG, "RMS Levels - Left: " + bandRms[0] + ", Center: " + bandRms[1] + ", Right: " + bandRms[2]);
        
        // Normalize to 0-1 range with increased sensitivity for more reactivity
        float[] normalizedLevels = new float[3];
        for (int band = 0; band < 3; band++) {
            normalizedLevels[band] = Math.min(1.0f, bandRms[band] / 5000.0f);
        }
        
        // Convert to segment counts for each active column
        int leftHeight = Math.round(normalizedLevels[0] * MAX_SEGMENTS);
        int centerHeight = Math.round(normalizedLevels[1] * MAX_SEGMENTS);
        int rightHeight = Math.round(normalizedLevels[2] * MAX_SEGMENTS);
        
        // Ensure center bar is always taller than the side bars
        int maxSideHeight = Math.max(leftHeight, rightHeight);
        centerHeight = Math.max(centerHeight, maxSideHeight + 2); // Center at least 2 segments taller than sides
        
        // Set heights for columns with dark columns in between
        columnHeights[0][0] = 0; // Dark column
        columnHeights[1][0] = Math.max(2, leftHeight); // Active column 1 (left, based on low frequencies)
        columnHeights[2][0] = 0; // Dark column
        columnHeights[3][0] = Math.max(2, centerHeight); // Active column 2 (center, based on mid frequencies)
        columnHeights[4][0] = 0; // Dark column
        columnHeights[5][0] = Math.max(2, rightHeight); // Active column 3 (right, based on high frequencies)
        columnHeights[6][0] = 0; // Dark column
        
        lastUpdateTime = System.currentTimeMillis();
    }

    private void applyDecayMechanism() {
        long currentTime = System.currentTimeMillis();
        if (lastUpdateTime > 0 && (currentTime - lastUpdateTime) > DECAY_TIMEOUT_MS) {
            // Reduce the heights of active columns to prevent sticking at max
            columnHeights[1][0] = Math.max(2, columnHeights[1][0] - 1);
            columnHeights[3][0] = Math.max(2, columnHeights[3][0] - 1);
            columnHeights[5][0] = Math.max(2, columnHeights[5][0] - 1);
            Log.d(TAG, "Applying decay to VU meter heights due to timeout");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopVisualization();
    }
}
