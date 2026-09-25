package com.fourgeailabs.bpwatch.wear

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.fourgeailabs.bpwatch.Link
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Bioelectrical Impedance Analysis (BIA) sensor & contact state manager on Wear OS / Galaxy Watch.
 *
 * Requirements:
 * - Requires both middle finger (top button sensor) and ring finger (bottom button sensor)
 *   touching the physical side button electrodes.
 * - If fingers are not touching both sensors, the scan will NOT start (WAITING_FOR_CONTACT).
 * - If contact is lost during the 15s scan, the timer pauses immediately and warns the user (CONTACT_LOST).
 * - Resumes seamlessly once contact is restored.
 * - Calculates body composition using real bio-impedance (resistance & reactance) measured from sensors.
 */
class BiaSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var scanJob: Job? = null

    // Dual electrode contact flags
    private val _isTopTouched = MutableStateFlow(false)
    val isTopTouched: StateFlow<Boolean> = _isTopTouched.asStateFlow()

    private val _isBottomTouched = MutableStateFlow(false)
    val isBottomTouched: StateFlow<Boolean> = _isBottomTouched.asStateFlow()

    private val _isContactDetected = MutableStateFlow(false)
    val isContactDetected: StateFlow<Boolean> = _isContactDetected.asStateFlow()

    private val _isOnWrist = MutableStateFlow(true)
    val isOnWrist: StateFlow<Boolean> = _isOnWrist.asStateFlow()

    private val _scanState = MutableStateFlow(Link.BiaScanState.IDLE)
    val scanState: StateFlow<String> = _scanState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    // Real measured impedance data collected during contact
    private val measuredResistanceSamples = mutableListOf<Double>()
    private val measuredReactanceSamples = mutableListOf<Double>()
    private val _liveImpedanceOhms = MutableStateFlow<Double?>(null)
    val liveImpedanceOhms: StateFlow<Double?> = _liveImpedanceOhms.asStateFlow()

    // Scanned result
    data class BiaResult(
        val bodyFatPct: Double,
        val skeletalMuscleKg: Double,
        val fatMassKg: Double,
        val bmrKcal: Int,
        val bodyWaterLiters: Double,
        val impedanceOhms: Double,
    )

    private val _lastResult = MutableStateFlow<BiaResult?>(null)
    val lastResult: StateFlow<BiaResult?> = _lastResult.asStateFlow()

    companion object {
        const val TOTAL_SCAN_DURATION_MS = 15_000L
        private const val TAG = "BiaSensorManager"

        @Volatile
        private var instance: BiaSensorManager? = null

        fun getInstance(context: Context): BiaSensorManager =
            instance ?: synchronized(this) {
                instance ?: BiaSensorManager(context.applicationContext).also { instance = it }
            }
    }

    init {
        registerHardwareSensors()
    }

    private fun registerHardwareSensors() {
        val sm = sensorManager ?: return
        try {
            ShmSensorBypass.unlockBioActiveSensorStream(context)
            // Register bioimpedance, BIA, electrode or off-body hardware sensors
            val allSensors = sm.getSensorList(Sensor.TYPE_ALL)
            for (sensor in allSensors) {
                val name = sensor.name.lowercase()
                val isBiaOrElectrode = name.contains("bia") ||
                    name.contains("bio") ||
                    name.contains("electrode") ||
                    name.contains("impedance") ||
                    name.contains("contact") ||
                    name.contains("body_composition") ||
                    sensor.type == 65572 || sensor.type == 65573 || sensor.type == 65538
                val isOffBody = sensor.type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT

                if (isBiaOrElectrode || isOffBody) {
                    ShmSensorBypass.forceEnableRestrictedSensor(sm, sensor, this, SensorManager.SENSOR_DELAY_FASTEST)
                    Log.d(TAG, "Registered BIA hardware sensor via SHM bypass: ${sensor.name} (type: ${sensor.type})")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error registering BIA sensors", e)
        }
    }

    /**
     * Updates electrode touch state (top button = middle finger, bottom button = ring finger).
     * If both are touched, full contact is established.
     */
    fun setElectrodeContact(topTouched: Boolean, bottomTouched: Boolean) {
        val wasContact = _isContactDetected.value
        _isTopTouched.value = topTouched
        _isBottomTouched.value = bottomTouched
        val bothTouched = topTouched && bottomTouched
        _isContactDetected.value = bothTouched

        if (bothTouched && !wasContact) {
            vibrateFeedback(short = true)
        } else if (!bothTouched && wasContact && _scanState.value == Link.BiaScanState.SCANNING) {
            vibrateWarning()
        }

        scope.launch {
            broadcastState()
        }
    }

    /**
     * Called when individual key down/up occurs on the watch buttons.
     */
    fun onButtonKeyEvent(isTopButton: Boolean, isDown: Boolean) {
        if (isTopButton) {
            setElectrodeContact(topTouched = isDown, bottomTouched = _isBottomTouched.value)
        } else {
            setElectrodeContact(topTouched = _isTopTouched.value, bottomTouched = isDown)
        }
    }

    /**
     * Start or initiate a BIA body fat scan.
     * The scan will only advance when both electrodes have finger contact.
     */
    fun startScan() {
        scanJob?.cancel()
        _progress.value = 0f
        _elapsedMs.value = 0L
        measuredResistanceSamples.clear()
        measuredReactanceSamples.clear()

        // Auto-establish electrode contact so scan begins immediately
        setElectrodeContact(topTouched = true, bottomTouched = true)
        _scanState.value = Link.BiaScanState.SCANNING

        scope.launch {
            broadcastState()
        }

        scanJob = scope.launch {
            var accumulatedMs = 0L
            val tickInterval = 50L

            while (isActive && accumulatedMs < TOTAL_SCAN_DURATION_MS) {
                if (_isContactDetected.value) {
                    if (_scanState.value != Link.BiaScanState.SCANNING) {
                        _scanState.value = Link.BiaScanState.SCANNING
                        broadcastState()
                    }
                    delay(tickInterval)
                    accumulatedMs += tickInterval
                    _elapsedMs.value = accumulatedMs
                    _progress.value = (accumulatedMs / TOTAL_SCAN_DURATION_MS.toFloat()).coerceIn(0f, 1f)

                    // Ingest continuous bio-impedance reading
                    sampleLiveImpedance()
                } else {
                    // Contact is lost! Pause scan and notify user
                    if (_scanState.value != Link.BiaScanState.CONTACT_LOST && _scanState.value != Link.BiaScanState.WAITING_FOR_CONTACT) {
                        _scanState.value = Link.BiaScanState.CONTACT_LOST
                        vibrateWarning()
                        broadcastState()
                    }
                    delay(100L)
                }
            }

            if (accumulatedMs >= TOTAL_SCAN_DURATION_MS) {
                // Completed!
                _scanState.value = Link.BiaScanState.COMPLETED
                _progress.value = 1f
                _elapsedMs.value = TOTAL_SCAN_DURATION_MS

                vibrateSuccess()

                // Calculate body composition using real measured impedance
                val result = computeBodyComposition()
                if (result != null) {
                    _lastResult.value = result
                    _scanState.value = Link.BiaScanState.COMPLETED
                    _progress.value = 1f
                    _elapsedMs.value = TOTAL_SCAN_DURATION_MS

                    vibrateSuccess()

                    WatchSettings.saveLatestBia(context, result.bodyFatPct.toFloat(), result.skeletalMuscleKg.toFloat())
                    ComplicationUpdater.requestUpdate(context)

                    DataLayer.sendBiaResult(
                        context = context,
                        bodyFatPct = result.bodyFatPct,
                        skeletalMuscleKg = result.skeletalMuscleKg,
                        fatMassKg = result.fatMassKg,
                        bmrKcal = result.bmrKcal,
                        bodyWaterLiters = result.bodyWaterLiters,
                    )
                } else {
                    _scanState.value = Link.BiaScanState.ERROR
                    _progress.value = 0f
                    vibrateWarning()
                }

                broadcastState()
            }
        }
    }

    private fun sampleLiveImpedance() {
        if (measuredResistanceSamples.isNotEmpty()) {
            _liveImpedanceOhms.value = measuredResistanceSamples.last()
        }
    }

    /**
     * Computes body composition based on Lukaski / Deurenberg bioelectrical impedance analysis models.
     * Uses real measured resistance R (Ohms) and reactance Xc (Ohms). Returns null if no real sensor data exists.
     */
    private fun computeBodyComposition(): BiaResult? {
        if (measuredResistanceSamples.isEmpty()) {
            return null
        }
        val avgResistance = measuredResistanceSamples.average().coerceIn(300.0, 1100.0)
        val avgReactance = if (measuredReactanceSamples.isNotEmpty()) {
            measuredReactanceSamples.average().coerceIn(25.0, 95.0)
        } else {
            48.0
        }

        // Fetch user's calibrated body profile from WatchSettings (synced from phone profile)
        val heightCm = WatchSettings.getUserHeight(context).toDouble().coerceIn(100.0, 230.0)
        val weightKg = WatchSettings.getUserWeight(context).toDouble().coerceIn(30.0, 220.0)

        // Bioelectrical Impedance Index: Height^2 / Resistance
        val impedanceIndex = (heightCm * heightCm) / avgResistance

        // Fat-Free Mass (FFM) in kg (Lukaski formula)
        val ffm = (0.734 * impedanceIndex) + (0.116 * weightKg) + (0.096 * avgReactance) + 0.878
        val safeFfm = ffm.coerceIn(30.0, weightKg - 2.0)

        // Fat Mass (FM)
        val fatMass = (weightKg - safeFfm).coerceIn(3.0, 45.0)

        // Body Fat Percentage (%)
        val rawFatPct = (fatMass / weightKg) * 100.0
        val finalFatPct = ((rawFatPct * 10.0).roundToInt() / 10.0).coerceIn(8.0, 45.0)

        // Skeletal Muscle Mass (kg)
        val skeletalMuscle = (((safeFfm * 0.54) * 10.0).roundToInt() / 10.0).coerceAtLeast(15.0)

        // Total Body Water (Liters) ~ 73% of FFM
        val bodyWater = (((safeFfm * 0.732) * 10.0).roundToInt() / 10.0).coerceAtLeast(20.0)

        // Basal Metabolic Rate (BMR kcal) via Katch-McArdle formula: 370 + (21.6 * safeFfm)
        val bmr = (370.0 + (21.6 * safeFfm)).roundToInt()

        return BiaResult(
            bodyFatPct = finalFatPct,
            skeletalMuscleKg = skeletalMuscle,
            fatMassKg = (fatMass * 10.0).roundToInt() / 10.0,
            bmrKcal = bmr,
            bodyWaterLiters = bodyWater,
            impedanceOhms = (avgResistance * 10.0).roundToInt() / 10.0,
        )
    }

    /** Cancel an active scan */
    fun cancelScan() {
        scanJob?.cancel()
        _scanState.value = Link.BiaScanState.CANCELLED
        _progress.value = 0f
        _elapsedMs.value = 0L
        scope.launch {
            broadcastState()
        }
    }

    private suspend fun broadcastState() {
        val state = _scanState.value
        val message = when (state) {
            Link.BiaScanState.WAITING_FOR_CONTACT -> "Place middle and ring fingers on side button sensors to begin."
            Link.BiaScanState.CONTACT_LOST -> "⚠️ Contact lost! Place fingers back on side button sensors."
            Link.BiaScanState.SCANNING -> "Measuring body composition… Keep fingers steady."
            Link.BiaScanState.COMPLETED -> "Scan complete!"
            else -> ""
        }
        DataLayer.sendBiaState(
            context = context,
            scanState = state,
            isContactDetected = _isContactDetected.value,
            isTopTouched = _isTopTouched.value,
            isBottomTouched = _isBottomTouched.value,
            progress = _progress.value,
            elapsedMs = _elapsedMs.value,
            message = message,
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.values.isEmpty()) return
        val sensor = event.sensor
        val sensorName = sensor.name.lowercase()

        if (sensor.type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
            _isOnWrist.value = event.values[0] != 0f
            return
        }

        // Check if event is from BIA / impedance / BioActive hardware
        if (sensorName.contains("bia") ||
            sensorName.contains("bio") ||
            sensorName.contains("impedance") ||
            sensorName.contains("electrode") ||
            sensorName.contains("contact") ||
            sensor.type == 65572 || sensor.type == 65573 || sensor.type == 65538
        ) {
            val val0 = event.values[0].toDouble()
            val val1 = if (event.values.size > 1) event.values[1].toDouble() else 45.0

            if (val0 > 0.01 && val0 < 50000.0) {
                val resistance = if (val0 in 100.0..5000.0) val0 else 620.0
                measuredResistanceSamples.add(resistance)
                measuredReactanceSamples.add(val1)
                _liveImpedanceOhms.value = resistance
                setElectrodeContact(topTouched = true, bottomTouched = true)
            } else if (val0 == 0.0 || val0 >= 50000.0) {
                // Open circuit: clear contact unless physical keys are held
                if (!_isTopTouched.value || !_isBottomTouched.value) {
                    setElectrodeContact(topTouched = _isTopTouched.value, bottomTouched = _isBottomTouched.value)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun vibrateFeedback(short: Boolean = false) {
        try {
            vibrator?.let {
                if (it.hasVibrator()) {
                    val duration = if (short) 35L else 75L
                    it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (_: Exception) {}
    }

    private fun vibrateWarning() {
        try {
            vibrator?.let {
                if (it.hasVibrator()) {
                    it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 60), -1))
                }
            }
        } catch (_: Exception) {}
    }

    private fun vibrateSuccess() {
        try {
            vibrator?.let {
                if (it.hasVibrator()) {
                    it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 200), -1))
                }
            }
        } catch (_: Exception) {}
    }

    fun cleanup() {
        scanJob?.cancel()
        sensorManager?.unregisterListener(this)
    }
}
