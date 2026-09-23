# BPWatch — Blood Pressure & Wellness Estimates for Galaxy Watch Ultra + Pixel

A two-app system that works around Samsung's lock-in:

- **Watch app** (`:wear`) — runs on your Galaxy Watch Ultra. Measures heart
  rate with the watch's PPG sensor, estimates an experimental stress score,
  and sends results to your phone over the Wear OS Data Layer. Shows the
  latest BP estimate sent back from the phone. Supports an hourly background
  mode (inexact alarm + boot receiver) so estimates survive the UI dying.
- **Phone app** (`:mobile`) — runs on your Pixel. Receives watch readings,
  estimates blood pressure from **your own cuff calibration**, stores
  history, charts trends, publishes BP estimates back to Health Connect, and can **install/update the watch app
  over Wi-Fi debugging** (no PC needed) — including the full pairing-code
  flow with a from-scratch SPAKE2 implementation.

**This is not a medical device.** Never use estimates to diagnose, treat, or
adjust medication. Always confirm with a cuff.

## Recent Updates

### v2.08.00 (Current)
- **GitHub Actions CI/CD Compatibility**: Upgraded GitHub Actions Gradle workflow runner to Gradle 9.3.1 to ensure 100% compatibility with Android Gradle Plugin (AGP) 9.1.1.
- **Native Android SDK Environment**: Configured CI build runner to use GitHub's native pre-installed Android SDK toolchain and automated platform licensing, eliminating third-party action failures.
- **Automated SDK Component Downloads**: Configured `android.builder.sdkDownload=true` in `gradle.properties` for seamless platform/build-tools resolution.
- **Package and Key Consistency**: Guaranteed fixed `applicationId` (`com.fourgeailabs.bpwatch`) and deterministic debug keystore signing for continuous APK upgrades.

### v2.07.00
- **Automatic Battery-Saving Mode (< 20% Watch Battery)**: Implemented an intelligent power-saving mode on the Wear OS watch that automatically triggers when watch battery capacity falls below 20%.
- **Dynamic Sensor Polling Reduction**: Reduces high-frequency accelerometer sensor listening from `SENSOR_DELAY_UI` (60ms) to `SENSOR_DELAY_NORMAL` (200ms) and step sensor from `FASTEST` to `NORMAL`.
- **Extended Background Check Intervals**: Relaxes scheduled blood pressure check intervals to a minimum of 60 minutes (preserving daily morning checks) and relaxes periodic continuous heart rate recording from 10 minutes to 30 minutes.
- **Shortened Optical PPG Measurement Window**: Cuts active PPG sensor sampling duration from 30 seconds to 15 seconds, halving optical LED battery consumption during low battery states.
- **Throttled Live Mirroring & Batch Uploads**: Throttles live Bluetooth mirroring updates from 10 seconds to 30 seconds and extends continuous HR batch upload aggregation from 60 seconds to 180 seconds to dramatically cut radio wakeups.
- **Real-Time Battery Telemetry & Settings Integration**: Added a "Watch Battery & Power Saver" card in the Watch section of the Settings menu, showing real-time battery telemetry, saver status, and active power conservation parameters.
- **Wear OS Battery Broadcast Receiver**: Registered `BatteryStateReceiver` to dynamically detect battery drops, power-save mode changes, and recovery events, automatically re-arming background alarms to match the active power budget.

### v2.06.01
- **Visual Real-Time Sync Indicator & Compose Charts**: Implemented an animated multi-node visual progress indicator and custom Compose chart displaying real-time sensor data transmission from the wearable (Galaxy Watch) through the processing gateway into Samsung Health.
- **Dynamic Circular Progress Gauge & Telemetry Chart**: Real-time transmission progress arc, animated pipeline flow (Watch Sensors → BP Gateway → Samsung Health), and live telemetry sparkline tracking heart rate, blood pressure, latency, and posture packets.
- **Consolidated Watch Section Settings**: Relocated watch location (Left / Right wrist) and all calibration controls directly into the dedicated Watch section of the Settings menu.
- **Watch Calibrations Card**: Dedicated interface for sensor orientation bias calibration (3-axis accelerometer lateral bias inversion) and cuff blood pressure regression model state with one-tap access to calibration.
- **Interactive Telemetry Sync Testing**: One-tap trigger in the UI allowing users to test and verify real-time data flow directly to Samsung Health via Health Connect.

