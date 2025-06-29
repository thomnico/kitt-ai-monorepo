package com.kitt.android;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kitt.android.R;
import com.kitt.android.KittButton;
import com.kitt.android.KittScannerViewJava;
import com.kitt.android.KittSpectrumView;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main KITT dashboard container that combines all UI components
 */
public class KittDashboardView extends LinearLayout {
    private KittScannerViewJava scannerView;
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
    
    public KittDashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        
        String[] buttonLabels = {"LANG", "VOSK", "P1", "P2", "S1", "S2"};
        int[] buttonColors = {
            Color.YELLOW, // Changed LANG to yellow as per request
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
                80, 60); // Fixed size instead of weight-based
            params.setMargins(3, 3, 3, 3);
            sideButtonsLayout.addView(buttons[i], params);
        }
        
        LinearLayout.LayoutParams sideParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.MATCH_PARENT);
        addView(sideButtonsLayout, sideParams);
    }
    
    private TextView transcriptionView;

    private void initCenterPanel(Context context) {
        LinearLayout centerPanel = new LinearLayout(context);
        centerPanel.setOrientation(VERTICAL);
        centerPanel.setBackgroundColor(Color.BLACK);
        centerPanel.setPadding(20, 20, 20, 20);
        
        // KITT Scanner
        scannerView = new KittScannerViewJava(context);
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
        
        // Transcription TextView
        transcriptionView = new TextView(context);
        transcriptionView.setText(R.string.transcription_waiting);
        transcriptionView.setTextColor(Color.WHITE);
        transcriptionView.setTextSize(16);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 20, 0, 0);
        centerPanel.addView(transcriptionView, textParams);
        
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.MATCH_PARENT, 2.0f);
        addView(centerPanel, centerParams);
    }
    
    private void onKittButtonClick(String buttonLabel) {
        Toast.makeText(getContext(), getContext().getString(R.string.button_activated_format, buttonLabel), Toast.LENGTH_SHORT).show();
        
        // Add specific button functionality here
        switch (buttonLabel) {
            case "LANG":
                // Language selection control (ENG/FR)
                break;
            case "VOSK":
                // Voice model diagnostics
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
        if (scannerView != null) {
            scannerView.startScanning();
        }
        if (spectrumView != null) {
            spectrumView.startVisualization();
        }
        
        // Optional: Start button glow effects
        if (buttons != null) {
            for (KittButton button : buttons) {
                button.startGlow();
            }
        }
    }
    
    public void stopSystems() {
        if (scannerView != null) {
            scannerView.stopScanning();
        }
        if (spectrumView != null) {
            spectrumView.stopVisualization();
        }
        
        if (buttons != null) {
            for (KittButton button : buttons) {
                button.stopGlow();
            }
        }
    }
    
    /**
     * Update the transcription text in the dashboard
     * @param text The transcription text to display
     */
    public void updateTranscription(String text) {
        if (transcriptionView != null) {
            transcriptionView.setText(getContext().getString(R.string.transcription_format, text));
        }
    }
    
    /**
     * Simulate a button press programmatically (for voice commands)
     */
    public void simulateButtonPress(String buttonLabel) {
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                KittButton button = buttons[i];
                // Get the button's text to match with the label
                if (buttonLabel.equals(getButtonLabel(i))) {
                    // Trigger the button's click action
                    button.performClick();
                    // Add visual feedback
                    button.startGlow();
                    // Stop glow after a short delay
                    button.postDelayed(() -> button.stopGlow(), 1000);
                    break;
                }
            }
        }
        
        // Also trigger the same logic as manual button click
        onKittButtonClick(buttonLabel);
    }
    
    /**
     * Get button label by index
     */
    private String getButtonLabel(int index) {
        String[] buttonLabels = {"LANG", "VOSK", "P1", "P2", "S1", "S2"};
        if (index >= 0 && index < buttonLabels.length) {
            return buttonLabels[index];
        }
        return "";
    }
}
