package com.kitt.android.voice

import android.content.Context
import android.util.Log
import com.kitt.android.KittDashboardView

/**
 * Processes voice commands and maps them to KITT interface actions
 */
class VoiceCommandProcessor(
    private val context: Context,
    private val dashboardView: KittDashboardView?
) {
    
    companion object {
        private const val TAG = "VoiceCommandProcessor"
        
        // Voice command mappings
        private val COMMAND_MAPPINGS = mapOf(
            // Air conditioning commands
            "language" to "LANG",
            "language setting" to "LANG",
            "english" to "LANG",
            "french" to "LANG",
            
            // Voice model selection commands
            "vosk" to "VOSK",
            "model check" to "VOSK",
            "voice system" to "VOSK",
            "diagnostics" to "VOSK",
            
            // Program commands
            "program one" to "P1",
            "program 1" to "P1",
            "p1" to "P1",
            "program two" to "P2",
            "program 2" to "P2",
            "p2" to "P2",
            
            // Scanner commands
            "scanner one" to "S1",
            "scanner 1" to "S1",
            "s1" to "S1",
            "scanner two" to "S2",
            "scanner 2" to "S2",
            "s2" to "S2",
            "scan" to "S1",
            
            // System commands
            "activate" to "ACTIVATE",
            "deactivate" to "DEACTIVATE",
            "start" to "START",
            "stop" to "STOP",
            "kitt" to "KITT_WAKE",
            "hello kitt" to "KITT_WAKE"
        )
    }
    
    /**
     * Process a voice command and execute the corresponding action
     */
    fun processCommand(command: String): Boolean {
        val normalizedCommand = command.lowercase().trim()
        Log.d(TAG, "Processing voice command: $normalizedCommand")
        
        // Find matching command
        val mappedCommand = findBestMatch(normalizedCommand)
        
        return if (mappedCommand != null) {
            executeCommand(mappedCommand, normalizedCommand)
            true
        } else {
            Log.w(TAG, "No matching command found for: $normalizedCommand")
            false
        }
    }
    
    /**
     * Find the best matching command using fuzzy matching
     */
    private fun findBestMatch(input: String): String? {
        // Direct match first
        COMMAND_MAPPINGS[input]?.let { return it }
        
        // Partial match - check if input contains any command keywords
        for ((keyword, command) in COMMAND_MAPPINGS) {
            if (input.contains(keyword)) {
                return command
            }
        }
        
        // Fuzzy match for common variations
        return when {
            input.contains("language") || input.contains("english") || input.contains("french") -> "LANG"
            input.contains("vosk") || input.contains("diagnostic") -> "VOSK"
            input.contains("program") && (input.contains("1") || input.contains("one")) -> "P1"
            input.contains("program") && (input.contains("2") || input.contains("two")) -> "P2"
            input.contains("scanner") || input.contains("scan") -> "S1"
            input.contains("start") || input.contains("activate") -> "START"
            input.contains("stop") || input.contains("deactivate") -> "STOP"
            else -> null
        }
    }
    
    /**
     * Execute the mapped command
     */
    private fun executeCommand(command: String, originalInput: String) {
        Log.i(TAG, "Executing command: $command (from input: $originalInput)")
        
        when (command) {
            "LANG" -> {
                dashboardView?.simulateButtonPress("LANG")
                provideFeedback("Language selection activated (ENG/FR)")
            }
            "VOSK" -> {
                dashboardView?.simulateButtonPress("VOSK")
                provideFeedback("Voice model diagnostics initiated")
            }
            "P1" -> {
                dashboardView?.simulateButtonPress("P1")
                provideFeedback("Program 1 activated")
            }
            "P2" -> {
                dashboardView?.simulateButtonPress("P2")
                provideFeedback("Program 2 activated")
            }
            "S1" -> {
                dashboardView?.simulateButtonPress("S1")
                provideFeedback("Scanner mode 1 engaged")
            }
            "S2" -> {
                dashboardView?.simulateButtonPress("S2")
                provideFeedback("Scanner mode 2 engaged")
            }
            "START" -> {
                dashboardView?.startSystems()
                provideFeedback("KITT systems online")
            }
            "STOP" -> {
                dashboardView?.stopSystems()
                provideFeedback("KITT systems offline")
            }
            "KITT_WAKE" -> {
                provideFeedback("KITT online. How may I assist you?")
            }
            "ACTIVATE" -> {
                dashboardView?.startSystems()
                provideFeedback("Systems activated")
            }
            "DEACTIVATE" -> {
                dashboardView?.stopSystems()
                provideFeedback("Systems deactivated")
            }
        }
    }
    
    /**
     * Provide audio feedback to the user
     */
    private fun provideFeedback(message: String) {
        Log.i(TAG, "Voice feedback: $message")
        // TODO: Integrate with text-to-speech or audio feedback system
        // For now, just log the feedback message
    }
    
    /**
     * Get available voice commands for help/training
     */
    fun getAvailableCommands(): List<String> {
        return COMMAND_MAPPINGS.keys.toList().sorted()
    }
    
    /**
     * Check if a command is recognized
     */
    fun isCommandRecognized(command: String): Boolean {
        return findBestMatch(command.lowercase().trim()) != null
    }
}
