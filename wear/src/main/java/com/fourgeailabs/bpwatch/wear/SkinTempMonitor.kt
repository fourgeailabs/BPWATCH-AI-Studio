package com.fourgeailabs.bpwatch.wear

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Skin temperature sensor listener on Wear OS (Samsung Galaxy Watch / Wear OS 3+).
 * Reads ambient, body, or skin temperature hardware sensors.
 */
class SkinTempMonitor(context: Context) : SensorEventListener {

    val appContext: Context = context.applicationContext

    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var tempSensor: Sensor? = null

    var lastCelsius: Float? = null
        private set

    init {
        findTempSensor()
    }

    private fun findTempSensor() {
        val sm = sensorManager ?: return
        val allSensors = sm.getSensorList(Sensor.TYPE_ALL)
        for (s in allSensors) {
            val name = s.name.lowercase()
            if (s.type == Sensor.TYPE_AMBIENT_TEMPERATURE ||
                s.type == 13 /* TYPE_TEMPERATURE */ ||
                s.type == 65578 /* TYPE_SKIN_TEMPERATURE */ ||
                name.contains("skin_temp") ||
                name.contains("temperature") ||
                name.contains("temp")
            ) {
                tempSensor = s
                Log.d("SkinTempMonitor", "Found skin temp sensor: ${s.name} (type ${s.type})")
                break
            }
        }
    }

    val available: Boolean get() = tempSensor != null

    fun start() {
        val s = tempSensor ?: return
        try {
            sensorManager?.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL)
        } catch (e: Exception) {
            Log.w("SkinTempMonitor", "Error registering temp sensor", e)
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
        if (raw in 20.0f..45.0f) {
            lastCelsius = raw
        } else if (raw in -5.0f..10.0f) {
            // Delta from 37°C baseline
            lastCelsius = 37.0f + raw
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
