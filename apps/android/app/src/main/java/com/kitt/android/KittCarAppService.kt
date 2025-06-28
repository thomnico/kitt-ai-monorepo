package com.kitt.android

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.validation.HostValidator
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.model.ActionStrip
import androidx.core.graphics.drawable.IconCompat
import androidx.core.content.ContextCompat
import com.kitt.android.R
import android.util.Log
import com.kitt.android.voice.VoiceEngine

class KittCarAppService : CarAppService() {
    private val TAG = "KittCarAppService"
    private var voiceEngine: VoiceEngine? = null

    override fun onCreateSession(): Session {
        Log.d(TAG, "Creating new session for Android Auto")
        voiceEngine = VoiceEngine(this)
        voiceEngine?.initVoiceEngine()
        return KittSession(voiceEngine)
    }

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onDestroy() {
        voiceEngine = null
        super.onDestroy()
        Log.d(TAG, "KittCarAppService destroyed")
    }
}

class KittSession(private val voiceEngine: VoiceEngine?) : Session() {
    private val TAG = "KittSession"
    
    override fun onCreateScreen(intent: Intent): Screen {
        Log.d(TAG, "Creating main screen for Android Auto with intent: $intent")
        return KittMainScreen(carContext, voiceEngine)
    }
}

class KittMainScreen(carContext: androidx.car.app.CarContext, private val voiceEngine: VoiceEngine?) : Screen(carContext) {
    private val TAG = "KittMainScreen"
    private var isVoiceActive = false
    private var currentMode = "STANDBY"
    private var currentTab = "communication"
    private var isRecording = false
    private var transcriptionText = ""
    
    init {
        Log.d(TAG, "Initializing KITT main screen for Android Auto")
        setupVoiceInteraction()
    }
    
