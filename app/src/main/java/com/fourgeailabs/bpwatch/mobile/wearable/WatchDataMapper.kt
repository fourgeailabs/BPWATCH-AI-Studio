package com.fourgeailabs.bpwatch.mobile.wearable

import android.content.Context
import android.util.Log
import com.fourgeailabs.bpwatch.mobile.BpRepository
import com.fourgeailabs.bpwatch.mobile.data.HealthLog
import com.fourgeailabs.bpwatch.mobile.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale

/**
 * Mapping utility in the mobile app that translates raw sensor data packets
 * received from the watch into Android Health Connect record types
 * (SleepSessionRecord, BodyFatRecord, SkinTemperatureRecord) and persists them locally.
 */
object WatchDataMapper {
    private const val TAG = "WatchDataMapper"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun mapAndPersistSleep(context: Context, startMs: Long, endMs: Long, minutes: Int) {
        scope.launch {
            try {
                val start = Instant.ofEpochMilli(startMs)
                val end = Instant.ofEpochMilli(endMs)
                
                // 1. Write SleepSessionRecord to Health Connect
                try {
                    val hc = HealthConnectManager(context)
                    hc.writeSleepSession(start, end, "Watch Sleep Session")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write SleepSessionRecord to Health Connect", e)
                }

                // 2. Write to local repository
                try {
                    val repo = BpRepository.get(context)
                    val hrs = minutes / 60.0
                    repo.addHealthLog(
                        HealthLog(
                            timestamp = endMs,
                            kind = "sleep",
                            value = hrs,
                            label = String.format(Locale.US, "%.1f hrs", hrs),
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write sleep to local repository", e)
                }
                Log.i(TAG, "Mapped and persisted sleep session: $minutes minutes")
            } catch (e: Exception) {
                Log.e(TAG, "Error mapping sleep packet", e)
            }
        }
    }

    fun mapAndPersistBodyFat(context: Context, bodyFatPct: Double, skeletalMuscleKg: Double, fatMassKg: Double, bmrKcal: Int, bodyWaterLiters: Double) {
        scope.launch {
            try {
                val now = Instant.now()

                // 1. Write BodyFatRecord to Health Connect
                try {
                    val hc = HealthConnectManager(context)
                    hc.writeBodyFat(bodyFatPct, now)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write BodyFatRecord to Health Connect", e)
                }

                // 2. Write to local repository
                try {
                    val repo = BpRepository.get(context)
                    repo.addHealthLog(
                        HealthLog(
                            timestamp = System.currentTimeMillis(),
                            kind = "body_fat",
                            value = bodyFatPct,
                            label = String.format(Locale.US, "Body Fat: %.1f%% (Muscle: %.1fkg)", bodyFatPct, skeletalMuscleKg),
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write body fat to local repository", e)
                }
                Log.i(TAG, "Mapped and persisted body fat: $bodyFatPct%")
            } catch (e: Exception) {
                Log.e(TAG, "Error mapping body fat packet", e)
            }
        }
    }

    fun mapAndPersistSkinTemp(context: Context, skinTempC: Float, timestamp: Long) {
        scope.launch {
            try {
                val time = Instant.ofEpochMilli(timestamp)

                // 1. Write SkinTemperatureRecord to Health Connect via HealthConnectManager
                try {
                    val hc = HealthConnectManager(context)
                    hc.writeSkinTemperature(skinTempC, time)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write SkinTemperatureRecord to Health Connect", e)
                }

                // 2. Write to local repository
                try {
                    val repo = BpRepository.get(context)
                    repo.addHealthLog(
                        HealthLog(
                            timestamp = timestamp,
                            kind = "skin_temp",
                            value = skinTempC.toDouble(),
                            label = String.format(Locale.US, "Skin Temp: %.1f°C", skinTempC),
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write skin temp to local repository", e)
                }
                Log.i(TAG, "Mapped and persisted skin temperature: $skinTempC°C")
            } catch (e: Exception) {
                Log.e(TAG, "Error mapping skin temp packet", e)
            }
        }
    }

    fun mapAndPersistEkg(context: Context, voltages: FloatArray, classification: String, timestamp: Long) {
        scope.launch {
            try {
                // Write to local repository
                try {
                    val repo = BpRepository.get(context)
                    repo.addHealthLog(
                        HealthLog(
                            timestamp = timestamp,
                            kind = "ekg",
                            value = if (classification.contains("Sinus", true)) 1.0 else 0.0,
                            label = "EKG: $classification (${voltages.size} samples)",
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write EKG to local repository", e)
                }
                Log.i(TAG, "Mapped and persisted EKG: $classification with ${voltages.size} samples")
            } catch (e: Exception) {
                Log.e(TAG, "Error mapping EKG packet", e)
            }
        }
    }
}
