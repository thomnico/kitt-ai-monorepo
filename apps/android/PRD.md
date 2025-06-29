# KITT Android App - Product Requirements Document

**Document Version:** 1.0  
**Date:** June 22, 2025  
**Status:** DEV  
**Product Manager:** Nicolas Thomas  
**Android Lead:** Nicolas Thomas  
**Target Release:** Q1 2026

## Android Auto Integration

### Compatibility for Volkswagen Golf GTE
- **Target Models**: The app supports Android Auto for in-car display integration, including Volkswagen Golf GTE models from 2015 onwards equipped with the MIB2 infotainment system.
- **Remote Display Mode**: This feature allows projection of the Android Auto interface to the car's display and is supported on 2015+ Golf GTE models with compatible hardware.
- **Aftermarket Solutions**: For 2015 models without native Android Auto support (those with MIB1 systems), aftermarket head units or retrofitting with MIB2 systems may be considered, though these require significant hardware/software modifications.

### Design Guidelines for Driver Safety
- **Minimal Distraction**: The dashboard interface must prioritize simplicity, avoiding debugging text or unnecessary visual elements to minimize cognitive load. All critical interactions must be accessible via voice commands.
- **Voice-First Interaction**: Ensure all features can be operated hands-free, with voice as the primary input method. Every touch action must have a voice alternative.
- **Typography and Contrast**: Use sans-serif fonts with a minimum size of 24sp for readability. Implement high-contrast elements, especially in dark mode, to reduce glare during night driving.
- **Large Touch Targets**: Design touch targets to be at least 48x48dp to facilitate easy interaction without precise aiming, adhering to driver distraction guidelines.
- **Performance Optimization**: Optimize the app for low-latency responses (<200ms) to prevent driver frustration, ensuring smooth transitions and immediate feedback on actions.

## Executive Summary

The KITT Android App is a native Android application that brings conversational AI assistant capabilities to vehicles through Android Auto integration. Built for the Crosscall Core-Z5 and similar rugged devices, it leverages on-device AI models (Kyutai Moshi, Gemma) to provide natural voice interactions while maintaining offline-first functionality. "Offline-first" means relying on data local to the device with download and synchronization possible when a network is available.

**Key Features:**

- Full-duplex voice conversations with 200ms response latency
- Android Auto compliance for safe in-vehicle operation  
- Kitt must be a full assistant, can act on the device with Google assistant or independant using impaired features.
- GPU-accelerated AI inference on Adreno 643
- Offline-first with cloud sync capabilities
- French/English bilingual support
- The UI must be inspired by K2000 Kitt tv series for the voice and UI interaction, especially the thinking.

## 1. Product Overview

### 1.1 Purpose

Deliver a production-ready Android application that demonstrates the capabilities of the KITT Framework while serving as a reference implementation for developers building similar voice assistant applications.

### 1.2 Target Market

- **Primary**: Crosscall Core-Z5 users using android auto
- **Secondary**: Android Auto compatible vehicles and aftermarket systems
- **Tertiary**: Largest possible Android support

### 1.3 Success Criteria

- **Performance**: Achieve <200ms voice response latency on Crosscall Core-Z5
- **Compliance**: Pass Google Play Console car app quality review
- **User Experience**: 4.5+ star rating with positive voice interaction feedback
- **Technical**: 99% uptime for offline voice processing

## 2. Technical Specifications

### 2.1 Target Hardware: Crosscall Core-Z5

- **Processor**: Qualcomm QCM6490 (6nm)
  - 4x Cortex-A78 @ 2.4GHz  
  - 3x Cortex-A55 @ 1.9GHz
- **GPU**: Adreno 643 @ 812MHz (OpenCL 2.0, Vulkan 1.1)
- **RAM**: 4GB LPDDR4X
- **Storage**: 64GB UFS 2.1
- **Operating Temperature**: -25°C to +60°C

### 2.2 Android Platform Requirements

- **Minimum SDK**: API 28 (Android 9.0) for Android Auto support
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: Latest stable (API 35+)
- **Build Tools**: Android Studio Hedgehog or newer
- **NDK**: Required for AI model integration (C++ components)
- **Gradle Wrapper**: The Gradle wrapper version must not be changed or updated to ensure consistent builds across development environments. Use the version specified in `gradle/wrapper/gradle-wrapper.properties`.

