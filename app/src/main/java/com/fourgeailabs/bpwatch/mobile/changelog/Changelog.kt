package com.fourgeailabs.bpwatch.mobile.changelog

/**
 * BPWatch release history (v2.3, "What's new" screen).
 *
 * HOW TO ADD A RELEASE: add one [ChangelogEntry] at the TOP of [CHANGELOG]
 * (newest first) with the new versionName, versionCode, release date
 * (yyyy-MM-dd) and 1-4 short bullet notes. The device's installed version
 * (BuildConfig.VERSION_NAME) automatically gets the "Current" badge, so
 * nothing else needs changing.
 */
data class ChangelogEntry(
    val versionName: String,
    val versionCode: Int,
    /** Release date, ISO yyyy-MM-dd. */
    val date: String,
    val notes: List<String>,
)

/** Newest first. The full history starts at the first public version. */
val CHANGELOG: List<ChangelogEntry> = listOf(
    ChangelogEntry(
        versionName = "2.07.15",
        versionCode = 57,
        date = "2026-09-23",
        notes = listOf(
            "Removed BPWatch text title header from watch main screen for cleaner display",
            "Moved heart rate display to sit centered directly below blood pressure reading",
            "Adjusted layout spacing on Wear OS so action buttons sit comfortably below the top fold",
            "Updated Wear OS app to v2.5.3 (39) and Phone app to v2.07.15 (57)",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.14",
        versionCode = 56,
        date = "2026-09-23",
        notes = listOf(
            "Fixed BP check frequency issue — resolved config sync loop so watch checks adhere strictly to configured interval (e.g., hourly)",
            "Fixed Body Fat (BIA) index scan — auto-established dual electrode contact for seamless 15s body composition calculation",
            "Implemented active off-body/off-wrist detection — automatically pauses background BP/HR polling when watch is not being worn",
            "Restored Sleep and Skin Temperature data sync — enabled overnight sleep tracking, Health Connect integration, and skin temp telemetry",
            "Updated Wear OS app to v2.5.2 (38) and Phone app to v2.07.14 (56)",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.13",
        versionCode = 55,
        date = "2026-09-23",
        notes = listOf(
            "Added Wear OS Tiles for Body Fat (BIA) and Heart Rate Variability (HRV) with one-tap measurement triggers",
            "Added Watch-face complications for Body Fat % and HRV RMSSD (supporting Short Text and Ranged Value complication types)",
            "Updated Wear OS module to v2.5.1 (37) with real-time complication and tile streaming data updates",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.12",
        versionCode = 54,
        date = "2026-09-23",
        notes = listOf(
            "Updated bundled watch APK inside the phone companion app to Wear OS v2.5.0 (36)",
            "Ensured one-tap watch update seamlessly beams v2.5.0 (36) to Galaxy Watch with zero version conflicts",
            "Added automatic screen wake and auto-launch to Watch app when remote BIA body fat scans or BP checks are triggered from phone",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.11",
        versionCode = 53,
        date = "2026-09-23",
        notes = listOf(
            "Enhanced Sleep, Body Fat (BIA), and Heart Rate Variability (HRV) metrics processing and sync engine",
            "Added dynamic HRV RMSSD calculation from PPG heart rate beat intervals when standalone HRV records are absent in Health Connect",
            "Added dynamic user profile (Height & Weight) sync from phone companion to Wear OS watch for personalized BIA body fat composition calculations",
            "Expanded +Log sheet on mobile home screen with direct logging for Sleep, Body Fat %, and HRV RMSSD with Health Connect integration",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.10",
        versionCode = 52,
        date = "2026-09-23",
        notes = listOf(
            "Resolved Body Fat Index (BFI) finger electrode contact detection across Samsung Galaxy Watch Ultra and Wear OS side button keys",
            "Added comprehensive KEYCODE_HOME, KEYCODE_BACK, KEYCODE_STEM_1/2/3 key interceptors via dispatchKeyEvent to detect physical side electrode contact",
            "Expanded watch DataLayer payload to transmit live top and bottom button electrode touch states to the mobile app",
            "Added press-and-hold interactive electrode touch cards on the watch UI for touch sensing feedback",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.09",
        versionCode = 51,
        date = "2026-09-23",
        notes = listOf(
            "Enhanced Health Connect data porting for Skin Temperature, HRV, and Sleep across Samsung Health and third-party health providers",
            "Added BodyTemperatureRecord query and baseline/delta evaluation fallback for skin temperature telemetry",
            "Expanded HRV RMSSD and Sleep session lookback windows with robust paginated query fallbacks",
            "Included STAGE_TYPE_SLEEPING stage mapping in 7-night sleep overview and hypnogram breakdown",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.08",
        versionCode = 50,
        date = "2026-09-23",
        notes = listOf(
            "Added interactive Measurement Posture Guide in Settings to educate users on optimal body posture and heart-level wrist placement for accurate blood pressure checks",
            "Included step-by-step guidance on seated rest, hydrostatic pressure error reduction, stillness during PPG scans, and matching cuff calibration posture",
            "Added Do vs Don't clinical comparisons and progress indicators in the posture tutorial overlay",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.07",
        versionCode = 49,
        date = "2026-09-23",
        notes = listOf(
            "Enforced real hardware electrode touch sensing in Body Fat Index reader; eliminated manual touch toggle buttons",
            "Automatic scan pause/stop and warning prompt whenever fingers lose contact with top/bottom watch buttons",
            "Moved sensor orientation bias (wrist) exclusively into the Watch card settings screen and removed duplicates from Settings hub and Monitoring & Alerts",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.06",
        versionCode = 48,
        date = "2026-09-23",
        notes = listOf(
            "Renamed settings customization card to Home with a dedicated home icon",
            "Added BackHandler and top back navigation header to Home Customization screen",
            "Enhanced interactive card reordering and visibility switches for custom home screen layout",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.05",
        versionCode = 47,
        date = "2026-09-23",
        notes = listOf(
            "Opened trend detail view: tapping any trend card now cleanly opens its dedicated full-screen detail view with independent scroll state and zero-scroll jump",
            "Added Android system BackHandler and top Back arrow to immediately return to the all-trends menu from any opened trend",
            "Removed duplicate bottom trend list from detail view to eliminate card rearrangement perception",
            "Added Metric Guidelines and Clinical Reference Card to every open trend with health target ranges and AHA information",
            "Added Recorded History Log table listing individual timestamped data points for each open trend",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.04",
        versionCode = 46,
        date = "2026-09-23",
        notes = listOf(
            "Refactored Trends menu: replaced horizontal metric chip slider with full interactive cards that click into each individual trend",
            "Displayed the most current data point, unit, and status directly on each trend card in the Trends overview",
            "Added robust dual data reporting for Body Fat, Resting HR, HRV, and Skin Temperature combining local watch telemetry, BIA scans, and Health Connect series",
            "Hardened historical sleep data polling with safe time boundaries and fallback queries for Health Connect",
            "Consolidated Home Screen health layout into clean Material 3 metric grid, removing redundant standalone cards",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.03",
        versionCode = 45,
        date = "2026-09-23",
        notes = listOf(
            "Determinate loading circle for Blood Pressure checks: replaced indeterminate spinning graphic with a smooth loading circle that completes from 0% to 100% as the 30-second measurement progresses",
            "Added Body Fat Index (BIA) scanner with interactive determinate loading circle showing real-time scan progress (0% to 100%), elapsed time, and live status feedback",
            "Body Fat reader directly accessible from Home screen and Trends section with body composition insights and Health Connect sync",
            "Added comprehensive Health Connect read and write permissions in AndroidManifest and HealthConnectManager for HRV, Resting HR, Body Fat, Skin Temperature, Basal Metabolic Rate, VO2 Max, Lean Body Mass, and Oxygen Saturation",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.02",
        versionCode = 44,
        date = "2026-09-23",
        notes = listOf(
            "Integrated interactive audio playback interface for detected snoring recordings inside Sleep History and Sleep Detail views",
            "Added playback controls with audio scrub bar, real-time elapsed time tracking, and animated sound wave indicator",
            "Enabled seamless sharing of individual snoring recordings via Android system Share Intent with FileProvider audio streaming",
            "Added one-tap export to public Downloads folder and permanent deletion with storage and database cleanup",
            "Expanded general History screen with segmented tabs for Blood Pressure and Sleep Snoring audio recordings",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.01",
        versionCode = 43,
        date = "2026-09-23",
        notes = listOf(
            "Added real-time Heart Rate Variability (HRV RMSSD) dashboard card on the home screen matching the Material 3 design system",
            "Live streaming telemetry mirror from Galaxy Watch optical PPG sensors with pulsing LIVE status badge",
            "Real-time autonomic balance analysis (parasympathetic recovery vs. sympathetic strain interpretation)",
            "Dynamic sparkline waveform canvas showing continuous beat-to-beat variation trends",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.00",
        versionCode = 42,
        date = "2026-09-23",
        notes = listOf(
            "Configurable sleep schedule (bedtime and wake-up times) in dedicated Sleep settings for precise overnight tracking and snore window scheduling",
            "Enhanced Health Connect historical sleep session retrieval syncing seamlessly from Samsung Health",
            "Snore detection recording playback, permanent deletion, system share sheet, and export to Downloads folder",
            "Customizable weight check reminders card in Settings supporting daily or specific day-of-week repeat alerts",
            "Added live Skin Temperature card on Home screen and Trends chart, integrated into multimodal stress calculation",
            "Heart Rate Variability (HRV RMSSD) tracking with fallback merging of local telemetry and Health Connect series",
            "Body Fat Index integration from Galaxy Watch sensors and enhanced BMI calculations",
            "Motion-assisted resting and sleeping heart rate measurement combining accelerometer, gyroscope, and PPG sensors",
            "Accurate calibration history tracking with detailed date and time stamps for all cuff readings",
        ),
    ),
    ChangelogEntry(
        versionName = "2.06.01",
        versionCode = 41,
        date = "2026-09-23",
        notes = listOf(
            "Resolved Health Connect skin temperature permission granting on Google Pixel 10 Pro XL and Samsung Health",
            "Added customizable Battery Saving Mode in Watch Settings: user-defined trigger threshold (10%-50%) and live status indicator",
            "Automatic dynamic sensor polling throttling when watch battery drops below user threshold",
            "Real-time visual progress indicator and Compose chart tracking sensor telemetry synchronization between Wearable and Samsung Health",
            "Consolidated Watch Location (Left / Right wrist selection) and Calibration profiles exclusively inside the Watch section of the Settings menu",
            "Enhanced clinical BP check reporting with sensor-driven real-time posture detection (Sitting, Standing, Walking, Lying down) and body-side location logging",
        ),
    ),
    ChangelogEntry(
        versionName = "2.07.00",
        versionCode = 39,
        date = "2026-09-22",
        notes = listOf(
            "Implemented battery-saving mode reducing sensor polling frequency when watch battery drops below 20%",
            "Adjusts accelerometer sampling delay from UI mode (60ms) to NORMAL mode (200ms) under low battery",
            "Relaxes scheduled BP checks to ≥ 60 min and HR recording from 10 min to 30 min",
            "Reduces optical PPG sampling window from 30s to 15s to halve active optical sensor current",
            "Throttles Bluetooth live telemetry mirroring from 10s to 30s to minimize radio wakeups",
            "Extends continuous HR upload averaging window from 60s to 180s",
            "Added Watch Battery & Power Saver card in the Watch section of settings with live telemetry",
            "Integrated BatteryStateReceiver on Wear OS to dynamically reschedule alarms on battery transitions",
        ),
    ),
    ChangelogEntry(
        versionName = "2.06.01",
        versionCode = 38,
        date = "2026-09-22",
        notes = listOf(
            "Created visual progress indicator & Compose chart for real-time sensor sync to Samsung Health",
            "Multi-node pipeline visualization (Watch Sensors -> BP Gateway -> Samsung Health)",
            "Dynamic circular progress gauge and real-time telemetry sparkline & wave chart",
            "Consolidated watch location (Left/Right wrist) and all calibrations in the Watch settings section",
            "Added Watch Calibrations card managing sensor orientation bias and cuff regression model state",
            "Live interactive telemetry sync test with instant transmission feedback",
        ),
    ),
    ChangelogEntry(
        versionName = "2.06.00",
        versionCode = 37,
        date = "2026-09-22",
        notes = listOf(
            "Added explicit 'Left' vs 'Right' wrist toggle in the settings menu",
            "Adjusts 3-axis accelerometer sensor orientation bias in posture & motion processing algorithms",
            "Inverts lateral mirror bias for right wrist to accurately normalize roll and tilt kinematics",
            "Added reclining posture classification combining pitch angle and low variance detection",
            "Maintains full synchronization across Wear OS watch and phone via Wearable Data Layer",
        ),
    ),
    ChangelogEntry(
        versionName = "2.05.00",
        versionCode = 36,
        date = "2026-09-22",
        notes = listOf(
            "Accurate body posture detection (standing, walking, sitting, lying down) during BP checks",
            "Added wrist selection (left or right wrist) to calibrate body side location",
            "Enhanced clinical relevance with real sensor data logged directly to Health Connect & Samsung Health",
            "BP card & History view now report clinical posture and measurement location",
        ),
    ),
    ChangelogEntry(
        versionName = "2.4.6",
        versionCode = 35,
        date = "2026-09-22",
        notes = listOf(
            "BP card now shows your heart rate at the time of the reading",
            "New Sleep button on Home opens the full sleep detail view",
            "Sleep detail: score, stages chart, HR, breathing rate, skin temperature, snoring",
            "Tap any sleep factor for its full breakdown",
        ),
    ),
    ChangelogEntry(
        versionName = "2.4.5",
        versionCode = 34,
        date = "2026-09-22",
        notes = listOf(
            "Fixes the launch crash (initialization order bug in the dashboard refresh)",
        ),
    ),
    ChangelogEntry(
        versionName = "2.4.4",
        versionCode = 33,
        date = "2026-09-22",
        notes = listOf(
            "Fixes the phone app crashing at launch",
            "If a crash happens, the report now shows on next launch so it can be copied and sent for diagnosis",
        ),
    ),
    ChangelogEntry(
        versionName = "2.4.3",
        versionCode = 32,
        date = "2026-09-22",
        notes = listOf(
            "Watch screen stays on while the app is open",
        ),
    ),
    ChangelogEntry(
        versionName = "2.4.2",
        versionCode = 31,
        date = "2026-09-22",
        notes = listOf(
            "Pull down on Home to refresh all the tiles.",
            "Starting a BP check from the phone now shows the same " +
                "beating heart wrapped in the shifting colour ring as " +
                "the watch while it measures.",
            "Snoring gets a proper card on Home: last night's episode " +
                "count and total minutes, plus a 7-night bar chart.",
            "Calibrate moved off Home into Settings, in the Watch app " +
                "section where it belongs.",
            "The phone screen now stays on while a BP check is " +
                "measuring, so you can watch the heartbeat.",
            "New watch Tile: swipe to the BPWatch card for the latest " +
                "reading and the time it was taken; tap it to open the app.",
            "Watch-face complications now open the app when tapped, " +
                "and the BP complication carries the reading's time.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.4.1",
        versionCode = 30,
        date = "2026-09-21",
        notes = listOf(
            "Home screen decluttered: the readings timeline is gone from " +
                "Home — your full BP history lives on the History tab, " +
                "where it belongs.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.4.0",
        versionCode = 29,
        date = "2026-09-21",
        notes = listOf(
            "Settings redesigned: every section is now a clickable card that " +
                "opens its own screen — the Watch tab moved into Settings → " +
                "Watch app, and the bottom bar is back to Home, Trends, " +
                "History, Settings.",
            "One-tap watch updater: the watch now declares the install " +
                "permission it needs, the phone warns you upfront if " +
                "\"Install unknown apps\" isn't allowed for BPWatch on the " +
                "watch, and a signature mismatch fails fast with a clear " +
                "message instead of a cryptic installer error.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.3.3",
        versionCode = 28,
        date = "2026-09-21",
        notes = listOf(
            "Trends: every single graph now has its own labelled Refresh button — " +
                "heart rate, stress and blood pressure included, not just the " +
                "Health Connect ones. No more hunting for it.",
            "Home: removed the redundant Trends card at the bottom — the bottom " +
                "tab bar already takes you there.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.3.2",
        versionCode = 27,
        date = "2026-09-21",
        notes = listOf(
            "Trends: every Health Connect graph now has its own refresh button " +
                "with an \"updated at\" stamp, so a single trend can be re-pulled " +
                "without switching metrics.",
            "Settings → Samsung Health has a new \"Re-request permissions\" button " +
                "that re-fires the permission request even when everything looks " +
                "granted — for silently-revoked or stuck grants.",
            "Sleep: new \"Check sleep data\" diagnostic (Settings → Sleep, and in " +
                "the Trends sleep empty state) showing what Health Connect actually " +
                "holds — grant state, raw sessions, stage counts, which app wrote " +
                "them, and any read error.",
            "Back button: the system back button now walks back through screens " +
                "instead of closing the app.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.3.1",
        versionCode = 26,
        date = "2026-09-21",
        notes = listOf(
            "Steps stay fresh: the watch now pushes its step count every 15 minutes " +
                "on its own schedule (plus with every scheduled BP check), instead of " +
                "only on Bluetooth reconnect or while continuous recording is on.",
            "Steps match Samsung Health: when Health Connect is linked, the Home tile " +
                "prefers its merged phone-plus-watch step count over the watch-only number.",
            "Stress Trends finally shows history without continuous recording — the graph " +
                "now includes the stress score from every BP check alongside recorded samples.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.3.0",
        versionCode = 25,
        date = "2026-09-21",
        notes = listOf(
            "Home reshuffle: hydration and the discontinued blood-oxygen tiles " +
                "removed, new Stress 0-100 tile added; the phone can now " +
                "trigger a BP check on the watch (90-second timeout, retry).",
            "Health data: blood-oxygen sensing removed entirely; sleep duration now counts " +
                "sleep stages only; live heart-rate ticks persist for HR " +
                "Trends; the stress tile falls back to the newest timestamped value.",
            "New: opt-in phone-side snore detection (overnight microphone, " +
                "clips stay on the phone), an About screen, and this What's " +
                "new changelog. Fixed the Trends chart going blank when " +
                "switching metrics quickly.",
            "Watch side (companion build): watch-face complications, broader " +
                "Wear OS support, off-body pause with reliable BP intervals, " +
                "latest BP and heart rate on watch launch. Nothing in 2.3.0 " +
                "has run on real hardware yet: needs real-device validation.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.2.0",
        versionCode = 24,
        date = "2026-09-20",
        notes = listOf(
            "Full health coverage: new Health Connect reads (resting heart " +
                "rate, HRV, body fat and more) with permission hardening.",
            "New BMI tile on the Home grid; the watch reports its own step " +
                "count directly instead of waiting on Samsung's sync.",
            "Settings shows every Health Connect permission's real grant " +
                "status with per-permission re-request buttons.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.1.0",
        versionCode = 23,
        date = "2026-09-20",
        notes = listOf(
            "Every Home tile now taps through to its own Trends history view.",
            "New Hour range plus history graphs for steps, distance, " +
                "calories, weight, sleep and hydration.",
            "Heart-rate tile contrast and readability fix.",
        ),
    ),
    ChangelogEntry(
        versionName = "2.0.0",
        versionCode = 22,
        date = "2026-09-20",
        notes = listOf(
            "Google Health-style Home dashboard and hand-rolled Trends " +
                "graphs (heart rate, stress, blood pressure, blood oxygen).",
            "Continuous heart-rate and stress recording (opt-in): the watch " +
                "samples every 10 minutes and batches to the phone.",
            "Watch updater hardened: beamed APKs are SHA-256 verified " +
                "before install; real installer status codes reported.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.15.2",
        versionCode = 21,
        date = "2026-09-20",
        notes = listOf(
            "The bundled watch APK is now always re-extracted from assets, " +
                "fixing stale watch updates after phone app updates.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.15.1",
        versionCode = 20,
        date = "2026-09-20",
        notes = listOf(
            "Watch UI fixes and a one-tap updater bootstrap hint.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.15.0",
        versionCode = 19,
        date = "2026-09-20",
        notes = listOf(
            "One-tap watch updater: the phone beams the watch APK over " +
                "Bluetooth and the watch installs it itself.",
            "One shared signing key so updates preserve watch settings; " +
                "the phone re-pushes config, calibration and resting heart " +
                "rate on every connection.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.14.0",
        versionCode = 18,
        date = "2026-09-20",
        notes = listOf(
            "Google-colour shifting loader on the watch; alert severity " +
                "levels with per-severity cooldowns.",
            "Phone \"Watch live\" card with live heart-rate dot; extreme " +
                "alerts take over the phone screen with vibration.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.13.0",
        versionCode = 17,
        date = "2026-09-20",
        notes = listOf(
            "Monitoring and alerts: opt-in continuous heart-rate service, " +
                "high heart-rate alert, BP check intervals, high/low BP alerts.",
            "Watch alert screen with vibration; live heart-rate ticks and " +
                "alert mirroring to the phone over the Data Layer.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.12.0",
        versionCode = 16,
        date = "2026-09-20",
        notes = listOf(
            "Full Pixel-style Material 3 overhaul of the phone app plus a " +
                "Wear OS Material refresh on the watch.",
            "Settings footer now reads the version dynamically instead of " +
                "a hard-coded string.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.11.4",
        versionCode = 15,
        date = "2026-09-20",
        notes = listOf(
            "The actual Health Connect fix: permission-rationale entry " +
                "points declared in the manifest, so the system permission " +
                "dialog finally appears on Android 14+.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.11.3",
        versionCode = 14,
        date = "2026-09-20",
        notes = listOf(
            "Health Connect read-only permission A/B test button to " +
                "diagnose the silent permission failure.",
            "Fresh README and CI-built APKs published as release artifacts.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.11.2",
        versionCode = 13,
        date = "2026-09-20",
        notes = listOf(
            "\"Grant permissions manually\" fallback opening Health " +
                "Connect's per-app screen, plus a settings-screen fallback.",
        ),
    ),
    ChangelogEntry(
        versionName = "1.11.1",
        versionCode = 12,
        date = "2026-09-20",
        notes = listOf(
            "Semantic versioning adopted with a documented versioning " +
                "policy; mobile and watch modules stay in sync.",
            "Health Connect diagnostics: grant-count toasts and " +
                "per-permission status lines in Settings.",
        ),
    ),
    ChangelogEntry(
        versionName = "11.0.0",
        versionCode = 11,
        date = "2026-09-19",
        notes = listOf(
            "Health Connect connection fix attempt: manifest queries block " +
                "so the Health Connect intent resolves on Android 30+.",
        ),
    ),
    ChangelogEntry(
        versionName = "10.0.0",
        versionCode = 10,
        date = "2026-09-19",
        notes = listOf(
            "Experimental watch stress estimate (0-100) shown on the " +
                "watch; watch results auto-upload to the phone with a " +
                "notification carrying BP, heart rate, stress, blood oxygen and time.",
            "Resting heart rate pushed back to the watch as the stress " +
                "baseline; body-profile section (height, weight, age, sex, " +
                "BMI) added to Settings; Samsung Health connect flow reworked.",
        ),
    ),
    ChangelogEntry(
        versionName = "9.0.0",
        versionCode = 9,
        date = "2026-09-19",
        notes = listOf(
            "Initial public release: package renamed to " +
                "com.fourgeailabs.bpwatch; full source and APKs published " +
                "to GitHub.",
        ),
    ),
)
