@echo off
echo ========================================
echo Kashmir Meeqat App - Device Installer
echo ========================================

REM Check if APK exists
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ERROR: APK file not found!
    echo Please run build-app.bat first to build the app
    pause
    exit /b 1
)

echo Checking for connected Android devices...

REM Check if adb is available
where adb >nul 2>&1
if errorlevel 1 (
    if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
        set ADB_PATH=%ANDROID_HOME%\platform-tools\adb.exe
    ) else if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
    ) else (
        echo ERROR: ADB not found!
        echo Please install Android SDK platform-tools
        pause
        exit /b 1
    )
) else (
    set ADB_PATH=adb
)

REM List connected devices
echo Connected devices:
"%ADB_PATH%" devices

REM Check if any device is connected
"%ADB_PATH%" devices | find "device" | find /v "List" >nul
if errorlevel 1 (
    echo.
    echo ERROR: No Android device found!
    echo.
    echo Please make sure:
    echo 1. Your Android device is connected via USB
    echo 2. "Developer Options" is enabled on your device
    echo 3. "USB Debugging" is enabled in Developer Options
    echo 4. You have accepted the USB debugging prompt on your device
    echo.
    echo Alternatively, you can manually copy the APK file to your device:
    echo   File location: %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    pause
    exit /b 1
)

echo.
echo Installing Kashmir Meeqat app...
"%ADB_PATH%" install -r "app\build\outputs\apk\debug\app-debug.apk"

if errorlevel 0 (
    echo.
    echo ✅ SUCCESS: Kashmir Meeqat app installed successfully!
    echo.
    echo 📱 You can now find the app on your device as "Kashmir Meeqat"
    echo.
    echo First time setup:
    echo 1. Grant location permissions when prompted
    echo 2. Grant notification permissions when prompted
    echo 3. The app will automatically detect your location in Kashmir
    echo 4. Prayer times will be calculated and displayed
    echo.
) else (
    echo.
    echo ❌ Installation failed!
    echo Try enabling "Install apps from unknown sources" in your device settings
)

pause