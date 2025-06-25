# Android Auto Testing Guide for Kitt AI

This guide provides detailed instructions on how to simulate an Android Auto
environment for developing and testing the Kitt AI app. By using the Android
Auto Desktop Head Unit (DHU), you can test your app's integration without
needing a physical vehicle.

## Prerequisites

- **Android Studio**: Ensure it is installed with the Android SDK.
- **Android Auto Support**: Verify that your app has Android Auto support
  implemented. This includes having the `KittCarAppService.kt` file, an updated
  `AndroidManifest.xml` with the car app service declaration, and the necessary
  dependency in `build.gradle`.

## Steps to Simulate Android Auto

### 1. Install the Android Auto Desktop Head Unit (DHU)

- Open Android Studio and go to **Tools > SDK Manager**.
- Navigate to the **SDK Tools** tab.
- Check the box for **Android Auto Desktop Head Unit** and click **Apply** to
  install it. This tool will be downloaded to your SDK directory.

### 2. Set Up an Android Device or Emulator

- Use either a physical Android device or an Android Virtual Device (AVD) emulator.
- Ensure the device or emulator runs Android 6.0 (API level 23) or higher.
- Install your Kitt AI app on the device or emulator via Android Studio by
  selecting **Run > Run 'app'** and choosing your target device.

### 3. Enable Developer Mode on the Device/Emulator

- On the device or emulator, go to **Settings > About phone**.
- Tap **Build number** seven times to enable Developer options.
- Go back to **Settings > System > Developer options** and enable **Stay awake**
  and **USB debugging** (for physical devices).

### 4. Start the Desktop Head Unit (DHU)

- Connect your physical device via USB to your computer (if using a physical device), or ensure your emulator is running.
- Open a terminal or command prompt.
- Navigate to the DHU location in your SDK directory, typically at `<SDK_PATH>/extras/google/auto/`.
- Run the following command to start the DHU:

  ```bash
  ./desktop-head-unit
  ```

- For a physical device, ensure USB debugging is enabled and accept the
  debugging prompt. For an emulator, the DHU should automatically detect it.

### 5. Connect the Device/Emulator to DHU

- On the device or emulator, open the **Android Auto** app (pre-installed or
  downloadable from the Google Play Store).
- Follow the prompts to connect to the DHU. You should see the Android Auto
  interface on your computer's screen via the DHU window.

### 6. Test Your App in the Simulated Environment

- Navigate through the Android Auto interface on the DHU to find your Kitt AI app.
- Test the app's functionality, focusing on voice interactions and the
  simplified UI defined in `KittCarAppService.kt`. Ensure voice commands
  trigger expected actions and the interface adheres to Android Auto's safety
  guidelines (minimal distraction, large touch targets).
- Use the DHU's controls to simulate different car states (e.g., parked,
  driving) to see how your app behaves under various conditions.

### 7. Debugging and Logs

- Use Android Studio's Logcat to monitor logs from both the device/emulator and
  the DHU for debugging purposes.
- Check for errors or warnings related to Android Auto integration to refine
  your app's implementation.

## Additional Notes

- The DHU simulates a car's infotainment system but may not replicate all
  hardware-specific behaviors. For final testing, consider using a real vehicle
  with Android Auto if possible.
- Ensure your app complies with Android Auto's design guidelines to prepare for
  submission to the Google Play Store, as Android Auto apps require a review
  process.

This guide should help you set up a simulation environment for developing and
testing your Android Auto integration with the Kitt AI app.

ref: <https://www.linkedin.com/pulse/how-test-android-auto-apple-carplay-without-car-set-up-niraj-subedi-wqsdc/>
