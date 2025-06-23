package com.kitt.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kitt.android.voice.VoiceEngine

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: android.speech.tts.TextToSpeech
    private lateinit var transcriptionTextView: TextView
    private lateinit var sttStatusTextView: TextView
    private lateinit var startListeningButton: Button
    private lateinit var readCguButton: Button
    private lateinit var modelSelector: Spinner
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
        Log.i(TAG, "onCreate called")
        // UI initialization
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        sttStatusTextView = findViewById(R.id.sttStatusTextView)
        startListeningButton = findViewById(R.id.startListeningButton)
        readCguButton = findViewById(R.id.readCguButton)
        micActivityIndicator = findViewById(R.id.micActivityIndicator)
        modelSelector = findViewById(R.id.modelSelector)

        // Initialize STT status
        updateSttStatus()

        // Set up model selector
        modelSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedModel = parent.getItemAtPosition(position).toString()
                Log.i(TAG, "Selected model: $selectedModel")
                voiceEngine.setModel(if (selectedModel == "French") "fr" else "en-us")
                updateSttStatus()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Initialize Voice Engine
        voiceEngine = VoiceEngine(this)
        if (voiceEngine.initVoiceEngine()) {
            isVoiceEngineInitialized = true
            Log.i(TAG, "Voice engine initialized successfully")
        } else {
            Log.e(TAG, "Failed to initialize voice engine")
        }

    // Initialize TextToSpeech
    textToSpeech = android.speech.tts.TextToSpeech(this) { status ->
        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
            Log.i(TAG, "TextToSpeech initialized successfully")
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
        }
    }

        // Set up button actions
        startListeningButton.setOnClickListener {
            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }
        readCguButton.setOnClickListener {
            readAndroidCgu()
        }

        // Check and request audio permission
        checkPermission()
    }

    private fun checkPermission() {
        Log.i(TAG, "Checking audio permission")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult called")
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            Log.i(TAG, "Audio permission result: ${grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED}")
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize voice engine if not already done
                if (!isVoiceEngineInitialized) {
                    if (voiceEngine.initVoiceEngine()) {
                        isVoiceEngineInitialized = true
                        Log.i(TAG, "Voice engine initialized successfully after permission grant")
                    } else {
                        Log.e(TAG, "Failed to initialize voice engine after permission grant")
                    }
                }
            } else {
                // Permission denied
                Log.w(TAG, "Audio permission denied")
            }
        }
    }

    private var isListening = false

    private fun startListening() {
        Log.i(TAG, "Starting listening for speech")
        showMicActivityAnimation(true)

        // Test if VoiceEngine is initialized as a basic check for listening capability
        if (!isVoiceEngineInitialized) {
            Log.e(TAG, "Voice engine is not initialized")
            transcriptionTextView.text = "Error: Voice engine is not initialized. Please restart the app."
            startListeningButton.isEnabled = true
            showMicActivityAnimation(false)
            return
        }

        // Check permission before starting
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
            startListeningButton.isEnabled = true
            showMicActivityAnimation(false)
            return
        }

        // Use the VoiceEngine for STT
        try {
            if (voiceEngine.startListening()) {
                isListening = true
                startListeningButton.text = "Stop Listening"
                Log.i(TAG, "Started listening with VoiceEngine")
                // Start processing voice input in a loop
                processVoiceInputLoop()
            } else {
                Log.e(TAG, "Failed to start listening with VoiceEngine")
                transcriptionTextView.text = "Error: Failed to start listening."
                startListeningButton.isEnabled = true
                showMicActivityAnimation(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting listening with VoiceEngine: ${e.message}")
            transcriptionTextView.text = "Error starting listening."
            startListeningButton.isEnabled = true
            showMicActivityAnimation(false)
        }
    }

    private fun respondToUser(transcription: String) {
        val response = if (transcription.isNotEmpty()) "I heard: $transcription. Do you want me to repeat?" else "Do you want me to repeat?"
        try {
            Log.i(TAG, "Using Android TTS for output")
            if (::textToSpeech.isInitialized) {
                textToSpeech.speak(response, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                transcriptionTextView.text = response
            } else {
                Log.w(TAG, "TextToSpeech not initialized yet")
                transcriptionTextView.text = response
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with Android TTS: ${e.message}")
            transcriptionTextView.text = response
        }
        Log.i(TAG, "Responding with: $response")
    }

    fun onInit(status: Int) {
        Log.i(TAG, "onInit called with status: $status")
        // TTS initialization logic handled in onCreate
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy called")
        // Clean up resources
        if (isListening) {
            voiceEngine.stopListening()
            isListening = false
        }
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        Log.i(TAG, "Activity destroyed, resources released")
    }

    private fun readAndroidCgu() {
        val cguText = "This is a placeholder for the Android Terms of Use. By using this application, you agree to the terms and conditions set forth by the developers. For demonstration purposes, this text is being read to allow voice interruption."
        try {
            Log.i(TAG, "Using Android TTS for output of CGU")
            if (::textToSpeech.isInitialized) {
                textToSpeech.speak(cguText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                transcriptionTextView.text = cguText
            } else {
                Log.w(TAG, "TextToSpeech not initialized yet")
                transcriptionTextView.text = cguText
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with Android TTS for CGU: ${e.message}")
            transcriptionTextView.text = cguText
        }
        Log.i(TAG, "Reading Android CGU for interruption")
        // Enable interruption after starting to read CGU
        isInterruptionEnabled = true
        updateSttStatus()
        Log.i(TAG, "Voice interruption enabled")
    }


    private fun updateSttStatus() {
        val interruptionStatus = if (isInterruptionEnabled) "Enabled" else "Disabled"
        val modelSize = getVoskModelSize()
        val gpuInfo = getGpuInfo()
        val selectedModel = if (modelSelector.selectedItem != null) modelSelector.selectedItem.toString() else "Unknown"
        sttStatusTextView.text = "STT: $sttSystem | Interruption: $interruptionStatus | Model: $selectedModel | Vosk Model Size: $modelSize | GPU: $gpuInfo"
        Log.i(TAG, "Updated STT status: STT=$sttSystem, Interruption=$interruptionStatus, Model=$selectedModel, Vosk Model Size=$modelSize, GPU=$gpuInfo")
    }

    private fun getVoskModelSize(): String {
        return try {
            Log.i(TAG, "Getting Vosk model size")
            "50 MB" // Approximate size for small models as per documentation
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Vosk model size: ${e.message}")
            return "Unknown"
        }
    }

    private fun getGpuInfo(): String {
        return "Adreno 643" // Hardcoded as per monorepo rules for target hardware
    }

    private fun showMicActivityAnimation(show: Boolean) {
        if (show) {
            micActivityIndicator.visibility = View.VISIBLE
            val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
            micActivityIndicator.startAnimation(pulseAnimation)
            Log.i(TAG, "Mic activity indicator shown with animation")
        } else {
            micActivityIndicator.visibility = View.GONE
            micActivityIndicator.clearAnimation()
            Log.i(TAG, "Mic activity indicator hidden")
        }
    }

    private fun processVoiceInputLoop() {
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
                        if (isInterruptionEnabled) {
                            Log.i(TAG, "Interruption enabled, checking for specific command")
                            if (partialResult.contains("stop", ignoreCase = true) || partialResult.contains("interrupt", ignoreCase = true)) {
                                Log.i(TAG, "Interruption detected in transcription")
                                isListening = false
                                voiceEngine.stopListening()
                                runOnUiThread {
                                    startListeningButton.text = "Start Listening"
                                    showMicActivityAnimation(false)
                                    respondToUser(partialResult)
                                }
                                break
                            }
                        }
                    }
                    Thread.sleep(100) // Adjust sleep time to balance latency and CPU usage
                } catch (e: Exception) {
                    Log.e(TAG, "Error in voice input processing loop: ${e.message}")
                }
            }
            Log.i(TAG, "Stopped voice input processing loop")
        }.start()
    }

    private fun stopListening() {
        Log.i(TAG, "Stopping listening for speech")
        isListening = false
        showMicActivityAnimation(false)
        startListeningButton.text = "Start Listening"
        val finalResult = voiceEngine.stopListening()
        Log.i(TAG, "Final transcription: $finalResult")
        transcriptionTextView.text = "Final: $finalResult"
        respondToUser(finalResult)
    }
}
