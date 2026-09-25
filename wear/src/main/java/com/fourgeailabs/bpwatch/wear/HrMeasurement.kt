package com.fourgeailabs.bpwatch.wear

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.fourgeailabs.bpwatch.Link
import kotlinx.coroutines.delay

/**
 * One self-contained heart-rate sampling run, usable from the UI or from the
 * background hourly check. The sensor listener runs on its own thread so this
 * works without a UI Looper.
 */
object HrMeasurement {
    const val DURATION_MS = 30_000L

    data class Result(
        /** Average heart rate in bpm. */
        val averageHr: Float,
        /** Raw bpm samples (for stress estimation). */
        val samples: List<Float>,
        /**
         * True when the hardware off-body sensor explicitly reported
         * off-body during this run (v2.3). Callers should treat this as
         * "watch not on wrist" and skip recording/alerts.
         */
        val offBody: Boolean = false,
        /** Detected posture (Health Connect BodyPosition constant). */
        val bodyPosition: Int = Link.Posture.SITTING_DOWN,
        /** Detected activity: "sitting", "standing", "walking", "lying_down". */
        val activity: String = Link.ActivityState.SITTING,
        /** Measured skin temperature in Celsius during this check. */
        val skinTempC: Float = 36.6f,
        /** Measured or estimated stress score (0-100). */
        val stressScore: Int = -1,
    )

    /** @return the measurement, or null if no valid samples. */
    suspend fun measure(context: Context, durationMs: Long = DURATION_MS): Result? {
        val monitor = HeartRateMonitor(context)
        if (!monitor.available) return null
        // Start the hardware off-body sensor alongside the HR sensor when
        // present — its lastState is authoritative for this window.
        val offBodySensor = OffBodySensor(context)
        val postureDetector = PostureDetector(context)
        val skinTempMonitor = SkinTempMonitor(context)
        val stressMonitor = StressMonitor(context)
        val samples = mutableListOf<Float>()
        val thread = HandlerThread("bpwatch-hr").apply { start() }
        var postureEval = PostureDetector.Evaluation(
            bodyPosition = Link.Posture.SITTING_DOWN,
            activity = Link.ActivityState.SITTING,
            confidence = 0.5f,
            stepsDetected = 0,
        )
        try {
            monitor.onSample = { hr ->
                if (hr > 0f) samples.add(hr)
            }
            monitor.start(Handler(thread.looper))
            if (offBodySensor.present) offBodySensor.start(Handler(thread.looper))
            postureDetector.start(Handler(thread.looper))
            skinTempMonitor.start()
            stressMonitor.start()
            delay(durationMs)
            postureEval = postureDetector.evaluate()
        } finally {
            monitor.stop()
            offBodySensor.stop()
            postureDetector.stop()
            skinTempMonitor.stop()
            stressMonitor.stop()
            thread.quitSafely()
        }
        val valid = samples.filter { it in 25f..250f }
        if (valid.isEmpty()) return null
        val skinTempC = skinTempMonitor.getAverageOrLast() ?: 36.6f
        val hardwareStress = stressMonitor.getAverageOrLast() ?: -1
        val restingHr = WatchSettings.getRestingHr(context)
        val estimatedStress = StressEstimator.estimate(valid, restingHr)
        val finalStress = if (hardwareStress >= 0) hardwareStress else estimatedStress
        return Result(
            averageHr = valid.average().toFloat(),
            samples = valid,
            offBody = offBodySensor.lastState == false,
            bodyPosition = postureEval.bodyPosition,
            activity = postureEval.activity,
            skinTempC = skinTempC,
            stressScore = finalStress,
        )
    }
}
