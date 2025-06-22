# Kyutai Moshi Integration in KITT Framework Voice Engine

## Overview

The KITT Framework Voice Engine integrates the Kyutai Moshi model for voice interaction capabilities on Android and macOS platforms. This document outlines the setup, performance considerations, and licensing compliance for using Kyutai Moshi within our framework.

## Performance Requirements

As per the KITT Framework monorepo rules, the voice engine adheres to the following performance constraints:

- **Latency Target:** All voice interactions must achieve an end-to-end latency of less than 200ms on target hardware (Crosscall Core-Z5 for Android).
- **Hardware Constraints:** Optimized for devices with 4GB RAM and Adreno 643 GPU on Android, ensuring minimal resource usage.
- **Offline-First Design:** The engine is designed to function fully without an internet connection, using local model processing.

## Platform Support

- **Android:** Model files are stored at `/data/data/com.kitt.android/models/moshi` for optimal access on Android hardware.
- **macOS:** Model files are bundled within the application at `/Applications/KITT.app/Contents/Resources/models/moshi`.

## Integration Details

The voice engine initializes Kyutai Moshi with platform-specific configurations, ensuring offline operation and performance monitoring to meet latency targets. Key functions include:

- `initVoiceEngine(options)`: Initializes the voice engine with platform-specific settings and Kyutai Moshi model.
- `processVoiceInput(input)`: Processes voice input with latency tracking to ensure compliance with the 200ms rule.

## Licensing and Attribution

The Kyutai Moshi model is used under the **Creative Commons Attribution (CC-BY)** license. We provide the following attribution as required:

- **Kyutai Moshi Model Attribution:** The KITT Framework Voice Engine utilizes the Kyutai Moshi model for voice processing. Kyutai Moshi is developed by Kyutai and licensed under CC-BY. For more information, visit the Kyutai project repository or official documentation.

In compliance with the monorepo rules, no references to copyrighted intellectual property are made, and generic terms like "voice assistant" are used throughout the codebase and documentation.

## Usage Notes

- Ensure that the model files are correctly placed in the specified paths for each platform.
- Monitor performance logs to address any latency issues exceeding the 200ms target.
- All features must remain functional in offline mode, adhering to the offline-first foundation of the KITT Framework.

## Contact

For further information or support regarding the voice engine integration, contact the KITT Framework development team.

---
*This documentation is part of the KITT Framework monorepo and adheres to the coding and legal guidelines specified therein.*
