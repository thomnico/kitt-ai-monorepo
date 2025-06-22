# KITT Framework - AI Models Integration

This directory contains the AI model integration layer for the KITT voice assistant framework, providing a common interface for Kyutai Moshi and Gemma models across platforms.

## Overview

The ai-models package serves as an abstraction layer for integrating on-device AI models used in the KITT framework. It supports Kyutai's Moshi model for voice interactions and Gemma models for response generation, optimized for mobile constraints through quantization and efficient memory management.

## Implementation Guidelines

- **Models**: Integrates Kyutai Moshi for voice processing and Gemma (2B parameter model with 4-bit quantization) for text generation.
- **Optimization**: Focuses on on-device execution with appropriate quantization to meet mobile storage (2-3GB for model files) and performance requirements.
- **Common Interface**: Provides a unified API to minimize platform-specific code in Android and iOS implementations.
- **Memory Management**: Implements efficient loading/unloading strategies to manage device resources.

## Development Steps

1. Define interfaces for voice processing and text generation that can be implemented by specific AI models.
2. Implement native bindings or wrappers for Kyutai Moshi and Gemma models.
3. Optimize model inference with threading and hardware acceleration (GPU on Android, Neural Engine on iOS).
4. Secure model files to prevent unauthorized access.
5. Create unit tests to validate model integration and performance.

For detailed implementation guidelines, security considerations, and performance optimizations, refer to the main documentation in the `docs/` directory at the root of the monorepo.