### 2.3 AI Model Configuration

- **Voice Model**: Vosk (small, 4-bit quantized)
  - Size: ~50MB per language model
  - Framework: Vosk-Android, based on Kaldi
  - GPU Acceleration: N/A (CPU-based)
  - Languages: French/English via pre-trained models

- **Action Model**: Currently integrated directly into VoiceCommandProcessor.kt  
  - Awaiting a suitable on-device, offline-first alternative for Gemma.

## 3. Feature Requirements

### 3.1 Voice Interaction Features (P0)

#### 3.1.1 Wake Word Detection

- **Activation Phrases**: Customizable (default: "Hey Assistant")
- **Performance**: <500ms detection latency
- **Power**: Low-power always-on detection
- **Accuracy**: 95%+ detection rate, <1% false positives
- **Implementation**: TensorFlow Lite Micro on dedicated DSP if available

#### 3.1.2 Speech Recognition (ASR)

- **Engine**: Kyutai Moshi native ASR or Whisper.cpp fallback
- **Languages**: French, English with automatic detection
- **Latency**: <200ms from speech end to text
- **Accuracy**: 90%+ in quiet environments, 85%+ with road noise
- **Streaming**: Real-time partial results for responsiveness

#### 3.1.3 Natural Language Understanding

- **Intent Recognition**: Built into Moshi/Gemma models
- **Entity Extraction**: Location, time, media preferences
- **Context Awareness**: Multi-turn conversation memory
- **Action Mapping**: Convert intents to vehicle commands
- **Confidence Scoring**: Clarification requests for low confidence

#### 3.1.4 Text-to-Speech (TTS)

- **Engine**: Kyutai Moshi for full-duplex or Kokoro fallback
- **Quality**: Natural-sounding, expressive speech
- **Latency**: <100ms from text to audio start
- **Interruption**: Support for barge-in during TTS playback
- **Voice Customization**: Multiple voice personas

#### 3.1.5 End-of-Speech Detection

- **Detection Mechanism**: Accurately detect when a user's sentence or utterance is complete using voice engine capabilities (e.g., Vosk's `acceptWaveForm()` method)
- **Latency**: <200ms from speech end to detection of completion
- **User Experience**: Smooth transition from listening to processing or response mode upon detection
- **Accuracy**: 90%+ detection rate for complete sentences in supported languages
- **Performance Maintenance**: Implement periodic listener resets (e.g., every 2 minutes) to prevent freezing or performance degradation during extended use

#### 3.1.6 Full-Duplex Communication

- **Simultaneous I/O**: Speak and listen simultaneously via Moshi
- **Echo Cancellation**: Prevent feedback loops
- **Background Noise**: Adaptive noise suppression
- **Turn-Taking**: Natural conversation flow management
- **Overlap Handling**: Graceful interruption and resumption

### 3.2 Android Auto Integration (P0)

#### 3.2.1 App Category Compliance

- **Category**: Media/Communication hybrid
- **Templates**: Use Android for Cars App Library
- **Activities**: Minimal UI, voice-first interaction
- **Services**: CarAppService for Android Auto connection
- **Manifest**: Proper automotive declarations

#### 3.2.2 Driver Distraction Requirements

- **Loading Time**: <10 seconds app launch
- **Content Loading**: <10 seconds for any operation
- **Response Time**: <2 seconds for button interactions, targeting <200ms for voice responses
- **Visual Distraction**: Minimal on-screen elements, no debugging or extraneous text on the dashboard
- **Voice Priority**: All features must be accessible via voice, with voice as the primary interaction method to minimize visual attention

#### 3.2.3 Quality Guidelines

- **Contrast**: Meet Android Auto contrast requirements with high-contrast elements for day and night modes
- **Icons**: White icon sets for system colorization, ensuring visibility in all lighting conditions
- **Text Scrolling**: No automatically scrolling text to prevent distraction
- **Performance**: No freezing or stuttering, with optimized rendering for immediate response
- **Error Handling**: Graceful failure with voice feedback instead of visual error messages when possible

### 3.3 Offline-First Architecture (P0)

