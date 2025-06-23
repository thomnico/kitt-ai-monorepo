# Vosk Integration in KITT Framework

This document outlines the integration of the Vosk speech recognition toolkit within the KITT Framework for offline voice processing.

## Overview

Vosk is an offline open-source speech recognition toolkit that supports over 20 languages and dialects, including French. It provides portable per-language models, each approximately 50MB in size, making it suitable for mobile applications with hardware constraints.

## Integration Steps

1. **Dependency**: Add the Vosk library to the Android app's `build.gradle`:

   ```gradle
   implementation 'com.alphacephei:vosk-android:0.3.45'
   ```

2. **Model**: Download the appropriate language model (e.g., `vosk-model-small-fr-0.22` for French) and place it in the app's assets or internal storage.
3. **Initialization**: Initialize the Vosk recognizer with the model path in the `VoiceEngine` class.
4. **Audio Processing**: Use Android's `AudioRecord` to capture audio at 16kHz and process it through Vosk for real-time transcription.

## Performance Considerations

- **Latency**: Ensure end-to-end latency is under 200ms as per the monorepo rules. Vosk processes audio with approximately 0.2s latency on mid-tier devices.
- **Model Size**: Use compact models for mobile deployment to adhere to memory constraints (4GB RAM on target hardware).

## License Compliance

- Vosk is released under the Apache 2.0 license. Proper attribution must be provided in all distributions of the KITT Framework.

## Troubleshooting

- **Model Loading**: Verify the model path and ensure it matches the code configuration.
- **Audio Issues**: Confirm the audio sample rate is 16000Hz and the format is PCM 16-bit for compatibility with Vosk.

For detailed implementation, refer to the `VoiceEngine.kt` file in the Android app source code.
