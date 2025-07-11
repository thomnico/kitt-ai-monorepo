package com.kitt.android.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.util.Log
import com.kitt.android.OfflineAssistantService
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.IOException
import android.content.res.AssetManager
import androidx.annotation.RequiresPermission
import java.io.InputStream
import java.io.OutputStream
import java.io.FileOutputStream
import android.os.Bundle
import android.os.IBinder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Performance target: Ensure end-to-end latency < 200ms as per monorepo rules
private const val MAX_LATENCY_MS = 200L
private const val TAG = "VoiceEngine"
private const val SAMPLE_RATE = 16000
private const val BUFFER_SIZE = 4096

/**
 * Voice Engine for KITT Framework on Android
 * This class serves as the entry point for voice interaction capabilities,
 * integrating Vosk for voice processing on Android.
 *
 * License: Apache 2.0 for Vosk model usage. Attribution provided in documentation.
 */
class VoiceEngine(private val context: Context) {

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var recorder: AudioRecord? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFilePath: String? = null
    private val modelPath = "${context.filesDir.absolutePath}/models/vosk"
    private val internalRecordingsPath = "${context.filesDir.absolutePath}/recordings"
    private var externalRecordingsPath: String? = null
    private var useExternalStorage = false
    private val recordingsPath: String
        get() = if (useExternalStorage && externalRecordingsPath != null) externalRecordingsPath!! else internalRecordingsPath
    private var isListening = false
    private var isRecording = false
    private var currentModelKey = "en-us"
    private var currentLanguage = "en-US"
    private var useNativeAndroid = false
    private var transcriptionCallback: TranscriptionCallback? = null
    private var listeningStartTime: Long = 0
    private var lastResultTime: Long = 0
    private var lastRmsZeroTime: Long = 0
    private val RESET_THRESHOLD_MS = 60000 // 1 minute
    private val FREEZE_TIMEOUT_MS = 10000 // 10 seconds to detect freeze
    private val RMS_ZERO_TIMEOUT_MS = 10000 // 10 seconds to detect RMS at 0
    private var assistantService: OfflineAssistantService? = null
    private var isStreamingToAssistant = false

    /**
     * Interface for transcription callbacks.
     */
    interface TranscriptionCallback {
        fun onTranscription(transcription: String)
        fun onEngineReset(reason: String)
    }

    /**
     * Set the callback for transcription results and engine events.
     * @param callback The callback to be invoked with transcription results and engine events.
     */
    fun setTranscriptionCallback(callback: TranscriptionCallback) {
        this.transcriptionCallback = callback
    }