### v2.06.00
- **Sensor Orientation Bias Toggle**: Added an explicit Material 3 segmented toggle in the Settings menu (and Monitoring & Alerts) allowing users to select 'Left' or 'Right' wrist.
- **Orientation Bias Processing Adjustment**: Adjusts the 3-axis accelerometer and gravity sensor orientation bias in the data processing algorithms (negating the lateral mirror axis for right wrist), normalizing roll/tilt angles and ensuring accurate posture evaluation (sitting, reclining, standing, lying down).
- **Wear OS Bidirectional Synchronization**: Updates on either watch or phone propagate across both devices instantly via the Wearable Data Layer (`/bpwatch/wrist_set`).
- **Health Connect Integration**: Ensures exact clinical measurement location (`LEFT_WRIST` or `RIGHT_WRIST`) is accurately transmitted to Health Connect and Samsung Health.

### v2.05.00
- **Clinical Posture & Motion Detection**: Integrates real watch sensor telemetry (accelerometer, gravity, and step detector) to capture and report posture (sitting, standing, lying down, reclining) and motion context (walking) during blood pressure checks.
- **Wrist Calibration**: Added watch wrist location selection (Left wrist / Right wrist) in phone settings and onboarding prompt, synchronizing bidirectionally across watch and phone.
- **Enhanced Clinical Relevance for Samsung Health**: Maps real posture and measurement location to Android Health Connect `BloodPressureRecord` attributes (`bodyPosition` and `measurementLocation`), providing accurate clinical context when synced with Samsung Health.
- **Clinical Reporting in UI**: The Home BP hero card and History screen now display posture and measurement location alongside systolic/diastolic and heart rate metrics.
- **Database Schema Migration**: Implemented Room migration 6 → 7 to persist `bodyPosition`, `measurementLocation`, and `activity` with each reading.

## The honest technical picture

Samsung's on-watch blood-pressure feature is proprietary: it uses pulse-wave
analysis plus cuff calibration, and Samsung only enables it when the watch is
paired to a Samsung phone. Google restricts HRV sensor data to
system apps, and blood pressure isn't exposed to third-party apps
directly from the Galaxy Watch's sensors.

So BPWatch does the next-best honest thing — the same high-level approach as
Samsung's own feature, minus their proprietary algorithm:

1. **Heart rate is readable.** Any Wear OS app can read HR from the PPG
   sensor. The watch takes a 30-second resting measurement and averages it.
2. **You calibrate with a real cuff.** In the phone app you enter ≥3 cuff
   readings taken at the same time as watch HR measurements. The app fits a
   least-squares line mapping HR → systolic and HR → diastolic.
3. **Later readings are estimated** through that line. They are wellness
   estimates, not measurements — heart rate alone is a weak predictor of BP.

## What's in the phone app

- **Home** — latest estimate, heart rate, manual logging.
- **Calibrate** — cuff calibration points (≥3), least-squares fit.
- **History** — charts and past readings.
- **Watch** — update the watch app with one tap: the phone beams the bundled
  watch APK over Bluetooth and the watch installs it itself via
  PackageInstaller (no debugging, settings preserved). For first-time
  installs onto a fresh watch, the tab also has the Wi-Fi debugging
  installer: enter the watch IP/port, tap Test connection or
  Install/Update. Handles the pairing-code flow against the watch's TLS
  wireless-debugging port. A pure Kotlin ADB client (protocol framing, RSA
  auth, shell, sync push) with unit tests against a fake daemon lives in
  `mobile/…/adb/`.
- **Settings** — Health Connect connect flow, SDK-status diagnostics,
  body-profile section (height, weight, age, sex, BMI), app version.

Watch measurements auto-upload to the phone with a notification carrying the
BP estimate, heart rate, stress score and time; the phone pushes resting HR
back to the watch to calibrate the stress baseline.

## Project layout

