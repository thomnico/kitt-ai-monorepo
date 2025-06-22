package com.kitt.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kitt.android.voice.VoiceEngine
import java.util.Locale
import android.view.animation.AnimationUtils
import com.kitt.android.R

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech
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
    private val sttSystem = "Kyutai Moshi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        sttStatusTextView = findViewById(R.id.sttStatusTextView)
        startListeningButton = findViewById(R.id.startListeningButton)
        readCguButton = findViewById(R.id.readCguButton)
        downloadMoshiButton = findViewById(R.id.downloadMoshiButton)
        micActivityIndicator = findViewById(R.id.micActivityIndicator)

        // Initialize STT status
        updateSttStatus()

        // Initialize Voice Engine
        voiceEngine = VoiceEngine(this)
        if (voiceEngine.initVoiceEngine()) {
            isVoiceEngineInitialized = true
            Log.d(TAG, "Voice engine initialized successfully")
        } else {
            Log.e(TAG, "Failed to initialize voice engine")
            Toast.makeText(this@MainActivity, "Failed to initialize voice engine", Toast.LENGTH_SHORT).show()
        }

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // Set up buttons
        startListeningButton.setOnClickListener {
            startListening()
        }
        readCguButton.setOnClickListener {
            readAndroidCgu()
        }
        downloadMoshiButton.setOnClickListener {
            downloadMoshiModel()
        }

        // Check and request audio permission
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Audio permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Audio permission denied", Toast.LENGTH_SHORT).show()
                startListeningButton.isEnabled = false
            }
        }
    }

    private fun startListening() {
        transcriptionTextView.text = "Listening..."
        startListeningButton.isEnabled = false
        Log.d(TAG, "Started listening for speech")

        // Show mic activity indicator if interruption is enabled
        if (isInterruptionEnabled) {
            showMicActivityAnimation(true)
        }

        // Test if VoiceEngine is initialized as a basic check for listening capability
        if (!isVoiceEngineInitialized) {
            Log.e(TAG, "Voice engine is not initialized")
            transcriptionTextView.text = "Error: Voice engine is not initialized. Please restart the app."
            startListeningButton.isEnabled = true
            showMicActivityAnimation(false)
            return
        }

        // Use the VoiceEngine for STT
        try {
            Log.d(TAG, "Processing voice input with VoiceEngine")
            val transcription = voiceEngine.processVoiceInput(ByteArray(0)) // Placeholder input
            transcriptionTextView.text = transcription
            Log.d(TAG, "Transcribed text: $transcription")
            if (isInterruptionEnabled) {
                Log.d(TAG, "Voice interruption is enabled, checking for interruption...")
                Handler(Looper.getMainLooper()).postDelayed({
                    transcriptionTextView.text = "Interrupted: User interruption detected."
                    Log.d(TAG, "User interruption simulated")
                    showMicActivityAnimation(true)
                }, 100)
            } else {
                respondToUser()
            }
            startListeningButton.isEnabled = true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing voice input with VoiceEngine: ${e.message}")
            transcriptionTextView.text = "Error processing voice input."
            startListeningButton.isEnabled = true
            showMicActivityAnimation(false)
        }
    }

    private fun respondToUser() {
        val response = "Do you want me to repeat?"
        // Placeholder for TTS with VoiceEngine
        try {
            Log.d(TAG, "Using VoiceEngine for TTS output")
            // In a real implementation, this would call a TTS function from VoiceEngine
            // For now, fallback to Android TTS
            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error with VoiceEngine TTS: ${e.message}")
            // Fallback to Android TTS if VoiceEngine TTS fails
            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        Log.d(TAG, "Responding with: $response")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Language not supported for TTS")
            Toast.makeText(this@MainActivity, "TTS language not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
            Toast.makeText(this@MainActivity, "TTS initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
        Log.d(TAG, "Activity destroyed, resources released")
    }

    private fun readAndroidCgu() {
        val cguText = "This is a placeholder for the Android Terms of Use. By using this application, you agree to the terms and conditions set forth by the developers. For demonstration purposes, this text is being read to allow voice interruption."
        // Placeholder for TTS with VoiceEngine for CGU
        try {
            Log.d(TAG, "Using VoiceEngine for TTS output of CGU")
            // In a real implementation, this would call a TTS function from VoiceEngine
            // For now, fallback to Android TTS
            textToSpeech.speak(cguText, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error with VoiceEngine TTS for CGU: ${e.message}")
            // Fallback to Android TTS if VoiceEngine TTS fails
            textToSpeech.speak(cguText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        Log.d(TAG, "Reading Android CGU for interruption")
        // Enable interruption after starting to read CGU
        isInterruptionEnabled = true
        updateSttStatus()
        Toast.makeText(this@MainActivity, "Voice interruption enabled", Toast.LENGTH_SHORT).show()
    }

    private fun downloadMoshiModel() {
        Toast.makeText(this@MainActivity, "Initiating Moshi model download...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Starting Moshi model download process")
        
        // Use VoiceEngine to download the Moshi model from a predefined source
        val sourceUrl = "https://kyutai.org/moshi-model" // Actual URL to be defined based on project configuration
        try {
            val downloadSuccess = voiceEngine.downloadMoshiModel(sourceUrl)
            if (downloadSuccess) {
                Log.d(TAG, "Moshi model download initiated successfully from $sourceUrl")
                Toast.makeText(this@MainActivity, "Moshi model download initiated.", Toast.LENGTH_SHORT).show()
                updateSttStatus() // Update status to reflect any changes in model size
            } else {
                Log.e(TAG, "Failed to initiate Moshi model download from $sourceUrl")
                Toast.makeText(this@MainActivity, "Failed to initiate Moshi model download.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Moshi model download: ${e.message}")
            Toast.makeText(this@MainActivity, "Error downloading Moshi model.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSttStatus() {
        val interruptionStatus = if (isInterruptionEnabled) "Enabled" else "Disabled"
        val modelSize = getMoshiModelSize()
        val gpuInfo = getGpuInfo()
        sttStatusTextView.text = "STT: $sttSystem | Interruption: $interruptionStatus | Moshi Model Size: $modelSize | GPU: $gpuInfo"
        Log.d(TAG, "Updated STT status: STT=$sttSystem, Interruption=$interruptionStatus, Moshi Model Size=$modelSize, GPU=$gpuInfo")
    }

    private fun getMoshiModelSize(): String {
        return try {
            val modelPath = "models/moshi/placeholder_model.txt"
            val assetManager = assets
            val inputStream = assetManager.open(modelPath)
            val size = inputStream.available().toLong()
            inputStream.close()
            val sizeInMB = size / (1024 * 1024)
            "$sizeInMB MB"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Moshi model size: ${e.message}")
            "Unknown"
        }
    }

    private fun getGpuInfo(): String {
        return "Adreno 643" // Hardcoded as per monorepo rules for target hardware
    }

    private fun showMicActivityAnimation(show: Boolean) {
        if (show) {
            micActivityIndicator.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.pulse)
            micActivityIndicator.startAnimation(animation)
            Log.d(TAG, "Mic activity indicator shown with animation")
        } else {
            micActivityIndicator.visibility = View.VISIBLE
            micActivityIndicator.clearAnimation()
            Log.d(TAG, "Mic activity indicator hidden")
        }
    }
}
