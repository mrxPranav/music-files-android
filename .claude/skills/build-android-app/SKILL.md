---
name: build-android-app
description: Build the Music Files Android app from the terminal with the Gradle wrapper (.\gradlew.bat assembleDebug). Use when asked to build, compile, or assemble the Android app, or to check that it compiles.
---

# Build the Music Files Android app

Project root: `F:\claude_tutorial\remote\android`

## Steps

1. Open a terminal in the project root.
2. Run the debug build:

   ```bash
   .\gradlew.bat assembleDebug
   ```

3. The APK is written to `app\build\outputs\apk\debug\app-debug.apk`.

## Requirements on this machine

- JDK 17 (`JAVA_HOME` = `C:\Program Files\Java\jdk-17.0.10`).
- Android SDK at `C:\Users\Pranav\AppData\Local\Android\Sdk` (set in `local.properties` as `sdk.dir`). The build needs platform `android-35` and build-tools `35.0.0`. The first build installs build-tools 35.0.0 automatically if it is missing.
- The first build downloads Gradle 9.1.0 and the Android/Maven dependencies, so it can take several minutes. Later builds are faster.

## Notes

- The wrapper was generated with Gradle 9.1.0 and the Android Gradle Plugin is 8.13.0. Do not change one without the other.
- The first build can take several minutes. Run it in the background and wait rather than assuming it hung.
- If the build reports `SDK location not found`, check that `local.properties` points at the SDK above.
- To install on a connected phone with USB debugging, run `.\gradlew.bat installDebug` instead.
