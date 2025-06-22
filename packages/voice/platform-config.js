/**
 * Platform-specific configurations for the voice engine.
 * This file defines settings for Android and macOS to integrate Kyutai Moshi.
 * 
 * License: CC-BY for Kyutai Moshi model usage. Attribution provided in documentation.
 */

/**
 * Configuration object for supported platforms.
 * Each platform defines paths and settings necessary for voice engine initialization.
 */
export const platform = {
  android: {
    platform: 'android',
    modelPath: '/data/data/com.kitt.android/models/moshi', // Path optimized for Android hardware
    performanceTarget: {
      latencyMs: 200, // Enforce 200ms latency rule
      hardware: 'Crosscall Core-Z5', // Target hardware as per monorepo rules
      ramConstraint: '4GB', // Hardware constraint
      gpu: 'Adreno 643' // Hardware constraint
    },
    offlineSupport: true // Enforce offline-first design
  },
  macos: {
    platform: 'macos',
    modelPath: '/Applications/KITT.app/Contents/Resources/models/moshi', // Path for macOS app bundle
    performanceTarget: {
      latencyMs: 200, // Enforce 200ms latency rule
      hardware: 'MacBook Pro', // Generic macOS hardware target
      ramConstraint: '8GB+', // Minimum RAM for macOS
      gpu: 'Integrated' // Generic GPU for macOS
    },
    offlineSupport: true // Enforce offline-first design
  }
};
