package com.fourgeailabs.bpwatch.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One blood-pressure tracking record.
 *
 * A record can come from several sources:
 * - "watch": heart rate measured on the Galaxy Watch, BP estimated from calibration
 * - "cuff":  a calibration point (cuff reading + simultaneous watch heart rate)
 * - "manual": hand-entered BP
 */
@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val heartRate: Float? = null,
    val sysCuff: Int? = null,
    val diaCuff: Int? = null,
    val sysEstimate: Int? = null,
    val diaEstimate: Int? = null,
    /** Experimental stress score 0-100 from the watch (-1/NULL = unknown). */
    val stress: Int? = null,
    /** Health Connect BodyPosition constant (e.g. sitting, standing). */
    val bodyPosition: Int? = null,
    /** Health Connect MeasurementLocation constant (e.g. left wrist, right wrist). */
    val measurementLocation: Int? = null,
    /** Sensor-detected activity (e.g. "sitting", "walking", "standing"). */
    val activity: String? = null,
    val source: String = "manual",
)