    /**
     * Initialize the voice engine with platform-specific configurations.
     * @return Boolean indicating if initialization was successful.
     */
    fun initVoiceEngine(): Boolean {
        val startTime = System.currentTimeMillis()
        // Bind to OfflineAssistantService
        bindToAssistantService()

        // Ensure internal recordings directory exists
        val internalRecordingsDir = File(internalRecordingsPath)
        if (!internalRecordingsDir.exists()) {
            internalRecordingsDir.mkdirs()
            Log.i(TAG, "Created internal recordings directory: $internalRecordingsPath")
        }

        // Attempt to set up external recordings directory
        setupExternalStorage()

        if (useNativeAndroid) {
            // Initialize native Android speech recognizer
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.i(TAG, "Native Android recognizer ready for speech")
                    }

                    override fun onBeginningOfSpeech() {
                        Log.i(TAG, "Speech input started")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // RMS change can be used for visualization if needed
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Not typically used for speech recognition
                    }

                    override fun onEndOfSpeech() {
                        Log.i(TAG, "Speech input ended")
                    }

                    override fun onError(error: Int) {
                        Log.e(TAG, "Speech recognition error: $error")
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (matches != null && matches.isNotEmpty()) {
                            val transcription = matches[0]
                            Log.i(TAG, "Speech recognition result: $transcription")
                            transcriptionCallback?.onTranscription(transcription)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (partialMatches != null && partialMatches.isNotEmpty()) {
                            val partialTranscription = partialMatches[0]
                            Log.i(TAG, "Partial speech recognition result: $partialTranscription")
                            transcriptionCallback?.onTranscription(partialTranscription)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {
                        // Handle any additional events if necessary
                    }
                })
                val initTime = System.currentTimeMillis() - startTime
                Log.i(TAG, "Native Android voice engine initialized in ${initTime}ms")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize native Android voice engine: ${e.message}")
                return false
            }
        } else {
            // Ensure model directory exists for Vosk
            val modelDir = File(modelPath)
            if (!modelDir.exists()) {
                Log.i(TAG, "Model directory does not exist: $modelPath, attempting to create and copy from assets")
                try {
                    modelDir.mkdirs()
                    // Copy the model from APK assets for offline use
                    copyVoskModelFromAssets()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create model directory or copy model from assets: ${e.message}")
                }
            } else {
                Log.i(TAG, "Model directory exists: $modelPath")
                // Check if the directory contains any files
                val modelFiles = modelDir.listFiles()
                if (modelFiles == null || modelFiles.isEmpty()) {
                    Log.w(TAG, "Model directory is empty: $modelPath, attempting to copy from assets")
                    try {
                        copyVoskModelFromAssets()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to copy model from assets: ${e.message}")
                    }
                } else {
                    Log.i(TAG, "Model directory contains files: $modelPath")
                    // Only extract if necessary (e.g., check for specific model folders)
                    val expectedModelFolderEn = File("$modelPath/vosk-model-small-en-us-0.15")
                    val expectedModelFolderFr = File("$modelPath/vosk-model-small-fr-0.22")
                    if (!expectedModelFolderEn.exists() || !expectedModelFolderFr.exists()) {
                        Log.i(TAG, "Not all expected model folders found, extracting models from assets")
                        try {
                            copyVoskModelFromAssets()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to copy or extract model from assets: ${e.message}")
                        }
                    } else {
                        Log.i(TAG, "Expected model folders found, skipping extraction")
                    }
                }
            }

            // Check again if model files are present before initialization
            val finalModelFiles = modelDir.listFiles()
            if (finalModelFiles == null || finalModelFiles.isEmpty()) {
                Log.e(TAG, "Model directory is still empty after copy attempt: $modelPath. Cannot initialize voice engine.")
                return false
            }

            // Check if the files are valid model files (not just zip files or placeholders)
            var hasValidModel = false
            for (file in finalModelFiles) {
                if (!file.name.endsWith(".zip") && file.name != "placeholder_model.txt") {
                    hasValidModel = true
                    break
                }
            }
            if (!hasValidModel) {
                Log.w(TAG, "No direct valid Vosk model files found in $modelPath. Only zip files or placeholder files are present. Attempting initialization anyway as zip files may have been extracted.")
            }

            // Initialize Vosk with offline-first approach
            try {
                // Load the Vosk model based on configuration
                val configJson = readModelConfig()
                val models = configJson.getJSONObject("models")
                val modelConfig = models.optJSONObject(currentModelKey)
                if (modelConfig != null) {
                    val modelFileName = modelConfig.optString("path", if (currentModelKey == "en-us") "vosk-model-small-en-us-0.15.zip" else "vosk-model-small-fr-0.22.zip")
                    val extractedModelPath = "$modelPath/${modelFileName.removeSuffix(".zip")}"
                    Log.i(TAG, "Loading model: $currentModelKey from $extractedModelPath")
                    model = Model(extractedModelPath)
                    recognizer = Recognizer(model, SAMPLE_RATE.toFloat())
                    val initTime = System.currentTimeMillis() - startTime
                    if (initTime > MAX_LATENCY_MS) {
                        Log.w(TAG, "Voice engine initialization exceeded latency target: ${initTime}ms")
                    }
                    Log.i(TAG, "Voice engine initialized successfully with Vosk model $currentModelKey in ${initTime}ms")
                    return true
                } else {
                    Log.e(TAG, "Model $currentModelKey not found in configuration")
                    return false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize voice engine with Vosk: ${e.message}")
                return false
            }
        }
    }

