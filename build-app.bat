@echo off
echo =========================================
echo Kashmir Meeqat App - Automated Builder
echo =========================================

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher from: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/4] Java found, checking Android SDK...

REM Check if Android SDK is available
if not exist "%ANDROID_HOME%\platforms" (
    if not exist "%LOCALAPPDATA%\Android\Sdk\platforms" (
        echo ERROR: Android SDK not found!
        echo.
        echo Please install Android SDK:
        echo 1. Download Android Studio from: https://developer.android.com/studio
        echo 2. Install it and open it once to download SDK
        echo 3. Or download command line tools from: https://developer.android.com/studio#command-tools
        echo.
        echo Then set ANDROID_HOME environment variable to your SDK path
        echo Example: set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
        pause
        exit /b 1
    ) else (
        echo Found Android SDK at default location
        set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
    )
) else (
    echo Found Android SDK at: %ANDROID_HOME%
)

echo [2/4] Android SDK found, setting up permissions...

REM Make gradlew executable (on Windows this is automatic)
echo Gradle wrapper ready

echo [3/4] Building the app...

REM Clean and build the project
call gradlew.bat clean
if errorlevel 1 (
    echo ERROR: Clean failed
    pause
    exit /b 1
)

call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ERROR: Build failed
    echo.
    echo Common solutions:
    echo 1. Make sure you have Android SDK installed
    echo 2. Check your internet connection for downloading dependencies
    echo 3. Try running: gradlew.bat --refresh-dependencies clean assembleDebug
    pause
    exit /b 1
)

echo [4/4] Build completed!

REM Check if APK was created
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ✅ SUCCESS: APK file created at:
    echo    %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 📱 To install on your Android device:
    echo    1. Enable "Developer Options" and "USB Debugging" on your phone
    echo    2. Connect phone via USB
    echo    3. Run: install-app.bat
    echo.
    echo    Or manually copy app-debug.apk to your phone and install it
    echo.
) else (
    echo ERROR: APK file not found after build
)

pause