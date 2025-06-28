package com.kitt.android

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ShortBuffer

class OfflineAssistantService : Service() {
    private val TAG = "OfflineAssistantService"
    private var audioDataStream: ByteArrayOutputStream? = null
    private var isProcessing = false
    private var responseCallback: ((String) -> Unit)? = null
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private val SAMPLE_RATE = 16000

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound")
        return LocalBinder()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Offline Assistant Service created")
        initializeVosk()
    }

    override fun onDestroy() {
        recognizer?.close()
        model?.close()
        super.onDestroy()
        Log.d(TAG, "Offline Assistant Service destroyed")
    }

    fun startProcessing() {
        if (!isProcessing) {
            audioDataStream = ByteArrayOutputStream()
            isProcessing = true
            Log.d(TAG, "Started processing audio stream")
        } else {
            Log.w(TAG, "Already processing audio stream")
        }
    }

    fun processAudioBuffer(buffer: ShortArray, size: Int) {
        if (isProcessing && audioDataStream != null) {
            val byteBuffer = ByteBuffer.allocate(size * 2)
            val shortBuffer = ShortBuffer.wrap(buffer, 0, size)
            byteBuffer.asShortBuffer().put(shortBuffer)
            audioDataStream?.write(byteBuffer.array())
            Log.d(TAG, "Processed audio buffer of size $size")
            
            // Process with Vosk if initialized
            if (recognizer != null) {
                val isFinal = recognizer?.acceptWaveForm(buffer, size) ?: false
                if (isFinal) {
                    val result = recognizer?.result ?: "{}"
                    responseCallback?.invoke("Recognized: $result")
                    Log.d(TAG, "Final recognition result: $result")
                } else {
                    val partialResult = recognizer?.partialResult ?: "{}"
                    if (partialResult.isNotEmpty()) {
                        responseCallback?.invoke("Partial: $partialResult")
                        Log.d(TAG, "Partial recognition result: $partialResult")
                    }
                }
            } else {
                // Fallback to simple echo response for testing
                if (audioDataStream?.size() ?: 0 > 16000) { // Roughly 1 second of audio at 16kHz
                    responseCallback?.invoke("Echo: Received audio data")
                    audioDataStream?.reset()
                }
            }
        }
    }

    fun stopProcessing(): String {
        if (isProcessing) {
            isProcessing = false
            val responseText = "Processing stopped. Total audio data received: ${audioDataStream?.size() ?: 0} bytes"
            audioDataStream = null
            Log.d(TAG, responseText)
            responseCallback?.invoke(responseText)
            // Reset recognizer if used
            if (recognizer != null) {
                recognizer = null
                model?.close()
                model = null
                initializeVosk()
            }
            return responseText
        } else {
            Log.w(TAG, "Not currently processing audio stream")
            return "Not processing audio"
        }
    }

    fun setResponseCallback(callback: (String) -> Unit) {
        this.responseCallback = callback
        Log.d(TAG, "Response callback set")
    }
    
    private fun initializeVosk() {
        try {
            // Path to Vosk model - this should match the path used in VoiceEngine
            val modelPath = "${applicationContext.filesDir.absolutePath}/models/vosk/vosk-model-small-en-us-0.15"
            this.model = Model(modelPath)
            this.recognizer = Recognizer(this.model, SAMPLE_RATE.toFloat())
            Log.d(TAG, "Vosk initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Vosk: ${e.message}")
            this.model = null
            this.recognizer = null
        }
    }

    inner class LocalBinder : android.os.Binder() {
        fun getService(): OfflineAssistantService {
            return this@OfflineAssistantService
        }
    }
}
