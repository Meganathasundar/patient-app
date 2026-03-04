# AGENTS.md

## Cursor Cloud specific instructions

### Project overview

Health Patient Monitoring Android app (Kotlin, Jetpack Compose, Firebase). Two roles: Doctor and Patient. Includes Firebase Cloud Functions (Node.js/TypeScript) for scheduled push notifications. See `README.md` for full details.

### Environment variables

The following must be set (already configured in `~/.bashrc`):

- `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`
- `ANDROID_HOME=/opt/android-sdk`
- Both should be on `PATH`

### Building

- **Android app**: `./gradlew assembleDebug` from repo root. The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.
- **Cloud Functions**: `cd functions && npm run build`. Output in `functions/lib/`.

### Important caveats

- `google-services.json` lives at the repo root but must also exist at `app/google-services.json` for the Gradle build. The update script copies it automatically.
- The `./gradlew lint` task may fail with an internal `KotlinClassMetadata` compatibility error in `ComposableStateFlowValueDetector`. This is an AGP/lint tooling issue, not a code issue. The build (`assembleDebug`) is unaffected.
- There are no unit tests or instrumented tests in this project (`src/test/` and `src/androidTest/` are empty/absent).
- This is a mobile-only project (no web UI). The app cannot be run in the cloud VM since there is no Android emulator; the build is the primary verification step.
- Cloud Functions require Node.js 18+ and are optional for core app functionality.
