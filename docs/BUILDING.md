# Building ETHER

## Automated build (recommended)

Every push to `main` runs the Android build workflow.

1. Open [GitHub Actions](https://github.com/etheros999-sketch/ETHER-OS/actions).
2. Select the latest **Android build** run.
3. Wait for the build and unit tests to finish successfully.
4. Under **Artifacts**, download `ETHER-debug-apk`.
5. Extract the ZIP and locate `app-debug.apk`.

A debug APK is for testing, not a signed production release. Only install APKs you trust.

## Build locally

Requirements:
- JDK 17
- Android SDK with platform 35 installed
- Gradle 8.9

From the repository root, run:

```sh
gradle --no-daemon assembleDebug
gradle --no-daemon testDebugUnitTest
```

The debug APK should be created at:

`app/build/outputs/apk/debug/app-debug.apk`

## Current limitations

The first version is a UI scaffold. The AI provider, microphone speech recognition, speech output, OAuth connections, and business actions are not implemented yet. Do not treat the connection cards as connected services.
