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
import com.kitt.android.KittButton
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
                transcriptionTextView.text = getString(R.string.microphone_permission_denied)
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
                transcriptionTextView.text = getString(R.string.transcription_format, transcription)
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
        val talkSwitch: KittButton = findViewById(R.id.talkSwitch)
        val languageSwitch: KittButton = findViewById(R.id.languageSwitch)
        val controlSwitch1: KittButton = findViewById(R.id.controlSwitch1)
        val controlSwitch2: KittButton = findViewById(R.id.controlSwitch2)
        val controlSwitch3: KittButton = findViewById(R.id.controlSwitch3)
        val controlSwitch4: KittButton = findViewById(R.id.controlSwitch4)

        var isTalkModeEnabled = false
        var isFrenchLanguage = false
        var isControl1On = false
        var isControl2On = false
        var isControl3On = false
        var isControl4On = false

        talkSwitch.setOnClickListener {
            isTalkModeEnabled = !isTalkModeEnabled
            kittScannerView.setTalkingMode(isTalkModeEnabled)
            if (isTalkModeEnabled) {
                transcriptionTextView.text = getString(R.string.talk_mode_enabled)
            } else {
                transcriptionTextView.text = getString(R.string.talk_mode_disabled)
            }
        }

        languageSwitch.setOnClickListener {
            isFrenchLanguage = !isFrenchLanguage
            currentLanguage = if (isFrenchLanguage) "fr-FR" else "en-US"
            val languageText = if (isFrenchLanguage) getString(R.string.language_french) else getString(R.string.language_english)
            transcriptionTextView.text = getString(R.string.language_set_format, languageText)
        }

        controlSwitch1.setOnClickListener {
            isControl1On = !isControl1On
            transcriptionTextView.text = if (isControl1On) "Control 1: ON" else "Control 1: OFF"
        }

        controlSwitch2.setOnClickListener {
            isControl2On = !isControl2On
            transcriptionTextView.text = if (isControl2On) "Control 2: ON" else "Control 2: OFF"
        }

        controlSwitch3.setOnClickListener {
            isControl3On = !isControl3On
            transcriptionTextView.text = if (isControl3On) "Control 3: ON" else "Control 3: OFF"
        }

        controlSwitch4.setOnClickListener {
            isControl4On = !isControl4On
            transcriptionTextView.text = if (isControl4On) "Control 4: ON" else "Control 4: OFF"
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
                        transcriptionTextView.text = getString(R.string.hearing_format, partialResult)
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
        transcriptionTextView.text = getString(R.string.final_format, finalResult)
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
        val sttStatus = if (isListening) getString(R.string.stt_status_listening) else getString(R.string.stt_status_not_listening)
        val languageText = if (currentLanguage == "fr-FR") getString(R.string.language_french) else getString(R.string.language_english)
        val gpuStatus = getString(R.string.gpu_status)
        val ramStatus = getString(R.string.ram_status)
        val sttEngine = getString(R.string.stt_engine)
        val ttsEngine = getString(R.string.tts_engine)
        sttStatusTextView.text = getString(R.string.stt_status_format, sttEngine, ttsEngine, languageText, sttStatus, interruptionStatus, gpuStatus, ramStatus)
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
            transcriptionTextView.text = getString(R.string.permission_error_format, e.message)
        }
    }

    private fun respondToUser(input: String) {
        val response = when {
            input.contains("hello", ignoreCase = true) || input.contains("bonjour", ignoreCase = true) -> {
                if (currentLanguage == "fr-FR") getString(R.string.response_hello_fr) else getString(R.string.response_hello_en)
            }
            input.contains("time", ignoreCase = true) || input.contains("heure", ignoreCase = true) -> {
                val currentTime = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
                if (currentLanguage == "fr-FR") getString(R.string.response_time_fr_format, currentTime) else getString(R.string.response_time_en_format, currentTime)
            }
            input.contains("stop", ignoreCase = true) || input.contains("arrête", ignoreCase = true) -> {
                if (currentLanguage == "fr-FR") getString(R.string.response_stop_fr) else getString(R.string.response_stop_en)
            }
            else -> {
                if (currentLanguage == "fr-FR") getString(R.string.response_unknown_fr) else getString(R.string.response_unknown_en)
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