#### 3.3.1 Local Data Storage

- **Database**: SQLite with Room persistence library
- **Conversations**: Local conversation history (30 days)
- **User Preferences**: Voice settings, personalization
- **Cache**: Media metadata and frequently accessed data
- **Encryption**: AES-256 encryption for sensitive data

#### 3.3.2 Sync Protocol

- **Backend**: Supabase with PostgreSQL
- **Mechanism**: Background sync via WorkManager
- **Frequency**: Every 15 minutes when connected
- **Conflict Resolution**: Last-write-wins with user intervention
- **Offline Queue**: Queue actions for later sync

#### 3.3.3 Connectivity Handling

- **Network Detection**: Real-time connectivity monitoring
- **Graceful Degradation**: Full functionality offline
- **Sync Status**: Clear indication of sync state
- **Manual Sync**: User-initiated sync option
- **Data Usage**: Efficient delta sync to minimize data

### 3.4 Voice Command Categories (P1)

#### 3.4.1 Media Control

- **Music Playback**: Play, pause, skip, volume control
- **Source Selection**: Radio, Spotify, local media
- **Search**: Find songs, artists, playlists by voice
- **Recommendations**: Suggest content based on preferences
- **Integration**: Android Auto media APIs

#### 3.4.2 Navigation Commands

- **Destination**: "Navigate to [location]"
- **Route Options**: Fastest, scenic, avoid highways
- **Traffic Updates**: Real-time traffic information
- **Points of Interest**: Find gas stations, restaurants
- **Integration**: Google Maps intents

#### 3.4.3 Communication

- **Phone Calls**: Initiate calls via voice
- **Text Messages**: Read and respond to messages
- **Contacts**: Access contact list via voice
- **Hands-Free**: Complete hands-free operation
- **Privacy**: Optional conversation logging

#### 3.4.4 Vehicle Information

- **Diagnostics**: Fuel level, maintenance reminders
- **Climate Control**: Temperature, fan, A/C control
- **Trip Information**: Distance, time, efficiency
- **Settings**: Adjust vehicle preferences via voice
- **Safety**: Emergency commands and roadside assistance

### 3.5 Personalization Features (P1)

#### 3.5.1 Voice Profiles

- **Multi-User**: Support for multiple driver profiles
- **Recognition**: Voice-based user identification
- **Preferences**: Per-user settings and favorites
- **Privacy**: Isolated conversation histories
- **Quick Switch**: Fast profile switching via voice

#### 3.5.2 Learning & Adaptation

- **Usage Patterns**: Learn frequently used commands
- **Preferences**: Adapt to user communication style
- **Context**: Remember common destinations and preferences
- **Feedback**: Improve accuracy based on corrections
- **Privacy**: On-device learning without cloud dependency

## 4. Performance Requirements

### 4.1 Latency Targets

- **Wake Word**: <500ms detection
- **Speech Recognition**: <200ms processing
- **Intent Processing**: <100ms model inference
- **Text-to-Speech**: <100ms synthesis start
- **End-to-End**: <400ms total response time

### 4.2 Resource Utilization

- **CPU**: Average <30% utilization during conversation
- **GPU**: Peak 80% during AI inference, average <20%
- **RAM**: <2GB total app memory footprint
- **Storage**: <5GB including models and user data
- **Battery**: <5% drain per hour of continuous use

### 4.3 GPU Optimization (Adreno 643)

- **AI Models**: Use OpenCL/Vulkan compute shaders
- **Memory Management**: Efficient GPU memory allocation
- **Thermal Throttling**: Adaptive performance scaling
- **Power Efficiency**: GPU power management integration
- **Fallback**: CPU execution when GPU unavailable

## 5. User Experience Requirements

### 5.1 Voice Interaction Design

- **Natural Speech**: Conversational, not command-based
- **Error Recovery**: Clear error messages and retry options
- **Confirmation**: Verbal confirmation for important actions
- **Feedback**: Audio/visual feedback for system status
- **Accessibility**: Support for hearing-impaired users

### 5.2 Visual Interface (Minimal)

- **Launch Screen**: Simple logo and status indicator
- **Settings Panel**: Basic configuration options
- **Status Display**: Connection, sync, and model status
- **Emergency Mode**: Large buttons for emergency situations
- **Night Mode**: Dark theme for night driving

