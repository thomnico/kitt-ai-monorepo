# Prompt for Cline Coding Extension: KITT Framework Monorepo Development

## Project Overview

Create a cross-platform monorepo for the KITT framework - an intelligent voice assistant inspired by Knight Rider that operates on-device using Kyutai and Gemma AI models. The monorepo should support Android native, iOS native, and a Supabase backend with offline-first capabilities.

## Monorepo Structure

Set up a monorepo with the following structure:

```plaintext
kitt-framework/
├── apps/
│   ├── android/        # Android native app
│   ├── ios/            # iOS native app
│   └── api/            # API functions
├── packages/
│   ├── shared/         # Cross-platform shared logic
│   ├── ai-models/      # AI model integration layer
│   ├── ui-components/  # Shared UI components (where applicable)
│   ├── data-sync/      # Offline-first data sync implementation
│   └── voice/          # Voice processing modules
├── tools/              # Build and development tools
├── .github/            # GitHub Actions for CI/CD
└── docs/               # Documentation
```

This structure enables efficient code sharing while maintaining platform-specific implementations where needed.

## Core Requirements

### 1. Cross-Platform Support

- Implement native Android development using Kotlin for maximum performance and hardware access
- Implement native iOS development using Swift and Core ML for Apple's AI frameworks
- Create shared business logic and data models across platforms

### 2. AI Model Integration

- Integrate Kyutai's Moshi model for voice interactions using native bindings
- Integrate Gemma on-device models with proper quantization for mobile constraints
- Develop a common interface layer to support both AI frameworks with minimal platform-specific code

### 3. Offline-First Architecture

- Implement a local-first data approach using SQLite with proper sync mechanisms
- Connect to Supabase backend for remote storage and synchronization
- Build conflict resolution strategies with last-write-wins or custom merging logic

## Implementation Guidelines

### Android Implementation

```kotlin
// Example code structure for Android native implementation
class KittVoiceAssistant(context: Context) {
    private val moshiEngine = MoshiEngine.initialize(context)
    private val gemmaLLM = GemmaLLM.initialize(context, modelPath)
    
    fun processVoiceInput(audioData: ByteArray): Flow<AIResponse> {
        // Process with Kyutai Moshi for voice recognition
        // Forward transcribed text to Gemma for response generation
    }
}
```

Ensure the Android implementation leverages the GPU for AI acceleration through OpenCL/Vulkan and properly handles lifecycle events.

### iOS Implementation

```swift
// Example code structure for iOS native implementation
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

Utilize Core ML for efficient model inference and Apple's Neural Engine where available.

### Data Synchronization

```typescript
// Example Supabase synchronization logic
class OfflineFirstSync {
    private localDB: SQLiteDatabase
    private supabaseClient: SupabaseClient
    
    async syncChanges() {
        // Identify local changes since last sync
        // Push changes to Supabase when online
        // Pull remote changes and resolve conflicts
    }
}
```

Implement background synchronization with proper network status monitoring and battery efficiency considerations.

## Development Workflow

1. Set up the monorepo using Yarn workspaces or Turborepo for dependency management
2. Implement shared code packages first, then platform-specific applications
3. Create unit and integration tests for each package
4. Configure CI/CD pipelines for automated testing and deployment

## AI Model Requirements

- Kyutai Moshi: Optimize for on-device execution with appropriate quantization
- Gemma: Use 2B parameter model with 4-bit quantization for mobile constraints
- Storage requirements: Reserve approximately 2-3GB for model files
- Memory management: Implement efficient loading/unloading strategies

## Offline-First Data Strategy

1. Use a local SQLite database as the primary source of truth
2. Queue write operations when offline for later synchronization
3. Implement optimistic UI updates that show pending changes
4. Develop a background sync service that operates when the app is active
5. Handle conflict resolution with configurable strategies

## Security Considerations

- Implement proper authentication and data encryption
- Secure AI model files to prevent unauthorized access
- Handle sensitive voice data with proper privacy controls

## Performance Optimization

- Optimize AI model inference with proper threading and hardware acceleration
- Implement efficient data caching and prefetching strategies
- Monitor and manage battery usage, especially during voice processing and sync operations

## Documentation Requirements

Generate comprehensive documentation covering:

1. Architecture overview and design decisions
2. Platform-specific implementation details
3. Offline synchronization strategies and conflict resolution
4. AI model integration and optimization techniques
5. Developer setup and contribution guidelines

This monorepo should enable efficient development of a KITT-like assistant across platforms while maintaining performance, reliability, and a seamless user experience regardless of network connectivity.

Sources
[1] cline/cline - GitHub <https://github.com/cline/cline>
[2] Roo Code (prev. Roo Cline) gives you a whole dev team of ... - GitHub <https://github.com/RooCodeInc/Roo-Code>
[4] Cline - AI Autonomous Coding Agent for VS Code <https://cline.bot>
[5] Cline 项目使用与启动指南 <https://blog.csdn.net/gitblog_00789/article/details/147058446>
[7] GitHub Codespaces lets you code in your browser without any setup <https://thenextweb.com/news/github-codespace-lets-you-code-in-your-browser-without-any-setup>
[8] Announcing the Public Beta of GitHub Copilot Extensions 🎉 - GitHub Changelog <https://github.blog/changelog/2024-09-17-announcing-the-public-beta-of-github-copilot-extensions-%F0%9F%8E%89/>
[10] Building offline-first mobile apps with Supabase, Flutter and Brick <https://supabase.com/blog/offline-first-flutter-apps>
[11] Bringing offline-first to Supabase, the right way - PowerSync <https://www.powersync.com/blog/bringing-offline-first-to-supabase>
[13] Machine Learning & AI - Apple Developer <https://developer.apple.com/machine-learning/>
[14] Updates to Apple's On-Device and Server Foundation Language ... <https://machinelearning.apple.com/research/apple-foundation-models-2025-updates>
[16] React Native monorepo for every platform: Android & iOS <https://mmazzarolo.com/blog/2021-09-18-running-react-native-everywhere-mobile/>
[17] Monorepos and why to consider them for your team's iOS projects <https://www.runway.team/blog/monorepos-and-why-to-consider-them-for-your-teams-ios-projects>
[19] Moshi AI by Kyutai | Advanced Native Speech AI Model <https://moshi-ai.com>
[20] kyutai: open-science AI lab <https://kyutai.org>
[22] Development tools for Android | Google AI Edge | Google AI for Developers <https://ai.google.dev/edge/litert/android/development>
[25] Kyutai Moshi: Breakthrough In Live Voice Interaction <https://www.youtube.com/watch?v=CXwDQOb_0cQ>
[28] How to Build Device Synchronization with Supabase | Bootstrapped <https://hub.bootstrapped.app/feature/how-to-build-device-synchronization-with-supabase>
[29] Integrate Supabase with Flutter: A Comprehensive Guide <https://iconflux.com/blog/supabase-with-flutter>
[31] Building an offline-first app with Flutter, Supabase, and Brick <https://www.youtube.com/watch?v=Fo-3uaAclBc>
[35] Hands-On: Mobile AI with Gemma - iOS, Android - Reflections <https://annjose.com/post/mobile-on-device-ai-hands-on-gemma/>
[37] Monorepo vs Polyrepo: Which One Should You Choose in 2025? <https://dev.to/md-afsar-mahmud/monorepo-vs-polyrepo-which-one-should-you-choose-in-2025-g77>
[38] Monorepo Explained <https://monorepo.tools>
[40] Monorepo Tooling in 2025: A Comprehensive Guide - Wisp CMS <https://www.wisp.blog/blog/monorepo-tooling-in-2025-a-comprehensive-guide>
