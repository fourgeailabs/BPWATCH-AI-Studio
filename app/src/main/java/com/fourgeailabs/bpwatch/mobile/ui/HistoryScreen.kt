package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fourgeailabs.bpwatch.mobile.MainViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val readings by viewModel.readings.collectAsState()
    val snoreEvents by viewModel.observeAllSnoreEvents().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("History", style = MaterialTheme.typography.headlineMedium)

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_tab_row"),
        ) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {
                    Icon(Icons.Filled.Favorite, contentDescription = null)
                },
                label = { Text("Blood Pressure (${readings.size})") },
            )
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {
                    Icon(Icons.Filled.Mic, contentDescription = null)
                },
                label = { Text("Snoring Clips (${snoreEvents.size})") },
            )
        }

        if (selectedTab == 0) {
            // Blood Pressure History
            if (readings.isEmpty()) {
                Text(
                    "Nothing here yet. Your blood pressure readings and estimates will land in this list.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(readings) { r ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = {
                                val sys = r.sysEstimate ?: r.sysCuff
                                val dia = r.diaEstimate ?: r.diaCuff
                                Text(
                                    text = if (sys != null && dia != null) "$sys/$dia mmHg" else "—",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            },
                            supportingContent = {
                                val details = buildList {
                                    r.heartRate?.let { add("${it.toInt()} bpm") }
                                    r.stress?.let { add("stress $it/100") }
                                    val posture = when (r.bodyPosition) {
                                        com.fourgeailabs.bpwatch.Link.Posture.STANDING_UP -> "standing"
                                        com.fourgeailabs.bpwatch.Link.Posture.LYING_DOWN -> "lying down"
                                        com.fourgeailabs.bpwatch.Link.Posture.RECLINING -> "reclining"
                                        com.fourgeailabs.bpwatch.Link.Posture.SITTING_DOWN -> "sitting"
                                        else -> r.activity?.lowercase()
                                    }
                                    posture?.let { add(it) }
                                    val loc = when (r.measurementLocation) {
                                        com.fourgeailabs.bpwatch.Link.MeasurementLocation.RIGHT_WRIST -> "right wrist"
                                        com.fourgeailabs.bpwatch.Link.MeasurementLocation.LEFT_WRIST -> "left wrist"
                                        com.fourgeailabs.bpwatch.Link.MeasurementLocation.RIGHT_UPPER_ARM -> "right arm"
                                        com.fourgeailabs.bpwatch.Link.MeasurementLocation.LEFT_UPPER_ARM -> "left arm"
                                        else -> null
                                    }
                                    loc?.let { add(it) }
                                    add(formatDateTime(r.timestamp))
                                }.joinToString("  ·  ")
                                Text(details, style = MaterialTheme.typography.bodySmall)
                            },
                            trailingContent = {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                ) {
                                    Text(
                                        text = sourceLabel(r.source, r.sysEstimate != null),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
        } else {
            // Sleep & Snoring Audio Recordings History
            if (snoreEvents.isEmpty()) {
                Text(
                    "No snoring audio recordings found in your sleep history. Detected snoring audio clips from overnight sleep sessions will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return
            }

            val totalDurationMin = (snoreEvents.sumOf { it.durationMs }) / 60_000
            Text(
                "${snoreEvents.size} recorded audio clip${if (snoreEvents.size == 1) "" else "s"} · ${totalDurationMin.coerceAtLeast(1)} min total playback",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(snoreEvents, key = { it.timestamp }) { event ->
                    SnoreRecordingPlayerCard(
                        event = event,
                        onDeleted = {
                            // Automatically updates via observeAllSnoreEvents Flow
                        },
                    )
                }
            }
        }
    }
}

private fun sourceLabel(source: String, isEstimate: Boolean): String = when (source) {
    "watch" -> if (isEstimate) "watch estimate" else "watch"
    "cuff" -> "cuff calibration"
    "manual" -> "manual"
    "health_connect" -> "health connect"
    else -> source
}

private fun formatDateTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(epochMillis))
}
