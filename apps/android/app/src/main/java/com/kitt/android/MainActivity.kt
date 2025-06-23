package com.kitt.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kitt.android.voice.VoiceEngine
import android.view.animation.AnimationUtils
import com.kitt.android.R

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: Any // Placeholder for TextToSpeech
    private lateinit var transcriptionTextView: TextView
    private lateinit var sttStatusTextView: TextView
    private lateinit var startListeningButton: Button
    private lateinit var readCguButton: Button
    private lateinit var downloadMoshiButton: Button
    private lateinit var micActivityIndicator: ImageView
    private lateinit var voiceEngine: VoiceEngine
    private var isVoiceEngineInitialized = false
    private val RECORD_AUDIO_PERMISSION_CODE = 1
    private val TAG = "MainActivity"
    private var isInterruptionEnabled = false
    private val sttSystem = "Vosk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("onCreate called")
        // UI initialization
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        sttStatusTextView = findViewById(R.id.sttStatusTextView)
        startListeningButton = findViewById(R.id.startListeningButton)
        readCguButton = findViewById(R.id.readCguButton)
        downloadMoshiButton = findViewById(R.id.downloadMoshiButton) // This button will now be used to check voice engine status
        micActivityIndicator = findViewById(R.id.micActivityIndicator)

        // Initialize STT status
        updateSttStatus()

        // Initialize Voice Engine
        voiceEngine = VoiceEngine(this)
        if (voiceEngine.initVoiceEngine()) {
            isVoiceEngineInitialized = true
            println("Voice engine initialized successfully")
        } else {
            println("Failed to initialize voice engine")
        }

        // Initialize TextToSpeech placeholder
        textToSpeech = Any()

        // Set up button actions (placeholders)
        println("Button listeners setup placeholder")

        // Check and request audio permission placeholder
        println("Audio permission check placeholder")
    }

    private fun checkPermission() {
        println("Checking audio permission (placeholder)")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("onRequestPermissionsResult called")
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            println("Audio permission result: ${grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED}")
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize voice engine if not already done
                if (!isVoiceEngineInitialized) {
                    if (voiceEngine.initVoiceEngine()) {
                        isVoiceEngineInitialized = true
                        println("Voice engine initialized successfully after permission grant")
                    } else {
                        println("Failed to initialize voice engine after permission grant")
                    }
                }
            } else {
                // Permission denied
                println("Audio permission denied")
            }
        }
    }

    private fun startListening() {
        println("Listening... (placeholder)")
        println("Started listening for speech")

        // Show mic activity indicator if interruption is enabled (placeholder)
        if (isInterruptionEnabled) {
            println("Showing mic activity animation (placeholder)")
        }

        // Test if VoiceEngine is initialized as a basic check for listening capability
        if (!isVoiceEngineInitialized) {
            println("Voice engine is not initialized")
            println("Error: Voice engine is not initialized. Please restart the app.")
            println("Enabling start listening button (placeholder)")
            println("Hiding mic activity animation (placeholder)")
            return
        }

        // Use the VoiceEngine for STT
        try {
            println("Processing voice input with VoiceEngine")
            val transcription = voiceEngine.processVoiceInput()
            println("Transcribed text: $transcription")
            if (isInterruptionEnabled) {
                println("Voice interruption is enabled, checking for interruption...")
                println("Simulating user interruption after delay (placeholder)")
                println("Interrupted: User interruption detected.")
                println("Showing mic activity animation (placeholder)")
            } else {
                respondToUser()
            }
            println("Enabling start listening button (placeholder)")
        } catch (e: Exception) {
            println("Error processing voice input with VoiceEngine: ${e.message}")
            println("Error processing voice input.")
            println("Enabling start listening button (placeholder)")
            println("Hiding mic activity animation (placeholder)")
        }
    }

    private fun respondToUser() {
        val response = "Do you want me to repeat?"
        // Placeholder for TTS with VoiceEngine
        try {
            println("Using VoiceEngine for TTS output")
            // In a real implementation, this would call a TTS function from VoiceEngine
            // For now, fallback to Android TTS (placeholder)
            println("Speaking response: $response (placeholder)")
        } catch (e: Exception) {
            println("Error with VoiceEngine TTS: ${e.message}")
            // Fallback to Android TTS if VoiceEngine TTS fails (placeholder)
            println("Speaking response via fallback: $response (placeholder)")
        }
        println("Responding with: $response")
    }

    fun onInit(status: Int) {
        println("onInit called with status: $status (placeholder)")
        // Placeholder for TTS initialization logic
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy called")
        // Placeholder for resource cleanup
        println("Activity destroyed, resources released")
    }

    private fun readAndroidCgu() {
        val cguText = "This is a placeholder for the Android Terms of Use. By using this application, you agree to the terms and conditions set forth by the developers. For demonstration purposes, this text is being read to allow voice interruption."
        // Placeholder for TTS with VoiceEngine for CGU
        try {
            println("Using VoiceEngine for TTS output of CGU")
            // In a real implementation, this would call a TTS function from VoiceEngine
            // For now, fallback to Android TTS (placeholder)
            println("Speaking CGU text (placeholder)")
        } catch (e: Exception) {
            println("Error with VoiceEngine TTS for CGU: ${e.message}")
            // Fallback to Android TTS if VoiceEngine TTS fails (placeholder)
            println("Speaking CGU text via fallback (placeholder)")
        }
        println("Reading Android CGU for interruption")
        // Enable interruption after starting to read CGU
        isInterruptionEnabled = true
        updateSttStatus()
        println("Voice interruption enabled (placeholder)")
    }

    private fun checkVoiceEngine() {
        println("Checking voice engine status... (placeholder)")
        println("Starting voice engine status check")
        
        // Check if the voice engine is initialized
        if (isVoiceEngineInitialized) {
            println("Voice engine is initialized and ready")
            // Optionally, perform a deeper check or test if supported by VoiceEngine
            try {
                val status = voiceEngine.getEngineStatus()
                println("Voice engine detailed status: $status")
            } catch (e: Exception) {
                println("Error retrieving detailed voice engine status: ${e.message}")
            }
        } else {
            println("Voice engine is not initialized")
            // Attempt to initialize if not already done
            if (voiceEngine.initVoiceEngine()) {
                isVoiceEngineInitialized = true
                println("Voice engine initialized successfully during status check")
            } else {
                println("Failed to initialize voice engine during status check")
            }
        }
        updateSttStatus() // Update status to reflect any changes
    }

    private fun updateSttStatus() {
        val interruptionStatus = if (isInterruptionEnabled) "Enabled" else "Disabled"
        val modelSize = getVoskModelSize()
        val gpuInfo = getGpuInfo()
        println("STT: $sttSystem | Interruption: $interruptionStatus | Vosk Model Size: $modelSize | GPU: $gpuInfo (placeholder)")
        println("Updated STT status: STT=$sttSystem, Interruption=$interruptionStatus, Vosk Model Size=$modelSize, GPU=$gpuInfo")
    }

    private fun getVoskModelSize(): String {
        return try {
            println("Getting Vosk model size (placeholder)")
            "0 MB" // Placeholder size
        } catch (e: Exception) {
            println("Error getting Vosk model size: ${e.message}")
            "Unknown"
        }
    }

    private fun getGpuInfo(): String {
        return "Adreno 643" // Hardcoded as per monorepo rules for target hardware
    }

    private fun showMicActivityAnimation(show: Boolean) {
        if (show) {
            println("Mic activity indicator shown with animation (placeholder)")
        } else {
            println("Mic activity indicator hidden (placeholder)")
        }
    }
}
