package com.fourgeailabs.bpwatch.mobile.wearable

import android.content.Context
import com.fourgeailabs.bpwatch.Link
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Live BIA (Bioelectrical Impedance Analysis) electrode contact & scan state on the phone.
 *
 * Requirements:
 * - Requires both middle and ring fingers touching the physical side button electrodes on the watch.
 * - If fingers are not touching both sensors, the scan will NOT start.
 * - If contact is lost during the scan, the timer pauses and warns the user to touch the sensors again.
 * - Resumes seamlessly once contact is restored.
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

    fun requestStartScan(context: Context) {
        _progress.value = 0f
        _elapsedMs.value = 0L
        if (!_contactDetected.value) {
            _scanState.value = Link.BiaScanState.WAITING_FOR_CONTACT
            _statusMessage.value = "Place middle and ring fingers on watch side button sensors to begin."
        } else {
            _scanState.value = Link.BiaScanState.SCANNING
            _statusMessage.value = "Measuring body composition..."
        }

        scope.launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                val payload = DataMap().apply {
                    putLong(Link.KEY_TIMESTAMP, System.currentTimeMillis())
                }.toByteArray()
                nodes.forEach { node ->
                    Wearable.getMessageClient(context)
                        .sendMessage(node.id, Link.PATH_BIA_REQUEST, payload)
                        .await()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun requestCancelScan(context: Context) {
        reset()
        scope.launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                val payload = ByteArray(0)
                nodes.forEach { node ->
                    Wearable.getMessageClient(context)
                        .sendMessage(node.id, Link.PATH_BIA_CANCEL, payload)
                        .await()
                }
            } catch (_: Exception) {
            }
        }
    }
}
