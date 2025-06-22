# Prompt for Cline Coding Extension: KITT Framework Monorepo Development

## Project Overview

Create a cross-platform monorepo for the KITT framework - an intelligent voice assistant inspired by Knight Rider that operates on-device using Kyutai and Gemma AI models [1][2][3]. The monorepo should support Android native, iOS native, and a Supabase backend with offline-first capabilities [4][5][6].

## Monorepo Structure

Set up a monorepo with the following structure:

```
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

This structure enables efficient code sharing while maintaining platform-specific implementations where needed [7][8][9].

## Core Requirements

### 1. Cross-Platform Support

- Implement native Android development using Kotlin for maximum performance and hardware access [10][11][12]
- Implement native iOS development using Swift and Core ML for Apple's AI frameworks [13][14][15]
- Create shared business logic and data models across platforms [16][17][18]

### 2. AI Model Integration

- Integrate Kyutai's Moshi model for voice interactions using native bindings [19][20][21]
- Integrate Gemma on-device models with proper quantization for mobile constraints [22][23][24]
- Develop a common interface layer to support both AI frameworks with minimal platform-specific code [23][25][26]

### 3. Offline-First Architecture

- Implement a local-first data approach using SQLite with proper sync mechanisms [10][11][27]
- Connect to Supabase backend for remote storage and synchronization [28][29][30]
- Build conflict resolution strategies with last-write-wins or custom merging logic [31][32][33]

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

Ensure the Android implementation leverages the GPU for AI acceleration through OpenCL/Vulkan and properly handles lifecycle events [26][34][23].

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

Utilize Core ML for efficient model inference and Apple's Neural Engine where available [15][35][36].

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

Implement background synchronization with proper network status monitoring and battery efficiency considerations [27][32][33].

## Development Workflow

1. Set up the monorepo using Yarn workspaces or Turborepo for dependency management [37][38][39]
2. Implement shared code packages first, then platform-specific applications [16][17][18]
3. Create unit and integration tests for each package [39][6][9]
4. Configure CI/CD pipelines for automated testing and deployment [40][37][38]

## AI Model Requirements

- Kyutai Moshi: Optimize for on-device execution with appropriate quantization [19][20][21]
- Gemma: Use 2B parameter model with 4-bit quantization for mobile constraints [22][23][24]
- Storage requirements: Reserve approximately 2-3GB for model files [24][23][19]
- Memory management: Implement efficient loading/unloading strategies [25][24][34]

## Offline-First Data Strategy

1. Use a local SQLite database as the primary source of truth [10][31][32]
2. Queue write operations when offline for later synchronization [28][32][33]
3. Implement optimistic UI updates that show pending changes [27][31][41]
4. Develop a background sync service that operates when the app is active [28][31][32]
5. Handle conflict resolution with configurable strategies [32][33][41]

## Security Considerations

- Implement proper authentication and data encryption [27][29][30]
- Secure AI model files to prevent unauthorized access [24][26][34]
- Handle sensitive voice data with proper privacy controls [19][21][42]

## Performance Optimization

- Optimize AI model inference with proper threading and hardware acceleration [22][23][26]
- Implement efficient data caching and prefetching strategies [10][27][32]
- Monitor and manage battery usage, especially during voice processing and sync operations [23][26][34]

## Documentation Requirements

Generate comprehensive documentation covering:

1. Architecture overview and design decisions [40][38][39]
2. Platform-specific implementation details [16][13][22] 
3. Offline synchronization strategies and conflict resolution [31][32][33]
4. AI model integration and optimization techniques [19][22][24]
5. Developer setup and contribution guidelines [6][9][18]

This monorepo should enable efficient development of a KITT-like assistant across platforms while maintaining performance, reliability, and a seamless user experience regardless of network connectivity [1][2][3].

Sources
[1] cline/cline - GitHub https://github.com/cline/cline
[2] Roo Code (prev. Roo Cline) gives you a whole dev team of ... - GitHub https://github.com/RooCodeInc/Roo-Code
[3] Home · cline/cline Wiki - GitHub https://github.com/cline/cline/wiki
[4] Cline - AI Autonomous Coding Agent for VS Code https://cline.bot
[5] Cline 项目使用与启动指南 https://blog.csdn.net/gitblog_00789/article/details/147058446
[6] Managing Multiple Mobile Apps in a Monorepo with Angular and Ionic https://www.bluesunrise.com/blog/monorepo/
[7] GitHub Codespaces lets you code in your browser without any setup https://thenextweb.com/news/github-codespace-lets-you-code-in-your-browser-without-any-setup
[8] Announcing the Public Beta of GitHub Copilot Extensions 🎉 - GitHub Changelog https://github.blog/changelog/2024-09-17-announcing-the-public-beta-of-github-copilot-extensions-%F0%9F%8E%89/
[9] GitHub - psalishol/rn-monorepo: A monorepo built for React Native with support for mobile, macOS, Windows, and web using TypeScript. The monorepo structure enables shared code, improved build performance, and consistent development practices. Includes linting and formatting tools for consistency. https://github.com/psalishol/rn-monorepo
[10] Building offline-first mobile apps with Supabase, Flutter and Brick https://supabase.com/blog/offline-first-flutter-apps
[11] Bringing offline-first to Supabase, the right way - PowerSync https://www.powersync.com/blog/bringing-offline-first-to-supabase
[12] Using Supabase offline #357 - GitHub https://github.com/orgs/supabase/discussions/357
[13] Machine Learning & AI - Apple Developer https://developer.apple.com/machine-learning/
[14] Updates to Apple's On-Device and Server Foundation Language ... https://machinelearning.apple.com/research/apple-foundation-models-2025-updates
[15] Why Native iOS Is the Future of AI-Driven Mobile Development https://www.designrush.com/news/ai-driven-mobile-development-in-2025-why-native-ios-solutions-lead-the-way
[16] React Native monorepo for every platform: Android & iOS https://mmazzarolo.com/blog/2021-09-18-running-react-native-everywhere-mobile/
[17] Monorepos and why to consider them for your team's iOS projects https://www.runway.team/blog/monorepos-and-why-to-consider-them-for-your-teams-ios-projects
[18] Building Scalable Multi-Platform Projects with Angular and Nx https://giancarlobuomprisco.com/angular/building-scalable-multi-platform-projects-with-angular-and-nx
[19] Moshi AI by Kyutai | Advanced Native Speech AI Model https://moshi-ai.com
[20] kyutai: open-science AI lab https://kyutai.org
[21] A Promising New Open-Source Multimodal AI Model - LinkedIn https://www.linkedin.com/pulse/kyutai-labs-introduces-moshi-promising-new-multimodal-robyn-le-sueur-ry9uf
[22] Development tools for Android | Google AI Edge | Google AI for Developers https://ai.google.dev/edge/litert/android/development
[23] How to Run Gemma 3n on Android ? - Apidog https://apidog.com/blog/gemma-3n-android/
[24] GitHub - eleven-day/gemma-chat-app: An Android chatbot app using Gemma 3 1B model with on-device inference https://github.com/eleven-day/gemma-chat-app
[25] Kyutai Moshi: Breakthrough In Live Voice Interaction https://www.youtube.com/watch?v=CXwDQOb_0cQ
[26] Running Machine Learning Models on Android Devices - Issue #9 https://newsletter.victordibia.com/p/running-machine-learning-models-on-android-devices-issue-9-1199608
[27] Offline-first React Native Apps with Expo, WatermelonDB, and ... https://supabase.com/blog/react-native-offline-first-watermelon-db
[28] How to Build Device Synchronization with Supabase | Bootstrapped https://hub.bootstrapped.app/feature/how-to-build-device-synchronization-with-supabase
[29] Integrate Supabase with Flutter: A Comprehensive Guide https://iconflux.com/blog/supabase-with-flutter
[30] ElectricSQL | Works With Supabase https://supabase.com/partners/integrations/electricsql
[31] Building an offline-first app with Flutter, Supabase, and Brick https://www.youtube.com/watch?v=Fo-3uaAclBc
[32] How to build an offline-first app with Expo, Supabase and ... - Morrow https://www.themorrow.digital/blog/building-an-offline-first-app-with-expo-supabase-and-watermelondb
[33] Flutter Tutorial: building an offline-first chat app with Supabase and ... https://www.powersync.com/blog/flutter-tutorial-building-an-offline-first-chat-app-with-supabase-and-powersync
[34] Unlocking On-Device AI: A Guide to Running Gemma 3n on Your Android Device https://medium.com/@Erik_Milosevic/unlocking-on-device-ai-a-guide-to-running-gemma-3n-on-your-android-device-c0f381340678
[35] Hands-On: Mobile AI with Gemma - iOS, Android - Reflections https://annjose.com/post/mobile-on-device-ai-hands-on-gemma/
[36] On-Device AI is the Next Big Thing for iOS Apps in 2025 - DianApps https://dianapps.com/blog/why-on-device-ai-is-the-next-big-thing-for-ios-apps/
[37] Monorepo vs Polyrepo: Which One Should You Choose in 2025? https://dev.to/md-afsar-mahmud/monorepo-vs-polyrepo-which-one-should-you-choose-in-2025-g77
[38] Monorepo Explained https://monorepo.tools
[39] Understanding the workspace structure - Efficient Monorepo Management with Turborepo Caching & CI/CD https://app.studyraid.com/en/read/12467/402935/understanding-the-workspace-structure
[40] Monorepo Tooling in 2025: A Comprehensive Guide - Wisp CMS https://www.wisp.blog/blog/monorepo-tooling-in-2025-a-comprehensive-guide
[41] Supabase - Mobile - Offline first - Sync remote Postgres DB to a local ... https://stackoverflow.com/questions/76174717/supabase-mobile-offline-first-sync-remote-postgres-db-to-a-local-sqlite-db
[42] [PDF] Kyutai unveils today the very first voice-enabled AI openly ... https://kyutai.org/cp_moshi.pdf
[43] How to build an offline-first app with Expo, Supabase and ... - daily.dev https://app.daily.dev/posts/how-to-build-an-offline-first-app-with-expo-supabase-and-watermelondb-bzazknoub
[44] Implementing Local-First Strategy in Flutter with Supabase - Reddit https://www.reddit.com/r/FlutterDev/comments/1fcgumr/implementing_localfirst_strategy_in_flutter_with/
[45] Integrating Machine Learning Models With iOS CoreML https://medium.com/tribalscale/integrating-machine-learning-models-with-ios-coreml-a930187c803d
[46] How to send object model from objective-C(In framework) to Swift (outside of framework)? https://stackoverflow.com/questions/41844984/how-to-send-object-model-from-objective-cin-framework-to-swift-outside-of-fra
[47] Deploy machine learning and AI models on-device with Core ML - WWDC24 - Videos - Apple Developer https://developer.apple.com/videos/play/wwdc2024/10161/
[48] How to Integrate Trained Models into iOS Apps Using Core ML https://www.willowtreeapps.com/craft/integrating-trained-models-into-your-ios-app-using-core-ml
[49] Master monorepos: The ultimate guide to managing npm & yarn https://www.linkedin.com/pulse/mastering-monorepos-guide-npmyarn-workspaces-frontend-varghese-chacko-ibogc
[50] Tutorial: How to share code between iOS, Android & Web using ... https://dev.to/brunolemos/tutorial-100-code-sharing-between-ios-android--web-using-react-native-web-andmonorepo-4pej
[51] brick_offline_first_with_supabase https://pub.dev/documentation/brick_offline_first_with_supabase/latest/
[52] PowerSync and Supabase for Local-First Data Management https://ignitecookbook.com/docs/recipes/LocalFirstDataWithPowerSync/
[53] teethliberty/Roo-Cline: Autonomous coding agent right in your IDE ... https://github.com/teethliberty/Roo-Cline
[54] Tried Both Appwrite and Supabase for an Offline-First App - Reddit https://www.reddit.com/r/FlutterDev/comments/1ij4ke0/tried_both_appwrite_and_supabase_for_an/
[55] Work with monorepos - Expo Documentation https://docs.expo.dev/guides/monorepos/
