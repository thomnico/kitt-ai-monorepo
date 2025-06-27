package com.kitt.android

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class KittCarAppService : CarAppService() {
    private val TAG = "KittCarAppService"
    
    override fun onCreateSession(): Session {
        android.util.Log.d(TAG, "Creating new Android Auto session")
        return KittSession()
    }
    
    // Ensure the service starts with minimal latency for driver interaction
    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d(TAG, "Starting KittCarAppService for Android Auto")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun createHostValidator(): HostValidator {
        android.util.Log.d(TAG, "Creating host validator for Android Auto")
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d(TAG, "KittCarAppService created for Android Auto")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "KittCarAppService destroyed")
    }
}

class KittSession : Session() {
    private val TAG = "KittSession"
    
    override fun onCreateScreen(intent: android.content.Intent): androidx.car.app.Screen {
        android.util.Log.d(TAG, "Creating main screen for Android Auto with intent: $intent")
        return KittMainScreen(carContext)
    }
}

class KittMainScreen(carContext: androidx.car.app.CarContext) : androidx.car.app.Screen(carContext) {
    private val TAG = "KittMainScreen"
    private var isVoiceActive = false
    private var currentMode = "STANDBY"
    
    init {
        android.util.Log.d(TAG, "Initializing KITT main screen for Android Auto")
        setupVoiceInteraction()
    }
    
    override fun onGetTemplate(): androidx.car.app.model.Template {
        android.util.Log.d(TAG, "Building KITT dashboard template for Android Auto")
        
        // Create KITT-style status rows without debugging text
        val statusRow = androidx.car.app.model.Row.Builder()
            .setTitle("🔴 KITT AI SYSTEMS")
            .addText("Status: $currentMode")
            .build()
            
        val voiceRow = androidx.car.app.model.Row.Builder()
            .setTitle("🎤 VOICE INTERFACE")
            .addText(if (isVoiceActive) "LISTENING..." else "Say 'Hey Assistant' to activate")
            .build()
            
        val scannerRow = androidx.car.app.model.Row.Builder()
            .setTitle("📡 SCANNER ARRAY")
            .addText("Frequency monitoring active")
            .build()
            
        val spectrumRow = androidx.car.app.model.Row.Builder()
            .setTitle("📊 SPECTRUM ANALYZER")
            .addText("Audio analysis running")
            .build()

        // Create action buttons for KITT functions
        val langAction = androidx.car.app.model.Action.Builder()
            .setTitle("LANG")
            .setOnClickListener { toggleLanguage() }
            .build()
            
        val voskAction = androidx.car.app.model.Action.Builder()
            .setTitle("VOSK")
            .setOnClickListener { checkVoiceModel() }
            .build()

        val paneBuilder = androidx.car.app.model.Pane.Builder()
            .addRow(statusRow)
            .addRow(voiceRow)
            .addRow(scannerRow)
            .addRow(spectrumRow)
            .addAction(langAction)
            .addAction(voskAction)
            .setLoading(false)

        return androidx.car.app.model.PaneTemplate.Builder(paneBuilder.build())
            .setHeaderAction(androidx.car.app.model.Action.APP_ICON)
            .setTitle("KITT AI Dashboard")
            .build()
    }
    
    private fun toggleLanguage() {
        android.util.Log.d(TAG, "Language toggle requested")
        // Toggle between ENG/FR
        currentMode = if (currentMode == "ENG") "FR" else "ENG"
        invalidate() // Refresh the template
    }
    
    private fun checkVoiceModel() {
        android.util.Log.d(TAG, "Voice model check requested")
        currentMode = "VOICE_CHECK"
        invalidate() // Refresh the template
    }
    
    private fun setupVoiceInteraction() {
        android.util.Log.d(TAG, "Setting up voice interaction for Android Auto")
        // Placeholder for voice engine initialization
        // This will integrate with VoiceEngine.kt for wake word detection and command processing
        // Ensure voice-first interaction as per Android Auto guidelines for driver safety
    }
    
    // Future integration point for voice command processing with VoiceEngine.kt
    fun processVoiceCommand(command: String) {
        android.util.Log.d(TAG, "Processing voice command: $command")
        // Placeholder for voice command processing logic
        // This will be integrated with VoiceEngine.kt for full voice-first interaction
    }
}
