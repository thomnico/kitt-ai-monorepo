package com.kitt.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var transcriptionTextView: TextView
    private lateinit var startListeningButton: Button
    private val RECORD_AUDIO_PERMISSION_CODE = 1
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        startListeningButton = findViewById(R.id.startListeningButton)

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // Set up button to start listening
        startListeningButton.setOnClickListener {
            startListening()
        }

        // Check and request audio permission
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
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
                Toast.makeText(this, "Audio permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show()
                startListeningButton.isEnabled = false
            }
        }
    }

    private fun startListening() {
        transcriptionTextView.text = "Listening..."
        startListeningButton.isEnabled = false
        Log.d(TAG, "Started listening for speech")

        // Simulate local speech recognition with Kyutai Moshi model
        // In a real implementation, this would involve capturing audio input and processing it with the Kyutai model
        // For now, we'll simulate a delay and a mock transcription result
        Handler(Looper.getMainLooper()).postDelayed({
            val mockTranscription = "This is a mock transcription from Kyutai Moshi model."
            transcriptionTextView.text = mockTranscription
            Log.d(TAG, "Mock transcribed text: $mockTranscription")
            respondToUser()
            startListeningButton.isEnabled = true
        }, 3000) // Simulate 3 seconds of listening
    }

    private fun respondToUser() {
        val response = "Do you want me to repeat?"
        textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d(TAG, "Responding with: $response")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported for TTS")
                Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
            Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
        Log.d(TAG, "Activity destroyed, resources released")
    }
}
