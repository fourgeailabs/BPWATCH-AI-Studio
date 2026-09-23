package com.fourgeailabs.bpwatch

/**
 * Data Layer contract shared by the watch and phone apps.
 * These two files must stay identical.
 */
object Link {
    const val PATH_HR_READING = "/bpwatch/hr_reading"
    const val PATH_BP_ESTIMATE = "/bpwatch/bp_estimate"
    const val PATH_CALIBRATION = "/bpwatch/calibration"
    const val PATH_MONITORING_CONFIG = "/bpwatch/monitoring_config"
    /**
     * Watch → phone: the user picked a new BP-check interval on the watch.
     * Payload is a DataMap with KEY_BP_INTERVAL_MIN. The phone persists it
     * so both sides stay in sync.
     */
    const val PATH_INTERVAL_SET = "/bpwatch/interval_set"
    /**
     * Phone ↔ watch: synchronize the wrist the watch is worn on.
     * Payload is a DataMap with KEY_WRIST ("left" | "right").
     */
    const val PATH_WRIST_SET = "/bpwatch/wrist_set"
    /**
     * Watch → phone: live heart-rate tick, throttled to ~10s while measuring
     * or continuously monitoring. Payload: KEY_HEART_RATE + KEY_TIMESTAMP.
     * Lets the phone mirror what the watch is showing, live.
     */
    const val PATH_HR_LIVE = "/bpwatch/hr_live"
    /**
     * Watch → phone: an alert fired on the watch. Payload: KEY_ALERT_TYPE,
     * KEY_SEVERITY, KEY_ALERT_TITLE, KEY_ALERT_MESSAGE, KEY_TIMESTAMP.
     * The phone mirrors it as a notification — or a full-screen takeover
     * for extreme readings.
     */
    const val PATH_ALERT = "/bpwatch/alert"
    /**
     * Watch → phone: battery state update. Payload: KEY_BATTERY_LEVEL (0..100)
     * and KEY_BATTERY_SAVER_ACTIVE (boolean).
     */
    const val PATH_BATTERY_STATE = "/bpwatch/battery_state"
    const val KEY_BATTERY_LEVEL = "battery_level"
    const val KEY_BATTERY_SAVER_ACTIVE = "battery_saver_active"
    const val KEY_BATTERY_SAVER_THRESHOLD = "battery_saver_threshold"
    const val KEY_BATTERY_SAVER_ENABLED = "battery_saver_enabled"


    // ------------------------------------------------------------------
    // Phone-triggered BP check (v2.3). The phone app can ask the watch to
    // take a measurement instead of waiting for the watch's own UI or its
    // schedule. The watch runs the same sampling routine its UI uses and
    // sends the HR reading on PATH_HR_READING; the estimate still flows
    // back on PATH_BP_ESTIMATE via the normal pipeline.
    // ------------------------------------------------------------------
    /**
     * Phone -> watch: "take a BP check now". Payload: KEY_TIMESTAMP (the
     * phone's request time, so the phone can match the result). The watch
     * replies on PATH_BP_RESULT ("started" or "failed").
     */
    const val PATH_BP_REQUEST = "/bpwatch/bp_request"
    /**
     * Watch -> phone: outcome of a PATH_BP_REQUEST. Payload: KEY_BP_RESULT
     * ("started" | "failed") + KEY_BP_MESSAGE for human-readable detail.
     * "started" means a measurement is running - the estimate arrives on
     * PATH_BP_ESTIMATE through the normal HR-reading pipeline.
     */
    const val PATH_BP_RESULT = "/bpwatch/bp_result"
    const val KEY_BP_RESULT = "bp_result"
    const val KEY_BP_MESSAGE = "bp_message"

