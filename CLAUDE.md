# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is the KITT Framework monorepo - a cross-platform voice assistant system inspired by Knight Rider. It uses on-device AI models (Kyutai Moshi for voice, Gemma for responses) with an offline-first architecture designed for rugged mobile devices.

## Critical Development Rules (From .clinerules)

### Performance First
- **200ms Rule**: All voice interactions MUST have end-to-end latency <200ms on target hardware (Crosscall Core-Z5)
- **Hardware Constraints**: Optimize for 4GB RAM, Adreno 643 GPU - performance is non-negotiable
- **Offline-First**: Every feature must work without internet connection - online is enhancement only

### Legal Compliance
- **Zero Knight Rider IP**: No references to "KITT," "Knight Industries," or show catchphrases - use generic terms
- **License Adherence**: Strict compliance with AI model licenses (Kyutai Moshi: CC-BY, Gemma: Apache 2.0)

### Monorepo Discipline
- **Sacred Structure**: Code stays in designated packages/apps - no cross-dependency violations
- **Trunk-Based Development**: Short-lived branches off main, merge within days maximum
- **Feature Flags**: Hide incomplete features behind flags, never long-lived branches

### Quality Gates
- **Test Everything**: All PRs must pass automated tests, linters, formatters - no exceptions
- **Selective Builds**: CI/CD only builds/tests affected components
- **Centralized Dependencies**: One root lockfile, internal packages as versioned dependencies

## Architecture

### Monorepo Structure

- **apps/android/**: Native Android app (Kotlin) with voice processing, Android Auto integration
- **apps/ios/**: Native iOS app (Swift) with Core ML integration
- **apps/api/**: Supabase Edge Functions for backend services
- **packages/**: Shared code across platforms (when implemented)
  - **shared/**: Cross-platform business logic
  - **ai-models/**: AI model integration layer
  - **data-sync/**: Offline-first sync with Supabase
  - **voice/**: Voice processing modules

### Key Technologies

- **Android**: Kotlin, Vosk speech recognition, Android Auto, GPU acceleration, NDK for native performance
- **iOS**: Swift, Core ML (planned for Phase 3)
- **Backend**: Supabase with offline-first SQLite sync
- **AI Models**: Kyutai Moshi (voice), Gemma (responses) with quantization for mobile
- **Target Hardware**: Crosscall Core-Z5 (4GB RAM, Adreno 643 GPU, rugged design)

## Development Commands

### Root Level (Yarn Workspaces)

```bash
yarn build          # Build all workspaces
yarn test           # Test all workspaces  
yarn lint           # Lint all workspaces
yarn start          # Start API workspace
```

### Android Development

```bash
cd apps/android
./gradlew build                    # Build Android app
./gradlew assembleDebug           # Build debug APK
./gradlew installDebug            # Install debug APK
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests
./gradlew clean                   # Clean build artifacts
```

### Android Auto Testing

The Android app includes Android Auto integration. Refer to `apps/android/ANDROID_AUTO_TESTING_GUIDE.md` for testing procedures.

## Current Implementation Status

### Android App (Primary Focus - Phase 2 Complete)

- **Voice Engine**: Vosk-based speech recognition with multiple language support (English, French)
- **UI Components**: KITT-style scanner view, dashboard, spectrum analyzer with real-time GPU/RAM monitoring
- **Voice Commands**: Processing and end-of-speech detection implemented with command routing
- **Android Auto**: Car integration service implemented for hands-free operation
- **Performance**: Optimized for <200ms response times, hardware-aware resource management
- **Integration**: Voice-to-dashboard communication, system status monitoring

### Phase 3 Roadmap (Next Implementation)

- **Enhanced Voice Processing**: Emotion recognition, contextual responses
- **Advanced UI**: Adaptive themes, accessibility improvements
- **Cross-Platform**: iOS app development with Core ML integration
- **Backend Services**: Supabase integration for data sync

### Key Android Classes

- `MainActivity.kt`: Main app interface with voice controls
- `VoiceEngine.kt`: Core voice processing with Vosk integration  
- `KittActivity.java`: KITT-style dashboard interface
- `KittCarAppService.kt`: Android Auto integration
- `VoiceCommandProcessor.kt`: Command parsing and handling

### Build Configuration

- **Target SDK**: 36 (Android 14+)
- **Min SDK**: 30 (Android 11+)
- **NDK**: Native library support for voice processing
- **Dependencies**: Vosk Android, JNA, Android Auto, Material Design

## Development Guidelines

### Code Style

- Follow existing Kotlin/Java conventions in Android app
- Use ViewBinding for Android UI components
- Implement proper lifecycle management for voice processing
- **Mandatory Performance**: <200ms voice response latency on Crosscall Core-Z5
- **Hardware Optimization**: 4GB RAM, Adreno 643 GPU constraints drive all decisions
- **Offline-First Design**: All features must work without network connectivity
- **PRD Compliance**: All development must align with project PRDs (check status: draft/dev/prod)

### PRD-Driven Development

- **Draft PRD**: Suggest enhancements, can modify with permission
- **Dev PRD**: Follow instructions first, PRD second - may use mock data for validation
- **Prod PRD**: Strict adherence, no modifications - ask for clarification when uncertain

### Voice Processing

- Models stored in `apps/android/app/src/main/assets/models/vosk/`
- Supports multiple languages (English, French configured)
- End-of-speech detection implemented for better UX
- GPU acceleration utilized where possible

### Testing

- Unit tests: `./gradlew test`
- Integration tests: `./gradlew connectedAndroidTest`
- Android Auto testing requires DHU (Desktop Head Unit)

### Performance Targets (Non-Negotiable)

- **Voice Response Latency**: <200ms end-to-end (primary success metric)
- **Memory Usage**: Optimized for 4GB RAM devices (Crosscall Core-Z5)
- **GPU Utilization**: Efficient Adreno 643 usage for AI model inference
- **Battery Efficiency**: Proper lifecycle management for rugged device deployment
- **Offline Operation**: 100% functionality without network connectivity

### Testing and Validation

- **Screenshot Analysis**: Use ADB/Android CLI for UI capture and validation
- **Hardware Testing**: Verify performance on target Crosscall Core-Z5 specifications
- **Integration Testing**: Voice-to-dashboard communication, Android Auto functionality
- **Performance Profiling**: Monitor GPU/RAM usage, response times under load

## Important Files

- **Core Documentation**:
  - `.clinerules/monorepo-coding.md`: Non-negotiable development rules
  - `apps/android/PRD.md`: Android app product requirements (Status: DEV)
  - `apps/android/plan.md`: Implementation roadmap and phase planning
  - `tools/CodingGuidelines.md`: Detailed coding standards and monorepo workflow
  - `research-decisions-ADR.md`: Architecture decision records

- **Implementation Guides**:
  - `apps/android/PHASE2_IMPLEMENTATION_SUMMARY.md`: Current phase completion status
  - `apps/android/PHASE3_INTEGRATION_PLAN.md`: Next development phase
  - `apps/android/ANDROID_AUTO_TESTING_GUIDE.md`: Car integration testing procedures

- **Configuration Files**:
  - Voice model configs: `apps/android/app/src/main/assets/models/vosk/config.json`
  - Build configuration: `apps/android/app/build.gradle`
  - `.gitignore`: File exclusion rules (must be respected in all analysis)

### File Management Rules

- **Respect .gitignore**: All analysis and automation must exclude files per .gitignore rules
- **Minimize File Creation**: Only create files when absolutely necessary for the goal
- **Prefer Editing**: Always edit existing files rather than creating new ones
- **No Proactive Documentation**: Never create .md or README files unless explicitly requested
