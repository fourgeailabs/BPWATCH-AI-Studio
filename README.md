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

### v2.07.04 (Current)
- **Interactive Trends Menu with Clickable Trend Cards**: Replaced the horizontal filter chips slider with a responsive grid of interactive cards representing all 14 wellness metrics (Blood Pressure, Heart Rate, Resting HR, HRV, Stress, Sleep, Steps, Distance, Calories, Weight, Body Fat, Skin Temp, Hydration, BMI).
- **Most Current Data on Every Trend Card**: Every trend card displays its latest recorded measurement value, units, status badge, and thematic accent icon directly on the card face.
- **Dedicated Trend Detail Views**: Tapping any card opens its detailed historical chart with multi-range time selectors (Hour, Day, Week, Month, Year, All), statistical aggregates (Min, Max, Average), and one-tap switching to other trends.
- **Unified Health Connect & Local Telemetry Reporting**: Integrated fallback and merging logic across Body Fat (BIA sensor scans + Health Connect), Resting HR, HRV, Skin Temperature, and Sleep so trends always display data regardless of source.
- **Hardened Historical Sleep Polling**: Fixed Health Connect sleep query boundaries to prevent future timestamp rejections and added 30-day fallback queries.
- **Streamlined Home Screen Layout**: Consolidated health telemetry into standard Material 3 tiles in the customizable home grid, removing redundant cards.

### v2.07.03
- **Determinate Loading Circle for Blood Pressure Measurements**: Replaced the indeterminate spinning animation with a smooth determinate circular progress indicator that completes from 0% to 100% over the 30-second measurement cycle, displaying live seconds countdown, completion percentage, and active heart rate.
- **Body Fat Index (BIA) Reader with Determinate Scan Circle**: Built an interactive Bioelectrical Impedance Analysis (BIA) body composition scanner with a determinate 15-second loading circle, real-time status feedback, and instant sync to Health Connect.
- **Home and Trends Quick Launchers**: Added instant access to the Body Fat scanner directly from the Home screen Body Fat health tile and the Trends graph view.
- **Comprehensive Health Connect Sensor Permissions**: Declared and registered full read and write permissions in `AndroidManifest.xml` and `HealthConnectManager.kt` for HRV, Resting Heart Rate, Body Fat, Skin Temperature, Basal Metabolic Rate, VO2 Max, Lean Body Mass, and Oxygen Saturation.

### v2.07.02
- **Snoring Audio Playback in Sleep History**: Implemented a comprehensive audio playback and management interface for detected snoring events directly within the Sleep History and Sleep Detail views.
- **Interactive Playback Controls & Waveform Scrubber**: Integrated audio playback with position slider, duration counters, play/pause controls, and pulsing audio wave indicator.
- **Share via Android Intent**: Integrated one-tap sharing using Android `ACTION_SEND` Intent and `FileProvider` to forward audio clips to email, messaging, or cloud storage.
- **Export to Downloads & Deletion Management**: Added one-tap export to the device's public `Downloads` directory and permanent file deletion with automatic database and storage pruning.
- **Enhanced History Screen**: Expanded the main History section with segmented tabs for Blood Pressure records and Sleep & Snoring audio history.

### v2.07.01
- **Real-Time HRV Dashboard Card**: Added a dedicated, Material 3-styled Heart Rate Variability (HRV) component on the Home dashboard displaying live RMSSD values from the Galaxy Watch optical sensor stream.
- **Autonomic Nervous System Recovery Insights**: Real-time evaluation of parasympathetic (rest & recovery) vs. sympathetic (stress & physical exertion) balance with instant clinical status interpretations.
- **Dynamic Waveform Canvas & Sparkline**: Integrated real-time sparkline graph visualizing continuous beat-to-beat variation trends and live streaming telemetry with a pulsing `LIVE` badge.

### v2.07.00
- **Configurable Sleep Tracking Window**: Added dedicated Sleep Schedule settings in the phone app (Bedtime & Wake-up times) to accurately schedule overnight sleep monitoring and snore detection.
- **Historical Sleep Sync via Health Connect**: Optimized multi-page historical sleep session queries from Health Connect, allowing seamless synchronization of historical sleep records from Samsung Health.
- **Snore Detection Audio Management**: Enabled in-app audio playback of recorded snore events, permanent deletion with database pruning, Android system share sheet forwarding, and file export to user-accessible Downloads folder.
- **Weight Check Reminders**: Introduced customizable Weight Check Reminders card in Settings with repeat alarms for everyday or specific days of the week.
- **Real-Time Skin Temperature & Multimodal Stress**: Integrated live Skin Temperature card on Home screen, trend charts in Trends screen, and incorporation of skin temp into stress calculations alongside HR and BP.
- **Heart Rate Variability (HRV RMSSD)**: Added real-time HRV monitoring and trend charting with local sensor and Health Connect data merging.
- **Body Fat Index & Advanced BMI**: Integrated body fat index metrics from Samsung Galaxy Watch BIA sensors to provide accurate BMI and body composition analytics.
- **Motion-Assisted Resting & Sleeping HR**: Combined accelerometer and gyroscope kinematics with optical PPG to detect true resting heart rate and sleeping heart rate.
- **Calibration Timestamping**: Added explicit date and time logging for all cuff calibration points to enhance calibration tracking over time.

### v2.06.01
- **Health Connect Skin Temperature Permissions**: Resolved permission grant issues for Skin Temperature on Android / Google Pixel 10 Pro XL and Samsung Health by declaring `READ_SKIN_TEMPERATURE` and `WRITE_SKIN_TEMPERATURE` permissions and integrating them with the Health Connect permission manager.
- **Customizable Battery-Saving Mode**: Added a battery-saving mode in the Watch section of the Settings menu allowing users to configure the trigger percentage (e.g. 15%, 20%, 25%, 30%, 40%) or toggle it on/off.
- **Dynamic Sensor Polling Reduction**: When watch battery drops below the configured percentage, sensor polling frequency, accelerometer sampling delay (`SENSOR_DELAY_UI` -> `SENSOR_DELAY_NORMAL`), PPG sampling duration, and Bluetooth telemetry mirroring are automatically throttled to preserve battery.
- **Visual Real-Time Sync Indicator & Compose Charts**: Built an animated multi-node progress indicator and Compose chart displaying real-time sensor data transmission from the wearable (Galaxy Watch) through the processing gateway to Samsung Health and vice-versa.
- **Consolidated Watch Settings**: Located watch wrist location (Left / Right wrist selection) and calibration management inside the Watch section of the Settings menu.
- **Sensor Orientation Bias Toggle**: Select Left or Right wrist in Settings to adjust lateral sensor bias in data processing algorithms.
- **Sensor-Driven Posture & Body-Side BP Reporting**: Enhanced clinical accuracy of BP checks by detecting user posture (Sitting, Standing, Walking, Lying down, Reclining) and wrist location from watch sensors and reporting to Samsung Health via Health Connect.

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
