package com.fourgeailabs.bpwatch.wear

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Watch-face complication for Heart Rate Variability (HRV RMSSD in ms).
 * Supports SHORT_TEXT and RANGED_VALUE types.
 * Tapping opens BPWatch measurement mode.
 */
class HrvComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        buildComplicationData(type, "45 ms", 45f, "HRV 45 ms RMSSD")

    override suspend fun onComplicationRequest(
        request: ComplicationRequest,
    ): ComplicationData = try {
        val hrv = WatchSettings.loadLatestHrv(this)
        if (hrv > 0f) {
            val formatted = "${hrv.toInt()} ms"
            buildComplicationData(
                request.complicationType,
                formatted,
                hrv,
                "HRV $formatted RMSSD",
            )
        } else {
            buildComplicationData(
                request.complicationType,
                "—",
                0f,
                "HRV — no measurements yet",
            )
        }
    } catch (_: Exception) {
        buildComplicationData(
            request.complicationType,
            "—",
            0f,
            "HRV — no measurements yet",
        )
    }

    private fun buildComplicationData(
        type: ComplicationType,
        text: String,
        value: Float,
        contentDescription: String,
    ): ComplicationData {
        val titleText = PlainComplicationText.Builder("HRV").build()
        val mainText = PlainComplicationText.Builder(text).build()
        val descText = PlainComplicationText.Builder(contentDescription).build()
        val tapAction = openMeasureAction()

        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = value.coerceIn(0f, 150f),
                    min = 0f,
                    max = 120f,
                    contentDescription = descText,
                )
                    .setText(mainText)
                    .setTitle(titleText)
                    .setTapAction(tapAction)
                    .build()
            }
            else -> {
                ShortTextComplicationData.Builder(
                    text = mainText,
                    contentDescription = descText,
                )
                    .setTitle(titleText)
                    .setTapAction(tapAction)
                    .build()
            }
        }
    }

    private fun openMeasureAction(): PendingIntent =
        PendingIntent.getActivity(
            this,
            102,
            Intent(this, MainActivity::class.java).apply {
                putExtra("action", "measure")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