### 5.3 Setup & Onboarding

- **First Launch**: Voice-guided setup process
- **Permissions**: Request only necessary permissions
- **Model Download**: Background AI model installation
- **Voice Training**: Optional voice recognition training
- **Tutorial**: Interactive voice command examples

## 6. Technical Architecture

### 6.1 App Architecture

```bash
kitt-android/
├── app/                    # Main application module
├── core/                   # Core business logic
├── data/                   # Data layer (Room, sync)
├── domain/                 # Domain models and use cases
├── presentation/           # UI layer (minimal for voice-first)
├── ai-engine/              # AI model integration
├── voice-processing/       # Audio I/O and processing
├── automotive/             # Android Auto specific code
└── shared/                 # Shared utilities
```

### 6.2 Key Components

#### 6.2.1 AI Engine

- **Moshi Integration**: JNI wrapper for PyTorch/ONNX
- **Model Manager**: Load/unload models dynamically
- **Inference Pipeline**: Audio → AI → Response
- **GPU Acceleration**: OpenCL backend integration
- **Memory Pool**: Efficient memory management

#### 6.2.2 Voice Processing

- **Audio Capture**: 24kHz stereo recording
- **Preprocessing**: Noise reduction, echo cancellation
- **VAD**: Voice activity detection for wake words
- **Streaming**: Real-time audio processing
- **Audio Output**: High-quality TTS playback

#### 6.2.3 Automotive Integration

- **CarAppService**: Android Auto service implementation
- **Templates**: Use Car App Library templates
- **Media Session**: MediaBrowserService for media control
- **Navigation**: Integration with navigation apps
- **Phone Integration**: Hands-free calling support

#### 6.2.4 Data Sync

- **Local Database**: Room with SQLite
- **Sync Engine**: Bidirectional sync with Supabase
- **Conflict Resolution**: Automated and manual resolution
- **Offline Queue**: Persist actions for later sync
- **Encryption**: End-to-end encryption for sensitive data

## 7. Security & Privacy

### 7.1 Data Protection

- **Local Encryption**: AES-256 for local data
- **Transport Security**: TLS 1.3 for network communication
- **Key Management**: Android Keystore for key storage
- **Biometric Auth**: Optional fingerprint/face unlock
- **Data Minimization**: Collect only necessary data

### 7.2 Privacy Controls

- **Conversation Logging**: User-controlled logging
- **Data Retention**: Configurable retention periods
- **Data Export**: User data export capability
- **Data Deletion**: Complete data deletion option
- **Anonymous Usage**: Optional anonymous analytics

### 7.3 Automotive Security

- **CAN Bus Protection**: No direct vehicle bus access
- **Sandbox Execution**: Isolated AI model execution
- **Permission Model**: Minimal Android permissions
- **Update Security**: Secure model and app updates
- **Emergency Fallback**: Safe mode for system failures

## 8. Testing Requirements

### 8.1 Functional Testing

- **Voice Recognition**: Accuracy testing with various accents
- **AI Responses**: Quality and appropriateness testing
- **Android Auto**: Compliance testing with DHU simulator
- **Offline Mode**: Extended offline operation testing
- **Sync Testing**: Conflict resolution and data integrity

### 8.2 Performance Testing

- **Latency Testing**: Response time measurement
- **Load Testing**: Extended conversation sessions
- **Memory Testing**: Memory leak detection
- **GPU Testing**: Graphics performance profiling
- **Thermal Testing**: Performance under thermal stress

### 8.3 Compatibility Testing

- **Device Testing**: Various Android devices and versions
- **Vehicle Testing**: Different Android Auto head units
- **Language Testing**: French and English accuracy
- **Network Testing**: Various connectivity conditions
- **Integration Testing**: Third-party app integration

## 9. Deployment & Distribution

### 9.1 Google Play Store

- **App Category**: Auto & Vehicles
- **Target Audience**: 17+ (driving age)
- **Content Rating**: Everyone
- **Privacy Policy**: Comprehensive privacy policy
- **App Permissions**: Minimal required permissions

### 9.2 Development Builds

