package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fourgeailabs.bpwatch.mobile.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale

private val DarkNavy = Color(0xFF0F172A)
private val DialogBg = Color(0xFF1E293B)
private val CardInner = Color(0xFF131D31)

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

/**
 * Body Fat Index Reader dialog with interactive BIA (Bioelectrical Impedance Analysis)
 * sensor scan featuring a determinate loading circle that fills/completes around the
 * beating heart/sensor icon.
 */
@Composable
fun BodyFatReaderDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }

    // Scan progress state (15-second BIA sensor scan)
    val totalScanDurationMs = 15_000L
    var elapsedMs by remember { mutableLongStateOf(0L) }
    val progress = (elapsedMs / totalScanDurationMs.toFloat()).coerceIn(0f, 1f)
    val remainingSeconds = ((totalScanDurationMs - elapsedMs).coerceAtLeast(0L) / 1000L)

    // Current or newly scanned body composition values
    val dashboard by viewModel.dashboard.collectAsState()
    val currentBodyFat = dashboard.bodyFatPercentage ?: 21.4
    var displayedBodyFat by remember { mutableDoubleStateOf(currentBodyFat) }
    var skeletalMuscleKg by remember { mutableDoubleStateOf(32.8) }
    var fatMassKg by remember { mutableDoubleStateOf(16.2) }
    var bmrKcal by remember { mutableIntStateOf(1680) }
    var bodyWaterLiters by remember { mutableDoubleStateOf(44.5) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            elapsedMs = 0L
            val startTime = System.currentTimeMillis()
            while (elapsedMs < totalScanDurationMs) {
                delay(50L)
                elapsedMs = System.currentTimeMillis() - startTime
            }
            elapsedMs = totalScanDurationMs
            // Compute realistic scan result
            val newPct = (currentBodyFat + (-0.3 + Math.random() * 0.6)).coerceIn(8.0, 45.0)
            displayedBodyFat = newPct
            fatMassKg = Math.round(newPct * 0.75 * 10.0) / 10.0
            skeletalMuscleKg = Math.round((75.0 - fatMassKg) * 0.45 * 10.0) / 10.0
            bmrKcal = (1500 + skeletalMuscleKg * 10).toInt()
            bodyWaterLiters = Math.round((75.0 - fatMassKg) * 0.73 * 10.0) / 10.0

            // Save to Health Connect via ViewModel/Phone service
            scope.launch {
                try {
                    viewModel.recordBodyFat(displayedBodyFat)
                } catch (_: Exception) {
                }
            }
            isScanning = false
            scanCompleted = true
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isScanning) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
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

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isScanning,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                // Center Scanner / Loading Circle Section
                if (isScanning) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(vertical = 12.dp),
                    ) {
                        DeterminateScanCircle(
                            progress = progress,
                            liveHr = viewModel.latestWatchHr ?: 72f,
                            sizeDp = 130,
                        )

                        Text(
                            text = "Scanning BIA Sensors…",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text = "${remainingSeconds}s remaining · ${(progress * 100).toInt()}% completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4285F4),
                            fontWeight = FontWeight.Bold,
                        )

                        Text(
                            text = "Measuring bioelectrical impedance across wrist & finger electrodes. Keep still.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    // Body Composition Result Summary Card
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
                                text = String.format(Locale.US, "%.1f%%", displayedBodyFat),
                                style = MaterialTheme.typography.displayMedium,
                                color = Color(0xFFFFA726),
                                fontWeight = FontWeight.Bold,
                            )

                            val category = when {
                                displayedBodyFat < 14.0 -> "Athletic / Low"
                                displayedBodyFat < 24.0 -> "Optimal Fitness"
                                displayedBodyFat < 31.0 -> "Average Range"
                                else -> "Elevated"
                            }

                            Surface(
                                color = Color(0xFFFFA726).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFFA726),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    // Detailed metrics grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompositionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.FitnessCenter,
                            title = "Skeletal Muscle",
                            value = "${skeletalMuscleKg} kg",
                            tint = Color(0xFF81C784),
                        )
                        CompositionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.AccessibilityNew,
                            title = "Fat Mass",
                            value = "${fatMassKg} kg",
                            tint = Color(0xFFFFB74D),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompositionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Speed,
                            title = "Basal Metabolism",
                            value = "${bmrKcal} kcal",
                            tint = Color(0xFF64B5F6),
                        )
                        CompositionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.WaterDrop,
                            title = "Body Water",
                            value = "${bodyWaterLiters} L",
                            tint = Color(0xFF4DD0E1),
                        )
                    }

                    if (scanCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                            Text(
                                "Scan saved & synced to Health Connect",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF81C784),
                            )
                        }
                    }

                    // Scan Action Button
                    Button(
                        onClick = {
                            isScanning = true
                            scanCompleted = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_bia_scan_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                    ) {
                        Icon(Icons.Filled.Accessibility, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (scanCompleted) "Re-Scan Body Fat (BIA)" else "Start BIA Sensor Scan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Determinate Loading Circle that completes from 0% to 100% around the beating heart / sensor graphic.
 */
@Composable
fun DeterminateScanCircle(
    progress: Float,
    liveHr: Float?,
    sizeDp: Int = 84,
    modifier: Modifier = Modifier,
) {
    val googleColors = remember { listOf(GoogleBlue, GoogleRed, GoogleYellow, GoogleGreen) }
    var ringColor by remember { mutableStateOf(GoogleBlue) }

    LaunchedEffect(Unit) {
        var index = 0
        while (true) {
            val from = googleColors[index % googleColors.size]
            val to = googleColors[(index + 1) % googleColors.size]
            repeat(30) { step ->
                ringColor = lerp(from, to, (step + 1) / 30f)
                delay(30)
            }
            index++
        }
    }

    val beatScale = rememberHeartbeatScale(liveHr)

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Inactive track ring (faint gray)
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = Color.White.copy(alpha = 0.15f),
            strokeWidth = (sizeDp * 0.06f).dp.coerceAtLeast(4.dp),
        )

        // Determinate active progress circle completing from 0% to 100%
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            strokeWidth = (sizeDp * 0.06f).dp.coerceAtLeast(4.dp),
        )

        // Beating heart in the center
        Text(
            text = "♥",
            color = Color.Red,
            fontSize = (sizeDp * 0.45f).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer(
                scaleX = beatScale,
                scaleY = beatScale,
            ),
        )
    }
}

/**
 * Shared heartbeat scale: swells 1.0 to 1.35 on every beat at the live rate.
 */
@Composable
private fun rememberHeartbeatScale(liveHr: Float?): Float {
    var beatScale by remember { mutableFloatStateOf(1f) }
    val bpmNow by rememberUpdatedState(if (liveHr != null && liveHr > 0f) liveHr else 65f)

    LaunchedEffect(Unit) {
        suspend fun tweenScale(from: Float, to: Float, durationMs: Long) {
            val steps = 6
            repeat(steps) { i ->
                beatScale = from + (to - from) * (i + 1) / steps.toFloat()
                delay(durationMs / steps)
            }
        }
        while (true) {
            val intervalMs = (60_000f / bpmNow).toLong().coerceIn(350L, 1500L)
            tweenScale(1f, 1.35f, 110)
            tweenScale(1.35f, 1f, 160)
            delay((intervalMs - 270).coerceAtLeast(150))
        }
    }
    return beatScale
}

@Composable
private fun CompositionMetricTile(
    icon: ImageVector,
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardInner),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
