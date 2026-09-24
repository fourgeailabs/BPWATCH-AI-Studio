package com.fourgeailabs.bpwatch.wear

import android.content.Context
import com.fourgeailabs.bpwatch.Link
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object DataLayer {

    /**
     * Sends an averaged heart-rate reading (plus stress score, -1 if unknown,
     * posture, and wrist measurement location) to every connected node
     * (the phone companion via Bluetooth/Data Layer).
     */
    suspend fun sendHrReading(
        context: Context,
        heartRate: Float,
        timestamp: Long,
        stress: Int = -1,
        bodyPosition: Int = Link.Posture.SITTING_DOWN,
        activity: String = Link.ActivityState.SITTING,
        measurementLocation: Int = WatchSettings.getMeasurementLocation(context),
        hrvRmssd: Float = -1f,
        skinTempC: Float = -1f,
    ): Boolean {
        return try {
            val payload = DataMap().apply {
                putFloat(Link.KEY_HEART_RATE, heartRate)
                putLong(Link.KEY_TIMESTAMP, timestamp)
                putInt(Link.KEY_STRESS, stress)
                if (hrvRmssd > 0f) {
                    putFloat(Link.KEY_HRV_RMSSD, hrvRmssd)
                }
                putInt(Link.KEY_POSTURE, bodyPosition)
                putString(Link.KEY_ACTIVITY, activity)
                putInt(Link.KEY_WRIST_LOCATION, measurementLocation)
                putString(Link.KEY_WRIST, WatchSettings.getWrist(context))
                putInt(Link.KEY_BATTERY_LEVEL, WatchBatterySaver.getBatteryLevel(context))
                putBoolean(Link.KEY_BATTERY_SAVER_ACTIVE, WatchBatterySaver.isBatterySaverActive(context))
                val finalSkinTemp = if (skinTempC > 0f) {
                    skinTempC
                } else {
                    try {
                        val monitor = SkinTempMonitor(context)
                        monitor.start()
                        kotlinx.coroutines.delay(200L)
                        val t = monitor.getAverageOrLast() ?: 36.6f
                        monitor.stop()
                        t
                    } catch (_: Exception) {
                        36.6f
                    }
                }
                putFloat(Link.KEY_SKIN_TEMP_C, finalSkinTemp)
            }.toByteArray()

            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isEmpty()) return false

            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, Link.PATH_HR_READING, payload)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Watch → phone: current watch battery level and battery saver status.
     */
    suspend fun sendBatteryState(context: Context, batteryLevel: Int, isBatterySaverActive: Boolean): Boolean {
        val payload = DataMap().apply {
            putInt(Link.KEY_BATTERY_LEVEL, batteryLevel)
            putBoolean(Link.KEY_BATTERY_SAVER_ACTIVE, isBatterySaverActive)
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_BATTERY_STATE, payload)
    }

    /**
     * Watch → phone: the user changed the BP-check interval on the watch.
     * The phone persists it so the two stay in sync (the phone re-broadcasts
     * its config on every reading, which would otherwise clobber the watch's
     * choice).
     */
    suspend fun sendIntervalSet(context: Context, intervalMinutes: Int): Boolean {
        val payload = DataMap().apply {
            putInt(Link.KEY_BP_INTERVAL_MIN, intervalMinutes)
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_INTERVAL_SET, payload)
    }

    /**
     * Watch → phone: user toggled wrist bias on the watch. Synchronize it
     * with the phone so both sides have matching orientation bias configuration.
     */
    suspend fun sendWristSet(context: Context, wrist: String): Boolean {
        val payload = DataMap().apply {
            putString(Link.KEY_WRIST, wrist)
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_WRIST_SET, payload)
    }

    suspend fun sendEkgReading(context: Context, ekgVoltages: FloatArray, classification: String, timestamp: Long): Boolean {
        val payload = DataMap().apply {
            putFloatArray(Link.KEY_EKG_DATA, ekgVoltages)
            putString(Link.KEY_EKG_CLASSIFICATION, classification)
            putLong(Link.KEY_EKG_TIMESTAMP, timestamp)
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_EKG_SYNC, payload)
    }

    /**
     * Watch → phone: live heart-rate tick so the phone can mirror what the
     * watch is showing. Callers throttle this (~10s); the phone also guards
     * against stale data.
     */
    suspend fun sendHrLive(context: Context, heartRate: Float): Boolean {
        val payload = DataMap().apply {
            putFloat(Link.KEY_HEART_RATE, heartRate)
            putLong(Link.KEY_TIMESTAMP, System.currentTimeMillis())
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_HR_LIVE, payload)
    }

    /**
     * Watch → phone: an alert just fired on the watch. The phone mirrors it
     * as a notification — or a full-screen takeover when [severity] is
     * [Link.Severity.EXTREME].
     */
    suspend fun sendAlert(
        context: Context,
        alertType: String,
        severity: String,
        title: String,
        message: String,
    ): Boolean {
        val payload = DataMap().apply {
            putString(Link.KEY_ALERT_TYPE, alertType)
            putString(Link.KEY_SEVERITY, severity)
            putString(Link.KEY_ALERT_TITLE, title)
            putString(Link.KEY_ALERT_MESSAGE, message)
            putLong(Link.KEY_TIMESTAMP, System.currentTimeMillis())
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_ALERT, payload)
    }

    private suspend fun sendToAllNodes(
        context: Context,
        path: String,
        payload: ByteArray,
    ): Boolean {
        return try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isEmpty()) return false
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, path, payload)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ------------------------------------------------------------------
    // Updater + settings sync (v1.15+).
    // ------------------------------------------------------------------

    /** Current installed version, for the phone's update UI. */
    fun installedVersion(context: Context): Pair<Long, String> {
        val pm = context.packageManager
        val info = pm.getPackageInfo(context.packageName, 0)
        val code = if (android.os.Build.VERSION.SDK_INT >= 28) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
        return code to (info.versionName ?: "?")
    }

    /**
     * Watch → phone: "here's what I'm running". Sent on every peer connect
     * and in reply to PATH_WATCH_INFO_REQUEST so the phone always knows
     * whether an update is available.
     */
    suspend fun sendWatchInfo(context: Context): Boolean {
        val (code, name) = installedVersion(context)
        val payload = DataMap().apply {
            putLong(Link.KEY_APK_VERSION_CODE, code)
            putString(Link.KEY_APK_VERSION_NAME, name)
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_WATCH_INFO, payload)
    }

    /**
     * Watch → phone: the watch's current monitoring config, in the same
     * DataMap format as PATH_MONITORING_CONFIG. The phone adopts it when it
     * has never been configured itself — so settings survive even a full
     * phone reinstall with the watch as the bridge.
     */
    suspend fun sendWatchConfig(context: Context): Boolean {
        val config = WatchSettings.getMonitorConfig(context)
        val payload = DataMap().apply {
            putInt(Link.KEY_CONFIG_V, 1)
            putBoolean(Link.KEY_CONTINUOUS_HR, config.continuousHr)
            putBoolean(Link.KEY_HR_HIGH_ENABLED, config.hrHighEnabled)
            putInt(Link.KEY_HR_HIGH_THRESHOLD, config.hrHighThreshold)
            putInt(Link.KEY_BP_INTERVAL_MIN, config.bpIntervalMinutes)
            putBoolean(Link.KEY_BP_HIGH_ENABLED, config.bpHighEnabled)
            putInt(Link.KEY_SYS_HIGH, config.sysHigh)
            putInt(Link.KEY_DIA_HIGH, config.diaHigh)
            putBoolean(Link.KEY_BP_LOW_ENABLED, config.bpLowEnabled)
            putInt(Link.KEY_SYS_LOW, config.sysLow)
            putInt(Link.KEY_DIA_LOW, config.diaLow)
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_WATCH_CONFIG, payload)
    }

    /**
     * Watch → phone: live BIA scan state & electrode contact update.
     */
    suspend fun sendBiaState(
        context: Context,
        scanState: String,
        isContactDetected: Boolean,
        isTopTouched: Boolean,
        isBottomTouched: Boolean,
        progress: Float,
        elapsedMs: Long,
        message: String = "",
    ): Boolean {
        val payload = DataMap().apply {
            putString(Link.KEY_BIA_SCAN_STATE, scanState)
            putBoolean(Link.KEY_BIA_CONTACT_DETECTED, isContactDetected)
            putBoolean(Link.KEY_BIA_TOP_BUTTON_TOUCHED, isTopTouched)
            putBoolean(Link.KEY_BIA_BOTTOM_BUTTON_TOUCHED, isBottomTouched)
            putFloat(Link.KEY_BIA_PROGRESS, progress)
            putLong(Link.KEY_BIA_ELAPSED_MS, elapsedMs)
            putString(Link.KEY_BIA_MESSAGE, message)
            putLong(Link.KEY_TIMESTAMP, System.currentTimeMillis())
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_BIA_STATE, payload)
    }

    /**
     * Watch → phone: completed BIA body composition results.
     */
    suspend fun sendBiaResult(
        context: Context,
        bodyFatPct: Double,
        skeletalMuscleKg: Double,
        fatMassKg: Double,
        bmrKcal: Int,
        bodyWaterLiters: Double,
    ): Boolean {
        val payload = DataMap().apply {
            putDouble(Link.KEY_BODY_FAT_PCT, bodyFatPct)
            putDouble(Link.KEY_SKELETAL_MUSCLE_KG, skeletalMuscleKg)
            putDouble(Link.KEY_FAT_MASS_KG, fatMassKg)
            putInt(Link.KEY_BMR_KCAL, bmrKcal)
            putDouble(Link.KEY_BODY_WATER_LITERS, bodyWaterLiters)
            putLong(Link.KEY_TIMESTAMP, System.currentTimeMillis())
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_BIA_RESULT, payload)
    }

    /** Watch → phone: off-body / off-wrist status update. */
    suspend fun sendOffBodyState(context: Context, isOffBody: Boolean): Boolean {
        val payload = DataMap().apply {
            putBoolean(Link.KEY_IS_OFF_BODY, isOffBody)
            putLong(Link.KEY_TIMESTAMP, System.currentTimeMillis())
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_OFF_BODY_STATE, payload)
    }

    /** Watch → phone: overnight sleep session sync. */
    suspend fun sendSleepSession(context: Context, startMs: Long, endMs: Long, sleepMinutes: Int): Boolean {
        val payload = DataMap().apply {
            putLong(Link.KEY_SLEEP_START, startMs)
            putLong(Link.KEY_SLEEP_END, endMs)
            putInt(Link.KEY_SLEEP_MINUTES, sleepMinutes)
            putLong(Link.KEY_TIMESTAMP, System.currentTimeMillis())
        }.toByteArray()
        return sendToAllNodes(context, Link.PATH_SLEEP_SESSION_SYNC, payload)
    }

    /**
     * Watch → phone: "send me the full config + calibration state". Used on
     * boot and peer connect so a wiped/reinstalled watch re-programs itself
     * from the phone within seconds — never by hand.
     */
    suspend fun requestConfig(context: Context): Boolean =
        sendToAllNodes(context, Link.PATH_CONFIG_REQUEST, ByteArray(0))

    /**
     * Fire-and-forget variant for BroadcastReceivers (no coroutine scope).
     * Best effort — the peer-connect hook covers the cases this misses.
     */
    fun announceToPhone(context: Context) {
        try {
            val (code, name) = installedVersion(context)
            val infoPayload = DataMap().apply {
                putLong(Link.KEY_APK_VERSION_CODE, code)
                putString(Link.KEY_APK_VERSION_NAME, name)
            }.toByteArray()
            val nodeClient = Wearable.getNodeClient(context)
            val messageClient = Wearable.getMessageClient(context)
            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    messageClient.sendMessage(
                        node.id,
                        Link.PATH_WATCH_INFO,
                        infoPayload,
                    )
                    messageClient.sendMessage(
                        node.id,
                        Link.PATH_CONFIG_REQUEST,
                        ByteArray(0),
                    )
                }
            }
        } catch (_: Exception) {
        }
    }
}
