package com.fourgeailabs.bpwatch.mobile.wearable

import com.fourgeailabs.bpwatch.Link
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * BIA (Bioelectrical Impedance Analysis) result state on the phone.
 *
 * The phone no longer requests live BIA scans: Samsung reserves the
 * watch's BIA electrodes for Samsung Health, so a scan triggered from
 * here could never return impedance data. This object now only carries
 * a genuine result if one ever arrives from the watch data layer, and
 * the UI shows real body-composition data from Health Connect instead.
 */
object BiaState {

    data class BiaResult(
        val bodyFatPct: Double,
        val skeletalMuscleKg: Double,
        val fatMassKg: Double,
        val bmrKcal: Int,
        val bodyWaterLiters: Double,
        val timestamp: Long = System.currentTimeMillis(),
    )

    private val _contactDetected = MutableStateFlow(false)
    val contactDetected: StateFlow<Boolean> = _contactDetected.asStateFlow()

    private val _topTouched = MutableStateFlow(false)
    val topTouched: StateFlow<Boolean> = _topTouched.asStateFlow()

    private val _bottomTouched = MutableStateFlow(false)
    val bottomTouched: StateFlow<Boolean> = _bottomTouched.asStateFlow()

    private val _scanState = MutableStateFlow(Link.BiaScanState.IDLE)
    val scanState: StateFlow<String> = _scanState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _latestResult = MutableStateFlow<BiaResult?>(null)
    val latestResult: StateFlow<BiaResult?> = _latestResult.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun onStateReceived(
        state: String,
        isContact: Boolean,
        top: Boolean,
        bottom: Boolean,
        prog: Float,
        elapsed: Long,
        msg: String,
    ) {
        _scanState.value = state
        _contactDetected.value = isContact
        _topTouched.value = top
        _bottomTouched.value = bottom
        _progress.value = prog
        _elapsedMs.value = elapsed
        _statusMessage.value = msg
    }

    fun onResultReceived(result: BiaResult) {
        _latestResult.value = result
        _scanState.value = Link.BiaScanState.COMPLETED
        _progress.value = 1f
        _elapsedMs.value = 15_000L
        _statusMessage.value = "Scan complete!"
    }

    fun setElectrodeContact(top: Boolean, bottom: Boolean) {
        _topTouched.value = top
        _bottomTouched.value = bottom
        val both = top && bottom
        _contactDetected.value = both

        if (_scanState.value == Link.BiaScanState.WAITING_FOR_CONTACT && both) {
            _scanState.value = Link.BiaScanState.SCANNING
            _statusMessage.value = "Measuring body composition..."
        } else if (_scanState.value == Link.BiaScanState.SCANNING && !both) {
            _scanState.value = Link.BiaScanState.CONTACT_LOST
            _statusMessage.value = "Contact lost! Touch the side button sensors on your watch to continue scan."
        } else if (_scanState.value == Link.BiaScanState.CONTACT_LOST && both) {
            _scanState.value = Link.BiaScanState.SCANNING
            _statusMessage.value = "Measuring body composition..."
        }
    }

    fun reset() {
        _scanState.value = Link.BiaScanState.IDLE
        _progress.value = 0f
        _elapsedMs.value = 0L
        _statusMessage.value = ""
    }

    // Honesty fix: the phone no longer requests fake BIA scans. The watch's
    // BIA channel is reserved by Samsung for Samsung Health, so any scan
    // request this app sent could never return impedance data. Real
    // body-composition data arrives only via Health Connect.
}
