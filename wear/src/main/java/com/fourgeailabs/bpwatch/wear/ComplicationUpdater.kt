package com.fourgeailabs.bpwatch.wear

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

/**
 * Refreshes watch-face complications (BP, HR, Stress, BIA, HRV) and Wear OS
 * tiles when new data lands. Called directly from data arrival points. Never throws.
 */
object ComplicationUpdater {

    fun requestUpdate(context: Context) {
        val appCtx = context.applicationContext
        updateComplication(appCtx, BpComplicationService::class.java)
        updateComplication(appCtx, HrComplicationService::class.java)
        updateComplication(appCtx, StressComplicationService::class.java)
        updateComplication(appCtx, BiaComplicationService::class.java)
        updateComplication(appCtx, HrvComplicationService::class.java)

        updateTile(appCtx, BpTileService::class.java)
        updateTile(appCtx, BiaTileService::class.java)
        updateTile(appCtx, HrvTileService::class.java)
    }

    private fun updateComplication(context: Context, service: Class<*>) {
        try {
            ComplicationDataSourceUpdateRequester.create(
                context,
                ComponentName(context, service),
            ).requestUpdateAll()
        } catch (_: Exception) {
        }
    }

    private fun updateTile(context: Context, service: Class<out TileService>) {
        try {
            TileService.getUpdater(context)
                .requestUpdate(service)
        } catch (_: Exception) {
        }
    }
}
