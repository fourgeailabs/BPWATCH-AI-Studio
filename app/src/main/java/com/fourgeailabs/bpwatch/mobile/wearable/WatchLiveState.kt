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

    /** The most recent alert fired on the watch, if any. */
    private val _lastAlert = MutableStateFlow<WatchAlert?>(null)
    val lastAlert: StateFlow<WatchAlert?> = _lastAlert.asStateFlow()

    /** Watch battery percentage (0..100), null if not yet received. */
    private val _batteryLevel = MutableStateFlow<Int?>(null)
    val batteryLevel: StateFlow<Int?> = _batteryLevel.asStateFlow()

    /** True when watch reports battery-saving mode is active (< 20%). */
    private val _batterySaverActive = MutableStateFlow(false)
    val batterySaverActive: StateFlow<Boolean> = _batterySaverActive.asStateFlow()

    /** A tick is stale after 60s without an update (watch out of range, etc.). */
    private const val STALE_AFTER_MS = 60_000L

    fun updateBatteryState(level: Int, saverActive: Boolean) {
        _batteryLevel.value = level
        _batterySaverActive.value = saverActive
    }

    fun updateLiveHr(hr: Float) {
        if (hr <= 0f) return
        _liveHr.value = hr
        _liveHrAt.value = System.currentTimeMillis()
    }

    fun postAlert(alert: WatchAlert) {
        _lastAlert.value = alert
    }

    /** True when the live HR value is fresh enough to show. */
    fun isLiveHrFresh(now: Long = System.currentTimeMillis()): Boolean {
        val at = _liveHrAt.value
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
