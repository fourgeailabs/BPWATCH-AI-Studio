package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fourgeailabs.bpwatch.mobile.MainViewModel
import java.util.Locale

private val DialogBg = Color(0xFF1E293B)
private val CardInner = Color(0xFF131D31)

private val GoogleBlue = Color(0xFF4285F4)

/**
 * Body composition dialog — honest display edition.
 *
 * The old version ran a fake 15-second "live BIA sensor scan": the watch's
 * BIA electrodes are gated by Samsung to Samsung Health only, so no scan
 * this app triggers can ever return impedance data — the progress circle
 * was theatre. This version shows only real data: the latest body-fat
 * percentage the dashboard holds (sourced from Samsung Health via Health
 * Connect — take the measurement in Samsung Health on the watch), or an
 * honest empty state. Nothing here is simulated, and no scan is offered
 * because none can work.
 */
@Composable
fun BodyFatReaderDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    // Dashboard body-fat percentage (Health Connect / Samsung Health).
    // Read directly from the dashboard flow so a refresh updates this UI.
    val dashboard by viewModel.dashboard.collectAsState()
    val displayedBodyFat = dashboard.bodyFatPercentage

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("body_fat_reader_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF6D4C41).copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Accessibility,
                                contentDescription = null,
                                tint = Color(0xFFD7CCC8),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            text = "Body Fat Index Reader",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                // Result Summary Card (real Health Connect data only)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardInner),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "CURRENT BODY FAT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp,
                        )

                        Text(
                            text = if (displayedBodyFat != null) String.format(Locale.US, "%.1f%%", displayedBodyFat) else "No shared data yet",
                            style = if (displayedBodyFat != null) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleMedium,
                            color = if (displayedBodyFat != null) Color(0xFFFFA726) else Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )

                        if (displayedBodyFat != null) {
                            val fatVal = displayedBodyFat
                            val category = when {
                                fatVal < 14.0 -> "Athletic / Low"
                                fatVal < 24.0 -> "Optimal Fitness"
                                fatVal < 31.0 -> "Average Range"
                                else -> "Elevated"
                            }

                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFFA726),
                                fontWeight = FontWeight.SemiBold,
                            )
                        } else {
                            Text(
                                text = "Measure in Samsung Health on your watch, then allow Samsung Health to share body composition with Health Connect.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                // Honest guidance: where real measurements come from.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Watch,
                            contentDescription = null,
                            tint = Color(0xFF90CAF9),
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "Body composition can't be measured by this app — Samsung reserves the watch's BIA sensors for Samsung Health. " +
                                "Take the measurement there (Samsung Health → Body composition), then in Samsung Health: " +
                                "Settings → Health Connect → allow Body fat. It appears here automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }

                // Refresh from Health Connect
                Button(
                    onClick = { viewModel.refreshDashboard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("refresh_body_fat_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Refresh from Health Connect",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}