    override fun onGetTemplate(): Template {
        Log.d(TAG, "Building KITT communication dashboard template for Android Auto")
        
        // Using ListTemplate for navigation between tabs
        val listBuilder = ItemList.Builder()
        
        // Create list items for tab navigation
        val communicationItem = Row.Builder()
            .setTitle("Communication")
            .addText("Voice call and messaging options")
            .setOnClickListener {
                Log.d(TAG, "Switching to Communication tab")
                currentTab = "communication"
                invalidate()
            }
            .build()
            
        val voiceModelsItem = Row.Builder()
            .setTitle("Voice Settings")
            .addText("Voice recognition and language options")
            .setOnClickListener {
                Log.d(TAG, "Switching to Voice Settings tab")
                currentTab = "voice"
                invalidate()
            }
            .build()
            
        val assistantItem = Row.Builder()
            .setTitle("Assistant")
            .addText("Interact with offline assistant")
            .setOnClickListener {
                Log.d(TAG, "Switching to Assistant tab")
                currentTab = "assistant"
                invalidate()
            }
            .build()
            
        listBuilder.addItem(communicationItem)
        listBuilder.addItem(voiceModelsItem)
        listBuilder.addItem(assistantItem)
        
        // Add content based on the selected tab
        when (currentTab) {
            "communication" -> {
                // Communication tab content
                val statusRow = Row.Builder()
                    .setTitle("🔴 KITT COMM SYSTEM")
                    .addText("Status: $currentMode")
                    .build()
                    
                val voiceRow = Row.Builder()
                    .setTitle("🎤 VOICE CALLS")
                    .addText(if (isVoiceActive) "ACTIVE CALL..." else "Say 'Hey Assistant' to make a call")
                    .build()
                    
                listBuilder.addItem(statusRow)
                listBuilder.addItem(voiceRow)
                
                val callItem = Row.Builder()
                    .setTitle("Make Call")
                    .addText("Initiate a voice call")
                    .setOnClickListener {
                        setMode("CALLING")
                    }
                    .build()
                    
                val messageItem = Row.Builder()
                    .setTitle("Send Message")
                    .addText("Send a voice-to-text message")
                    .setOnClickListener {
                        setMode("MESSAGING")
                    }
                    .build()
                    
                listBuilder.addItem(callItem)
                listBuilder.addItem(messageItem)
            }
            "voice" -> {
                // Voice Settings tab content
                val voskRow = Row.Builder()
                    .setTitle("🔊 VOSK Engine")
                    .addText("Offline voice recognition")
                    .build()
                    
                val androidRow = Row.Builder()
                    .setTitle("🤖 Android Speech")
                    .addText("Built-in speech recognition")
                    .build()
                    
                listBuilder.addItem(voskRow)
                listBuilder.addItem(androidRow)
                
                val frItem = Row.Builder()
                    .setTitle("French")
                    .addText("Set language to French")
                    .setOnClickListener {
                        setLanguage("FR")
                    }
                    .build()
                    
                val enItem = Row.Builder()
                    .setTitle("English")
                    .addText("Set language to English")
                    .setOnClickListener {
                        setLanguage("EN")
                    }
                    .build()
                    
                listBuilder.addItem(frItem)
                listBuilder.addItem(enItem)
            }
            "assistant" -> {
                // Assistant tab content
                val statusRow = Row.Builder()
                    .setTitle("🗣️ OFFLINE ASSISTANT")
                    .addText(if (isRecording) "RECORDING..." else "Ready to assist")
                    .build()
                    
                val transcriptionRow = Row.Builder()
                    .setTitle("Transcription")
                    .addText(if (transcriptionText.isEmpty()) "No transcription yet" else transcriptionText)
                    .build()
                    
                listBuilder.addItem(statusRow)
                listBuilder.addItem(transcriptionRow)
                
                val recordItem = Row.Builder()
                    .setTitle(if (isRecording) "Stop Recording" else "Start Recording")
                    .addText(if (isRecording) "Stop streaming to assistant" else "Start streaming audio to assistant")
                    .setOnClickListener {
                        if (isRecording) {
                            stopRecording()
                        } else {
                            startRecording()
                        }
                    }
                    .build()
                    
                listBuilder.addItem(recordItem)
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("KITT Comm Dashboard - ${when (currentTab) {
                "communication" -> "Communication"
                "voice" -> "Voice Settings"
                else -> "Assistant"
            }}")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
    
    private fun setMode(mode: String) {
        Log.d(TAG, "Setting mode to $mode")
        currentMode = mode
        invalidate() // Refresh the template
    }
    
    private fun setLanguage(lang: String) {
        Log.d(TAG, "Setting language to $lang")
        currentMode = if (lang == "FR") "French Mode" else "English Mode"
        invalidate() // Refresh the template
    }
    
    private fun setupVoiceInteraction() {
        Log.d(TAG, "Setting up voice interaction for Android Auto")
        voiceEngine?.setTranscriptionCallback(object : VoiceEngine.TranscriptionCallback {
            override fun onTranscription(transcription: String) {
                transcriptionText = transcription
                invalidate()
            }
            override fun onEngineReset(reason: String) {
                transcriptionText = "Engine reset: $reason"
                invalidate()
            }
        })
    }
    
    private fun startRecording() {
        Log.d(TAG, "Starting recording for assistant")
        if (voiceEngine?.startStreamingToAssistant() == true) {
            isRecording = true
            invalidate()
        } else {
            Log.e(TAG, "Failed to start streaming to assistant")
            transcriptionText = "Error: Failed to start streaming to assistant"
            invalidate()
        }
    }
    
    private fun stopRecording() {
        Log.d(TAG, "Stopping recording for assistant")
        transcriptionText = voiceEngine?.stopListening() ?: "Stopped recording"
        isRecording = false
        invalidate()
    }
    
    // Future integration point for voice command processing with VoiceEngine.kt
    fun processVoiceCommand(command: String) {
        Log.d(TAG, "Processing voice command: $command")
        // This can be expanded for additional voice command processing logic
        transcriptionText = "Processed command: $command"
        invalidate()
    }
}
