package com.kitt.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kitt.android.voice.VoiceEngine;
import com.kitt.android.KittDashboardView;
import com.kitt.android.R;

/**
 * Main KITT Activity that displays the K2000 interface
 */
public class KittActivity extends AppCompatActivity {
    private KittDashboardView kittDashboard;
    private TextView titleText;
    private VoiceEngine voiceEngine;
    private boolean isListening = false;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "KittActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitt);
        
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
        // Initialize voice engine
        voiceEngine = new VoiceEngine(this);
        voiceEngine.initVoiceEngine();
        
        // Start KITT systems with delay for dramatic effect
        titleText.postDelayed(() -> {
            if (kittDashboard != null) {
                kittDashboard.startSystems();
            }
            startVoiceRecognition();
            playStartupSound();
        }, 1000);
    }
    
    private void startVoiceRecognition() {
        if (voiceEngine != null && !isListening) {
            // Set up callback for transcription updates
            voiceEngine.setTranscriptionCallback(new VoiceEngine.TranscriptionCallback() {
                @Override
                public void onTranscription(String transcription) {
                    runOnUiThread(() -> {
                        if (kittDashboard != null) {
                            kittDashboard.updateTranscription(transcription);
                        }
                    });
                }
                
                @Override
                public void onEngineReset(String reason) {
                    runOnUiThread(() -> {
                        if (kittDashboard != null) {
                            kittDashboard.updateTranscription("RESET voice engine: " + reason);
                        }
                    });
                }
            });
            
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                voiceEngine.startListening();
            } else {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
            isListening = true;
            Log.i(TAG, "Started voice recognition with callback-based updates");
        }
    }
    
    private void stopVoiceRecognition() {
        if (voiceEngine != null && isListening) {
            String finalResult = voiceEngine.stopListening();
            Log.i(TAG, "Final transcription: " + finalResult);
            
            runOnUiThread(() -> {
                if (kittDashboard != null) {
                    kittDashboard.updateTranscription("Final: " + finalResult);
                }
            });
            
            isListening = false;
        }
    }
    
    private void playStartupSound() {
        // Optional: Play KITT startup sound
        try {
            // MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.kitt_startup);
            // if (mediaPlayer != null) {
            //     mediaPlayer.start();
            //     mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            // }
            Log.i(TAG, "Startup sound disabled (no raw resource)");
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
