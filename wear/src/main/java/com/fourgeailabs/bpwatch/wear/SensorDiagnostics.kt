package com.fourgeailabs.bpwatch.wear

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Diagnostic log utility in the wearable module.
 * Checks permission availability and hardware SensorManager availability for
 * Body Fat (BIA/impedance), Skin Temperature, Sleep, and Heart Rate sensors
 * to pinpoint why sensors may fail to populate data.
 */
object SensorDiagnostics {
    private const val TAG = "SensorDiagnostics"

    fun runDiagnostics(context: Context) {
        Log.i(TAG, "=== BPWatch Wearable Sensor & Permission Diagnostics ===")

        // 1. Check Permissions
        val permissions = listOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            "android.permission.READ_BODY_VITAL_SIGNS"
        )

        for (perm in permissions) {
            val granted = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            Log.i(TAG, "Permission $perm: ${if (granted) "GRANTED" else "DENIED"}")
        }

        // 2. Check Hardware Sensors via SensorManager
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sm == null) {
            Log.e(TAG, "SensorManager service is unavailable!")
            return
        }

        val allSensors = sm.getSensorList(Sensor.TYPE_ALL)
        Log.i(TAG, "Total hardware sensors available: ${allSensors.size}")

        var hasHeartRate = false
        var hasSkinTemp = false
        var hasBiaOrImpedance = false
        var hasOffBody = false

        for (s in allSensors) {
            val name = s.name.lowercase()
            val type = s.type

            if (type == Sensor.TYPE_HEART_RATE) hasHeartRate = true
            if (type == Sensor.TYPE_AMBIENT_TEMPERATURE || type == 65578 || name.contains("skin_temp") || name.contains("temperature")) hasSkinTemp = true
            if (name.contains("bia") || name.contains("bio") || name.contains("impedance") || name.contains("electrode") || type == 65572 || type == 65573 || type == 65538) hasBiaOrImpedance = true
            if (type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) hasOffBody = true

            Log.d(TAG, "Sensor -> Name: ${s.name} | Type: $type | Vendor: ${s.vendor}")
        }

        Log.i(TAG, "--- Diagnostic Summary ---")
        Log.i(TAG, "Heart Rate Sensor Present: $hasHeartRate")
        Log.i(TAG, "Skin Temperature Sensor Present: $hasSkinTemp")
        Log.i(TAG, "BIA / Body Composition Sensor Present: $hasBiaOrImpedance")
        Log.i(TAG, "Off-Body Sensor Present: $hasOffBody")
        Log.i(TAG, "=======================================================")
    }
}