    // ------------------------------------------------------------------
    // One-tap watch updater (v1.15+): the phone sends the bundled watch APK
    // over the Data Layer — no Wi-Fi debugging needed. The watch installs
    // the update itself via PackageInstaller (data is preserved).
    // ------------------------------------------------------------------
    /**
     * Phone → watch: "I have a watch update for you". Payload:
     * KEY_APK_VERSION_CODE (long) + KEY_APK_VERSION_NAME. The watch replies
     * on PATH_APK_READY so a 20 MB transfer only starts when needed.
     */
    const val PATH_APK_BEGIN = "/bpwatch/apk_begin"
    /** Watch → phone: reply to PATH_APK_BEGIN. Payload: KEY_APK_VERSION_CODE,
     * KEY_APK_VERSION_NAME (installed), KEY_APK_NEEDS_UPDATE (boolean). */
    const val PATH_APK_READY = "/bpwatch/apk_ready"
    /**
     * Phone → watch: the APK itself, as a DataItem carrying KEY_APK_ASSET
     * plus KEY_APK_VERSION_CODE/KEY_APK_VERSION_NAME/KEY_TIMESTAMP (the
     * timestamp forces the DataItem to count as changed every time) and
     * KEY_APK_SHA256 (hex SHA-256 of the APK bytes, so the watch can detect
     * a corrupted Bluetooth transfer before installing; absent when sent by
     * an older phone build, in which case the watch skips verification).
     */
    const val PATH_APK_UPDATE = "/bpwatch/apk_update"
    /**
     * Watch → phone: result of handling PATH_APK_UPDATE. Payload:
     * KEY_APK_RESULT ("installing" | "up_to_date" | "failed") and
     * KEY_APK_MESSAGE for human-readable detail.
     */
    const val PATH_APK_RESULT = "/bpwatch/apk_result"
    const val KEY_APK_VERSION_CODE = "apk_version_code"
    const val KEY_APK_VERSION_NAME = "apk_version_name"
    const val KEY_APK_NEEDS_UPDATE = "apk_needs_update"
    const val KEY_APK_ASSET = "apk_asset"
    const val KEY_APK_SHA256 = "apk_sha256"
    const val KEY_APK_RESULT = "apk_result"
    const val KEY_APK_MESSAGE = "apk_message"
    /**
     * v2.4.0: hex SHA-256 of the bundled APK's signing certificate, sent by
     * the phone on PATH_APK_UPDATE. The watch compares it against its own
     * signing certificate BEFORE installing and fails fast with a clear
     * message on mismatch, instead of beaming 20 MB and getting a cryptic
     * PackageInstaller failure.
     */
    const val KEY_APK_CERT_SHA256 = "apk_cert_sha256"
    /**
     * v2.4.0: watch → phone on PATH_APK_READY. True when the watch reports
     * PackageManager.canRequestPackageInstalls() — i.e. "Install unknown
     * apps" is allowed for BPWatch in the watch's Settings → Apps →
     * Special app access. Absent from older watch builds.
     */
    const val KEY_APK_CAN_INSTALL = "apk_can_install"

    // ------------------------------------------------------------------
    // Continuous HR + stress recording (v2.0, opt-in). The phone owns the
    // toggle; the watch samples every 10 minutes, persists samples locally
    // (survives reboot), and pushes them to the phone in batches. The phone
    // ACKs each batch so the watch can prune what arrived.
    // ------------------------------------------------------------------
    /**
     * Phone → watch: enable/disable continuous recording. Payload:
     * KEY_HR_RECORD (boolean). Sent on toggle change and on every full
     * sync (peer connect), so the phone's choice always wins — a phone
     * rebroadcast can never clobber it because the watch never changes it
     * locally.
     */
    const val PATH_HR_RECORD_SET = "/bpwatch/hr_record_set"
    /**
     * Watch → phone: one batch of recorded samples. Payload:
     * KEY_HIST_TS (long[]), KEY_HIST_BPM (float[]), KEY_HIST_STRESS (int[]),
     * all the same length. The phone replies PATH_HISTORY_PUSH_ACK.
     */
    const val PATH_HISTORY_PUSH = "/bpwatch/history_push"
    /**
     * Phone → watch: ack for a PATH_HISTORY_PUSH batch. Payload:
     * KEY_TIMESTAMP (long — the newest sample timestamp the phone stored).
     * The watch deletes everything up to that timestamp.
     */
    const val PATH_HISTORY_PUSH_ACK = "/bpwatch/history_push_ack"
    const val KEY_HR_RECORD = "hr_record"
    const val KEY_HIST_TS = "hist_ts"
    const val KEY_HIST_BPM = "hist_bpm"
    const val KEY_HIST_STRESS = "hist_stress"
    const val KEY_TIMESTAMP_MIN = "timestamp_min"

