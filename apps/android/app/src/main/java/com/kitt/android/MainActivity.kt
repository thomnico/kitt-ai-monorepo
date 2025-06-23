package com.kitt.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
// import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.SwitchCompat
import com.kitt.android.voice.VoiceEngine
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var kittScannerView: KittScannerView
    private lateinit var transcriptionTextView: TextView
    private lateinit var sttStatusTextView: TextView
    private var isListening = false
    private lateinit var voiceEngine: VoiceEngine
    private var currentLanguage = "en-US"
    private var interruptionStatus = "Unknown"
    private val TAG = "MainActivity"
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startListeningWithPermission()
        } else {
            Log.e(TAG, "Microphone permission denied")
            transcriptionTextView.text = "Microphone permission required for voice recognition"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        kittScannerView = findViewById(R.id.kittScannerView)
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        sttStatusTextView = findViewById(R.id.sttStatusTextView)

        voiceEngine = VoiceEngine(this)
        voiceEngine.initVoiceEngine()
        voiceEngine.setTranscriptionCallback(object : VoiceEngine.TranscriptionCallback {
            override fun onTranscription(transcription: String) {
                runOnUiThread {
                    transcriptionTextView.text = transcription
                }
            }
        })

        // Check permissions and start listening automatically when the app launches
        checkPermissionsAndStartListening()

        updateSttStatus()

        // Setup switch listeners for talk mode and language selection
        setupSwitchListeners()
    }

    private fun setupSwitchListeners() {
        val talkSwitch: SwitchCompat = findViewById(R.id.talkSwitch)
        val languageSwitch: SwitchCompat = findViewById(R.id.languageSwitch)

        talkSwitch.setOnCheckedChangeListener { _, isChecked ->
            kittScannerView.setTalkingMode(isChecked)
            if (isChecked) {
                transcriptionTextView.text = "Talk mode enabled"
            } else {
                transcriptionTextView.text = "Talk mode disabled"
            }
        }

        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            currentLanguage = if (isChecked) "fr-FR" else "en-US"
            val languageText = if (isChecked) "French" else "English"
            transcriptionTextView.text = "Language set to $languageText"
        }
    }

    @SuppressLint("MissingPermission")
    private fun startListening() {
        voiceEngine.startListening()
        Thread {
            Log.i(TAG, "Starting voice input processing loop")
            while (isListening) {
                try {
                    val partialResult = voiceEngine.processVoiceInput()
                    if (partialResult.isNotEmpty()) {
                        Log.i(TAG, "Partial transcription: $partialResult")
                        runOnUiThread {
                            transcriptionTextView.text = "Hearing: $partialResult"
                        }
                    }
                    Thread.sleep(100) // Adjust sleep time to balance latency and CPU usage
                } catch (e: Exception) {
                    Log.e(TAG, "Error in voice input processing loop: ${e.message}")
                }
            }
            Log.i(TAG, "Stopped voice input processing loop")
        }.start()
        isListening = true
        toggleScannerAnimation(true)
        updateSttStatus()
    }

    private fun stopListening() {
        val finalResult = voiceEngine.stopListening()
        Log.i(TAG, "Final transcription: $finalResult")
        transcriptionTextView.text = "Final: $finalResult"
        respondToUser(finalResult)
        isListening = false
        toggleScannerAnimation(false)
        updateSttStatus()
    }

    private fun toggleScannerAnimation(show: Boolean) {
        try {
            if (show) {
                kittScannerView.visibility = View.VISIBLE
                kittScannerView.resumeAnimation()
            } else {
                kittScannerView.stopAnimation()
                kittScannerView.visibility = View.VISIBLE // Keep visible but stop animation
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling scanner animation: ${e.message}")
        }
    }

    private fun updateSttStatus() {
        val sttStatus = if (isListening) "Listening" else "Not Listening"
        val languageText = if (currentLanguage == "fr-FR") "French" else "English"
        // Placeholder values for GPU and RAM as actual detection would require additional system queries
        val gpuStatus = "GPU: Detected (Placeholder)"
        val ramStatus = "RAM: 4GB (Placeholder)"
        val sttEngine = "STT: Vosk"
        val ttsEngine = "TTS: None (Vosk-only)"
        sttStatusTextView.text = "$sttEngine | $ttsEngine | Language: $languageText | $sttStatus | Interruption: $interruptionStatus | $gpuStatus | $ramStatus"
    }

    private fun checkPermissionsAndStartListening() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, start listening
                startListeningWithPermission()
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startListeningWithPermission() {
        try {
            startListening()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when starting voice recognition: ${e.message}")
            transcriptionTextView.text = "Permission error: ${e.message}"
        }
    }

    private fun respondToUser(input: String) {
        val response = when {
            input.contains("hello", ignoreCase = true) || input.contains("bonjour", ignoreCase = true) -> {
                if (currentLanguage == "fr-FR") "Bonjour, comment puis-je vous aider ?" else "Hello, how can I help you?"
            }
            input.contains("time", ignoreCase = true) || input.contains("heure", ignoreCase = true) -> {
                val currentTime = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
                if (currentLanguage == "fr-FR") "Il est $currentTime." else "The time is $currentTime."
            }
            input.contains("stop", ignoreCase = true) || input.contains("arrête", ignoreCase = true) -> {
                if (currentLanguage == "fr-FR") "D'accord, j'arrête." else "Okay, stopping now."
            }
            else -> {
                if (currentLanguage == "fr-FR") "Désolé, je ne comprends pas." else "Sorry, I don't understand."
            }
        }
        transcriptionTextView.text = response
        Log.i(TAG, "Voice feedback: $response")
        interruptionStatus = "Not Speaking"
        updateSttStatus()
    }

    override fun onDestroy() {
        // No release method in VoiceEngine, just stop listening if active
        if (isListening) {
            voiceEngine.stopListening()
        }
        super.onDestroy()
    }
}
