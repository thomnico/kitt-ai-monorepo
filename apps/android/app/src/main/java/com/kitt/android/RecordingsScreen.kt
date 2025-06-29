package com.kitt.android

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.model.Toggle
import android.util.Log
import com.kitt.android.voice.VoiceEngine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingsScreen(
    carContext: CarContext,
    private val audioPlaybackService: AudioPlaybackService?,
    private val voiceEngine: VoiceEngine?
) : Screen(carContext) {

    private val TAG = "RecordingsScreen"
    private var recordedFiles: List<File> = emptyList()
    private var currentlyPlayingFile: String? = null

    init {
        Log.d(TAG, "RecordingsScreen initialized")
        loadRecordedFiles()
    }

    override fun onGetTemplate(): Template {
        Log.d(TAG, "Building RecordingsScreen template")

        val listBuilder = ItemList.Builder()

        if (recordedFiles.isEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("No recordings found.")
                    .build()
            )
        } else {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            recordedFiles.forEach { file ->
                val lastModified = Date(file.lastModified())
                val fileSizeKB = file.length() / 1024
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(file.name)
                        .addText("Size: ${fileSizeKB} KB | ${dateFormat.format(lastModified)}")
                        .setOnClickListener {
                            if (currentlyPlayingFile == file.absolutePath) {
                                stopPlayback()
                            } else {
                                playFile(file.absolutePath)
                            }
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("My Recordings")
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun loadRecordedFiles() {
        val recordingsPath = voiceEngine?.getCurrentStoragePath()
        if (recordingsPath != null) {
            val directory = File(recordingsPath)
            if (directory.exists() && directory.isDirectory) {
                recordedFiles = directory.listFiles { file ->
                    file.isFile && file.extension == "3gp" // Assuming .3gp from VoiceEngine
                }?.toList() ?: emptyList()
                recordedFiles = recordedFiles.sortedByDescending { it.lastModified() }
                Log.d(TAG, "Loaded ${recordedFiles.size} recorded files from $recordingsPath")
            } else {
                Log.w(TAG, "Recordings directory does not exist or is not a directory: $recordingsPath")
                recordedFiles = emptyList()
            }
        } else {
            Log.e(TAG, "VoiceEngine recording path is null")
            recordedFiles = emptyList()
        }
        invalidate() // Refresh the UI
    }

    private fun playFile(filePath: String) {
        if (audioPlaybackService?.playAudio(filePath) == true) {
            currentlyPlayingFile = filePath
            Log.d(TAG, "Attempting to play $filePath")
        } else {
            currentlyPlayingFile = null
            Log.e(TAG, "Failed to play $filePath")
        }
        invalidate() // Refresh the UI to update toggle state
    }

    private fun stopPlayback() {
        audioPlaybackService?.stopPlayback()
        currentlyPlayingFile = null
        Log.d(TAG, "Stopped playback")
        invalidate() // Refresh the UI to update toggle state
    }
}
