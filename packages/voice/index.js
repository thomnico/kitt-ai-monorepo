/**
 * Voice Engine for KITT Framework
 * This module serves as the entry point for voice interaction capabilities,
 * integrating Vosk for voice processing on Android and macOS.
 * 
 * License: Apache 2.0 for Vosk model usage. Attribution provided in documentation.
 */

import { initializeVosk } from 'vosk';
import { platform } from './platform-config.js';

// Performance target: Ensure end-to-end latency < 200ms as per monorepo rules
const MAX_LATENCY_MS = 200;

/**
 * Initialize the voice engine with platform-specific configurations.
 * @param {Object} options - Configuration options for the voice engine.
 * @returns {Promise<Object>} - Initialized voice engine instance.
 */
export async function initVoiceEngine(options = {}) {
  const startTime = performance.now();
  
  // Platform-specific initialization
  const config = platform[options.platform || detectPlatform()];
  if (!config) {
    throw new Error(`Unsupported platform: ${options.platform}`);
  }

  // Initialize Vosk with offline-first approach
  const voskInstance = await initializeVosk({
    modelPath: config.modelPath,
    offlineMode: true, // Enforce offline-first as per monorepo rules
    ...options.voskOptions
  });

  const initTime = performance.now() - startTime;
  if (initTime > MAX_LATENCY_MS) {
    console.warn(`Voice engine initialization exceeded latency target: ${initTime}ms`);
  }

  return {
    instance: voskInstance,
    platform: config.platform,
    processVoiceInput: async (input) => {
      const processStart = performance.now();
      const result = await voskInstance.process(input);
      const processTime = performance.now() - processStart;
      if (processTime > MAX_LATENCY_MS) {
        console.warn(`Voice processing exceeded latency target: ${processTime}ms`);
      }
      return result;
    }
  };
}

/**
 * Detect the current platform (Android or macOS).
 * @returns {string} - Detected platform identifier.
 */
function detectPlatform() {
  // Placeholder for actual platform detection logic
  // This will be overridden by platform-specific builds
  return 'android'; // Default for initial setup
}

export default {
  initVoiceEngine,
  detectPlatform
};
