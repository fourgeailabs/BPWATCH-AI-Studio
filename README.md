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

### v2.07.14 (Current)
- **Resolved BP Check Frequency Loop**: Fixed an issue where config synchronization re-triggered alarm scheduling, ensuring watch BP checks strictly follow the configured interval (e.g. hourly).
- **Body Fat (BIA) Scan Electrode Fix**: Enabled automatic dual electrode contact detection when initiating BIA scans from watch or phone, allowing smooth 15-second body composition calculations.
- **Active Off-Body / Off-Wrist Detection**: Added active off-wrist sensing using hardware sensors and heuristics; automatically pauses BP checks, continuous HR monitoring, and alerts whenever the watch is taken off.
- **Sleep & Skin Temperature Data Sync**: Enhanced overnight sleep session tracking (`SleepTracker.kt`), skin temperature sensor readings (`SkinTempMonitor.kt`), and Health Connect integration so sleep and skin temp data populate reliably on phone & watch.
- **Version Code Synchronization**: Updated Wear OS watch app to `v2.5.2` (versionCode `38`) and companion mobile app to `v2.07.14` (versionCode `56`).
- **Wear OS Tiles for Body Fat & HRV**: Introduced dedicated Wear OS Tiles for Body Fat % (BIA) and Heart Rate Variability (HRV) on Galaxy Watch with one-tap action buttons to trigger measurements immediately.
- **Watch-Face Complications**: Added Watch-face complications for Body Fat % (`BiaComplicationService`) and HRV RMSSD (`HrvComplicationService`), supporting both Short Text and Ranged Value complication types for full watch face compatibility.
- **Real-Time Data Streaming & Updates**: Updated `ComplicationUpdater` to trigger real-time updates across all 5 complications and 3 tiles whenever new sensor measurements land.
- **Wear OS Module Version Bump**: Updated Wear OS module to `v2.5.1` (versionCode `37`) bundled within phone companion app `v2.07.13` (versionCode `55`).

### v2.07.12
- **Bundled Watch APK Update**: Updated bundled watch APK inside the phone companion app to Wear OS `v2.5.0` (`36`).
- **Seamless One-Tap Update Flow**: Ensured phone app correctly matches bundled watch APK version and streams the latest watch binary over Bluetooth or Wi-Fi.
- **Screen Wake & Auto-Launch**: Added automatic screen wake lock and auto-launch intent triggers on watch when remote BIA scans or BP checks are initiated from the phone.

### v2.07.11
- **Sleep, Body Fat (BIA), and HRV Engine Enhancements**: Comprehensive upgrade to Health Connect and local sensor data sync for Sleep sessions, Body Fat %, and HRV.
- **PPG Heart Rate Beat Interval HRV Fallback**: Computes beat-to-beat RMSSD (ms) directly from raw PPG heart rate sample arrays whenever standalone HRV records are missing in Health Connect or Samsung Health.
- **Personalized Wear OS BIA Calculation**: Synchronizes user height and weight from phone profile to watch `WatchSettings`, computing exact Fat-Free Mass (FFM), Fat Mass, Body Fat %, Skeletal Muscle Mass, total Body Water, and BMR on watch BIA scans.
- **Direct Log Sheet Metrics**: Added Body Fat %, Sleep duration, and HRV RMSSD options to the home screen `+Log` sheet for easy manual logging and immediate Health Connect + Room persistence.

### v2.07.10
- **BFI Finger Electrode Contact Fixes**: Fixed physical electrode touch detection on Samsung Galaxy Watch Ultra and Wear OS side button keys.
- **Side Key Event Interception**: Implemented activity-level `dispatchKeyEvent` catching `KEYCODE_HOME` (top key), `KEYCODE_BACK` (bottom key), `KEYCODE_STEM_1/2/3`, and navigation key events to detect finger contact on watch side buttons.
- **Bi-Directional Electrode State Sync**: Updated watch `DataLayer` state messages to transmit `KEY_BIA_TOP_BUTTON_TOUCHED` and `KEY_BIA_BOTTOM_BUTTON_TOUCHED` flags to the mobile app in real-time.
- **Hardware Sensor & Touch Gesture Integration**: Expanded BioActive / BIA vendor hardware sensor listeners and added press-and-hold touch gestures on watch UI electrode cards.

### v2.07.09
- **Enhanced Health Connect Synchronization for Skin Temperature, HRV, and Sleep**: Resolved data ingestion gaps across Samsung Health and third-party Health Connect providers.
- **Body Temperature Fallback & Baseline Evaluation**: Added `BodyTemperatureRecord` support and expanded `SkinTemperatureRecord` parsing to calculate temperature deltas from baseline records when explicit delta arrays are omitted by wearables.
- **Expanded Lookback & Query Resilience**: Expanded HRV RMSSD and Sleep session lookback windows (to 30 days and 48 hours respectively) with robust paginated query fallback handling.
- **Sleep Stage Mapping**: Included `STAGE_TYPE_SLEEPING` mapping for 7-night sleep overview cards, weekly sleep distribution, and hypnogram breakdown.

### v2.07.08
- **Interactive Measurement Posture Guide**: Added a dedicated **Measurement posture** card in Settings that launches an interactive 4-step tutorial overlay (`PostureTutorialDialog.kt`).
- **Optimal Ergonomics & Heart-Level Positioning**: Guides users through 5-minute seated rest, heart-level watch wrist alignment to prevent ~10 mmHg hydrostatic errors, complete stillness during optical checks, and matching cuff calibration posture.
- **Clinical Explanations & Do/Don't Lists**: Displays key clinical reasoning, Do vs. Don't lists, and progress tracking for every posture step.

### v2.07.07
- **Strict Hardware Electrode Contact Sensing**: Made electrode status indicators read-only indicators driven purely by physical sensor contact on watch side buttons. Removed manual simulation touch buttons.
- **Auto-Pause on Contact Loss**: When fingers lose contact with watch side buttons, the BIA scan progress automatically pauses immediately and prompts the user to place fingers back on the watch side buttons.
- **Real BIA Measurement Integration**: Eliminates random pseudo-scans when watch is off the body and saves actual bioimpedance composition metrics.
- **Moved Sensor Orientation Bias**: Sensor orientation bias setting is now exclusively located in the Watch app card in Settings and removed from Monitoring & Alerts and the main Settings hub.

### v2.07.06
- **Settings Home Card**: Renamed the settings customization section card to **Home** with a dedicated home icon (`Icons.Filled.Home`).
- **Home Customization View**: Tapping the **Home** card in Settings opens the customization interface where users can choose which cards appear on the Home screen and arrange their exact order.
- **System Back & Back Navigation Header**: Added a top Back arrow button and system `BackHandler` to return seamlessly to Settings.

### v2.07.05
- **Opened Trend Detail View**: Tapping any trend card now cleanly opens its dedicated full-screen detail view with independent scroll state, ensuring zero scroll jumping and seamless navigation.
- **Android Back Navigation & Top Back Arrow**: Pressing the Android system back button/gesture or tapping the top Back arrow immediately returns the user to the all-trends menu.
- **Removed Duplicate Trend List**: Eliminated the duplicate "Other Trends" card list from the bottom of opened trend screens, preventing any perception of card rearrangement.
- **Clinical Guidelines & Interpretation Cards**: Added comprehensive clinical guideline cards (AHA Blood Pressure classifications, Heart Rate zones, HRV RMSSD baselines, BIA ranges, Sleep duration targets, etc.) to every open trend.
- **Recorded History Log Table**: Added a timestamped data point log to every open trend displaying up to 15 recent recorded entries with exact values and units.

### v2.07.04
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