    // ------------------------------------------------------------------
    // Version reporting + settings sync (v1.15+). The phone is the source of
    // truth for monitoring settings; the watch also pushes its config on
    // connect so nothing is ever lost (updates, reinstalls, wipes).
    // ------------------------------------------------------------------
    /** Watch → phone: installed watch version. Payload: KEY_APK_VERSION_CODE
     * (long) + KEY_APK_VERSION_NAME. Sent on every peer connect and on
     * PATH_WATCH_INFO_REQUEST. */
    const val PATH_WATCH_INFO = "/bpwatch/watch_info"
    /** Phone → watch: "tell me your version". Empty payload. */
    const val PATH_WATCH_INFO_REQUEST = "/bpwatch/watch_info_request"
    /**
     * Watch → phone: the watch's current monitoring config (same DataMap
     * format as PATH_MONITORING_CONFIG). The phone adopts it when it has
     * never been configured itself; otherwise the phone's config wins and
     * is pushed back.
     */
    const val PATH_WATCH_CONFIG = "/bpwatch/watch_config"
    /** Watch → phone: "send me the full config + calibration state". Empty
     * payload; the phone replies with PATH_MONITORING_CONFIG and
     * PATH_CALIBRATION. Sent on watch boot and peer connect. */
    const val PATH_CONFIG_REQUEST = "/bpwatch/config_request"

    // ------------------------------------------------------------------
    // Watch-native steps (v2.2). The watch reads its own step counter via
    // Health Services and reports today's total; the phone prefers this
    // fresh count on the Steps tile, falling back to Health Connect.
    // ------------------------------------------------------------------
    /**
     * Watch → phone: today's step total, read on the watch itself.
     * Payload: KEY_STEPS (long), KEY_STEP_DATE ("yyyy-MM-dd" in the
     * watch's zone), KEY_TIMESTAMP. Sent when the count changes (after
     * each recording tick and on peer connect); the phone stores it
     * date-keyed, so reboots and reinstalls can't corrupt history.
     */
    const val PATH_STEPS_DAILY = "/bpwatch/steps_daily"
    const val KEY_STEPS = "steps"
    const val KEY_STEP_DATE = "step_date"

    // Posture and body location sensor reporting
    const val KEY_POSTURE = "posture"                 // Int: Posture.*
    const val KEY_ACTIVITY = "activity"               // String: ActivityState.*
    const val KEY_WRIST_LOCATION = "wrist_loc"        // Int: MeasurementLocation.*
    const val KEY_WRIST = "wrist"                     // String: "left" | "right"

    const val KEY_ALERT_TYPE = "alert_type"
    const val KEY_SEVERITY = "severity"
    const val KEY_ALERT_TITLE = "alert_title"
    const val KEY_ALERT_MESSAGE = "alert_message"

    const val KEY_HEART_RATE = "heart_rate"
    const val KEY_TIMESTAMP = "timestamp"
    const val KEY_SYS = "sys"
    const val KEY_DIA = "dia"
    const val KEY_STRESS = "stress"
    const val KEY_RESTING_HR = "resting_hr"
    const val KEY_SLEEPING_HR = "sleeping_hr"
    const val KEY_HRV_RMSSD = "hrv_rmssd"
    const val KEY_SKIN_TEMP_C = "skin_temp_c"
    const val KEY_BODY_FAT_PCT = "body_fat_pct"
    const val KEY_SKELETAL_MUSCLE_KG = "skeletal_muscle_kg"
    const val KEY_LEAN_MASS_KG = "lean_mass_kg"
    const val KEY_FAT_MASS_KG = "fat_mass_kg"
    const val KEY_BMR_KCAL = "bmr_kcal"
    const val KEY_BODY_WATER_LITERS = "body_water_liters"
    const val KEY_CALIBRATED = "calibrated"