```
bpwatch/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── wear/                          # Galaxy Watch app (Wear OS 3+, minSdk 30)
│   └── src/main/java/com/fourgeailabs/bpwatch/
│       ├── Link.kt                # Data Layer contract (keep in sync with mobile)
│       └── wear/
│           ├── MainActivity.kt    # Measure UI (Wear Compose)
│           ├── HeartRateMonitor.kt# PPG heart-rate via SensorManager
│           ├── StressEstimator.kt # Experimental 0–100 stress estimate
│           ├── DataLayer.kt       # Sends HR/stress to phone via MessageClient
│           ├── WatchListenerService.kt  # Receives estimates back
│           └── WatchState.kt
├── mobile/                        # Pixel companion app (minSdk 26)
│   └── src/main/java/com/fourgeailabs/bpwatch/
│       ├── Link.kt
│       ├── MainActivity.kt        # Bottom-nav host + disclaimer
│       ├── MainViewModel.kt
│       ├── BpRepository.kt
│       ├── adb/                   # Pure-Kotlin ADB client + SPAKE2 pairing
│       │   ├── AdbClient.kt AdbKey.kt AdbProtocol.kt AdbTls.kt
│       │   ├── PairingClient.kt WatchInstaller.kt
│       │   └── spake2/           # From-scratch SPAKE2 (BoringSSL transcript)
│       ├── calibration/           # CalibrationEngine (least-squares fit)
│       ├── data/                  # Room (Reading), CalibrationStore (DataStore)
│       ├── healthconnect/         # BP publishing to Health Connect
│       ├── notifications/         # Upload notification helper
│       ├── profile/               # Body-profile store
│       ├── wearable/              # PhoneListenerService (receives watch data)
│       └── ui/                    # Home / Calibrate / History / Settings / Watch
└── docs/
    └── wireless-debugging-pairing-brief.md
```

## Building

This project compiles headlessly with the Android command-line tools — no
Android Studio required:

```bash
export ANDROID_HOME=~/workspace/android-sdk
export JAVA_HOME=~/workspace/tools/jdk17
./gradlew :mobile:assembleDebug :wear:assembleDebug
```

Every phone build also rebuilds `:wear` and embeds the fresh watch APK as
`assets/bpwatch-wear.apk` (see the `bundleWearApk` task), so the Watch tab
always installs the latest build. Debug builds only; release signing is not
set up.

**Signing:** both modules sign debug builds with the pinned keystore in
`keystore/bpwatch-debug.keystore` (a debug key, safe to commit). CI runners
generate a fresh ephemeral debug key on every run — without the pinned key,
every build had a different signature, which forced an uninstall (wiping all
settings) on every update. With one stable key, updates install over the
top and all data is preserved. If BPWatch ever ships to the Play Store, swap
in a proper release key kept secret.

**Versioning:** proper semver `MAJOR.MINOR.PATCH` in `versionName`, with
`versionCode` incremented on every build. Both modules stay in sync; the
Settings footer reads `BuildConfig.VERSION_NAME` dynamically.

## Health Connect notes

- BP publishing goes through Health Connect
  (`1.1.0-alpha11`, compileSdk 35).
- On Android 16, requesting `WRITE_BLOOD_PRESSURE` alongside the read
  permissions can cause the system to cancel the whole permission request
  silently (no dialog, callback returns 0 granted). The Settings screen has a
  "Try read-only request" button to A/B this, plus a "Grant permissions
  manually" fallback that opens the platform per-app screen.
- The phone manifest declares the Health Connect `<queries>` package
  visibility block (required on API 30+ or the permission intent can't
  resolve), and the Settings card shows live SDK-status diagnostics
  (available / needs update / provider update required) so silent failures
  are visible.

## Prebuilt APKs

The `releases/` folder contains ready-to-install debug builds:

- `BPWatch-Phone-v1.11.3-debug.apk` — current phone app (embeds the watch APK;
  use the Watch tab to install/update the watch over Wi-Fi debugging)
- `BPWatch-Wear-v1.11.3-debug.apk` — current watch app, standalone
- `BPWatch-Phone-v9-debug.apk` / `BPWatch-Wear-GalaxyWatch-debug.apk` —
  legacy v9 builds

## Calibrating (do this first)

1. Sit quietly for 5 minutes, cuff on your arm, watch snug on the wrist.
2. On the watch, open BPWatch → **Measure**. Keep still for 30 seconds.
3. Take your cuff reading immediately and enter sys/dia in the phone app's
   Calibrate tab. The latest watch heart rate is shown there — tap refresh if
   needed — then **Save calibration point**.
4. Repeat at least **3 times**, ideally at different times of day (morning,
   evening, after light activity). More varied points = better fit.
5. Once calibrated, every watch measurement produces an estimate on the phone,
   which is also sent back to the watch display and written to Health Connect.

Recalibrate every few weeks, or when medication, fitness, or stress changes.

## Roadmap ideas

- Wear OS Tile + complication showing the last estimate at a glance
- Calibration quality score (R²) and outlier warnings
- CSV export of history for your doctor