    /**
     * Get the current status of the voice engine.
     * @return String indicating the status of the voice engine.
     */
    fun getEngineStatus(): String {
        Log.i(TAG, "Getting voice engine status")
        return if (useNativeAndroid) {
            if (speechRecognizer != null) "Initialized with Native Android and ready" else "Not initialized"
        } else {
            if (model != null) "Initialized with Vosk and ready" else "Not initialized"
        }
    }

    /**
     * Copy the Vosk model from APK assets to internal storage for offline use.
     * Handles both direct model files and zipped model files based on configuration.
     */
    private fun copyVoskModelFromAssets() {
        Log.i(TAG, "Copying Vosk model from assets to $modelPath")
        val assetManager: AssetManager = context.assets
        val modelAssetPath = "models/vosk"
        try {
            val configJson = readModelConfig()
            val models = configJson.optJSONObject("models") ?: org.json.JSONObject()
            val modelKeys = models.keys()
            val processedFiles = mutableSetOf<String>()
            while (modelKeys.hasNext()) {
                val key = modelKeys.next()
                val modelInfo = models.getJSONObject(key)
                val modelFileName = modelInfo.optString("path", "")
                if (modelFileName.isNotEmpty() && !processedFiles.contains(modelFileName)) {
                    val assetPath = "$modelAssetPath/$modelFileName"
                    if (modelFileName.endsWith(".zip")) {
                        Log.i(TAG, "Extracting zip file $assetPath to $modelPath")
                        extractZipFromAssets(assetManager, assetPath, modelPath)
                    } else {
                        val outPath = "$modelPath/$modelFileName"
                        copyAssetToFile(assetManager, assetPath, outPath)
                        Log.i(TAG, "Copied asset $assetPath to $outPath")
                    }
                    processedFiles.add(modelFileName)
                }
            }
            Log.i(TAG, "Vosk models copied successfully from assets based on configuration")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy Vosk model from assets: ${e.message}", e)
        } catch (e: org.json.JSONException) {
            Log.e(TAG, "Failed to parse model configuration: ${e.message}", e)
            // Fallback to copying all assets
            try {
                val assets = assetManager.list(modelAssetPath)
                if (assets != null && assets.isNotEmpty()) {
                    Log.i(TAG, "Found ${assets.size} assets in $modelAssetPath: ${assets.joinToString()}")
                    for (asset in assets) {
                        val assetPath = "$modelAssetPath/$asset"
                        if (asset.endsWith(".zip")) {
                            Log.i(TAG, "Extracting zip file $assetPath to $modelPath")
                            extractZipFromAssets(assetManager, assetPath, modelPath)
                        } else {
                            val outPath = "$modelPath/$asset"
                            copyAssetToFile(assetManager, assetPath, outPath)
                            Log.i(TAG, "Copied asset $assetPath to $outPath")
                        }
                    }
                    Log.i(TAG, "Vosk model copied successfully from assets as fallback")
                } else {
                    Log.w(TAG, "No model files found in assets at $modelAssetPath")
                }
            } catch (ex: IOException) {
                Log.e(TAG, "Fallback failed to copy Vosk model from assets: ${ex.message}", ex)
            }
        }
    }

