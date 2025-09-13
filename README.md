# Kashmir Meeqat - Prayer Times App for Kashmir

A native Android application that displays accurate prayer times for locations across Jammu & Kashmir, with Qibla direction compass and notification features.

## Features

- **Accurate Prayer Times**: Calculates prayer times using astronomical algorithms specifically calibrated for Kashmir region
- **Location Support**: 
  - Auto-detection of location within Kashmir
  - Manual selection from 21+ major Kashmir cities
- **Prayer Time Notifications**: Customizable notifications for each prayer time
- **Qibla Direction**: Built-in compass showing direction to Kaaba from current location
- **Offline Support**: Stores prayer times locally for offline access
- **Multiple Calculation Methods**: Supports various Islamic calculation methods

## Supported Locations

The app includes prayer times for major cities across J&K including:
- Srinagar, Jammu, Anantnag, Baramulla
- Sopore, Kupwara, Pulwama, Budgam
- Ganderbal, Kulgam, Shopian, Bandipora
- Kathua, Udhampur, Reasi, Rajouri
- Poonch, Doda, Kishtwar, Ramban, Samba

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK with API level 24+
- Kotlin support enabled

### Build Steps
1. Clone or download the project
2. Open in Android Studio
3. Sync project with Gradle files
4. Build and run on device/emulator

### Permissions Required
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: For auto-location detection
- `POST_NOTIFICATIONS`: For prayer time notifications
- `SCHEDULE_EXACT_ALARM`: For precise prayer time alerts

## Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Room Database**: Local storage for prayer times
- **WorkManager**: Reliable background notification scheduling
- **Material Design**: Modern Android UI components
- **Location Services**: Google Play Services for location detection

## Calculation Methods

The app supports multiple Islamic calculation methods:
- **Karachi** (University of Islamic Sciences) - Default for Kashmir
- **ISNA** (Islamic Society of North America)  
- **MWL** (Muslim World League)
- **Makkah** (Umm Al-Qura University)
- **Egypt** (Egyptian General Authority)
- **Kashmir Custom** - Optimized for local conditions

## Customization for Local Prayer Times

The app is designed to accept local prayer time adjustments. When local mosque/authority timings are provided, the calculation parameters can be fine-tuned to match exactly.

## Technical Details

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **Database**: Room (SQLite)
- **UI**: ViewBinding + Material Components

## Privacy

- Location data is used only for prayer time calculation
- No data is transmitted to external servers
- All prayer times calculated locally on device
- No user tracking or analytics

## Download APK

Latest APK builds are automatically generated via GitHub Actions and available in the [Releases](https://github.com/wasimqur11/jkmeeqat/releases) section or [Actions](https://github.com/wasimqur11/jkmeeqat/actions) artifacts.

## License

This app is developed for the Muslim community of Kashmir. Please use responsibly and verify prayer times with local Islamic authorities.