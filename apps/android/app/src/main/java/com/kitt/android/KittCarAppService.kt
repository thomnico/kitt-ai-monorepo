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
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.core.graphics.drawable.IconCompat
import androidx.core.content.ContextCompat
import com.kitt.android.R
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.kitt.android.voice.VoiceEngine
import android.content.Context
import android.os.Handler
import android.os.Looper

class KittCarAppService : CarAppService() {
    private val TAG = "KittCarAppService"
    private var voiceEngine: VoiceEngine? = null
    private var audioPlaybackService: AudioPlaybackService? = null
    private var isAudioPlaybackServiceBound = false

    private val audioPlaybackServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlaybackService.LocalBinder
            audioPlaybackService = binder.getService()
            isAudioPlaybackServiceBound = true
            Log.d(TAG, "AudioPlaybackService bound successfully")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioPlaybackService = null
            isAudioPlaybackServiceBound = false
            Log.d(TAG, "AudioPlaybackService disconnected")
        }
    }

    override fun onCreateSession(): Session {
        Log.d(TAG, "Creating new session for Android Auto")
        voiceEngine = VoiceEngine(this)
        voiceEngine?.initVoiceEngine()

        // Bind to AudioPlaybackService
        val intent = Intent(this, AudioPlaybackService::class.java)
        bindService(intent, audioPlaybackServiceConnection, Context.BIND_AUTO_CREATE)

        return KittSession(voiceEngine, audioPlaybackService)
    }

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onDestroy() {
        voiceEngine = null
        if (isAudioPlaybackServiceBound) {
            unbindService(audioPlaybackServiceConnection)
            isAudioPlaybackServiceBound = false
        }
        super.onDestroy()
        Log.d(TAG, "KittCarAppService destroyed")
    }
}

class KittSession(private val voiceEngine: VoiceEngine?, private val audioPlaybackService: AudioPlaybackService?) : Session() {
    private val TAG = "KittSession"
    
    override fun onCreateScreen(intent: Intent): Screen {
        Log.d(TAG, "Creating main screen for Android Auto with intent: $intent")
        // Pass both voiceEngine and audioPlaybackService to the main screen
        return KittMainScreen(carContext, voiceEngine, audioPlaybackService)
    }
}

class KittMainScreen(carContext: androidx.car.app.CarContext, private val voiceEngine: VoiceEngine?, private val audioPlaybackService: AudioPlaybackService?) : Screen(carContext) {
    private val TAG = "KittMainScreen"
    private var isVoiceActive = false
    private var currentMode = "STANDBY"
    private var currentTab = "communication" // This will be simplified or removed
    private var isAssistantRecording = false // Renamed from isRecording for clarity
    private var isAudioRecording = false // New state for file recording
    private var transcriptionText = ""

    init {
        Log.d(TAG, "KittMainScreen init: Initializing screen.")
        setupVoiceInteraction()
    }
    
    override fun onGetTemplate(): Template {
        Log.d(TAG, "onGetTemplate: Building KITT communication dashboard template for Android Auto.")
        
        // Create rows for the list instead of actions for a pane
        val assistantTalkRow = Row.Builder()
            .setTitle("🗣️ Assistant Talk")
            .setOnClickListener {
                if (isAssistantRecording) {
                    stopAssistantRecording()
                } else {
                    startAssistantRecording()
                }
            }
            .build()

        val recordAudioRow = Row.Builder()
            .setTitle("🎙️ Record Audio")
            .setOnClickListener {
                if (isAudioRecording) {
                    stopAudioRecording()
                } else {
                    startAudioRecording()
                }
            }
            .build()

        val recordingsNavigationRow = Row.Builder()
            .setTitle("🎧 My Recordings")
            .setOnClickListener {
                screenManager.push(RecordingsScreen(carContext, audioPlaybackService, voiceEngine))
            }
            .build()

        val openWebDashboardRow = Row.Builder()
            .setTitle("🌐 Open Web Dashboard")
            .setOnClickListener {
                val intent = Intent(carContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required for starting activity from non-activity context
                carContext.startActivity(intent)
            }
            .build()
        
        val itemList = ItemList.Builder()
            .addItem(assistantTalkRow)
            .addItem(recordAudioRow)
            .addItem(recordingsNavigationRow)
            .addItem(openWebDashboardRow) // Add the new row
            .build()

        return ListTemplate.Builder()
            .setSingleList(itemList)
            .setTitle("KITT DHU")
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
    
    private fun setupVoiceInteraction() {
        Log.d(TAG, "Setting up voice interaction for Android Auto")
        voiceEngine?.setTranscriptionCallback(object : VoiceEngine.TranscriptionCallback {
            override fun onTranscription(transcription: String) {
                transcriptionText = transcription
                // Only invalidate if the transcription is for the assistant interaction
                // Or if we want to show it on the main screen
                invalidate()
            }
            override fun onEngineReset(reason: String) {
                transcriptionText = "Engine reset: $reason"
                invalidate()
            }
        })
    }
    
    private fun startAssistantRecording() {
        Log.d(TAG, "startAssistantRecording: Starting streaming to assistant.")
        if (voiceEngine?.startStreamingToAssistant() == true) {
            isAssistantRecording = true
            invalidate()
            Log.d(TAG, "startAssistantRecording: Assistant recording started. Invalidating.")
        } else {
            Log.e(TAG, "startAssistantRecording: Failed to start streaming to assistant.")
            transcriptionText = "Error: Failed to start streaming to assistant"
            invalidate()
        }
    }
    
    private fun stopAssistantRecording() {
        Log.d(TAG, "stopAssistantRecording: Stopping streaming to assistant.")
        voiceEngine?.stopListening() // This also stops streaming to assistant
        isAssistantRecording = false
        invalidate()
        Log.d(TAG, "stopAssistantRecording: Assistant recording stopped. Invalidating.")
    }

    private fun startAudioRecording() {
        Log.d(TAG, "startAudioRecording: Starting audio recording to file.")
        val filePath = voiceEngine?.startRecording() // VoiceEngine has startRecording()
        if (filePath != null) {
            isAudioRecording = true
            transcriptionText = "Recording to: ${filePath.substringAfterLast('/')}"
            invalidate()
            Log.d(TAG, "startAudioRecording: Audio recording started. Invalidating.")
        } else {
            Log.e(TAG, "startAudioRecording: Failed to start audio recording.")
            transcriptionText = "Error: Failed to start audio recording"
            invalidate()
        }
    }

    private fun stopAudioRecording() {
        Log.d(TAG, "stopAudioRecording: Stopping audio recording to file.")
        val filePath = voiceEngine?.stopRecording() // VoiceEngine has stopRecording()
        if (filePath != null) {
            isAudioRecording = false
            transcriptionText = "Recording saved: ${filePath.substringAfterLast('/')}"
            invalidate()
        } else {
            Log.e(TAG, "stopAudioRecording: Failed to stop audio recording.")
            transcriptionText = "Error: Failed to stop audio recording"
            invalidate()
        }
    }
}
