package com.fourgeailabs.bpwatch.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Battery-Saving Mode for BPWatch on Wear OS.
 *
 * Automatically reduces sensor polling and hardware wake frequency when watch
 * battery drops below [BATTERY_SAVER_THRESHOLD_PERCENT] (20%):
 * 1. Background check interval: doubled (minimum 30 minutes).
 * 2. Optical PPG measurement duration: reduced from 30s to 15s (50% less optical LED runtime).
 * 3. Continuous HR live ticks: throttled from 2s to 10s.
 * 4. Continuous HR upload window: extended from 1 min to 3 min.
 * 5. Accelerometer polling delay: switched from SENSOR_DELAY_UI to SENSOR_DELAY_NORMAL (~70% fewer sensor interrupts).
 * 6. Continuous HR + stress recording cadence: relaxed from 10 min to 30 min.
 */
object WatchBatterySaver {
    private const val TAG = "WatchBatterySaver"

    /** Battery threshold (percentage) to trigger power-saving mode. */
    const val BATTERY_SAVER_THRESHOLD_PERCENT = 20

    /** Normal measurement duration (30 seconds). */
    const val DURATION_NORMAL_MS = 30_000L

    /** Battery saver measurement duration (15 seconds) — halves active optical sensor draw. */
    const val DURATION_SAVER_MS = 15_000L

    /** Normal continuous HR live tick interval (2 seconds). */
    const val LIVE_TICK_NORMAL_MS = 2_000L

    /** Battery saver live tick interval (10 seconds) — cuts BT radio wakeups by 80%. */
    const val LIVE_TICK_SAVER_MS = 10_000L

    /** Normal upload aggregation window (1 minute). */
    const val UPLOAD_NORMAL_MS = 60_000L

    /** Battery saver upload aggregation window (3 minutes). */
    const val UPLOAD_SAVER_MS = 180_000L

    /** Normal continuous recording cadence (10 minutes). */
    const val RECORD_NORMAL_MS = 10 * 60_000L

    /** Battery saver continuous recording cadence (30 minutes). */
    const val RECORD_SAVER_MS = 30 * 60_000L

    /** Reads the current battery percentage (0..100). */
    fun getBatteryLevel(context: Context): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val capacity = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            if (capacity in 0..100) {
                return capacity
            }
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                100
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read battery level", e)
            100
        }
    }

    /**
     * True when the watch battery is below 20% or system power save mode is active.
     */
    fun isBatterySaverActive(context: Context): Boolean {
        val level = getBatteryLevel(context)
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val powerSave = pm?.isPowerSaveMode == true
        return level < BATTERY_SAVER_THRESHOLD_PERCENT || powerSave
    }

    /**
     * Calculates the adjusted BP check interval (in minutes).
     * When battery drops below 20%, interval is doubled (minimum 30 minutes).
     */
    fun getAdjustedBpInterval(context: Context, baseIntervalMinutes: Int): Int {
        if (baseIntervalMinutes <= 0) return 0
        return if (isBatterySaverActive(context)) {
            maxOf(30, baseIntervalMinutes * 2)
        } else {
            baseIntervalMinutes
        }
    }

    /**
     * Sampling duration for HR & posture measurements.
     * Cut from 30s to 15s in battery saver mode.
     */
    fun getMeasurementDurationMs(context: Context): Long {
        return if (isBatterySaverActive(context)) DURATION_SAVER_MS else DURATION_NORMAL_MS
    }

    /**
     * Interval for live Bluetooth HR transmission ticks.
     * Throttled from 2s to 10s in battery saver mode.
     */
    fun getLiveTickIntervalMs(context: Context): Long {
        return if (isBatterySaverActive(context)) LIVE_TICK_SAVER_MS else LIVE_TICK_NORMAL_MS
    }

    /**
     * Interval for continuous HR upload averaging.
     */
    fun getUploadIntervalMs(context: Context): Long {
        val configIntervalMinutes = try {
            WatchSettings.getMonitorConfig(context).bpIntervalMinutes
        } catch (_: Exception) {
            0
        }
        val normalUpload = if (configIntervalMinutes > 0) {
            maxOf(UPLOAD_NORMAL_MS, configIntervalMinutes * 60_000L)
        } else {
            UPLOAD_NORMAL_MS
        }
        return if (isBatterySaverActive(context)) {
            maxOf(normalUpload, UPLOAD_SAVER_MS)
        } else {
            normalUpload
        }
    }

    /**
     * Cadence for continuous background recording.
     * Relaxed from 10 min to 30 min in battery saver mode.
     */
    fun getRecordIntervalMs(context: Context): Long {
        val configIntervalMinutes = try {
            WatchSettings.getMonitorConfig(context).bpIntervalMinutes
        } catch (_: Exception) {
            0
        }
        val baseInterval = if (configIntervalMinutes > 0) {
            maxOf(RECORD_NORMAL_MS, configIntervalMinutes * 60_000L)
        } else {
            RECORD_NORMAL_MS
        }
        return if (isBatterySaverActive(context)) {
            maxOf(baseInterval, RECORD_SAVER_MS)
        } else {
            baseInterval
        }
    }

    /**
     * Accelerometer sensor delay.
     * Slower delay (SENSOR_DELAY_NORMAL = 200ms) in battery saver mode reduces interrupt rate by ~70%.
     */
    fun getAccelerometerDelay(context: Context): Int {
        return if (isBatterySaverActive(context)) {
            SensorManager.SENSOR_DELAY_NORMAL
        } else {
            SensorManager.SENSOR_DELAY_UI
        }
    }

    /**
     * Reads current battery state, updates [WatchState], and broadcasts to phone.
     */
    fun refreshAndBroadcast(context: Context) {
        val level = getBatteryLevel(context)
        val saver = isBatterySaverActive(context)
        WatchState.onBatteryState(level, saver)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DataLayer.sendBatteryState(context.applicationContext, level, saver)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send battery state to phone", e)
            }
        }
    }
}

/**
 * BroadcastReceiver listening for battery level transitions to dynamically adjust
 * polling schedulers without requiring a manual app open.
 */
class BatteryStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BATTERY_CHANGED ||
            action == Intent.ACTION_BATTERY_LOW ||
            action == Intent.ACTION_BATTERY_OKAY ||
            action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED
        ) {
            val level = WatchBatterySaver.getBatteryLevel(context)
            val saver = WatchBatterySaver.isBatterySaverActive(context)
            Log.d("BatteryStateReceiver", "Battery state changed: $level% (saver active: $saver)")
            WatchState.onBatteryState(level, saver)

            // Reschedule background checks with updated battery-saving interval
            try {
                CheckScheduler.reschedule(context)
            } catch (e: Exception) {
                Log.w("BatteryStateReceiver", "Failed to reschedule checks", e)
            }

            // Reschedule continuous recording
            try {
                RecordScheduler.reschedule(context)
            } catch (e: Exception) {
                Log.w("BatteryStateReceiver", "Failed to reschedule recording", e)
            }

            // Sync battery state to phone companion
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    DataLayer.sendBatteryState(context.applicationContext, level, saver)
                } catch (_: Exception) {
                }
            }
        }
    }
}