    /**
     * Helper method to copy a single asset to a file.
     */
    private fun copyAssetToFile(assetManager: AssetManager, assetPath: String, outPath: String) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = assetManager.open(assetPath)
            val outFile = File(outPath)
            val parentDir = outFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
                Log.i(TAG, "Created parent directory: ${parentDir.absolutePath}")
            }
            outputStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var length: Int
            var totalBytes = 0L
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
                totalBytes += length
            }
            Log.i(TAG, "Copied $totalBytes bytes from $assetPath to $outPath")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy asset $assetPath to $outPath: ${e.message}", e)
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to close streams while copying asset $assetPath: ${e.message}", e)
            }
        }
    }

    /**
     * Extract a zip file from assets to the specified directory.
     */
    private fun extractZipFromAssets(assetManager: AssetManager, zipAssetPath: String, extractPath: String) {
        var inputStream: InputStream? = null
        try {
            inputStream = assetManager.open(zipAssetPath)
            val zipInputStream = java.util.zip.ZipInputStream(inputStream)
            var entry = zipInputStream.nextEntry
            val buffer = ByteArray(1024)
            while (entry != null) {
                val entryPath = "$extractPath/${entry.name}"
                if (entry.isDirectory) {
                    val dir = File(entryPath)
                    if (!dir.exists()) {
                        dir.mkdirs()
                        Log.i(TAG, "Created directory: $entryPath")
                    }
                } else {
                    val file = File(entryPath)
                    val parentDir = file.parentFile
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs()
                        Log.i(TAG, "Created parent directory: ${parentDir.absolutePath}")
                    }
                    var outputStream: FileOutputStream? = null
                    try {
                        outputStream = FileOutputStream(file)
                        var length: Int
                        var totalBytes = 0L
                        while (zipInputStream.read(buffer).also { length = it } > 0) {
                            outputStream.write(buffer, 0, length)
                            totalBytes += length
                        }
                        Log.i(TAG, "Extracted $totalBytes bytes to $entryPath")
                    } finally {
                        outputStream?.close()
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            zipInputStream.close()
            Log.i(TAG, "Successfully extracted $zipAssetPath to $extractPath")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to extract zip file from $zipAssetPath: ${e.message}", e)
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to close input stream for $zipAssetPath: ${e.message}", e)
            }
        }
    }

    /**
     * Read the model configuration from config.json in assets.
     * @return JSONObject representing the configuration.
     */
    private fun readModelConfig(): org.json.JSONObject {
        Log.i(TAG, "Reading Vosk model configuration from assets")
        val assetManager: AssetManager = context.assets
        val configPath = "models/vosk/config.json"
        try {
            val inputStream = assetManager.open(configPath)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            return org.json.JSONObject(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read model configuration from $configPath: ${e.message}", e)
            return org.json.JSONObject()
        }
    }

    /**
     * Load the Vosk model from the specified path.
     * @param path The path to the model files.
     * @return A Model object representing the loaded model.
     */
    private fun loadVoskModel(path: String): Model {
        Log.i(TAG, "Loading Vosk model from $path")
        return Model(path)
    }

    /**
     * Start listening for voice input using the initialized voice engine.
     * @return Boolean indicating if listening started successfully.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startListening(): Boolean {
        if (useNativeAndroid) {
            if (speechRecognizer == null) {
                Log.e(TAG, "Native Android voice engine not initialized")
                transcriptionCallback?.onTranscription("Error: Android STT engine not initialized")
                return false
            }
            try {
                val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Ensure on-device processing
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage) // Set the language
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguage)
                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, currentLanguage)
                speechRecognizer?.startListening(intent)
                isListening = true
                Log.i(TAG, "Started listening for voice input with Native Android in language: $currentLanguage")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening with Native Android: ${e.message}")
                transcriptionCallback?.onTranscription("Error: Failed to start Android STT - ${e.message}")
                return false
            }
        } else {
            if (recognizer == null) {
                Log.e(TAG, "Vosk voice engine not initialized")
                transcriptionCallback?.onTranscription("Error: VOSK engine not initialized")
                return false
            }
            try {
                val audioFormat = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                recorder?.startRecording()
                isListening = true
                listeningStartTime = System.currentTimeMillis()
                Log.i(TAG, "Started listening for voice input with Vosk at $listeningStartTime")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening with Vosk: ${e.message}")
                transcriptionCallback?.onTranscription("Error: Failed to start VOSK - ${e.message}")
                return false
            }
        }
    }

    /**
     * Set the model to use for voice recognition.
     * @param modelKey The key of the model to use (e.g., "en-us" or "fr").
     */
    fun setModel(modelKey: String) {
        Log.i(TAG, "Setting model to $modelKey")
        currentModelKey = modelKey
        // Reinitialize the voice engine with the new model if using Vosk
        if (!useNativeAndroid && model != null) {
            model?.close()
            model = null
            recognizer = null
            initVoiceEngine()
        }
    }
    
    /**
     * Set the language for voice recognition.
     * @param language The language code (e.g., "en-US" or "fr-FR").
     */
    @Synchronized
    fun setLanguage(language: String) {
        Log.i(TAG, "Setting language to $language")
        val previousLanguage = currentLanguage
        val previousModelKey = currentModelKey
        val wasListening = isListening
        
        try {
            // Stop any ongoing voice processing first
            if (wasListening) {
                Log.i(TAG, "Stopping voice processing for language switch")
                if (useNativeAndroid) {
                    speechRecognizer?.stopListening()
                } else {
                    recorder?.stop()
                    recorder?.release()
                    recorder = null
                    isListening = false
                }
                // Give time for any ongoing processing to complete
                Thread.sleep(100)
            }
            
            currentLanguage = language
            if (useNativeAndroid) {
                // For Android STT, we just store the language and use it when starting listening
                Log.i(TAG, "Language set for Android STT: $language")
            } else {
                // For Vosk, convert to model key and reinitialize if needed
                val modelKey = when (language) {
                    "fr-FR" -> "fr"
                    else -> "en-us"
                }
                
                if (modelKey != currentModelKey) {
                    Log.i(TAG, "Model key changed from $currentModelKey to $modelKey, reinitializing...")
                    
                    // Clean up existing resources safely
                    try {
                        recognizer = null
                        model?.close()
                        model = null
                        Log.i(TAG, "Cleaned up previous model resources")
                    } catch (cleanupException: Exception) {
                        Log.w(TAG, "Warning during resource cleanup: ${cleanupException.message}")
                    }
                    
                    // Set new model key and reinitialize
                    currentModelKey = modelKey
                    if (!initVoiceEngine()) {
                        throw Exception("Failed to initialize voice engine with new language model")
                    }
                    Log.i(TAG, "Successfully reinitialized voice engine for language: $language")
                } else {
                    Log.i(TAG, "Model key unchanged, no reinitialization needed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set language to $language: ${e.message}", e)
            // Rollback changes
            currentLanguage = previousLanguage
            currentModelKey = previousModelKey
            // Try to reinitialize with previous settings
            try {
                if (!useNativeAndroid) {
                    initVoiceEngine()
                }
            } catch (rollbackException: Exception) {
                Log.e(TAG, "Failed to rollback to previous language: ${rollbackException.message}")
            }
            throw e
        }
    }

    /**
     * Reset the Vosk recognizer to prevent freezing after prolonged use.
     * @param reason The reason for the reset, to be displayed in the UI.
     */
    private fun resetRecognizer(reason: String = "Prolonged use timeout") {
        if (!useNativeAndroid && recognizer != null) {
            Log.i(TAG, "Resetting Vosk recognizer: $reason")
            try {
                // Ensure any ongoing recording is stopped and released
                if (isListening) {
                    recorder?.stop()
                    recorder?.release()
                    recorder = null
                    isListening = false
                    Log.i(TAG, "Stopped and released audio recorder during reset")
                }
                recognizer = null
                model?.close()
                model = null
                Log.i(TAG, "Closed existing model and recognizer")
                val initSuccess = initVoiceEngine()
                listeningStartTime = System.currentTimeMillis()
                lastResultTime = System.currentTimeMillis()
                if (initSuccess) {
                    Log.i(TAG, "Recognizer reset successful, new start time: $listeningStartTime")
                    transcriptionCallback?.onEngineReset(reason)
                } else {
                    Log.e(TAG, "Failed to reinitialize voice engine after reset")
                    transcriptionCallback?.onTranscription("Error: Failed to reinitialize voice engine after reset")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting recognizer: ${e.message}", e)
                transcriptionCallback?.onTranscription("Error: Failed to reset recognizer - ${e.message}")
            }
        }
    }


    /**
     * Start audio recording.
     * @return The file path of the recording if started successfully, null otherwise.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(): String? {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return recordingFilePath
        }

        try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            val fileName = "kitt-$dateStr.3gp"
            recordingFilePath = "$recordingsPath/$fileName"
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFilePath)
                prepare()
                start()
            }
            isRecording = true
            Log.i(TAG, "Started recording to $recordingFilePath")
            return recordingFilePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            mediaRecorder?.release()
            mediaRecorder = null
            recordingFilePath = null
            isRecording = false
            return null
        }
    }

    /**
     * Start streaming audio to the offline assistant service.
     * @return Boolean indicating if streaming started successfully.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startStreamingToAssistant(): Boolean {
        if (isStreamingToAssistant) {
            Log.w(TAG, "Already streaming to assistant")
            return true
        }

        if (assistantService == null) {
            Log.e(TAG, "Assistant service not bound")
            bindToAssistantService()
            if (assistantService == null) {
                Log.e(TAG, "Failed to bind to assistant service")
                return false
            }
        }

        try {
            assistantService?.startProcessing()
            assistantService?.setResponseCallback { response ->
                transcriptionCallback?.onTranscription(response)
            }
            isStreamingToAssistant = true
            Log.i(TAG, "Started streaming to assistant service")
            if (!isListening) {
                return startListening()
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start streaming to assistant: ${e.message}")
            return false
        }
    }

    /**
     * Bind to the OfflineAssistantService.
     */
    private fun bindToAssistantService() {
        try {
            val intent = Intent(context, OfflineAssistantService::class.java)
            // Start the service explicitly to ensure it is running
            context.startService(intent)
            context.bindService(intent, object : android.content.ServiceConnection {
                override fun onServiceConnected(name: android.content.ComponentName?, service: IBinder?) {
                    assistantService = (service as OfflineAssistantService.LocalBinder).getService()
                    Log.i(TAG, "Bound to OfflineAssistantService")
                    // Attempt to start streaming if we were waiting for the service
                    if (isStreamingToAssistant && !isListening) {
                        Log.i(TAG, "Service bound, attempting to start streaming")
                        startStreamingToAssistant()
                    }
                }

                override fun onServiceDisconnected(name: android.content.ComponentName?) {
                    assistantService = null
                    Log.w(TAG, "Disconnected from OfflineAssistantService")
                    // Attempt to rebind on disconnection
                    bindToAssistantService()
                }
            }, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to OfflineAssistantService: ${e.message}")
            // Retry binding after a short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                Log.i(TAG, "Retrying binding to OfflineAssistantService")
                bindToAssistantService()
            }, 1000)
        }
    }

    /**
     * Stop audio recording.
     * @return The file path of the recording if stopped successfully, null otherwise.
     */
    fun stopRecording(): String? {
        if (!isRecording) {
            Log.w(TAG, "Not currently recording")
            return null
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Log.i(TAG, "Stopped recording to $recordingFilePath")
            return recordingFilePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            return null
        }
    }

    /**
     * Set up external storage for recordings if available.
     */
    private fun setupExternalStorage() {
        try {
            val externalDir = context.getExternalFilesDir(null)
            if (externalDir != null) {
                val externalPath = "${externalDir.absolutePath}/Recordings/KITT"
                val externalRecordingsDir = File(externalPath)
                if (!externalRecordingsDir.exists()) {
                    externalRecordingsDir.mkdirs()
                    Log.i(TAG, "Created external recordings directory: $externalPath")
                }
                externalRecordingsPath = externalPath
                Log.i(TAG, "External storage available for recordings: $externalPath")
            } else {
                Log.w(TAG, "External storage not available")
                externalRecordingsPath = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up external storage: ${e.message}")
            externalRecordingsPath = null
        }
    }

    /**
     * Toggle between internal and external storage for recordings.
     * @param useExternal Boolean indicating whether to use external storage if available.
     * @return Boolean indicating if the toggle was successful.
     */
    fun toggleStorageLocation(useExternal: Boolean): Boolean {
        if (useExternal && externalRecordingsPath == null) {
            Log.w(TAG, "Cannot toggle to external storage: not available")
            return false
        }
        useExternalStorage = useExternal
        Log.i(TAG, "Storage location toggled to ${if (useExternal) "external" else "internal"} storage: $recordingsPath")
        return true
    }

    /**
     * Check if external storage is available for recordings.
     * @return Boolean indicating if external storage is available.
     */
    fun isExternalStorageAvailable(): Boolean {
        return externalRecordingsPath != null
    }

    /**
     * Get the current storage location type.
     * @return String indicating the current storage location ("internal" or "external").
     */
    fun getStorageLocation(): String {
        return if (useExternalStorage && externalRecordingsPath != null) "external" else "internal"
    }

    /**
     * Get the current storage path for recordings.
     * @return String indicating the full path where recordings are stored.
     */
    fun getCurrentStoragePath(): String {
        return recordingsPath
    }

    /**
     * Check if recording is currently active.
     * @return Boolean indicating if recording is active.
     */
    fun isRecordingActive(): Boolean {
        return isRecording
    }

    /**
     * Process voice input in real-time using the initialized voice engine.
     * @return The partial or final result as a String if available, empty string otherwise.
     */
    fun processVoiceInput(): String {
        if (useNativeAndroid) {
            if (!isListening || speechRecognizer == null) {
                Log.e(TAG, "Native Android voice engine not initialized or not listening")
                return ""
            }
            // Native Android processing is handled via callbacks in RecognitionListener
            return ""
        } else {
            if (recorder == null || !isListening) {
                Log.e(TAG, "Voice engine not initialized or not listening")
                return ""
            }

            val startTime = System.currentTimeMillis()
            val buffer = ShortArray(BUFFER_SIZE)
            val read = recorder?.read(buffer, 0, BUFFER_SIZE) ?: 0
            if (read > 0) {
                // Calculate RMS for monitoring audio input level
                var sum = 0.0
                for (i in 0 until read) {
                    sum += buffer[i] * buffer[i]
                }
                val rms = Math.sqrt(sum / read).toFloat()
                Log.d(TAG, "Audio Input RMS: $rms")

                // Check if RMS is 0 and track duration
                if (rms == 0.0f) {
                    if (lastRmsZeroTime == 0L) {
                        lastRmsZeroTime = System.currentTimeMillis()
                    } else if (System.currentTimeMillis() - lastRmsZeroTime >= RMS_ZERO_TIMEOUT_MS) {
                        Log.w(TAG, "RMS level at 0 for ${RMS_ZERO_TIMEOUT_MS/1000}s, resetting engine")
                        resetRecognizer("RMS level at 0 for too long")
                        lastRmsZeroTime = System.currentTimeMillis() // Reset timer after action
                    }
                } else {
                    lastRmsZeroTime = 0L // Reset timer if RMS is not 0
                }

                // Stream to assistant service if enabled
                if (isStreamingToAssistant && assistantService != null) {
                    assistantService?.processAudioBuffer(buffer, read)
                    return ""
                } else {
                    if (recognizer == null) {
                        Log.e(TAG, "Vosk recognizer not initialized")
                        return ""
                    }
                    val isFinal = recognizer?.acceptWaveForm(buffer, read) ?: false
                    val result = if (isFinal) {
                        val finalResult = recognizer?.result ?: "{}"
                        Log.i(TAG, "Final result detected: $finalResult")
                        transcriptionCallback?.onTranscription(finalResult)
                        finalResult
                    } else {
                        val partialResult = recognizer?.partialResult ?: "{}"
                        if (partialResult.isNotEmpty()) {
                            transcriptionCallback?.onTranscription(partialResult)
                        }
                        partialResult
                    }
                    val processTime = System.currentTimeMillis() - startTime
                    if (processTime > MAX_LATENCY_MS) {
                        Log.w(TAG, "Voice processing exceeded latency target: ${processTime}ms")
                    }
                    Log.d(TAG, "${if (isFinal) "Final" else "Partial"} voice input processed in ${processTime}ms")

                    // Update last result time if we have a non-empty result
                    if (result.isNotEmpty()) {
                        lastResultTime = System.currentTimeMillis()
                    }

                    // Check if reset threshold is reached
                    if (listeningStartTime > 0 && (System.currentTimeMillis() - listeningStartTime) >= RESET_THRESHOLD_MS) {
                        Log.i(TAG, "Reset threshold of ${RESET_THRESHOLD_MS/1000}s reached, resetting recognizer")
                        resetRecognizer()
                    }

                    // Check for potential freeze based on last result time
                    if (lastResultTime > 0 && (System.currentTimeMillis() - lastResultTime) >= FREEZE_TIMEOUT_MS) {
                        Log.w(TAG, "No results for ${FREEZE_TIMEOUT_MS/1000}s, potential freeze detected, resetting recognizer")
                        resetRecognizer("Potential freeze detected")
                        transcriptionCallback?.onTranscription("Warning: Audio processing stalled, resetting engine...")
                    }

                    return result
                }
            }
            return ""
        }
    }

    /**
     * Stop listening for voice input.
     * @return The final processed result as a String.
     */
    fun stopListening(): String {
        if (!isListening) {
            Log.w(TAG, "Not currently listening")
            // Ensure resources are released even if not listening
            if (useNativeAndroid) {
                speechRecognizer?.stopListening()
                Log.i(TAG, "Ensured Native Android recognizer is stopped")
            } else {
                recorder?.stop()
                recorder?.release()
                recorder = null
                Log.i(TAG, "Ensured Vosk recorder is stopped and released")
            }
            return "Error: Not listening"
        }

        isListening = false
        listeningStartTime = 0
        if (useNativeAndroid) {
            speechRecognizer?.stopListening()
            Log.i(TAG, "Stopped listening for voice input with Native Android")
            return "Stopped Native Android listening"
        } else {
            recorder?.stop()
            recorder?.release()
            recorder = null
            if (isStreamingToAssistant && assistantService != null) {
                val response = assistantService?.stopProcessing() ?: "Stopped streaming to assistant"
                Log.i(TAG, "Stopped streaming to assistant service")
                isStreamingToAssistant = false
                return response
            } else {
                val finalResult = recognizer?.result ?: "{}"
                Log.i(TAG, "Stopped listening for voice input with Vosk")
                transcriptionCallback?.onTranscription(finalResult)
                return finalResult
            }
        }
    }

    /**
     * Switch between engines.
     * @param engine The engine to switch to ("VOSK" or "ANDROID").
     */
    fun switchEngine(engine: String) {
        val useNative = engine.uppercase() == "ANDROID"
        Log.i(TAG, "Switching to ${if (useNative) "Native Android" else "Vosk"} engine")
        useNativeAndroid = useNative
        // Ensure any ongoing listening is stopped
        if (isListening) {
            stopListening()
        }
        // Clean up existing resources thoroughly
        if (!useNativeAndroid) {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            Log.i(TAG, "Native Android recognizer resources fully released")
        } else {
            recognizer = null
            model?.close()
            model = null
            Log.i(TAG, "Vosk model and recognizer resources fully released")
        }
        // Reinitialize the voice engine based on the new setting
        initVoiceEngine()
        // Ensure language is set correctly for the new engine
        setLanguage(currentLanguage)
    }

    /**
     * Get the current engine type.
     * @return String indicating the current engine ("VOSK" or "ANDROID").
     */
    fun getCurrentEngine(): String {
        return if (useNativeAndroid) "ANDROID" else "VOSK"
    }
}
