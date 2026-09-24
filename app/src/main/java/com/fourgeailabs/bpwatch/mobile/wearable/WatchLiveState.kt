package com.fourgeailabs.bpwatch.mobile.wearable

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Live mirror of what the watch is showing: the current heart rate (sent as
 * throttled live ticks while measuring or continuously monitoring) and the
 * most recent alert fired on the watch.
 *
 * Updated by [PhoneListenerService] from Data Layer messages; observed by
 * the phone UI so the app mirrors the watch while it's open.
 */
object WatchLiveState {

    /** Live heart-rate in bpm, null when nothing has arrived yet. */
    private val _liveHr = MutableStateFlow<Float?>(null)
    val liveHr: StateFlow<Float?> = _liveHr.asStateFlow()

    /** When the last live HR tick arrived (epoch millis). */
    private val _liveHrAt = MutableStateFlow(0L)
    val liveHrAt: StateFlow<Long> = _liveHrAt.asStateFlow()

    /** Live HRV (RMSSD in ms) from the watch sensor stream. */
    private val _liveHrv = MutableStateFlow<Float?>(null)
    val liveHrv: StateFlow<Float?> = _liveHrv.asStateFlow()

    /** When the last live HRV tick arrived (epoch millis). */
    private val _liveHrvAt = MutableStateFlow(0L)
    val liveHrvAt: StateFlow<Long> = _liveHrvAt.asStateFlow()

    /** Recent live HRV samples for the real-time sparkline (last ~15 samples). */
    private val _liveHrvHistory = MutableStateFlow<List<Float>>(emptyList())
    val liveHrvHistory: StateFlow<List<Float>> = _liveHrvHistory.asStateFlow()

    /** The most recent alert fired on the watch, if any. */
    private val _lastAlert = MutableStateFlow<WatchAlert?>(null)
    val lastAlert: StateFlow<WatchAlert?> = _lastAlert.asStateFlow()

    /** Watch battery percentage (0..100), null if not yet received. */
    private val _batteryLevel = MutableStateFlow<Int?>(null)
    val batteryLevel: StateFlow<Int?> = _batteryLevel.asStateFlow()

    /** True when watch reports it is off-body / off-wrist. */
    private val _isOffBody = MutableStateFlow(false)
    val isOffBody: StateFlow<Boolean> = _isOffBody.asStateFlow()

    /** True when watch reports battery-saving mode is active (< 20%). */
    private val _batterySaverActive = MutableStateFlow(false)
    val batterySaverActive: StateFlow<Boolean> = _batterySaverActive.asStateFlow()

    /** A tick is stale after 60s without an update (watch out of range, etc.). */
    private const val STALE_AFTER_MS = 60_000L

    fun updateOffBodyState(offBody: Boolean) {
        _isOffBody.value = offBody
    }

    fun updateBatteryState(level: Int, saverActive: Boolean) {
        _batteryLevel.value = level
        _batterySaverActive.value = saverActive
    }

    fun updateLiveHr(hr: Float) {
        if (hr <= 0f) return
        _liveHr.value = hr
        _liveHrAt.value = System.currentTimeMillis()
    }

    fun updateLiveHrv(hrv: Float) {
        if (hrv <= 0f) return
        _liveHrv.value = hrv
        _liveHrvAt.value = System.currentTimeMillis()
        val current = _liveHrvHistory.value.toMutableList()
        current.add(hrv)
        if (current.size > 20) {
            current.removeAt(0)
        }
        _liveHrvHistory.value = current
    }

    fun postAlert(alert: WatchAlert) {
        _lastAlert.value = alert
    }

    /** True when the live HR value is fresh enough to show. */
    fun isLiveHrFresh(now: Long = System.currentTimeMillis()): Boolean {
        val at = _liveHrAt.value
        return at > 0L && now - at < STALE_AFTER_MS
    }

    /** True when the live HRV value is fresh enough to show. */
    fun isLiveHrvFresh(now: Long = System.currentTimeMillis()): Boolean {
        val at = _liveHrvAt.value
        return at > 0L && now - at < STALE_AFTER_MS
    }
}

/** An alert that fired on the watch, mirrored to the phone. */
data class WatchAlert(
    val type: String,
    val severity: String,
    val title: String,
    val message: String,
    val at: Long,
)