    const val PATH_BODY_FAT_SYNC = "/bpwatch/body_fat_sync"
    const val PATH_SENSOR_TELEMETRY = "/bpwatch/sensor_telemetry"

    // ------------------------------------------------------------------
    // Body Fat / BIA Electrodes & Scan contract (v2.7.4)
    // ------------------------------------------------------------------
    const val PATH_BIA_REQUEST = "/bpwatch/bia_request"
    const val PATH_BIA_STATE = "/bpwatch/bia_state"
    const val PATH_BIA_RESULT = "/bpwatch/bia_result"
    const val PATH_BIA_CANCEL = "/bpwatch/bia_cancel"

    const val KEY_BIA_CONTACT_DETECTED = "bia_contact_detected"
    const val KEY_BIA_TOP_BUTTON_TOUCHED = "bia_top_button_touched"
    const val KEY_BIA_BOTTOM_BUTTON_TOUCHED = "bia_bottom_button_touched"
    const val KEY_BIA_SCAN_STATE = "bia_scan_state"
    const val KEY_BIA_PROGRESS = "bia_progress"
    const val KEY_BIA_ELAPSED_MS = "bia_elapsed_ms"
    const val KEY_BIA_MESSAGE = "bia_message"

    object BiaScanState {
        const val IDLE = "idle"
        const val WAITING_FOR_CONTACT = "waiting_contact"
        const val SCANNING = "scanning"
        const val CONTACT_LOST = "contact_lost"
        const val COMPLETED = "completed"
        const val CANCELLED = "cancelled"
        const val ERROR = "error"
    }

    // Monitoring-config payload (PATH_MONITORING_CONFIG). Versioned with
    // KEY_CONFIG_V so the watch can ignore fields from newer phone builds.
    const val KEY_CONFIG_V = "v"
    const val KEY_CONTINUOUS_HR = "continuous_hr"
    const val KEY_HR_HIGH_ENABLED = "hr_high_enabled"
    const val KEY_HR_HIGH_THRESHOLD = "hr_high_threshold"
    const val KEY_BP_INTERVAL_MIN = "bp_interval_min"
    const val KEY_BP_HIGH_ENABLED = "bp_high_enabled"
    const val KEY_SYS_HIGH = "sys_high"
    const val KEY_DIA_HIGH = "dia_high"
    const val KEY_BP_LOW_ENABLED = "bp_low_enabled"
    const val KEY_SYS_LOW = "sys_low"
    const val KEY_DIA_LOW = "dia_low"

    /** Health Connect BodyPosition constants. */
    object Posture {
        const val UNKNOWN = 0
        const val STANDING_UP = 1
        const val SITTING_DOWN = 2
        const val LYING_DOWN = 3
        const val RECLINING = 4
    }

    /** Health Connect MeasurementLocation constants. */
    object MeasurementLocation {
        const val UNKNOWN = 0
        const val LEFT_WRIST = 1
        const val RIGHT_WRIST = 2
        const val LEFT_UPPER_ARM = 3
        const val RIGHT_UPPER_ARM = 4
    }

    /** Physical activity context during measurement. */
    object ActivityState {
        const val SITTING = "sitting"
        const val STANDING = "standing"
        const val WALKING = "walking"
        const val LYING_DOWN = "lying_down"
        const val UNKNOWN = "unknown"
    }

    /** Alert types carried on PATH_ALERT. */
    object AlertType {
        const val HR_HIGH = "hr_high"
        const val HR_LOW = "hr_low"
        const val BP_HIGH = "bp_high"
        const val BP_LOW = "bp_low"
    }

    /** Alert severities carried on PATH_ALERT. */
    object Severity {
        const val NORMAL = "normal"
        const val EXTREME = "extreme"
    }

    /**
     * Fixed extreme thresholds for v1.14.0 (not yet user-configurable).
     * Crossing one of these fires a full-screen takeover alert on the phone.
     */
    object ExtremeThresholds {
        const val HR_HIGH = 160f
        const val HR_LOW = 40f
        const val SYS_HIGH = 180
        const val DIA_HIGH = 120
        const val SYS_LOW = 80
        const val DIA_LOW = 50
    }
}
