# Cline Prompts for KITT K2000 Android Application

## Main Project Prompt

Create an Android application that mimics the KITT K2000 car interface from Knight Rider with the following specifications:

**Core Requirements:**
- Native Android app using Java/Kotlin
- Custom views for KITT-style interface
- 6 side buttons with KITT styling (AIR, OIL, P1, P2, S1, S2)
- Oscillating red scanner lights (like KITT's voice box)
- Real-time audio spectrum analyzer with scope visualization
- Dark/black theme with red, yellow, and green accents
- Futuristic, retro-tech aesthetic

**Technical Implementation:**
- Use Android Studio with minimum SDK 21 (Android 5.0)
- Implement custom View classes extending View/ViewGroup
- AudioRecord for microphone input
- FFT processing for spectrum analysis
- Canvas drawing for custom graphics
- ValueAnimator for smooth animations
- Proper audio permission handling

**UI Layout:**
- Left side: Vertical panel with 6 custom buttons
- Center: Main display area with scanner and spectrum analyzer
- Top: "KNIGHT INDUSTRIES 2000" title
- Full-screen immersive experience
- Landscape orientation optimized

**Visual Design:**
- Black background (#000000)
- Red accent color (#FF0000) for primary elements
- Yellow (#FFFF00) and green (#00FF00) for secondary elements
- Monospace font family for text
- Rounded corners on buttons
- Glow effects on active elements
- LED-style visual feedback

## Sub-Prompts for Specific Components

### 1. Custom Button Component Prompt
Create a custom Android View class called KittButton that:
- Extends View class
- Has customizable text and color properties
- Implements glow animation when pressed/active
- Uses rounded rectangle shape with border
- Includes shadow effects for depth
- Responds to touch events with visual feedback
- Uses monospace bold font
- Has configurable button colors (red, yellow, green, blue, cyan, magenta)

### 2. Scanner Light Component Prompt
Create a KittScannerView custom component that:
- Displays a row of LED-like rectangles
- Implements oscillating animation from left to right and back
- Uses red color with intensity falloff around the active position
- Has configurable speed and LED count
- Can start/stop the scanning animation
- Uses Canvas drawing for performance
- Implements proper fade effects between LEDs

### 3. Spectrum Analyzer Component Prompt
Create a KittSpectrumView that:
- Captures audio from device microphone
- Performs FFT analysis on audio data
- Displays frequency bars in real-time
- Uses KITT color scheme (red to yellow gradient based on intensity)
- Has 32 frequency bars with smooth updates
- Implements proper audio permission checking
- Uses background thread for audio processing
- Handles AudioRecord lifecycle properly

### 4. Main Dashboard Layout Prompt
Create the main dashboard layout that:
- Combines all components in a cohesive interface
- Uses LinearLayout with proper weights
- Implements side button panel with 6 buttons
- Centers the scanner and spectrum analyzer
- Adds proper margins and spacing
- Includes title text at the top
- Sets up full-screen immersive mode
- Handles orientation changes

### 5. Audio Permission Handling Prompt
Implement comprehensive audio permission management:
- Request RECORD_AUDIO permission at runtime
- Show permission rationale dialog
- Handle permission denial gracefully
- Implement permission request callbacks
- Add proper error handling
- Include settings redirect for denied permissions

## Advanced Feature Prompts

### 6. Animation System Prompt
Create sophisticated animation system with:
- ValueAnimator for smooth transitions
- Synchronized animations between components
- Configurable animation speeds
- Performance optimized drawing
- Pause/resume animation capabilities

### 7. Sound Processing Enhancement Prompt
Enhance audio processing with:
- Windowing functions for better FFT results
- Frequency band grouping for better visualization
- Peak detection and hold functionality
- Noise filtering and gain control
- Multiple visualization modes

### 8. User Interface Polish Prompt
Add final polish with:
- Startup animation sequence
- Button press sound effects
- Voice synthesis integration (optional)
- Settings screen for customization
- Responsive design for different screen sizes

## Implementation Guidelines

**Code Structure:**
- Use proper package organization
- Implement clean architecture patterns
- Add comprehensive documentation
- Include error handling and logging
- Follow Android development best practices

**Performance Considerations:**
- Optimize custom view drawing
- Use efficient audio processing
- Implement proper lifecycle management
- Minimize memory allocations in draw loops
- Use hardware acceleration where possible

**Testing Requirements:**
- Test on multiple device sizes
- Verify audio permission flows
- Check performance on older devices
- Test landscape/portrait orientations
- Validate memory usage patterns