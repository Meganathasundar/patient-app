# Health Patient App

Android app (Kotlin, Jetpack Compose, Firebase) with **Doctor** and **Patient** roles. Doctors add patients and view their daily form submissions in real time. Patients receive a daily 5 PM push notification to fill out a health form.

## Features

- **Authentication:** Firebase Auth (email/password).
- **Roles:** Doctor and Patient. Doctors register; patients are added by a doctor and then sign up with the same email.
- **Doctor:** Add patients by email and display name; view list of patients; tap a patient to see their **Daily Health Form** history in real time (Firestore snapshots).
- **Patient:** Daily form with Body Temperature (°C), Symptoms Description, and Pain Level (1–10). Submissions appear in the doctor’s app in real time.
- **Notifications:** Firebase Cloud Function runs daily at **5:00 PM** (configurable timezone), queries all users with role `patient`, and sends an FCM reminder to fill the form.

## Tech Stack

- **App:** Kotlin, Jetpack Compose, Material 3, MVVM.
- **Backend:** Firebase Auth, Firestore, FCM.
- **Functions:** Node.js/TypeScript, scheduled (Pub/Sub) at 17:00.

## Required software (run locally & simulate)

To build, run, and simulate the app on your machine you need:

### Android app

| Software | Purpose | Notes |
|--------|----------|------|
| **Android Studio** | IDE, SDK, emulator, Gradle | Recommended. Includes JDK, Android SDK, and device emulator. [Download](https://developer.android.com/studio). |
| **JDK 17** | Compile Kotlin/Android | Usually bundled with Android Studio. |
| **Android SDK** | Build & run the app | API level **26** (min) and **34** (compile/target). Install via Android Studio → SDK Manager. |
| **Android Emulator** or **physical device** | Run/simulate the app | Emulator: Android Studio → Device Manager → Create Virtual Device. USB debugging for a real device. |

**Minimum to run the app:** Android Studio + one Android Virtual Device (AVD) or a physical phone with USB debugging.

### Cloud Functions (optional, for 5 PM notifications)

| Software | Purpose |
|--------|----------|
| **Node.js 18** | Run and build the functions. [Download](https://nodejs.org/) or use `nvm`. |
| **npm** | Comes with Node.js. |
| **Firebase CLI** | Deploy and test functions. `npm install -g firebase-tools` then `firebase login`. |

### Summary

- **Only the Android app:** Install **Android Studio**, create an **emulator** (or use a device), open the project, sync Gradle, and run.
- **App + deploy scheduled reminders:** Also install **Node.js 18** and **Firebase CLI**, then deploy from the `functions` folder as in [Setup](#setup) below.

## Project Structure

```
patient-app/
├── app/                    # Android app
│   └── src/main/java/com/patientapp/health/
│       ├── data/           # Models, repositories, Firestore constants
│       ├── ui/             # auth, doctor, patient screens & ViewModels
│       ├── navigation/
│       └── fcm/            # FCM service
├── functions/              # Firebase Cloud Functions
│   └── src/index.ts        # sendDailyFormReminder (5 PM schedule)
├── firebase.json
├── firestore.rules
└── firestore.indexes.json
```

## Setup

### 1. Firebase project

1. Create a project in [Firebase Console](https://console.firebase.google.com).
2. Enable **Authentication** → Email/Password.
3. Create a **Firestore** database.
4. Add an **Android** app with package `com.patientapp.health` and download `google-services.json`.
5. Replace `app/google-services.json` with your file (the repo includes a placeholder).

### 2. Firestore indexes

Deploy indexes (or create them in the console when prompted by the first query):

```bash
firebase deploy --only firestore:indexes
```

### 3. Cloud Function (5 PM notifications)

1. Install dependencies and build:

```bash
cd functions
npm install
npm run build
```

2. Deploy:

```bash
cd ..
firebase deploy --only functions
```

3. To change the schedule or timezone, edit `functions/src/index.ts`:

- `"0 17 * * *"` = 17:00 daily (cron).
- `.timeZone("America/New_York")` — set to your desired timezone.

### 4. Android app

1. Open the project in Android Studio.
2. Sync Gradle and run on a device or emulator.

### 5. FCM (optional, for testing)

- For device testing, use **Firebase Console → Cloud Messaging** to send a test message, or rely on the scheduled function after 5 PM.
- Ensure the app has notification permission (Android 13+) and that the patient has opened the app at least once so their FCM token is stored in Firestore.

## Data model

- **users:** `email`, `role` (`"doctor"` | `"patient"`), `displayName`, `doctorId` (patients only), `fcmToken` (patients, for reminders).
- **pending_patients:** Used when a doctor adds a patient by email; document ID = normalized email. Patient claims the account by signing up with that email.
- **daily_forms:** `patientId`, `doctorId`, `bodyTemperature`, `symptomsDescription`, `painLevel`, `submittedAt`.

## Flow

1. **Doctor:** Registers (email/password, role Doctor), then adds patients by email and display name. Each patient gets a pending invite.
2. **Patient:** Signs up with the **same email** the doctor used; their Firestore `users` document is created with `doctorId`. They can then submit the daily form; the app updates their FCM token for the 5 PM reminder.
3. **5 PM:** Cloud Function runs, loads all patients’ FCM tokens from Firestore, and sends the “Daily Health Form” notification.
4. **Doctor:** Opens the app, sees the patient list, taps a patient, and sees form history in real time via Firestore listeners.

## Make executable apk
./gradlew assembleRelease

## Make executable debug apk 
./gradlew assembleDebug

## APK will be at below location
app/build/outputs/apk/debug/app-debug.apk