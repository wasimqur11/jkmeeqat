# Kashmir Meeqat App - Complete Setup Guide

This guide will help you build and install the Kashmir Meeqat prayer times app on your Android device **without needing Android Studio**.

## 📋 Prerequisites

### 1. Install Java
- Download and install Java 11 or higher from: https://adoptium.net/
- Choose "Latest LTS Release" for Windows
- During installation, make sure "Add to PATH" is checked

### 2. Install Android SDK (Command Line Tools)
You have two options:

#### Option A: Full Android Studio (Easier)
1. Download Android Studio from: https://developer.android.com/studio
2. Install and run it once - it will automatically download the SDK
3. The SDK will be installed at: `C:\Users\[YourUsername]\AppData\Local\Android\Sdk`

#### Option B: Command Line Tools Only (Smaller download)
1. Download "Command line tools only" from: https://developer.android.com/studio#command-tools
2. Extract to: `C:\Android\cmdline-tools`
3. Run these commands to install required packages:
   ```
   cd C:\Android\cmdline-tools\bin
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   ```

### 3. Set Environment Variable
1. Open Windows Settings → System → About → Advanced System Settings
2. Click "Environment Variables"
3. Under "System Variables", click "New"
4. Variable name: `ANDROID_HOME`
5. Variable value: `C:\Users\[YourUsername]\AppData\Local\Android\Sdk` (or your SDK path)
6. Click OK and restart Command Prompt

## 🔨 Building the App

### Step 1: Open Command Prompt
1. Press `Win + R`, type `cmd`, press Enter
2. Navigate to the app folder:
   ```
   cd "D:\SoftwareDevelopment\claude-projects\accounting-app\kashmir-meeqat-app"
   ```

### Step 2: Build the App
Simply run the automated build script:
```
build-app.bat
```

This will:
- Check if Java and Android SDK are installed
- Download required dependencies
- Build the APK file
- Show you where the APK is located

The build process may take 5-15 minutes on first run as it downloads dependencies.

### Step 3: Install on Your Device

#### Automatic Installation (Recommended)
1. Enable "Developer Options" on your Android phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"

2. Connect your phone to computer via USB
3. Run: `install-app.bat`
4. Accept the USB debugging prompt on your phone
5. The app will be installed automatically

#### Manual Installation
1. Copy the APK file from: `app\build\outputs\apk\debug\app-debug.apk`
2. Transfer it to your Android device
3. Enable "Install from Unknown Sources" in your device settings
4. Tap the APK file to install

## 📱 First Time Setup

When you first open the Kashmir Meeqat app:

1. **Grant Permissions**: Allow location and notification access when prompted
2. **Location Detection**: The app will automatically detect your location in Kashmir
3. **Prayer Times**: Prayer times will be calculated and displayed immediately
4. **Notifications**: Prayer time alerts will be automatically scheduled

## ⚙️ App Features

### Main Screen
- Today's prayer times with countdown to next prayer
- Current location display
- Next prayer highlighted with time remaining

### Settings
- Switch between auto-location and manual location selection
- Choose from 20+ Kashmir cities
- Select Islamic calculation method
- Toggle notifications for each prayer

### Qibla Compass
- Real-time direction to Kaaba from your location
- Visual compass with calibration instructions
- Works with device's built-in sensors

## 🔧 Troubleshooting

### Build Issues
- **"Java not found"**: Install Java 11+ and make sure it's in your PATH
- **"Android SDK not found"**: Set ANDROID_HOME environment variable
- **"Build failed"**: Run `gradlew.bat clean` then `gradlew.bat assembleDebug`

### Installation Issues
- **"Device not found"**: Enable USB debugging and accept the prompt
- **"Installation failed"**: Enable "Unknown Sources" in device security settings
- **"App won't open"**: Make sure your device runs Android 7.0 or higher

### App Issues
- **"No location detected"**: Grant location permission and try again
- **"Prayer times wrong"**: Go to Settings and select your exact city
- **"No notifications"**: Check notification permissions in device settings

## 📧 Support

If you encounter any issues:
1. Make sure you have the latest Android version supported
2. Check that all permissions are granted
3. Try restarting the app and your device
4. For calculation adjustments, provide local prayer time charts for fine-tuning

## 🎯 Next Steps

Once the app is working, you can:
1. Share the APK with other family/community members
2. Provide local mosque prayer timings for more accurate calculations
3. Customize notification sounds and timing
4. Use the Qibla compass for prayer direction

The app works completely offline after first setup and doesn't require internet connection for daily use.