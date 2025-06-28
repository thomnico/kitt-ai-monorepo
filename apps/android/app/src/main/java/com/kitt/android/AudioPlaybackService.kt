package com.kitt.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.IOException

class AudioPlaybackService : Service() {

    private val TAG = "AudioPlaybackService"
    private var mediaPlayer: MediaPlayer? = null
    private var bluetoothAudioService: BluetoothAudioService? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AudioPlaybackService created")
        bluetoothAudioService = BluetoothAudioService(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "AudioPlaybackService bound")
        return binder
    }

    /**
     * Plays an audio file from the given file path.
     * @param filePath The absolute path to the audio file.
     * @return True if playback started successfully, false otherwise.
     */
    fun playAudio(filePath: String): Boolean {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                // Route audio through Bluetooth if available and connected
                if (bluetoothAudioService?.isBluetoothAvailable() == true) {
                    bluetoothAudioService?.routeAudioToBluetooth()
                }
                start()
                Log.d(TAG, "Started playing audio from: $filePath")
            } catch (e: IOException) {
                Log.e(TAG, "Error playing audio from $filePath: ${e.message}")
                release()
                mediaPlayer = null
                return false
            }
        }

        mediaPlayer?.setOnCompletionListener {
            Log.d(TAG, "Audio playback completed")
            // Stop Bluetooth audio routing after playback finishes
            bluetoothAudioService?.stopBluetoothAudio()
            it.release()
            mediaPlayer = null
        }

        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            Log.e(TAG, "Error during audio playback: what=$what, extra=$extra")
            // Stop Bluetooth audio routing on error
            bluetoothAudioService?.stopBluetoothAudio()
            mp.release()
            mediaPlayer = null
            true // Indicate that the error was handled
        }
        return true
    }

    /**
     * Stops the current audio playback.
     */
    fun stopPlayback() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            Log.d(TAG, "Stopped audio playback")
        }
        mediaPlayer?.release()
        mediaPlayer = null
        // Ensure Bluetooth audio routing is stopped
        bluetoothAudioService?.stopBluetoothAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        bluetoothAudioService?.cleanup()
        Log.d(TAG, "AudioPlaybackService destroyed")
    }
}
