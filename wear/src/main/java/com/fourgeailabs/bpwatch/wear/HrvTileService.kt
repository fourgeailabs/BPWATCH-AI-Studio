package com.fourgeailabs.bpwatch.wear

import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.TimelineBuilders
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Wear OS Tile for Heart Rate Variability (HRV) & Heart Health.
 * Displays latest HRV RMSSD in ms and pulse rate.
 * Tapping the tile opens BPWatch in measurement mode.
 */
class HrvTileService : TileService() {

    companion object {
        private const val RESOURCES_VERSION = "1"
        private const val FRESHNESS_MS = 15 * 60 * 1000L
    }

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<TileBuilders.Tile> = try {
        val hrv = try {
            WatchSettings.loadLatestHrv(this)
        } catch (_: Exception) {
            0f
        }
        val hr = try {
            WatchSettings.loadLatestHr(this)
        } catch (_: Exception) {
            0f
        }
        val ts = try {
            WatchSettings.loadLatestHrvTs(this)
        } catch (_: Exception) {
            0L
        }

        val hasReading = hrv > 0f
        val reading = if (hasReading) "${hrv.toInt()} ms" else "-- ms"
        val whenText = if (hasReading) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
            if (hr > 0f) {
                String.format(Locale.US, "HR: %d bpm · %s", hr.toInt(), time)
            } else {
                "HRV RMSSD · $time"
            }
        } else {
            "Tap to measure HRV"
        }

        immediateFuture(buildTile(requestParams, reading, whenText))
    } catch (_: Exception) {
        immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTimeline(singleTextTimeline("-- ms"))
                .build(),
        )
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> =
        immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build(),
        )

    private fun buildTile(
        requestParams: RequestBuilders.TileRequest,
        reading: String,
        whenText: String,
    ): TileBuilders.Tile {
        val deviceParams = requestParams.deviceParameters
            ?: DeviceParametersBuilders.DeviceParameters.Builder().build()

        val openMeasure = ModifiersBuilders.Clickable.Builder()
            .setId("open_measure")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(packageName)
                            .setClassName(MainActivity::class.java.name)
                            .addKeyToExtraMapping(
                                "action",
                                ActionBuilders.stringExtra("measure"),
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()

        val root = LayoutElementBuilders.Column.Builder()
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(openMeasure)
                    .setSemantics(
                        ModifiersBuilders.Semantics.Builder()
                            .setContentDescription("Heart Rate Variability $reading, $whenText")
                            .build(),
                    )
                    .build(),
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("HRV & HEART HEALTH")
                    .setFontStyle(
                        LayoutElementBuilders.FontStyles.caption1(deviceParams).build(),
                    )
                    .build(),
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(
                        DimensionBuilders.DpProp.Builder().setValue(2f).build(),
                    )
                    .build(),
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(reading)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyles.display2(deviceParams).build(),
                    )
                    .build(),
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(
                        DimensionBuilders.DpProp.Builder().setValue(4f).build(),
                    )
                    .build(),
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(whenText)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyles.title3(deviceParams).build(),
                    )
                    .build(),
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(FRESHNESS_MS)
            .setTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(root)
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()
    }

    private fun singleTextTimeline(text: String): TimelineBuilders.Timeline =
        TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(
                                LayoutElementBuilders.Text.Builder()
                                    .setText(text)
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()

    private fun <T> immediateFuture(value: T): ListenableFuture<T> =
        object : ListenableFuture<T> {
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
            override fun isCancelled(): Boolean = false
            override fun isDone(): Boolean = true
            override fun get(): T = value
            override fun get(timeout: Long, unit: TimeUnit): T = value
            override fun addListener(listener: Runnable, executor: Executor) {
                executor.execute(listener)
            }
        }
}
