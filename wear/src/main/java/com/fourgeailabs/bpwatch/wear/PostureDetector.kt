package com.fourgeailabs.bpwatch.wear

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log
import com.fourgeailabs.bpwatch.Link
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Sensor data integration to detect posture (sitting, standing, lying down)
 * and physical activity (walking, stationary) during a blood pressure measurement.
 *
 * Uses the watch's Accelerometer and Step Detector to evaluate:
 * - Dynamic acceleration variance & cadence (walking detection)
 * - Static gravity orientation of the forearm (sitting vs. standing vs. lying down)
 */
class PostureDetector(private val context: Context) : SensorEventListener {

    data class Evaluation(
        val bodyPosition: Int,     // Link.Posture.*
        val activity: String,      // Link.ActivityState.*
        val confidence: Float,
        val stepsDetected: Int,
    )

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val stepDetector =
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val samples = mutableListOf<FloatArray>()
    private var stepCount = 0
    @Volatile private var isRunning = false

    fun start(handler: Handler? = null) {
        if (sensorManager == null || accelerometer == null) {
            Log.w(TAG, "Accelerometer not available on this device")
            return
        }
        synchronized(samples) {
            samples.clear()
            stepCount = 0
            isRunning = true
        }
        val accelDelay = WatchBatterySaver.getAccelerometerDelay(context)
        val registeredAccel = if (handler != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                accelDelay,
                handler,
            )
        } else {
            sensorManager.registerListener(
                this,
                accelerometer,
                accelDelay,
            )
        }
        Log.d(TAG, "Accelerometer registered: $registeredAccel (delay: $accelDelay)")

