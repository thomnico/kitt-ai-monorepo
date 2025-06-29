# Android App Testing and Issue Investigation Workflow

This workflow provides a systematic approach to running the KITT Android app, collecting diagnostic information, and investigating issues through structured observation and user interaction.

## Workflow Overview

The goal is to establish a repeatable process for:

1. Running the Android app in a controlled manner
2. Collecting comprehensive diagnostic information
3. Monitoring for issues and anomalies
4. Asking targeted questions when problems are detected
5. Building a knowledge base of app behavior patterns

## Phase 1: Environment Setup and Initial Run

### 1.1 Prepare Monitoring Environment

```bash
# Clear previous logs and start fresh monitoring
adb logcat -c
adb logcat -v threadtime | grep com.kitt.android
```

### 1.2 Build, Install, and Launch App

```bash
cd apps/android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.kitt.android/.MainActivity
```

### 1.3 Establish Baseline Monitoring

- Monitor logcat output for normal startup patterns
- Note successful initialization sequences
- Document expected behavior patterns
- Record timing of key operations (model loading, UI rendering, etc.)

## Phase 2: Information Collection

### 2.1 Log Analysis Categories

Monitor and categorize the following types of log entries:

**Normal Operations:**

- App startup and initialization
- Voice model loading
- UI component initialization
- Permission requests and grants

**Warning Indicators:**

- Performance warnings
- Memory usage alerts
- Permission issues
- Configuration warnings

**Error Indicators:**

- Crashes and exceptions
- ANR (Application Not Responding) events
- Force stops
- Resource loading failures

### 2.2 Pattern Recognition

Look for patterns in:

- Timing of operations
- Sequence of events
- Resource usage
- Error frequencies
- User interaction responses

## Phase 3: Issue Detection and Investigation

### 3.1 Automated Issue Detection

Monitor for these specific indicators:

- Process kills or force stops
- Exception stack traces
- ANR timeouts
- Memory pressure warnings
- Permission denials

### 3.2 User Experience Correlation

When issues are detected, immediately ask the user:

**Timing Questions:**

- "What were you doing exactly when the issue occurred?"
- "How long had the app been running before this happened?"
- "Was this the first time you used the app today?"

**Action Questions:**

- "What button or feature were you trying to use?"
- "Were you switching between languages?"
- "Were you speaking to the app when it happened?"

**Context Questions:**

- "Did you notice any unusual behavior before the crash?"
- "Has this happened before under similar circumstances?"
- "Were there any other apps running at the same time?"

## Phase 4: Data Analysis and Documentation

### 4.1 Log Correlation

- Match user actions with log timestamps
- Identify causal relationships
- Document reproduction steps
- Note environmental factors

### 4.2 Pattern Documentation

Create records of:

- Common failure modes
- Successful operation patterns
- Performance benchmarks
- User behavior correlations

## Phase 5: Iterative Testing

### 5.1 Hypothesis Formation

Based on collected data:

- Form hypotheses about root causes
- Design specific tests to validate theories
- Create reproduction scenarios

### 5.2 Controlled Testing

- Test specific user actions
- Vary environmental conditions
- Monitor for consistent patterns
- Validate or refute hypotheses

## Implementation Guidelines

### When to Use This Workflow

- Investigating reported crashes or issues
- Establishing baseline app behavior
- Performance analysis and optimization
- User experience research
- Quality assurance testing

### Key Principles

1. **Observe First:** Always collect data before making assumptions
2. **Ask Specific Questions:** Target questions based on observed data
3. **Document Everything:** Maintain detailed records of findings
4. **Iterate Systematically:** Use findings to refine investigation approach
5. **Correlate User Actions:** Always connect technical data with user experience

### Success Metrics

- Clear identification of issue patterns
- Reproducible test cases
- User-correlated technical data
- Actionable insights for development
- Improved app stability and performance

## Tools and Commands Reference

### Essential ADB Commands

```bash
# Clear logs and start monitoring
adb logcat -c && adb logcat -v threadtime | grep com.kitt.android

# Check device status
adb devices

# Install app
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.kitt.android/.MainActivity

# Force stop app
adb shell am force-stop com.kitt.android

# Take screenshot
adb exec-out screencap -p > screenshot_$(date +%Y%m%d_%H%M%S).png
```

### Log Analysis Filters

```bash
# Filter for errors only
adb logcat -v threadtime | grep -E "(E/|F/)" | grep com.kitt.android

# Filter for specific components
adb logcat -v threadtime | grep -E "(VoiceEngine|MainActivity)" 

# Monitor memory and performance
adb logcat -v threadtime | grep -E "(ActivityManager|WindowManager)" | grep com.kitt.android
```

This workflow ensures systematic investigation of Android app issues while maintaining focus on user experience correlation and actionable insights.
