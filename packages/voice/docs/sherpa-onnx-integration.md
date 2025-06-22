# sherpa-onnx Integration in KITT Framework Voice Engine

## Overview

The KITT Framework Voice Engine integrates the sherpa-onnx model for voice interaction capabilities on Android and macOS platforms. This document outlines the setup, performance considerations, and licensing compliance for using sherpa-onnx within our framework.

## Performance Requirements

As per the KITT Framework monorepo rules, the voice engine adheres to the following performance constraints:

- **Latency Target:** All voice interactions must achieve an end-to-end latency of less than 200ms on target hardware (Crosscall Core-Z5 for Android).
- **Hardware Constraints:** Optimized for devices with 4GB RAM and Adreno 643 GPU on Android, ensuring minimal resource usage.
- **Offline-First Design:** The engine is designed to function fully without an internet connection, using local model processing.

## Platform Support

- **Android:** Model files are stored at `/data/data/com.kitt.android/models/sherpa-onnx` for optimal access on Android hardware.
- **macOS:** Model files are bundled within the application at `/Applications/KITT.app/Contents/Resources/models/sherpa-onnx`.

## Integration Details

The voice engine initializes sherpa-onnx with platform-specific configurations, ensuring offline operation and performance monitoring to meet latency targets. Key functions include:

- `initVoiceEngine(options)`: Initializes the voice engine with platform-specific settings and sherpa-onnx model.
- `processVoiceInput(input)`: Processes voice input with latency tracking to ensure compliance with the 200ms rule.

## Licensing and Attribution

The sherpa-onnx model is used under the **MIT** license. We provide the following attribution as required:

- **sherpa-onnx Model Attribution:** The KITT Framework Voice Engine utilizes the sherpa-onnx model for voice processing. sherpa-onnx is developed by k2-fsa and licensed under MIT. For more information, visit the sherpa-onnx project repository at https://github.com/k2-fsa/sherpa-onnx.

In compliance with the monorepo rules, no references to copyrighted intellectual property are made, and generic terms like "voice assistant" are used throughout the codebase and documentation.

## Usage Notes

- Ensure that the model files are correctly placed in the specified paths for each platform.
- Monitor performance logs to address any latency issues exceeding the 200ms target.
- All features must remain functional in offline mode, adhering to the offline-first foundation of the KITT Framework.
- **Model Download:** If the sherpa-onnx model files are not already included or specified, they can be downloaded from the official repository at https://github.com/k2-fsa/sherpa-onnx.

## Contact

For further information or support regarding the voice engine integration, contact the KITT Framework development team.

---
*This documentation is part of the KITT Framework monorepo and adheres to the coding and legal guidelines specified therein.*
