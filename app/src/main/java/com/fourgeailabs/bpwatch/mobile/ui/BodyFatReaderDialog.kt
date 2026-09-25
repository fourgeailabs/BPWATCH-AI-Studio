package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fourgeailabs.bpwatch.Link
import com.fourgeailabs.bpwatch.mobile.MainViewModel
import com.fourgeailabs.bpwatch.mobile.wearable.BiaState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val DarkNavy = Color(0xFF0F172A)
private val DialogBg = Color(0xFF1E293B)
private val CardInner = Color(0xFF131D31)

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)
private val WarningAmber = Color(0xFFFFA000)

/**
 * Body Fat Index Reader dialog with interactive BIA (Bioelectrical Impedance Analysis).
 *
 * Mandatory Requirements:
 * 1. Requires fingers touching the physical electrodes on the side buttons of the watch.
 * 2. If the sensors on those buttons are not being touched, the scan will NOT start.
 * 3. If the sensors lose contact with the fingers during the scan, the timer pauses and the app
 *    instructs the user to touch those sensors again to finish the scan.
 * 4. Features a determinate loading circle that fills from 0% to 100% around the beating heart.
 */
@Composable
fun BodyFatReaderDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // BIA state observed directly from watch data layer and hardware sensors
    val liveContactDetected by BiaState.contactDetected.collectAsState()
    val liveTopTouched by BiaState.topTouched.collectAsState()
    val liveBottomTouched by BiaState.bottomTouched.collectAsState()
    val watchScanState by BiaState.scanState.collectAsState()
    val latestBiaResult by BiaState.latestResult.collectAsState()

    // Real hardware contact: true ONLY when watch sensors report finger contact
    val isTopTouched = liveTopTouched
    val isBottomTouched = liveBottomTouched
    val isBothTouched = liveContactDetected || (isTopTouched && isBottomTouched)

    var isScanningActive by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }

    // Determinate scan timer (15 seconds total)
    val totalScanDurationMs = 15_000L
    var elapsedMs by remember { mutableLongStateOf(0L) }
    val progress = (elapsedMs / totalScanDurationMs.toFloat()).coerceIn(0f, 1f)
    val remainingSeconds = ((totalScanDurationMs - elapsedMs).coerceAtLeast(0L) / 1000L)

    // Current and scanned body metrics (Real data only)
    val dashboard by viewModel.dashboard.collectAsState()
    var displayedBodyFat by remember { mutableStateOf<Double?>(dashboard.bodyFatPercentage) }
    var skeletalMuscleKg by remember { mutableStateOf<Double?>(null) }
    var fatMassKg by remember { mutableStateOf<Double?>(null) }
    var bmrKcal by remember { mutableStateOf<Int?>(null) }
    var bodyWaterLiters by remember { mutableStateOf<Double?>(null) }

    // Update with real BIA result from watch whenever received
    LaunchedEffect(latestBiaResult) {
        latestBiaResult?.let { res ->
            displayedBodyFat = res.bodyFatPct
            skeletalMuscleKg = res.skeletalMuscleKg
            fatMassKg = res.fatMassKg
            bmrKcal = res.bmrKcal
            bodyWaterLiters = res.bodyWaterLiters
            isScanningActive = false
            scanCompleted = true
            viewModel.recordBodyFat(res.bodyFatPct)
        }
    }

    // Active scan loop enforcing physical electrode contact
    LaunchedEffect(isScanningActive, isBothTouched) {
        if (isScanningActive && isBothTouched && elapsedMs < totalScanDurationMs) {
            val stepMs = 50L
            while (isScanningActive && isBothTouched && elapsedMs < totalScanDurationMs) {
                delay(stepMs)
                elapsedMs += stepMs
            }

            if (elapsedMs >= totalScanDurationMs) {
                // Completed 100% scan waiting for real BIA result from watch
                isScanningActive = false
                if (latestBiaResult != null) {
                    scanCompleted = true
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            BiaState.requestCancelScan(context)
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isScanningActive) onDismiss()
        },
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

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isScanningActive,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                // -------------------------------------------------------------
                // Watch Side Button Sensor Contact Status Indicator
                // -------------------------------------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBothTouched) Color(0xFF1E3A2F) else Color(0xFF332014)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isBothTouched) GoogleGreen.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.6f)
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = if (isBothTouched) Icons.Filled.Check else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (isBothTouched) GoogleGreen else WarningAmber,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = if (isBothTouched) "Sensors Contact: Connected" else "Sensors Contact: Not Detected",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isBothTouched) Color(0xFF81C784) else WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // Hardware telemetry indicator badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isBothTouched) GoogleGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                            ) {
                                Text(
                                    text = if (isBothTouched) "Hardware Sensing Active" else "Hardware Electrodes Ready",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isBothTouched) Color(0xFF81C784) else WarningAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        // Top and Bottom button electrode indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ElectrodeChip(
                                modifier = Modifier.weight(1f),
                                label = "Top Button Sensor",
                                sublabel = "Middle Finger",
                                isTouched = isTopTouched,
                            )
                            ElectrodeChip(
                                modifier = Modifier.weight(1f),
                                label = "Bottom Button Sensor",
                                sublabel = "Ring Finger",
                                isTouched = isBottomTouched,
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // Center Scanner / Determinate Loading Circle
                // -------------------------------------------------------------
                if (isScanningActive) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        // Determinate Circle with status styling
                        DeterminateScanCircle(
                            progress = progress,
                            liveHr = viewModel.latestWatchHr ?: 72f,
                            isContactLost = !isBothTouched,
                            sizeDp = 136,
                        )

                        if (!isBothTouched) {
                            // Flashing Warning when fingers lose contact
                            PulsingWarningBanner(
                                message = if (elapsedMs == 0L) {
                                    "Waiting for finger contact on watch side buttons… Touch both sensors to begin."
                                } else {
                                    "⚠️ Contact Lost! Touch the side button sensors on your watch again to finish the scan."
                                }
                            )
                        } else {
                            Text(
                                text = "Measuring Bioelectrical Impedance…",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Text(
                                text = "${remainingSeconds}s remaining · ${(progress * 100).toInt()}% completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoogleBlue,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Text(
                            text = "Keep your middle and ring fingertips resting on the two side keys until the circle completes 100%.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        OutlinedButton(
                            onClick = {
                                isScanningActive = false
                                elapsedMs = 0L
                                BiaState.requestCancelScan(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        ) {
                            Text("Cancel Scan")
                        }
                    }
                } else {
                    // Result Summary Card
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
                                text = if (displayedBodyFat != null) String.format(Locale.US, "%.1f%%", displayedBodyFat) else "Measurement unavailable",
                                style = if (displayedBodyFat != null) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleMedium,
                                color = if (displayedBodyFat != null) Color(0xFFFFA726) else Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )

                            if (displayedBodyFat != null) {
                                val fatVal = displayedBodyFat!!
                                val category = when {
                                    fatVal < 14.0 -> "Athletic / Low"
                                    fatVal < 24.0 -> "Optimal Fitness"
                                    fatVal < 31.0 -> "Average Range"
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
                            } else {
                                Text(
                                    text = "Connect your Galaxy Watch to measure",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
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
                            value = skeletalMuscleKg?.let { "$it kg" } ?: "—",
                            tint = Color(0xFF81C784),
                        )
                        CompositionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.AccessibilityNew,
                            title = "Fat Mass",
                            value = fatMassKg?.let { "$it kg" } ?: "—",
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
                            value = bmrKcal?.let { "$it kcal" } ?: "—",
                            tint = Color(0xFF64B5F6),
                        )
                        CompositionMetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.WaterDrop,
                            title = "Body Water",
                            value = bodyWaterLiters?.let { "$it L" } ?: "—",
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

                    // Instructions banner
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
                                text = "To scan, place middle finger on the top side button and ring finger on the bottom side button of your watch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                        }
                    }

                    // Start Scan Action Button
                    Button(
                        onClick = {
                            scanCompleted = false
                            elapsedMs = 0L
                            isScanningActive = true
                            BiaState.requestStartScan(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_bia_scan_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBothTouched) GoogleBlue else Color(0xFF5C6BC0)
                        ),
                    ) {
                        Icon(Icons.Filled.TouchApp, contentDescription = null, modifier = Modifier.size(20.dp))
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
 * Read-only electrode status indicator showing touch state of top and bottom side button sensors.
 */
@Composable
private fun ElectrodeChip(
    label: String,
    sublabel: String,
    isTouched: Boolean,
    modifier: Modifier = Modifier,
) {
    val activeColor = if (isTouched) GoogleGreen else Color.White.copy(alpha = 0.35f)
    val bgColor = if (isTouched) GoogleGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, activeColor.copy(alpha = 0.6f)),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(activeColor, CircleShape),
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (isTouched) "$sublabel · Touched" else "$sublabel · No Contact",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isTouched) Color(0xFF81C784) else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                )
            }
        }
    }
}

