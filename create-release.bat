@echo off
echo ================================================
echo Kashmir Meeqat App - Release Version Builder
echo ================================================

REM Check prerequisites
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Please install Java 11+ from https://adoptium.net/
    pause
    exit /b 1
)

echo [1/5] Creating keystore for app signing...

REM Create keystore if it doesn't exist
if not exist "kashmir-meeqat-keystore.jks" (
    echo Creating new keystore...
    keytool -genkey -v -keystore kashmir-meeqat-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias kashmir-meeqat ^
    -dname "CN=Kashmir Meeqat, OU=Kashmir Meeqat Team, O=Kashmir Islamic Apps, L=Srinagar, S=Jammu and Kashmir, C=IN" ^
    -storepass kashmirmeeqat123 -keypass kashmirmeeqat123
    
    if errorlevel 1 (
        echo ERROR: Failed to create keystore
        pause
        exit /b 1
    )
    echo ✅ Keystore created successfully
) else (
    echo ✅ Using existing keystore
)

echo [2/5] Setting up signing configuration...

REM Create signing config file
echo android.injected.signing.store.file=../kashmir-meeqat-keystore.jks > app\signing.properties
echo android.injected.signing.store.password=kashmirmeeqat123 >> app\signing.properties
echo android.injected.signing.key.alias=kashmir-meeqat >> app\signing.properties
echo android.injected.signing.key.password=kashmirmeeqat123 >> app\signing.properties

echo [3/5] Cleaning previous builds...
call gradlew.bat clean
if errorlevel 1 (
    echo ERROR: Clean failed
    pause
    exit /b 1
)

echo [4/5] Building signed release APK...
call gradlew.bat assembleRelease
if errorlevel 1 (
    echo ERROR: Release build failed
    echo Building debug version instead...
    call gradlew.bat assembleDebug
    if errorlevel 1 (
        echo ERROR: Debug build also failed
        pause
        exit /b 1
    )
    set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
    set APK_TYPE=debug
) else (
    set APK_PATH=app\build\outputs\apk\release\app-release-unsigned.apk
    set APK_TYPE=release
)

echo [5/5] Finalizing release...

REM Check if APK was created
if exist "%APK_PATH%" (
    echo.
    echo ✅ SUCCESS: %APK_TYPE% APK created!
    echo    Location: %CD%\%APK_PATH%
    echo.
    
    REM Create a timestamped copy
    for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c-%%a-%%b)
    for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
    set timestamp=%mydate%_%mytime: =0%
    
    copy "%APK_PATH%" "KashmirMeeqat_%timestamp%.apk"
    echo    Timestamped copy: KashmirMeeqat_%timestamp%.apk
    echo.
    echo 📱 Installation options:
    echo    1. Run install-app.bat (for USB connected device)
    echo    2. Transfer APK file to phone and install manually
    echo    3. Share APK file with others for installation
    echo.
    echo 🔐 App signing info:
    echo    Keystore: kashmir-meeqat-keystore.jks
    echo    Password: kashmirmeeqat123
    echo    Keep this keystore safe for future updates!
    echo.
) else (
    echo ERROR: APK file not created
)

pause