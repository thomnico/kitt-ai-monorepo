# Implementation and Validation Plan - KITT Android App

**Document Version:** 1.0  
**Date:** June 22, 2025  
**Author:** Cline (AI Assistant)  
**Status:** Draft  
**Target Validation Date:** Q3 2025

## Android Auto Integration Plan

### Compatibility for Volkswagen Golf GTE
- **Target Models**: Focus on Volkswagen Golf GTE models from 2015 onwards equipped with the MIB2 infotainment system for Android Auto integration.
- **Implementation Steps**:
  - Ensure the app uses the Android for Cars App Library to create a compatible interface for Android Auto.
  - Implement `CarAppService` to establish connection with Android Auto systems in supported vehicles.
  - Optimize the app for minimal UI interaction, prioritizing voice commands to comply with driver distraction guidelines.
  - Design the dashboard interface to exclude debugging text or unnecessary visual elements, ensuring a distraction-free experience.
  - Implement voice-first interaction, making all features accessible hands-free with voice as the primary input method.
  - Use high-contrast elements and sans-serif fonts (minimum 24sp) for readability, especially in dark mode for night driving.
  - Design large touch targets (at least 48x48dp) to facilitate easy interaction without precise aiming.
  - Optimize performance for low-latency responses (<200ms) to prevent driver frustration.
- **Remote Display Mode**: This feature allows projection of the Android Auto interface to the car's display and is supported on 2015+ Golf GTE models with compatible hardware (MIB2 system).
- **Aftermarket Solutions**: Note that for 2015 models without native Android Auto support (those with MIB1 systems), users may consider aftermarket head units or retrofitting with MIB2 systems, though these require significant modifications and are outside the scope of standard app support.

## 1. Purpose

This validation plan aims to confirm the feasibility of the KITT Android App's core functionality: 2-way voice interaction. The objective is to ensure the app can listen to user input, accurately transcribe speech to text, display the transcribed text on the screen, and provide a basic audible response. This initial validation is critical to establishing the project's viability, focusing on real speech-to-text capabilities without a backend.

## 2. Scope

This plan covers the minimal implementation of the KITT Android App, using Android's built-in `SpeechRecognizer` API for speech-to-text and `TextToSpeech` API for response output. It is a temporary solution to validate the concept, with the intent to replace these components with more advanced models like Kyutai Moshi as specified in the PRD for full performance optimization.

- **In Scope**: Audio capture, speech transcription, text display, basic response generation, and audio output.
- **Out of Scope**: Full integration with Kyutai Moshi or Gemma models, Android Auto compliance, advanced natural language understanding, and multi-language support.

## 3. Test Environment

- **Hardware**: Any Android device running API 28 (Android 9.0) or higher, with a functional microphone.
- **Software**: KITT Android App (version 1.0), built with Android Studio Hedgehog or newer.
- **Connectivity**: Offline mode, as per the offline-first principle of the KITT Framework. Internet may be used for initial speech recognition model download if required by Android's API, but testing must confirm functionality without active connectivity.

## 4. Test Scenarios

The following scenarios will be executed to validate the voice interaction flow:

### 4.1 Basic Speech Recognition and Response
- **Objective**: Verify that the app can transcribe simple user input and respond audibly, critical for Android Auto voice-first interaction.
- **Input**: User says a short, clear phrase like "Hello" or "Tell me the time."
- **Expected Output**:
  - The app indicates listening mode (via audio cue or minimal visual indicator) when activated.
  - The transcribed text (e.g., "Hello") is processed within 2-3 seconds after speech ends, with no debugging text displayed on the Android Auto dashboard.
  - The app responds audibly with "Do you want me to repeat?" immediately after transcription.
- **Environment**: Quiet setting to minimize background noise interference.

