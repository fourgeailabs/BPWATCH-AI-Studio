package com.fourgeailabs.bpwatch.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ECG card — honest "unsupported" state.
 *
 * The old card rendered a decorative synthetic waveform and defaulted its
 * badge to "Sinus Rhythm" even when nothing had ever been recorded, with a
 * "Record EKG" button that could never produce a real trace: Samsung's
 * ECG channel on Galaxy Watch is restricted to Samsung Health / partner
 * apps and is not exposed through public Android APIs or Health Connect.
 * This card says so plainly instead of pretending. The synthetic waveform
 * generator (EkgSensorManager), the fake analyser (EkgAnalyzer) and the
 * dummy provider sync (SHealthEkgSync) have been deleted from the watch.
 */
@Composable
fun EkgCard(
    onRecordEkgClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("ekg_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "ECG Icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            androidx.compose.foundation.layout.Column {
                Text(
                    text = "Electrocardiogram (ECG)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Not available in this app. Samsung reserves ECG recording " +
                        "for Samsung Health — take ECGs there; there is no way for " +
                        "a third-party app to record one on this watch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
