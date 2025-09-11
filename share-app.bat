@echo off
echo =============================================
echo Kashmir Meeqat App - Sharing Helper
echo =============================================

echo Looking for APK files...

REM Find the most recent APK
for %%i in (KashmirMeeqat_*.apk) do (
    set LATEST_APK=%%i
    goto found
)

REM Check for standard APK locations
if exist "app\build\outputs\apk\release\app-release.apk" (
    set LATEST_APK=app\build\outputs\apk\release\app-release.apk
    goto found
)

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    set LATEST_APK=app\build\outputs\apk\debug\app-debug.apk
    goto found
)

echo ❌ No APK file found!
echo Please run build-app.bat or create-release.bat first
pause
exit /b 1

:found
echo ✅ Found APK: %LATEST_APK%

REM Get file size
for %%i in ("%LATEST_APK%") do set APK_SIZE=%%~zi
set /a APK_SIZE_MB=%APK_SIZE%/1048576

echo    File size: ~%APK_SIZE_MB% MB

echo.
echo 📤 Sharing options:
echo.
echo 1. EMAIL SHARING:
echo    - Attach the APK file to email
echo    - Recipients can download and install on their Android devices
echo.
echo 2. WHATSAPP/TELEGRAM:
echo    - Send APK as file attachment
echo    - Recipients tap to install (after enabling unknown sources)
echo.
echo 3. USB TRANSFER:
echo    - Copy APK to USB drive
echo    - Transfer to other devices manually
echo.
echo 4. GOOGLE DRIVE/CLOUD:
echo    - Upload APK to cloud storage
echo    - Share download link with others
echo.
echo 📱 Installation instructions for recipients:
echo.
echo    1. Download the APK file to Android device
echo    2. Go to Settings → Security → Enable "Unknown Sources"
echo    3. Tap the APK file to install
echo    4. Grant location and notification permissions when asked
echo    5. The app will automatically detect location and show prayer times
echo.
echo 🔗 APK Location: %CD%\%LATEST_APK%

echo.
echo Would you like to:
echo [1] Copy APK path to clipboard
echo [2] Open APK folder in Explorer
echo [3] Create shareable ZIP package
echo [4] Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" (
    echo %CD%\%LATEST_APK% | clip
    echo ✅ APK path copied to clipboard!
)

if "%choice%"=="2" (
    explorer /select,"%CD%\%LATEST_APK%"
    echo ✅ Opening APK folder...
)

if "%choice%"=="3" (
    echo Creating shareable package...
    
    REM Create a folder with app and instructions
    mkdir KashmirMeeqatApp 2>nul
    copy "%LATEST_APK%" KashmirMeeqatApp\
    copy "SETUP_INSTRUCTIONS.md" KashmirMeeqatApp\ 2>nul
    copy "README.md" KashmirMeeqatApp\ 2>nul
    
    REM Create installation guide for end users
    echo Installation Guide for Kashmir Meeqat App > KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo. >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo 1. Copy the APK file to your Android device >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo 2. On your device, go to Settings ^> Security >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo 3. Enable "Install from Unknown Sources" or "Allow from this source" >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo 4. Tap the APK file and select Install >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo 5. When app opens, allow Location and Notification permissions >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo 6. App will automatically detect your Kashmir location and show prayer times >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo. >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo Features: >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo - Accurate prayer times for all Kashmir cities >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo - Prayer time notifications >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo - Qibla direction compass >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo - Works offline after first setup >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo. >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo Minimum Android version: 7.0 >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    echo App size: ~%APK_SIZE_MB% MB >> KashmirMeeqatApp\INSTALL_GUIDE.txt
    
    echo ✅ Created KashmirMeeqatApp folder with APK and instructions
    echo You can now ZIP this folder and share it with others
    
    explorer KashmirMeeqatApp
)

echo.
pause