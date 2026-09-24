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
import java.util.Locale

/**
 * Watch-face complication for Body Fat % / BIA Bioimpedance.
 * Supports SHORT_TEXT and RANGED_VALUE types.
 * Tapping opens the BIA Scan tool in BPWatch.
 */
class BiaComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        buildComplicationData(type, "18.5%", 18.5f, "Body Fat 18.5%")

    override suspend fun onComplicationRequest(
        request: ComplicationRequest,
    ): ComplicationData = try {
        val bodyFat = WatchSettings.loadLatestBodyFat(this)
        if (bodyFat > 0f) {
            val formatted = String.format(Locale.US, "%.1f%%", bodyFat)
            buildComplicationData(
                request.complicationType,
                formatted,
                bodyFat,
                "Body Fat $formatted",
            )
        } else {
            buildComplicationData(
                request.complicationType,
                "—",
                0f,
                "Body Fat — no scans yet",
            )
        }
    } catch (_: Exception) {
        buildComplicationData(
            request.complicationType,
            "—",
            0f,
            "Body Fat — no scans yet",
        )
    }

    private fun buildComplicationData(
        type: ComplicationType,
        text: String,
        value: Float,
        contentDescription: String,
    ): ComplicationData {
        val titleText = PlainComplicationText.Builder("FAT").build()
        val mainText = PlainComplicationText.Builder(text).build()
        val descText = PlainComplicationText.Builder(contentDescription).build()
        val tapAction = openBiaScanAction()

        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = value.coerceIn(0f, 100f),
                    min = 0f,
                    max = 50f,
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

    private fun openBiaScanAction(): PendingIntent =
        PendingIntent.getActivity(
            this,
            101,
            Intent(this, MainActivity::class.java).apply {
                putExtra("action", "bia_scan")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
