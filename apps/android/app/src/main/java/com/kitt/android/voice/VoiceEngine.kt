package com.kitt.android.voice

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.util.Log
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
    private val modelPath = "${context.filesDir.absolutePath}/models/vosk"
    private var isListening = false
    private var currentModelKey = "en-us"
    private var currentLanguage = "en-US"
    private var useNativeAndroid = false
    private var transcriptionCallback: TranscriptionCallback? = null

    /**
     * Interface for transcription callbacks.
     */
    interface TranscriptionCallback {
        fun onTranscription(transcription: String)
    }

    /**
     * Set the callback for transcription results.
     * @param callback The callback to be invoked with transcription results.
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
                Log.i(TAG, "Started listening for voice input with Vosk")
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
    fun setLanguage(language: String) {
        Log.i(TAG, "Setting language to $language")
        currentLanguage = language
        if (useNativeAndroid) {
            // For Android STT, we just store the language and use it when starting listening
            Log.i(TAG, "Language set for Android STT: $language")
        } else {
            // For Vosk, convert to model key
            val modelKey = when (language) {
                "fr-FR" -> "fr"
                else -> "en-us"
            }
            setModel(modelKey)
        }
    }

    /**
     * Switch between Vosk and Native Android voice processing.
     * @param useNative Boolean indicating whether to use Native Android processing.
     */
    fun switchToNativeAndroid(useNative: Boolean) {
        Log.i(TAG, "Switching to ${if (useNative) "Native Android" else "Vosk"} voice processing")
        useNativeAndroid = useNative
        // Clean up existing resources
        if (useNative) {
            model?.close()
            model = null
            recognizer = null
        } else {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
        // Reinitialize with the new setting
        if (!initVoiceEngine()) {
            transcriptionCallback?.onTranscription("Error: Failed to initialize ${if (useNative) "Android STT" else "VOSK"} engine")
        }
        // Ensure language is set correctly for the new engine
        setLanguage(currentLanguage)
    }

    /**
     * Stop listening for voice input.
     * @return The final processed result as a String.
     */
    fun stopListening(): String {
        if (!isListening) {
            Log.w(TAG, "Not currently listening")
            return "Error: Not listening"
        }

        isListening = false
        if (useNativeAndroid) {
            speechRecognizer?.stopListening()
            Log.i(TAG, "Stopped listening for voice input with Native Android")
            return "Stopped Native Android listening"
        } else {
            recorder?.stop()
            recorder?.release()
            recorder = null
            val finalResult = recognizer?.result ?: "{}"
            Log.i(TAG, "Stopped listening for voice input with Vosk")
            transcriptionCallback?.onTranscription(finalResult)
            return finalResult
        }
    }

    /**
     * Process voice input in real-time using the initialized voice engine.
     * @return The partial result as a String if available, empty string otherwise.
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
            if (recorder == null || !isListening || recognizer == null) {
                Log.e(TAG, "Vosk voice engine not initialized or not listening")
                return ""
            }

            val startTime = System.currentTimeMillis()
            val buffer = ShortArray(BUFFER_SIZE)
            val read = recorder?.read(buffer, 0, BUFFER_SIZE) ?: 0
            if (read > 0) {
                recognizer?.acceptWaveForm(buffer, read)
                val partialResult = recognizer?.partialResult ?: "{}"
                val processTime = System.currentTimeMillis() - startTime
                if (processTime > MAX_LATENCY_MS) {
                    Log.w(TAG, "Voice processing exceeded latency target: ${processTime}ms")
                }
                Log.i(TAG, "Partial voice input processed in ${processTime}ms")
                if (partialResult.isNotEmpty()) {
                    transcriptionCallback?.onTranscription(partialResult)
                }
                return partialResult
            }
            return ""
        }
    }

    /**
     * Get the current engine being used.
     * @return String indicating the current engine ("VOSK" or "ANDROID").
     */
    fun getCurrentEngine(): String {
        return if (useNativeAndroid) "ANDROID" else "VOSK"
    }

    /**
     * Switch between engines.
     * @param engine The engine to switch to ("VOSK" or "ANDROID").
     */
    fun switchEngine(engine: String) {
        val useNative = engine.uppercase() == "ANDROID"
        switchToNativeAndroid(useNative)
    }

}
