# Music Files (Android)

A simple Android app that lists every music file found in the phone's storage (via MediaStore).

## Features
- Requests audio read permission at runtime (`READ_MEDIA_AUDIO` on Android 13+, `READ_EXTERNAL_STORAGE` on older versions)
- Shows each song's title, artist and duration in a list, sorted by title
- Shows a message if permission is denied or no music is found

## Build and run
1. Install [Android Studio](https://developer.android.com/studio).
2. Open this `android` folder (File → Open). Studio will sync Gradle and create the Gradle wrapper automatically.
3. Press **Run** with a phone connected (USB debugging on) or an emulator with some audio files on it.

Requires minSdk 24 (Android 7.0), compiles against SDK 34, and JDK 17.

## Notes
- Songs are read from the shared storage MediaStore index, so files must be scanned by the system to appear. Copy music to the phone's Music/Download folder and it will show up.
- Only files flagged as music are listed (ringtones, notifications and recordings are excluded).
