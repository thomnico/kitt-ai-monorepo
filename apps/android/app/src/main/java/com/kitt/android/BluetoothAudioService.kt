package com.kitt.android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class BluetoothAudioService(private val context: Context) {
    private val TAG = "BluetoothAudioService"
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothHeadset: BluetoothHeadset? = null
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Profile listener to handle Bluetooth headset connection state
    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = proxy as BluetoothHeadset
                Log.d(TAG, "Bluetooth headset connected")
                routeAudioToBluetooth()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null
                Log.d(TAG, "Bluetooth headset disconnected")
            }
        }
    }

    init {
        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device")
        } else {
            // Request Bluetooth headset profile proxy
            bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
        }
    }

    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter?.isEnabled == true
    }

    fun routeAudioToBluetooth() {
        if (bluetoothHeadset != null) {
            // Set audio mode to communication for voice interaction
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            
            // Route audio to Bluetooth SCO (Synchronous Connection-Oriented)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
            } else {
                @Suppress("DEPRECATION")
                audioManager.startBluetoothSco()
                @Suppress("DEPRECATION")
                audioManager.isBluetoothScoOn = true
            }
            Log.d(TAG, "Audio routed to Bluetooth headset")
        } else {
            Log.w(TAG, "No Bluetooth headset connected to route audio")
        }
    }

    fun stopBluetoothAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
        } else {
            @Suppress("DEPRECATION")
            audioManager.stopBluetoothSco()
            @Suppress("DEPRECATION")
            audioManager.isBluetoothScoOn = false
        }
        audioManager.mode = AudioManager.MODE_NORMAL
        Log.d(TAG, "Bluetooth audio routing stopped")
    }

    fun cleanup() {
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
        bluetoothHeadset = null
        stopBluetoothAudio()
        Log.d(TAG, "Bluetooth service cleanup completed")
    }
}
