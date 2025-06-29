package com.kitt.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Color
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.ComponentActivity
// import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kitt.android.KittButton
import com.kitt.android.KittSpectrumView
import com.kitt.android.voice.VoiceEngine
import java.util.Locale
import android.content.Intent // Import for Intent

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView // Declare WebView
    private lateinit var kittSpectrumView: KittSpectrumView
    private lateinit var transcriptionTextView: TextView
    private lateinit var sttStatusTextView: TextView
    private lateinit var detectedTextRecyclerView: RecyclerView
    private lateinit var detectedTextAdapter: DetectedTextAdapter
    private val detectedTextList = mutableListOf<String>()
    private var isListening = false
    private lateinit var voiceEngine: VoiceEngine
    private var currentLanguage = "en-US"
    private var interruptionStatus = "Unknown"
    private var isVoiceRecorderActive = false
    private var isAiTalkActive = false
    private val TAG = "MainActivity"
    private lateinit var bluetoothAudioService: BluetoothAudioService
    
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

        // Start the WebServerService
        startService(Intent(this, WebServerService::class.java))

        // Set content view to a WebView
        webView = WebView(this)
        setContentView(webView)

        // Configure WebView settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // Enable DOM storage for web apps
        webView.webViewClient = WebViewClient() // Open links in the same WebView

        // Load the local web server URL
        webView.loadUrl("http://localhost:8080/index.html")

        // The following code is for the original KITT app functionality.
        // It will be commented out or integrated as needed for the web-based dashboard.
        /*
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        kittSpectrumView = findViewById(R.id.kittSpectrumView)
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        sttStatusTextView = findViewById(R.id.sttStatusTextView)
        detectedTextRecyclerView = findViewById(R.id.detectedTextRecyclerView)
        
        // Setup RecyclerView for detected text
        detectedTextAdapter = DetectedTextAdapter(detectedTextList)
        detectedTextRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this).apply {
            reverseLayout = false
            stackFromEnd = false
        }
        detectedTextRecyclerView.adapter = detectedTextAdapter

        // Use a singleton or persistent instance for VoiceEngine to avoid re-initialization
        if (!::voiceEngine.isInitialized) {
            voiceEngine = VoiceEngine(this)
            voiceEngine.initVoiceEngine()
        }
        voiceEngine.setTranscriptionCallback(object : VoiceEngine.TranscriptionCallback {
            override fun onTranscription(transcription: String) {
                runOnUiThread {
                    transcriptionTextView.text = getString(R.string.transcription_format, transcription)
                    // This callback might not be used if Vosk output is JSON, handled in startListening
                }
            }
            override fun onEngineReset(reason: String) {
                runOnUiThread {
                    val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                    val resetMessage = "[$timestamp] RESET voice engine: $reason"
                    detectedTextList.add(resetMessage)
                    detectedTextAdapter.notifyItemInserted(detectedTextList.size - 1)
                    detectedTextRecyclerView.scrollToPosition(detectedTextList.size - 1)
                    transcriptionTextView.text = resetMessage
                }
            }
        })

        // Check permissions and start listening automatically when the app launches
        checkPermissionsAndStartListening()

        updateSttStatus()

        // Initialize Bluetooth Audio Service
        bluetoothAudioService = BluetoothAudioService(this)
        if (bluetoothAudioService.isBluetoothAvailable()) {
            Log.d(TAG, "Bluetooth is available, routing audio")
            bluetoothAudioService.routeAudioToBluetooth()
        } else {
            Log.w(TAG, "Bluetooth is not available or not enabled")
        }
        
        // Setup switch listeners for talk mode and language selection
        setupSwitchListeners()
        */
    }

    private fun setupSwitchListeners() {
        val buttonLanguage: KittButton = findViewById(R.id.buttonLang) // Language switch button
        val buttonModel: KittButton = findViewById(R.id.buttonVosk) // Model switch button
        val buttonP1: KittButton = findViewById(R.id.buttonP1)
        val buttonP2: KittButton = findViewById(R.id.buttonP2)
        val buttonS1: KittButton = findViewById(R.id.buttonS1)
        val buttonS2: KittButton = findViewById(R.id.buttonS2)
        val buttonStorage: KittButton = findViewById(R.id.buttonP3)
        val buttonVMonitor: KittButton = findViewById(R.id.buttonP4)
        val buttonAiTalk: KittButton = findViewById(R.id.buttonAiTalk)
        val buttonVoiceRecorder: KittButton = findViewById(R.id.buttonVoiceRecorder)

        Log.i(TAG, "Setting up button listeners - Language button found: ${buttonLanguage != null}")
        
        // Post a runnable to get button position after layout is complete
        buttonLanguage.post {
            Log.i(TAG, "Button dimensions - Language: ${buttonLanguage.width}x${buttonLanguage.height}")
            Log.i(TAG, "Button position - Language: (${buttonLanguage.x}, ${buttonLanguage.y})")
            Log.i(TAG, "Button clickable: ${buttonLanguage.isClickable}")
        }
        
        // Initialize button states based on current settings
        updateLanguageButton(buttonLanguage)
        updateModelButton(buttonModel)
        
        // Set other buttons to default states
        buttonP1.setLighted(false)
        buttonP2.setLighted(false)
        buttonS1.setLighted(false)
        buttonS2.setLighted(false)
        buttonStorage.setLighted(false)
        buttonVMonitor.setLighted(false)
        
        // Set AI Talk mode ON by default
        buttonAiTalk.setLighted(true)
        buttonVoiceRecorder.setLighted(false)
        isAiTalkActive = true
        isVoiceRecorderActive = false

        // Language switch functionality
        buttonLanguage.setOnClickListener { 
            Log.i(TAG, "Language button clicked!")
            Log.i(TAG, "Current language before switch: $currentLanguage")
            switchLanguage()
            updateLanguageButton(buttonLanguage)
            Log.i(TAG, "Language switch completed, new language: $currentLanguage")
        }
        
        // Model switch functionality
        buttonModel.setOnClickListener { 
            switchModel()
            updateModelButton(buttonModel)
        }
        
        // Other buttons toggle their lighted state
        buttonP1.setOnClickListener { buttonP1.setLighted(!buttonP1.isLighted()) }
        buttonP2.setOnClickListener { buttonP2.setLighted(!buttonP2.isLighted()) }
        buttonS1.setOnClickListener { buttonS1.setLighted(!buttonS1.isLighted()) }
        buttonS2.setOnClickListener { buttonS2.setLighted(!buttonS2.isLighted()) }
        buttonStorage.setOnClickListener { 
            toggleStorageLocation(buttonStorage)
        }
        buttonVMonitor.setOnClickListener { 
            buttonVMonitor.setLighted(!buttonVMonitor.isLighted())
            toggleDiagnosticOverlay(buttonVMonitor.isLighted())
        }
        
        // Setup AI Talk and Voice Recorder with mutual exclusion
        setupAiTalkVoiceRecorderButtons(buttonAiTalk, buttonVoiceRecorder)
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
                        Log.i(TAG, "Partial transcription received")
                        runOnUiThread {
                            // Check if the result contains "partial" or "text" in JSON
                            if (partialResult.contains("\"partial\"")) {
                                // Extract the value of "partial" field from JSON
                                val partialMatch = Regex("\"partial\"\\s*:\\s*\"([^\"]+)\"").find(partialResult)
                                val partialValue = partialMatch?.groupValues?.get(1) ?: ""
                                if (partialValue.isNotEmpty()) {
                                    transcriptionTextView.text = partialValue
                                }
                            } else if (partialResult.contains("\"text\"")) {
                                // Extract the value of "text" field from JSON
                                val textMatch = Regex("\"text\"\\s*:\\s*\"([^\"]+)\"").find(partialResult)
                                val textValue = textMatch?.groupValues?.get(1) ?: ""
                                if (textValue.isNotEmpty()) {
                                    val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                                    val formattedText = "[$timestamp] $textValue"
                                    detectedTextList.add(formattedText)
                                    detectedTextAdapter.notifyItemInserted(detectedTextList.size - 1)
                                    detectedTextRecyclerView.scrollToPosition(detectedTextList.size - 1)
                                    transcriptionTextView.text = textValue
                                }
                            } else if (partialResult.contains("Warning") || partialResult.contains("Error")) {
                                // Display warning or error messages related to Vosk engine status
                                transcriptionTextView.text = partialResult
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
                kittSpectrumView.visibility = View.VISIBLE
                kittSpectrumView.startVisualization()
            } else {
                kittSpectrumView.stopVisualization()
                kittSpectrumView.visibility = View.VISIBLE // Keep visible but stop visualization
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling spectrum visualization: ${e.message}")
        }
    }

    private fun updateSttStatus() {
        try {
            val sttStatus = if (isListening) "🟢 LISTENING" else "🔴 STOPPED"
            val languageText = if (currentLanguage == "fr-FR") "🇫🇷 FRENCH" else "🇺🇸 ENGLISH"
            val engineText = try {
                "🤖 ${voiceEngine.getCurrentEngine()}"
            } catch (e: Exception) {
                Log.w(TAG, "Could not get engine name: ${e.message}")
                "🤖 Unknown"
            }
            
            // Safely fetch GPU model
            val gpuStatus = try {
                "⚡ ${getGpuModel()}"
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch GPU model: ${e.message}")
                "⚡ GPU: Unknown"
            }
            
            // Safely fetch RAM size
            val ramStatus = try {
                val memoryInfo = android.app.ActivityManager.MemoryInfo()
                val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
                activityManager?.getMemoryInfo(memoryInfo)
                val totalRamMb = memoryInfo.totalMem / (1024 * 1024) // Convert bytes to MB
                "💾 RAM: ${totalRamMb / 1024}GB"
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch RAM size: ${e.message}")
                "💾 RAM: Unknown"
            }
            
            val statusText = """
                $engineText | $languageText | $sttStatus
                $gpuStatus | $ramStatus | 🎯 KITT v2.0
            """.trimIndent()
            
            runOnUiThread {
                sttStatusTextView.text = statusText
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating STT status: ${e.message}")
            runOnUiThread {
                sttStatusTextView.text = "Status: Error updating"
            }
        }
    }
    
    private fun getGpuModel(): String {
        // Safe GPU detection without requiring OpenGL context
        return try {
            // Use Build properties to get GPU info safely
            val manufacturer = android.os.Build.MANUFACTURER
            val model = android.os.Build.MODEL
            
            // For Crosscall Core-Z5, we know it has Adreno 643
            when {
                model.contains("Core-Z5", ignoreCase = true) -> "Adreno 643 (Crosscall Core-Z5)"
                manufacturer.equals("qualcomm", ignoreCase = true) -> "Adreno GPU (Qualcomm)"
                else -> "GPU: $manufacturer $model"
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not determine GPU model: ${e.message}")
            "GPU: Unknown"
        }
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
                Log.w(TAG, "Audio permission not granted, requesting permission")
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

    @SuppressLint("MissingPermission")
    private fun switchLanguage() {
        val previousLanguage = currentLanguage
        currentLanguage = if (currentLanguage == "en-US") "fr-FR" else "en-US"
        Log.i(TAG, "Language switching from $previousLanguage to: $currentLanguage")
        
        try {
            // Stop listening if currently active and wait for voice processing to complete
            val wasListening = isListening
            if (wasListening) {
                isListening = false // Stop the processing loop first
                Thread.sleep(200) // Give time for processing loop to exit
                stopListening()
                Log.i(TAG, "Stopped listening for language switch")
            }
            
            // Set the language in the voice engine (this is now synchronized)
            voiceEngine.setLanguage(currentLanguage)
            Log.i(TAG, "Voice engine language updated to: $currentLanguage")
            
            // Update UI to reflect language change
            updateSttStatus()
            val languageText = if (currentLanguage == "fr-FR") "Langue changée en Français" else "Language changed to English"
            transcriptionTextView.text = languageText
            
            // Restart listening if it was active before
            if (wasListening) {
                // Longer delay to ensure voice engine is fully ready
                Thread {
                    Thread.sleep(1000)
                    runOnUiThread {
                        try {
                            startListening()
                            Log.i(TAG, "Resumed listening after language switch")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to resume listening after language switch: ${e.message}")
                            runOnUiThread {
                                transcriptionTextView.text = "Error resuming listening: ${e.message}"
                            }
                        }
                    }
                }.start()
            }
            
            Log.i(TAG, "Language switch completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during language switch: ${e.message}", e)
            // Rollback language change on error
            currentLanguage = previousLanguage
            runOnUiThread {
                transcriptionTextView.text = "Error during language switch: ${e.message}"
                updateSttStatus()
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun switchModel() {
        // Toggle between VOSK and Android local STT
        val currentEngine = voiceEngine.getCurrentEngine()
        val newEngine = if (currentEngine == "VOSK") "ANDROID" else "VOSK"
        
        Log.i(TAG, "Model switched to: $newEngine")
        
        // Restart voice engine with new model
        if (isListening) {
            voiceEngine.stopListening()
        }
        voiceEngine.switchEngine(newEngine)
        if (isListening) {
            voiceEngine.startListening()
        }
        updateSttStatus()
    }
    
    private fun updateLanguageButton(button: KittButton) {
        val languageText = if (currentLanguage == "en-US") "EN" else "FR"
        button.setText(languageText)
        button.setLighted(true) // Always lighted to show current state
    }
    
    private fun updateModelButton(button: KittButton) {
        val modelText = voiceEngine.getCurrentEngine()
        button.setText(modelText)
        button.setLighted(true) // Always lighted to show current state
    }

    private fun toggleDiagnosticOverlay(show: Boolean) {
        // This method will be expanded to show/hide diagnostic overlay for voice monitoring
        Log.i(TAG, "Diagnostic overlay toggled: $show")
        if (show) {
            transcriptionTextView.text = "Voice Monitoring: ON\nRMS Levels and Processing Times in Logcat"
        } else {
            transcriptionTextView.text = "Voice Monitoring: OFF"
        }
    }

    private fun toggleStorageLocation(button: KittButton) {
        val useExternal = !button.isLighted()
        val success = voiceEngine.toggleStorageLocation(useExternal)
        if (success) {
            button.setLighted(useExternal)
            val location = if (useExternal) "gdrive" else "local"
            val currentStoragePath = voiceEngine.getCurrentStoragePath()
            transcriptionTextView.text = "Storage Location: $location\nPath: $currentStoragePath"
            Log.i(TAG, "Storage location set to $location at path: $currentStoragePath")
        } else {
            button.setLighted(false)
            transcriptionTextView.text = "Failed to switch to gdrive Storage: Not Available"
            Log.w(TAG, "Failed to toggle storage location: gdrive storage not available")
        }
    }

    /**
     * Setup AI Talk and Voice Recorder buttons with mutual exclusion logic
     */
    private fun setupAiTalkVoiceRecorderButtons(buttonAiTalk: KittButton, buttonVoiceRecorder: KittButton) {
        Log.i(TAG, "Setting up AI Talk and Voice Recorder buttons with mutual exclusion")
        
        // AI Talk button click listener
        buttonAiTalk.setOnClickListener { 
            Log.i(TAG, "AI Talk button clicked")
            if (!isAiTalkActive) {
                // Activate AI Talk, deactivate Voice Recorder
                activateAiTalk(buttonAiTalk, buttonVoiceRecorder)
            } else {
                // Deactivate AI Talk
                deactivateAiTalk(buttonAiTalk)
            }
        }
        
        // Voice Recorder button click listener
        buttonVoiceRecorder.setOnClickListener { 
            Log.i(TAG, "Voice Recorder button clicked")
            if (!isVoiceRecorderActive) {
                // Activate Voice Recorder, deactivate AI Talk
                activateVoiceRecorder(buttonVoiceRecorder, buttonAiTalk)
            } else {
                // Deactivate Voice Recorder
                deactivateVoiceRecorder(buttonVoiceRecorder)
            }
        }
    }

    /**
     * Activate AI Talk mode (uses existing Vosk voice recognition)
     */
    private fun activateAiTalk(buttonAiTalk: KittButton, buttonVoiceRecorder: KittButton) {
        Log.i(TAG, "Activating AI Talk mode")
        
        // Deactivate Voice Recorder if active
        if (isVoiceRecorderActive) {
            deactivateVoiceRecorder(buttonVoiceRecorder)
        }
        
        // Set state
        isAiTalkActive = true
        isVoiceRecorderActive = false
        
        // Update button states
        buttonAiTalk.setLighted(true)
        buttonVoiceRecorder.setLighted(false)
        
        // Start voice recognition using existing system
        if (!isListening) {
            try {
                startListening()
                transcriptionTextView.text = "AI Talk: Listening for voice commands..."
                Log.i(TAG, "AI Talk: Started voice recognition")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AI Talk voice recognition: ${e.message}")
                transcriptionTextView.text = "AI Talk: Error starting voice recognition"
                isAiTalkActive = false
                buttonAiTalk.setLighted(false)
            }
        }
    }

    /**
     * Deactivate AI Talk mode
     */
    private fun deactivateAiTalk(buttonAiTalk: KittButton) {
        Log.i(TAG, "Deactivating AI Talk mode")
        
        isAiTalkActive = false
        buttonAiTalk.setLighted(false)
        
        // Stop voice recognition if no other modes are active
        if (isListening && !isVoiceRecorderActive) {
            stopListening()
            transcriptionTextView.text = "AI Talk: Stopped"
            Log.i(TAG, "AI Talk: Stopped voice recognition")
        }
    }

    /**
     * Activate Voice Recorder mode (records audio and listens for "hey kit")
     */
    private fun activateVoiceRecorder(buttonVoiceRecorder: KittButton, buttonAiTalk: KittButton) {
        Log.i(TAG, "Activating Voice Recorder mode")
        
        // Deactivate AI Talk if active
        if (isAiTalkActive) {
            deactivateAiTalk(buttonAiTalk)
        }
        
        // Set state
        isVoiceRecorderActive = true
        isAiTalkActive = false
        
        // Update button states
        buttonVoiceRecorder.setLighted(true)
        buttonAiTalk.setLighted(false)
        
        // Start voice recorder with wake word detection
        startVoiceRecorder()
    }

    /**
     * Deactivate Voice Recorder mode
     */
    private fun deactivateVoiceRecorder(buttonVoiceRecorder: KittButton) {
        Log.i(TAG, "Deactivating Voice Recorder mode")
        
        isVoiceRecorderActive = false
        buttonVoiceRecorder.setLighted(false)
        
        // Stop voice recorder
        stopVoiceRecorder()
    }

    /**
     * Start Voice Recorder with "hey kit" wake word detection
     */
    @SuppressLint("MissingPermission")
    private fun startVoiceRecorder() {
        Log.i(TAG, "Starting Voice Recorder with wake word detection")
        
        try {
            // Start actual audio recording
            val recordingFilePath = voiceEngine.startRecording()
            if (recordingFilePath != null) {
                val fileName = recordingFilePath.substringAfterLast('/')
                val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                val startMessage = "[$timestamp] 🎤 Recording Started: $fileName"
                detectedTextList.add(startMessage)
                detectedTextAdapter.notifyItemInserted(detectedTextList.size - 1)
                detectedTextRecyclerView.scrollToPosition(detectedTextList.size - 1)
                transcriptionTextView.text = "Voice Recorder: Recording to $fileName"
            } else {
                Log.w(TAG, "Recording failed to start")
                transcriptionTextView.text = "Voice Recorder: Failed to start recording"
            }
            
            // For now, use the existing voice engine for wake word detection
            if (!isListening) {
                voiceEngine.startListening()
                isListening = true
                toggleScannerAnimation(true)
            }
            
            // Start monitoring for "hey kit" wake phrase
            Thread {
                Log.i(TAG, "Voice Recorder: Monitoring for 'hey kit' wake phrase")
                while (isVoiceRecorderActive) {
                    try {
                        val partialResult = voiceEngine.processVoiceInput()
                        if (partialResult.isNotEmpty()) {
                            runOnUiThread {
                                // Check for "hey kit" wake phrase
                                if (partialResult.contains("\"text\"")) {
                                    val textMatch = Regex("\"text\"\\s*:\\s*\"([^\"]+)\"").find(partialResult)
                                    val textValue = textMatch?.groupValues?.get(1)?.lowercase() ?: ""
                                    
                                    if (textValue.contains("hey kit") || textValue.contains("hey kitt")) {
                                        Log.i(TAG, "Voice Recorder: 'Hey Kit' wake phrase detected!")
                                        onWakePhraseDetected()
                                    }
                                }
                                
                                // Update status if not overridden by wake phrase
                                if (transcriptionTextView.text.toString().contains("Recording Started") ||
                                    transcriptionTextView.text.toString().contains("Listening for 'Hey Kit'")) {
                                    transcriptionTextView.text = "Voice Recorder: Listening for 'Hey Kit'..."
                                }
                            }
                        }
                        Thread.sleep(100)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in voice recorder monitoring: ${e.message}")
                    }
                }
                Log.i(TAG, "Voice Recorder: Stopped monitoring")
            }.start()
            
            updateSttStatus()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start voice recorder: ${e.message}")
            transcriptionTextView.text = "Voice Recorder: Error starting recorder"
            isVoiceRecorderActive = false
        }
    }

    /**
     * Stop Voice Recorder
     */
    private fun stopVoiceRecorder() {
        Log.i(TAG, "Stopping Voice Recorder")
        
        // Stop recording
            val recordingFilePath = voiceEngine.stopRecording()
            if (recordingFilePath != null) {
                val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                val stopMessage = "[$timestamp] ⏹️ Recording Stopped\nPath: $recordingFilePath"
                detectedTextList.add(stopMessage)
                detectedTextAdapter.notifyItemInserted(detectedTextList.size - 1)
                detectedTextRecyclerView.scrollToPosition(detectedTextList.size - 1)
                transcriptionTextView.text = "Voice Recorder: Stopped\nSaved to: $recordingFilePath"
            } else {
                Log.w(TAG, "No recording was active to stop")
                transcriptionTextView.text = "Voice Recorder: Stopped (No recording active)"
            }
        
        // Stop listening if no other modes are active
        if (isListening && !isAiTalkActive) {
            voiceEngine.stopListening()
            isListening = false
            toggleScannerAnimation(false)
        }
        
        updateSttStatus()
    }

    /**
     * Handle wake phrase detection
     */
    private fun onWakePhraseDetected() {
        Log.i(TAG, "Wake phrase 'Hey Kit' detected!")
        
            // Stop recording if active
            if (voiceEngine.isRecordingActive()) {
                val recordingFilePath = voiceEngine.stopRecording()
                if (recordingFilePath != null) {
                    val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                    val stopMessage = "[$timestamp] ⏹️ Recording Stopped - Wake Phrase Detected\nPath: $recordingFilePath"
                    detectedTextList.add(stopMessage)
                    detectedTextAdapter.notifyItemInserted(detectedTextList.size - 1)
                    detectedTextRecyclerView.scrollToPosition(detectedTextList.size - 1)
                }
            }
        
        // Provide visual feedback
        runOnUiThread {
            transcriptionTextView.text = "Voice Recorder: 'Hey Kit' detected! 🎤"
            
            // Add detected wake phrase to the log
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
            val wakeMessage = "[$timestamp] 🔊 WAKE PHRASE: Hey Kit detected"
            detectedTextList.add(wakeMessage)
            detectedTextAdapter.notifyItemInserted(detectedTextList.size - 1)
            detectedTextRecyclerView.scrollToPosition(detectedTextList.size - 1)
            
            // Flash the spectrum view to indicate detection
            kittSpectrumView.startVisualization()
            
            // Auto-return to listening after a brief moment
            Thread {
                Thread.sleep(2000)
                runOnUiThread {
                    if (isVoiceRecorderActive) {
                        transcriptionTextView.text = "Voice Recorder: Listening for 'Hey Kit'..."
                    }
                }
            }.start()
        }
    }

    override fun onDestroy() {
        // No release method in VoiceEngine, just stop listening if active
        if (isListening) {
            voiceEngine.stopListening()
        }
        // Cleanup Bluetooth service
        bluetoothAudioService.cleanup()
        // Stop the WebServerService
        stopService(Intent(this, WebServerService::class.java))
        super.onDestroy()
    }
}

// Adapter for RecyclerView to display detected text
class DetectedTextAdapter(private val textList: List<String>) : RecyclerView.Adapter<DetectedTextAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = textList[position]
        holder.textView.setTextColor(Color.WHITE)
    }

    override fun getItemCount(): Int {
        return textList.size
    }
}
