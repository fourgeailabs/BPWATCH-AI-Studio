package com.fourgeailabs.bpwatch.mobile.wearable

import android.content.Context
import com.fourgeailabs.bpwatch.Link
import com.fourgeailabs.bpwatch.mobile.monitoring.MonitoringConfig
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Pushes the phone's "Monitoring & alerts" settings to the watch over the
 * Data Layer. Sent immediately whenever settings change, and re-sent to a
 * node whenever it delivers a heart-rate reading (covers watch reconnects
 * and app reinstalls — the watch persists the config itself).
 */
object WatchConfigSender {

    /** Sends to every currently-connected watch node. */
    suspend fun sendToAll(context: Context, config: MonitoringConfig): Boolean {
        return try {
            val payload = toPayload(context, config)
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isEmpty()) return false
            nodes.forEach { node ->
                sendToNode(context, node.id, payload)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Sends to one specific node (e.g. the node a reading just came from). */
    suspend fun sendToNode(context: Context, nodeId: String, config: MonitoringConfig) {
        sendToNode(context, nodeId, toPayload(context, config))
    }

    private suspend fun sendToNode(context: Context, nodeId: String, payload: ByteArray) {
        try {
            Wearable.getMessageClient(context)
                .sendMessage(nodeId, Link.PATH_MONITORING_CONFIG, payload)
                .await()
        } catch (_: Exception) {
            // Watch out of range — it'll pick the config up on the next
            // reading it manages to deliver.
        }
    }

    /**
     * Sends the wrist position preference to every connected watch so
     * posture and body side location calculations stay accurate.
     */
    suspend fun sendWristSet(context: Context, wrist: String): Boolean {
        return try {
            val payload = DataMap().apply {
                putString(Link.KEY_WRIST, wrist)
            }.toByteArray()
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isEmpty()) return false
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, Link.PATH_WRIST_SET, payload)
                    .await()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Sends the continuous-recording toggle (PATH_HR_RECORD_SET) to every
     * connected watch. The phone is the source of truth — this goes out on
     * every toggle change and on every full sync, so the watch always ends
     * up matching the phone.
     */
    suspend fun sendRecordSet(context: Context, enabled: Boolean): Boolean {
        return try {
            val payload = DataMap().apply {
                putBoolean(Link.KEY_HR_RECORD, enabled)
            }.toByteArray()
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isEmpty()) return false
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, Link.PATH_HR_RECORD_SET, payload)
                    .await()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun toPayload(context: Context, config: MonitoringConfig): ByteArray =
        DataMap().apply {
            putInt(Link.KEY_CONFIG_V, 1)
            putBoolean(Link.KEY_CONTINUOUS_HR, config.continuousHr)
            putBoolean(Link.KEY_HR_HIGH_ENABLED, config.hrHighEnabled)
            putInt(Link.KEY_HR_HIGH_THRESHOLD, config.hrHighThreshold)
            putInt(Link.KEY_BP_INTERVAL_MIN, config.bpIntervalMinutes)
            putBoolean(Link.KEY_BP_HIGH_ENABLED, config.bpHighEnabled)
            putInt(Link.KEY_SYS_HIGH, config.sysHigh)
            putInt(Link.KEY_DIA_HIGH, config.diaHigh)
            putBoolean(Link.KEY_BP_LOW_ENABLED, config.bpLowEnabled)
            putInt(Link.KEY_SYS_LOW, config.sysLow)
            putInt(Link.KEY_DIA_LOW, config.diaLow)
            val prefs = com.fourgeailabs.bpwatch.mobile.monitoring.MonitoringPrefs(context)
            putString(Link.KEY_WRIST, prefs.wrist.value)
            putInt(Link.KEY_BATTERY_SAVER_THRESHOLD, prefs.batterySaverThreshold.value)
            putBoolean(Link.KEY_BATTERY_SAVER_ENABLED, prefs.batterySaverEnabled.value)
        }.toByteArray()
}