### 4.2 Speech Recognition with Background Noise
- **Objective**: Assess transcription accuracy under less ideal conditions.
- **Input**: User says a short phrase like "How are you?" with moderate background noise (e.g., fan, low-volume TV).
- **Expected Output**:
  - The app transcribes the speech with reasonable accuracy (>70% word match, acknowledging the lightweight engine's limitations).
  - The transcribed text is displayed within 2-3 seconds.
  - The app responds with "Do you want me to repeat?" regardless of transcription accuracy.
- **Environment**: Controlled noisy setting.

### 4.3 No Speech Input (Timeout)
- **Objective**: Ensure the app handles cases where no speech is detected.
- **Input**: User presses "Start Listening" but does not speak for 5-10 seconds.
- **Expected Output**:
  - The app displays "Listening..." initially.
  - After a timeout, the app displays an error message like "Speech timeout" or "No speech recognized."
  - The "Start Listening" button becomes enabled again for a new attempt.
- **Environment**: Quiet setting.

### 4.4 Permission Handling
- **Objective**: Verify that the app properly handles audio recording permissions.
- **Input**: User denies audio permission when prompted on first launch.
- **Expected Output**:
  - The app displays a toast message indicating "Audio permission denied."
  - The "Start Listening" button is disabled until permission is granted.
- **Environment**: Any setting, tested by revoking permission in device settings if already granted.

## 5. Success Criteria

The validation will be considered successful if the following criteria are met:

- **Transcription Functionality**: The app transcribes speech with reasonable accuracy (>70% word match for simple phrases in quiet conditions) using a real speech-to-text engine.
- **Response Generation**: The app consistently responds with "Do you want me to repeat?" after each transcription attempt, audible through the device's speaker.
- **Timing**: Transcription text is displayed within 2-3 seconds after speech ends (relaxed from the PRD's 200ms target for this initial lightweight implementation).
- **Error Handling**: The app gracefully handles errors (e.g., no speech, permission denied) by displaying appropriate messages and re-enabling the listening button.
- **Offline Capability**: The app functions without active internet connectivity, though initial model download may require internet if mandated by Android's API.

## 6. Limitations and Future Steps

- **Current Limitations**: This implementation uses Android's built-in `SpeechRecognizer`, which may not meet the PRD's latency target of <200ms or provide the accuracy of Kyutai Moshi. It serves as a proof-of-concept to validate feasibility.
- **Next Steps Post-Validation**:
  - Replace the current speech recognition with Kyutai Moshi integration using native bindings for enhanced performance and accuracy.
  - Implement advanced response generation with the Gemma model for natural language understanding.
  - Enhance voice interaction by implementing end-of-speech detection using Vosk's `acceptWaveForm()` method to accurately detect when a user's sentence is complete, ensuring smooth transitions from listening to response mode.
  - Implement a listener reset mechanism for Vosk to prevent freezing, resetting the recognizer after 2 minutes of continuous listening to maintain performance.
  - Test end-of-speech detection and listener reset features to validate accuracy (>90% detection rate) and performance stability during extended use.
  - Optimize for Crosscall Core-Z5 hardware constraints (4GB RAM, Adreno 643 GPU) using OpenCL for acceleration.
  - Expand functionality and UI to meet full Android Auto compliance as per PRD, focusing on a distraction-free dashboard without debugging text.
  - Implement Android Auto design guidelines, ensuring voice-first interaction, high-contrast visuals, and large touch targets for driver safety.

## 7. Validation Schedule

- **Preparation**: Set up the Android project and build the app (1-2 days).
- **Testing**: Execute test scenarios over 1 day, iterating based on immediate feedback.
- **Reporting**: Document results and update this plan if necessary (1 day).
- **Total Duration**: Approximately 3-4 days from setup to initial validation completion.

## 8. Reporting and Feedback

- **Results Documentation**: Record outcomes of each test scenario, noting transcription accuracy, timing, and any errors encountered.
- **Feedback Loop**: Share results with the development team for immediate adjustments if success criteria are not met.
- **Final Report**: Summarize findings in an updated version of this document, indicating whether voice interaction feasibility is confirmed.

---

**Document Control:**

- **Next Review**: Upon completion of initial validation testing.
- **Approval Required**: Nicolas Thomas
- **Distribution**: Development Team