- **Internal Testing**: Alpha builds for team testing
- **Closed Testing**: Beta builds for select users
- **Open Testing**: Public beta for community feedback
- **Production**: Staged rollout for general availability
- **Emergency**: Fast-track process for critical fixes

### 9.3 Update Strategy

- **App Updates**: Monthly releases via Play Store
- **Model Updates**: Background model downloads
- **Feature Flags**: Server-controlled feature rollout
- **Rollback**: Quick rollback capability for issues
- **Analytics**: Update success rate monitoring

## 10. Legal & Compliance

### 10.1 Intellectual Property

- **KITT Trademark**: Avoid Knight Rider trademarks
- **Voice Rights**: Ensure TTS voice licensing
- **Model Licensing**: Comply with Kyutai/Gemma licenses
- **Patent Review**: Freedom to operate analysis
- **Attribution**: Proper open source attribution

### 10.2 Regulatory Compliance

- **GDPR**: European data protection compliance
- **CCPA**: California privacy law compliance
- **Automotive**: UN-R155 cybersecurity (if applicable)
- **Accessibility**: Android accessibility guidelines
- **Export Control**: AI model export restrictions

## 11. Success Metrics & KPIs

### 11.1 Technical Metrics

- **Voice Latency**: <200ms end-to-end response
- **Recognition Accuracy**: >90% in optimal conditions
- **Uptime**: 99.9% offline functionality
- **Crash Rate**: <0.1% crash rate
- **Performance**: Sustained performance without throttling

### 11.2 User Experience Metrics

- **User Satisfaction**: 4.5+ star Play Store rating
- **Engagement**: Average 30+ minutes daily usage
- **Retention**: 80% 7-day retention rate
- **Feature Adoption**: 70% usage of core voice features
- **Support Tickets**: <1% users requiring support

### 11.3 Business Metrics

- **Downloads**: 10K+ downloads in first 3 months
- **Active Users**: 5K+ monthly active users
- **Reviews**: 100+ positive reviews
- **Developer Interest**: 500+ GitHub stars
- **Partnership Inquiries**: 10+ automotive partnership inquiries

## 12. Timeline & Milestones

### 12.1 Phase 1: Core Development (Months 1-4)

- **Month 1**: Project setup, basic voice processing
- **Month 2**: Kyutai Moshi integration, GPU acceleration
- **Month 3**: Android Auto basic integration
- **Month 4**: Offline-first data architecture
- **Milestone**: Alpha build with core voice functionality

### 12.2 Phase 2: Feature Complete (Months 5-7)

- **Month 5**: Advanced voice commands, personalization
- **Month 6**: Sync implementation, conflict resolution
- **Month 7**: French language support, performance optimization
- **Milestone**: Beta build ready for testing

### 12.3 Phase 3: Production Ready (Months 8-9)

- **Month 8**: Comprehensive testing, bug fixes
- **Month 9**: Play Store submission, compliance review
- **Milestone**: Production release on Google Play

### 12.4 Phase 4: Iteration (Months 10-12)

- **Month 10**: User feedback integration
- **Month 11**: Advanced features, partnerships
- **Month 12**: Next version planning
- **Milestone**: Stable user base and feature expansion

## 13. Dependencies & Risks

### 13.1 External Dependencies

- **Vosk**: The vosk projec
- **Google**: Android Auto platform stability
- **Qualcomm**: Adreno GPU driver optimization
- **Crosscall**: Hardware partnership and testing
- **Supabase**: Backend platform reliability

### 13.2 Technical Risks

- **Model Performance**: AI models may not meet latency targets
- **GPU Drivers**: Adreno drivers may have limitations
- **Memory Constraints**: 4GB RAM may be insufficient
- **Thermal Throttling**: Performance degradation in hot vehicles
- **Compatibility**: Android Auto compliance challenges

### 13.3 Risk Mitigation

- **Model Fallbacks**: Multiple AI model options
- **Performance Testing**: Early and continuous testing
- **Hardware Alternatives**: Support multiple device types
- **Compliance Early**: Android Auto testing from day one
- **Memory Optimization**: Aggressive memory management

---

**Document Control:**

- **Next Review**: July 22, 2025
- **Approval Required**: Nicolas Thomas
- **Distribution**: Nicolas Thomas
