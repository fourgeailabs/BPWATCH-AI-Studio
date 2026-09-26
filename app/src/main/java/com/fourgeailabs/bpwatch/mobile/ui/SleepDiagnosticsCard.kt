package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fourgeailabs.bpwatch.mobile.healthconnect.SleepDiagnosis
import com.fourgeailabs.bpwatch.mobile.healthconnect.SleepDiagnosisOutcome
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * Sleep diagnostics: answers "why is my sleep not populating?" with facts
 * from Health Connect itself — the grant states it reports, the raw
 * sessions it holds (with stage counts and the app that wrote each one),
 * whether Samsung Health is installed, and a factual chain-of-custody
 * outcome. Used in Settings and in the Trends sleep empty state.
 */
@Composable
fun SleepDiagnosticsCard(
    onDiagnose: suspend () -> SleepDiagnosis,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<SleepDiagnosis?>(null) }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TintedIcon(icon = Icons.Filled.Bedtime, contentDescription = null)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Sleep diagnostics", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Shows exactly what Health Connect holds for sleep.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            result?.let { SleepDiagnosisResult(it) }

            Button(
                onClick = {
                    scope.launch {
                        running = true
                        try {
                            result = onDiagnose()
                        } finally {
                            running = false
                        }
                    }
                },
                enabled = !running,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (running) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(if (result == null) "Check sleep data" else "Check again")
                }
            }
        }
    }
}

@Composable
private fun SleepDiagnosisResult(d: SleepDiagnosis) {
    val zone = remember { ZoneId.systemDefault() }
    val fmt = remember {
        DateTimeFormatter.ofPattern("EEE HH:mm").withZone(zone)
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (d.error != null) {
            Text(
                "Couldn't read Health Connect: ${d.error}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            return
        }
        Text(
            "Health Connect: ${d.sdkStatus}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Read-sleep permission: " +
                if (d.readSleepGranted) "granted ✓" else "NOT granted ✗",
            style = MaterialTheme.typography.bodyMedium,
            color = if (d.readSleepGranted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
        )
        Text(
            "Write-sleep permission: " +
                if (d.writeSleepGranted) "granted ✓" else "NOT granted ✗",
            style = MaterialTheme.typography.bodyMedium,
            color = if (d.writeSleepGranted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
        )
        Text(
            "Samsung Health installed: " + if (d.shealthInstalled) "yes ✓" else "no ✗",
            style = MaterialTheme.typography.bodyMedium,
            color = if (d.shealthInstalled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
        )
        Text(
            "Sleep sessions in the last 48 hours: ${d.sessions48h.size}",
            style = MaterialTheme.typography.bodyMedium,
        )
        d.sessions48h.forEach { s ->
            val hours = s.minutesCounted / 60.0
            val hoursText = if (hours == hours.toLong().toDouble()) {
                hours.toLong().toString()
            } else {
                String.format("%.1f", hours)
            }
            Text(
                "• ${fmt.format(s.start)} → ${fmt.format(s.end)}: " +
                    "${hoursText}h counted, ${s.stageCount} stages" +
                    (s.originPackage?.let { ", via $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "Sleep sessions in the last 7 days: ${d.sessions7d}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            when (d.outcome) {
                SleepDiagnosisOutcome.SESSIONS_FOUND ->
                    "Sleep data is flowing into Health Connect — the Home tile and Trends should populate."
                SleepDiagnosisOutcome.HEALTH_CONNECT_UNAVAILABLE ->
                    "Health Connect isn't available on this device (${d.sdkStatus}), so no app can read sleep data."
                SleepDiagnosisOutcome.PERMISSION_DENIED ->
                    "Grant the sleep permission, then check again."
                SleepDiagnosisOutcome.SHEALTH_NOT_INSTALLED ->
                    "Samsung Health isn't installed on this device, so nothing is writing sleep sessions. " +
                        "Install Samsung Health on the phone and wear the watch to bed."
                SleepDiagnosisOutcome.NO_SESSIONS_SHARED ->
                    "Samsung Health is installed but shares no sleep sessions with Health Connect. " +
                        "In Samsung Health: Settings → Health Connect → make sure Sleep is allowed (and sync is switched on there)."
                SleepDiagnosisOutcome.QUERY_FAILED ->
                    "The Health Connect query failed — check again in a moment."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