/**
 * Pulsing warning banner displayed when fingers lose contact with the sensors.
 */
@Composable
private fun PulsingWarningBanner(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF422108)),
        border = BorderStroke(1.dp, WarningAmber),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = WarningAmber,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Determinate Loading Circle that fills from 0% to 100% around the beating heart / sensor graphic.
 */
@Composable
fun DeterminateScanCircle(
    progress: Float,
    liveHr: Float?,
    isContactLost: Boolean = false,
    sizeDp: Int = 84,
    modifier: Modifier = Modifier,
) {
    val googleColors = remember { listOf(GoogleBlue, GoogleRed, GoogleYellow, GoogleGreen) }
    var activeRingColor by remember { mutableStateOf(GoogleBlue) }

    LaunchedEffect(isContactLost) {
        if (!isContactLost) {
            var index = 0
            while (true) {
                val from = googleColors[index % googleColors.size]
                val to = googleColors[(index + 1) % googleColors.size]
                repeat(25) { step ->
                    activeRingColor = lerp(from, to, (step + 1) / 25f)
                    delay(35)
                }
                index++
            }
        }
    }

    val displayColor = if (isContactLost) WarningAmber else activeRingColor
    val beatScale = rememberHeartbeatScale(if (isContactLost) 0f else liveHr)

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Inactive background track ring
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
            color = displayColor,
            strokeWidth = (sizeDp * 0.06f).dp.coerceAtLeast(4.dp),
        )

        // Beating heart / sensor icon in the center
        Text(
            text = if (isContactLost) "⚠️" else "♥",
            color = if (isContactLost) WarningAmber else Color.Red,
            fontSize = if (isContactLost) (sizeDp * 0.35f).sp else (sizeDp * 0.45f).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer(
                scaleX = beatScale,
                scaleY = beatScale,
            ),
        )
    }
}

/**
 * Heartbeat scale: swells 1.0 to 1.35 on every beat at the live rate.
 */
@Composable
private fun rememberHeartbeatScale(liveHr: Float?): Float {
    var beatScale by remember { mutableFloatStateOf(1f) }
    val bpmNow by rememberUpdatedState(if (liveHr != null && liveHr > 0f) liveHr else 65f)

    LaunchedEffect(liveHr) {
        if (liveHr == null || liveHr <= 0f) {
            beatScale = 1f
            return@LaunchedEffect
        }
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
