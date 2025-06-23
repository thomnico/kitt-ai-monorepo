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
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * Real-time audio spectrum analyzer with KITT-style visualization
 */
public class KittSpectrumView extends View {
    private static final String TAG = "KittSpectrumView";
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
    
    public KittSpectrumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                // Handle cleanup errors gracefully
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
            Log.w(TAG, "Audio permission not granted, cannot start audio capture");
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
                double[] fftBuffer = new double[1024];
                
                while (isRecording && audioRecord != null && 
                       audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    try {
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
                        
                        Thread.sleep(50); // ~20 FPS update rate
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        // Handle other errors gracefully
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
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopVisualization();
    }
}