        val stepDelay = if (WatchBatterySaver.isBatterySaverActive(context)) {
            SensorManager.SENSOR_DELAY_NORMAL
        } else {
            SensorManager.SENSOR_DELAY_FASTEST
        }
        stepDetector?.let { stepSensor ->
            if (handler != null) {
                sensorManager.registerListener(
                    this,
                    stepSensor,
                    stepDelay,
                    handler,
                )
            } else {
                sensorManager.registerListener(
                    this,
                    stepSensor,
                    stepDelay,
                )
            }
        }
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering sensor listeners: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isRunning) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                synchronized(samples) {
                    stepCount++
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                synchronized(samples) {
                    // Limit buffer to avoid excessive memory on long runs
                    if (samples.size < 3000) {
                        samples.add(floatArrayOf(x, y, z))
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun evaluate(): Evaluation {
        val sampleList: List<FloatArray>
        val steps: Int
        synchronized(samples) {
            sampleList = samples.toList()
            steps = stepCount
        }

        if (sampleList.isEmpty()) {
            return Evaluation(
                bodyPosition = Link.Posture.SITTING_DOWN,
                activity = Link.ActivityState.SITTING,
                confidence = 0.5f,
                stepsDetected = steps,
            )
        }

        // Calculate mean gravity vector [meanX, meanY, meanZ]
        var sumX = 0.0
        var sumY = 0.0
        var sumZ = 0.0
        val magnitudes = DoubleArray(sampleList.size)
        var sumMag = 0.0

        for (i in sampleList.indices) {
            val s = sampleList[i]
            val x = s[0].toDouble()
            val y = s[1].toDouble()
            val z = s[2].toDouble()
            sumX += x
            sumY += y
            sumZ += z
            val m = sqrt(x * x + y * y + z * z)
            magnitudes[i] = m
            sumMag += m
        }

        val n = sampleList.size.toDouble()
        val meanX = sumX / n
        val meanY = sumY / n
        val meanZ = sumZ / n
        val meanMag = sumMag / n

        // Retrieve configured wrist to adjust sensor orientation bias in processing algorithms
        val wrist = WatchSettings.getWrist(context)
        val isRightWrist = wrist.equals(WatchSettings.WRIST_RIGHT, ignoreCase = true)
        // Adjust lateral sensor orientation bias for right-wrist mirror geometry
        val lateralBias = if (isRightWrist) -1.0 else 1.0
        val adjustedMeanX = meanX * lateralBias
        val rollDeg = Math.toDegrees(kotlin.math.atan2(adjustedMeanX, meanZ))
        val pitchDeg = Math.toDegrees(kotlin.math.atan2(meanY, sqrt(adjustedMeanX * adjustedMeanX + meanZ * meanZ)))
        Log.d(TAG, "Sensor orientation bias evaluated: wrist=$wrist (lateralBias=$lateralBias), adjX=${"%.2f".format(adjustedMeanX)}, roll=${"%.1f".format(rollDeg)}°, pitch=${"%.1f".format(pitchDeg)}°")

        // Variance of dynamic acceleration magnitude (indicates motion/walking)
        var varSum = 0.0
        for (m in magnitudes) {
            val diff = m - meanMag
            varSum += diff * diff
        }
        val stdDev = sqrt(varSum / n)

        // Count zero crossings of dynamic acceleration (cadence estimation)
        var zeroCrossings = 0
        var prevSign = 0
        for (m in magnitudes) {
            val diff = m - meanMag
            val sign = if (diff > 0.4) 1 else if (diff < -0.4) -1 else 0
            if (sign != 0 && prevSign != 0 && sign != prevSign) {
                zeroCrossings++
            }
            if (sign != 0) {
                prevSign = sign
            }
        }

        val isWalkingMotion = steps >= 2 || (stdDev > 1.3 && zeroCrossings >= 8)

        if (isWalkingMotion) {
            Log.i(TAG, "Evaluated: WALKING (steps=$steps, stdDev=${"%.2f".format(stdDev)}, zc=$zeroCrossings)")
            return Evaluation(
                bodyPosition = Link.Posture.STANDING_UP,
                activity = Link.ActivityState.WALKING,
                confidence = 0.9f,
                stepsDetected = steps,
            )
        }

        // Stationary postures:
        // On wear devices: Y axis points along the forearm towards the 12 o'clock / elbow.
        // Standing with arm hanging down: gravity aligns with Y axis (abs(meanY) >= 6.5 m/s^2).
        val absMeanY = abs(meanY)
        val absMeanZ = abs(meanZ)

        if (absMeanY >= 6.5) {
            Log.i(TAG, "Evaluated: STANDING (meanY=${"%.2f".format(meanY)}, stdDev=${"%.2f".format(stdDev)})")
            return Evaluation(
                bodyPosition = Link.Posture.STANDING_UP,
                activity = Link.ActivityState.STANDING,
                confidence = 0.85f,
                stepsDetected = steps,
            )
        }

        // Reclining: torso/arm inclined between horizontal and vertical (pitch 20°..60°, low variance)
        if (pitchDeg in 20.0..60.0 && abs(rollDeg) < 55.0 && stdDev < 0.35) {
            Log.i(TAG, "Evaluated: RECLINING (pitch=${"%.1f".format(pitchDeg)}°, roll=${"%.1f".format(rollDeg)}°, wrist=$wrist)")
            return Evaluation(
                bodyPosition = Link.Posture.RECLINING,
                activity = Link.ActivityState.SITTING,
                confidence = 0.85f,
                stepsDetected = steps,
            )
        }

        // Lying down: body is horizontal, arm is resting flat on bed/chest, watch screen is facing up/flat (absMeanZ >= 7.0, low Y, very low stdDev)
        if (absMeanZ >= 7.0 && absMeanY < 4.0 && stdDev < 0.25) {
            Log.i(TAG, "Evaluated: LYING_DOWN (meanZ=${"%.2f".format(meanZ)}, meanY=${"%.2f".format(meanY)})")
            return Evaluation(
                bodyPosition = Link.Posture.LYING_DOWN,
                activity = Link.ActivityState.LYING_DOWN,
                confidence = 0.8f,
                stepsDetected = steps,
            )
        }

        // Sitting: forearm resting on desk, lap, or bent at elbow at chest/heart level
        Log.i(TAG, "Evaluated: SITTING (meanY=${"%.2f".format(meanY)}, meanZ=${"%.2f".format(meanZ)})")
        return Evaluation(
            bodyPosition = Link.Posture.SITTING_DOWN,
            activity = Link.ActivityState.SITTING,
            confidence = 0.85f,
            stepsDetected = steps,
        )
    }

    companion object {
        private const val TAG = "BPWatch:Posture"
    }
}
