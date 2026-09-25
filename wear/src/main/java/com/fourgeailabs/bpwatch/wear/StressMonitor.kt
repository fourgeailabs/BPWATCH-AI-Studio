package com.fourgeailabs.bpwatch.wear

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * SHM-MOD native stress sensor monitor for Samsung Galaxy Watch / Wear OS.
 * Bypasses Samsung Health sensor gatekeeping to read hardware stress levels (0-100)
 * directly from the BioActive sensor via reflection.
 */
class StressMonitor(context: Context) : SensorEventListener {

    val appContext: Context = context.applicationContext

    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var stressSensor: Sensor? = null

    var lastStressLevel: Int? = null
        private set

    private val readings = mutableListOf<Float>()

    init {
        findStressSensor()
    }

    private fun findStressSensor() {
        val sm = sensorManager ?: return
        val allSensors = sm.getSensorList(Sensor.TYPE_ALL)
        for (s in allSensors) {
            val name = s.name.lowercase()
            if (s.type == 65576 ||
                s.type == 65577 ||
                s.type == 65579 ||
                s.type.toString() == "10012" ||
                name.contains("stress") ||
                name.contains("samsung.sensor.stress") ||
                name.contains("bioactive_stress")
            ) {
                stressSensor = s
                Log.d("StressMonitor", "Found native SHM-MOD stress sensor: ${s.name} (type ${s.type})")
                break
            }
        }
    }

    val available: Boolean get() = stressSensor != null

    fun start() {
        val s = stressSensor ?: return
        val sm = sensorManager ?: return
        try {
            ShmSensorBypass.forceEnableRestrictedSensor(sm, s, this, SensorManager.SENSOR_DELAY_UI)
        } catch (e: Exception) {
            Log.w("StressMonitor", "Error registering native stress sensor", e)
        }
    }

    fun stop() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.values.isEmpty()) return
        val raw = event.values[0]
        val score = when {
            raw in 0.0f..100.0f -> raw.toInt()
            raw in 100.0f..1000.0f -> (raw / 10.0f).toInt().coerceIn(0, 100)
            else -> null
        }
        if (score != null) {
            lastStressLevel = score
            readings.add(score.toFloat())
            Log.d("StressMonitor", "Native SHM-MOD stress level update: $score")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getAverageOrLast(): Int? {
        if (readings.isNotEmpty()) {
            return readings.average().toInt().coerceIn(0, 100)
        }
        return lastStressLevel
    }
}
