# KITT Framework - iOS App

This directory contains the native iOS implementation of the KITT voice assistant framework, developed using Swift and Core ML for integration with Apple's AI frameworks.

## Overview

The iOS app integrates on-device AI models (Kyutai Moshi for voice interactions and Gemma for response generation) to provide an intelligent voice assistant experience inspired by Knight Rider. It utilizes Core ML for efficient model inference and leverages Apple's Neural Engine where available for optimal performance.

## Implementation Guidelines

- **Language**: Swift
- **AI Integration**: Native bindings for Kyutai Moshi and Core ML for Gemma models with quantization for mobile constraints.
- **Hardware Acceleration**: Utilize Apple's Neural Engine and Core ML for AI processing.
- **Lifecycle Management**: Properly handle iOS app lifecycle events to manage resources.

## Example Structure

```swift
class KittVoiceAssistant {
    private let moshiEngine: MoshiEngine
    private let gemmaModel: MLModel
    
    init() {
        // Initialize Kyutai through native bindings
        // Load Gemma model through Core ML
    }
    
    func processVoiceInput(audioData: Data) -> AsyncStream<AIResponse> {
        // Process with native voice frameworks
        // Generate responses on-device
    }
}
```

## Development Steps

1. Set up the iOS project structure and dependencies.
2. Implement native bindings for AI models using Core ML.
3. Develop voice input processing and response generation logic.
4. Optimize for performance and battery efficiency.
5. Integrate with the shared data-sync package for offline-first capabilities.

For detailed implementation guidelines and architecture decisions, refer to the main documentation in the `docs/` directory at the root of the monorepo.
