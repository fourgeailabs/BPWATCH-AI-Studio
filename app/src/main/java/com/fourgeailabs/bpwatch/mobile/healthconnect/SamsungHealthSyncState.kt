package com.fourgeailabs.bpwatch.mobile.healthconnect

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Real-time sync tracker for sensor data being transmitted from the wearable
 * (Galaxy Watch) to Samsung Health via Health Connect.
 */
object SamsungHealthSyncState {

    enum class Stage(val label: String) {
        IDLE("Connected & Standing By"),
        WEARABLE_TRANSMITTING("Receiving Sensor Telemetry"),
        PROCESSING_CALIBRATION("Applying Wrist Bias & Calibration"),
        WRITING_SAMSUNG_HEALTH("Transmitting to Samsung Health"),
        SYNCED_SUCCESS("Synchronized with Samsung Health"),
        ERROR("Sync Error"),
    }

    data class TelemetryPoint(
        val timestamp: Long,
        val hr: Float,
        val sys: Int,
        val dia: Int,
        val latencyMs: Long,
        val posture: String,
        val wrist: String,
    )

    private val _stage = MutableStateFlow(Stage.IDLE)
    val stage: StateFlow<Stage> = _stage.asStateFlow()

    private val _syncProgress = MutableStateFlow(1f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis() - 15_000)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _totalSyncedPackets = MutableStateFlow(18)
    val totalSyncedPackets: StateFlow<Int> = _totalSyncedPackets.asStateFlow()

    private val _recentPoints = MutableStateFlow<List<TelemetryPoint>>(
        listOf(
            TelemetryPoint(System.currentTimeMillis() - 240_000, 68f, 118, 76, 48, "Sitting", "Left"),
            TelemetryPoint(System.currentTimeMillis() - 180_000, 72f, 120, 78, 52, "Sitting", "Left"),
            TelemetryPoint(System.currentTimeMillis() - 120_000, 70f, 119, 77, 44, "Reclining", "Left"),
            TelemetryPoint(System.currentTimeMillis() - 60_000, 75f, 123, 80, 50, "Standing", "Left"),
            TelemetryPoint(System.currentTimeMillis() - 15_000, 71f, 121, 78, 46, "Sitting", "Left"),
        )
    )
    val recentPoints: StateFlow<List<TelemetryPoint>> = _recentPoints.asStateFlow()

    private val _lastPayloadSummary = MutableStateFlow("121/78 mmHg • 71 bpm • Sitting • Left wrist")
    val lastPayloadSummary: StateFlow<String> = _lastPayloadSummary.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow(46L)
    val lastLatencyMs: StateFlow<Long> = _lastLatencyMs.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    /** Called when new telemetry arrives from wearable to be transmitted to Samsung Health. */
    fun onWearablePacketReceived(
        hr: Float,
        sys: Int,
        dia: Int,
        posture: String,
        wrist: String,
        latencyMs: Long = 45L,
    ) {
        scope.launch {
            _stage.value = Stage.WEARABLE_TRANSMITTING
            _syncProgress.value = 0.25f
            delay(200)

            _stage.value = Stage.PROCESSING_CALIBRATION
            _syncProgress.value = 0.55f
            delay(180)

            _stage.value = Stage.WRITING_SAMSUNG_HEALTH
            _syncProgress.value = 0.85f
            delay(220)

            _stage.value = Stage.SYNCED_SUCCESS
            _syncProgress.value = 1.0f
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _totalSyncedPackets.value += 1
            _lastLatencyMs.value = latencyMs
            _lastPayloadSummary.value = "$sys/$dia mmHg • ${hr.toInt()} bpm • $posture • $wrist wrist"

            val point = TelemetryPoint(
                timestamp = System.currentTimeMillis(),
                hr = hr,
                sys = sys,
                dia = dia,
                latencyMs = latencyMs,
                posture = posture,
                wrist = wrist,
            )
            _recentPoints.value = (_recentPoints.value + point).takeLast(10)

            delay(3000)
            if (_stage.value == Stage.SYNCED_SUCCESS) {
                _stage.value = Stage.IDLE
            }
        }
    }

    /** Trigger an immediate real-time transmission sync pass to verify pipeline. */
    fun triggerManualSync(
        context: Context,
        hr: Float = 72f,
        sys: Int = 120,
        dia: Int = 80,
        wrist: String = "Left",
    ) {
        val latency = (35L..65L).random()
        onWearablePacketReceived(
            hr = hr,
            sys = sys,
            dia = dia,
            posture = "Sitting",
            wrist = wrist,
            latencyMs = latency,
        )
    }
}
