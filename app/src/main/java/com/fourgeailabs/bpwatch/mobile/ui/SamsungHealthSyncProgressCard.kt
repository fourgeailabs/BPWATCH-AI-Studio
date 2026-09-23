package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fourgeailabs.bpwatch.mobile.healthconnect.SamsungHealthSyncState

/**
 * Visual progress indicator and Compose chart displaying the real-time sync status
 * of sensor data being transmitted from the wearable (Galaxy Watch) to Samsung Health.
 */
@Composable
fun SamsungHealthSyncProgressCard(
    modifier: Modifier = Modifier,
    wrist: String = "left",
) {
    val context = LocalContext.current
    val stage by SamsungHealthSyncState.stage.collectAsState()
    val rawProgress by SamsungHealthSyncState.syncProgress.collectAsState()
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "syncProgress",
    )
    val totalSynced by SamsungHealthSyncState.totalSyncedPackets.collectAsState()
    val recentPoints by SamsungHealthSyncState.recentPoints.collectAsState()
    val lastSummary by SamsungHealthSyncState.lastPayloadSummary.collectAsState()
    val lastLatency by SamsungHealthSyncState.lastLatencyMs.collectAsState()

    // Pulse animation for active transmission
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header with pulsating beacon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (stage != SamsungHealthSyncState.Stage.ERROR)
                                Color(0xFF10B981).copy(alpha = pulseAlpha)
                            else
                                MaterialTheme.colorScheme.error,
                        ),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Samsung Health Real-Time Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Wearable sensor telemetry transmission pipeline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = if (stage == SamsungHealthSyncState.Stage.IDLE) "LIVE" else "SYNCING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // Interactive Pipeline Flow: Wearable -> Phone Gateway -> Samsung Health
            SyncPipelineVisualizer(stage = stage, pulseAlpha = pulseAlpha)

            // Progress Arc and Stage Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Circular Canvas Progress Gauge
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    Canvas(modifier = Modifier.size(56.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        val sweepAngle = animatedProgress * 360f
                        // Track
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = strokeWidth),
                        )
                        // Progress arc
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stage.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Transmission latency: ${lastLatency}ms • $totalSynced records delivered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Real-Time Telemetry Sparkline / Area Chart
            Text(
                "Recent Telemetry Transmissions (Heart Rate & Posture Sync)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )

            ComposeTelemetryChart(
                points = recentPoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)),
            )

            // Current Telemetry Payload Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Latest Synced Payload:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Health Connect v2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = lastSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Trigger Manual Test Sync Button
            FilledTonalButton(
                onClick = {
                    SamsungHealthSyncState.triggerManualSync(
                        context = context,
                        wrist = if (wrist.equals("right", ignoreCase = true)) "Right" else "Left",
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Test Telemetry Sync to Samsung Health")
            }
        }
    }
}

/**
 * Visual multi-node pipeline indicator:
 * Wearable Sensors -> Phone Processing Gateway -> Samsung Health
 */
@Composable
private fun SyncPipelineVisualizer(
    stage: SamsungHealthSyncState.Stage,
    pulseAlpha: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PipelineNode(
            icon = Icons.Filled.Watch,
            title = "Watch Sensors",
            subtitle = "PPG + 3D Tilt",
            active = stage == SamsungHealthSyncState.Stage.WEARABLE_TRANSMITTING || stage == SamsungHealthSyncState.Stage.IDLE,
            pulseAlpha = if (stage == SamsungHealthSyncState.Stage.WEARABLE_TRANSMITTING) pulseAlpha else 1.0f,
        )

        PipelineLink(
            active = stage == SamsungHealthSyncState.Stage.WEARABLE_TRANSMITTING || stage == SamsungHealthSyncState.Stage.PROCESSING_CALIBRATION,
            pulseAlpha = pulseAlpha,
        )

        PipelineNode(
            icon = Icons.Filled.PhoneAndroid,
            title = "BP Gateway",
            subtitle = "Bias & Calib",
            active = stage == SamsungHealthSyncState.Stage.PROCESSING_CALIBRATION,
            pulseAlpha = if (stage == SamsungHealthSyncState.Stage.PROCESSING_CALIBRATION) pulseAlpha else 1.0f,
        )

        PipelineLink(
            active = stage == SamsungHealthSyncState.Stage.WRITING_SAMSUNG_HEALTH || stage == SamsungHealthSyncState.Stage.SYNCED_SUCCESS,
            pulseAlpha = pulseAlpha,
        )

        PipelineNode(
            icon = Icons.Filled.Favorite,
            title = "Samsung Health",
            subtitle = "Health Connect",
            active = stage == SamsungHealthSyncState.Stage.WRITING_SAMSUNG_HEALTH || stage == SamsungHealthSyncState.Stage.SYNCED_SUCCESS,
            pulseAlpha = if (stage == SamsungHealthSyncState.Stage.WRITING_SAMSUNG_HEALTH) pulseAlpha else 1.0f,
        )
    }
}

@Composable
private fun PipelineNode(
    icon: ImageVector,
    title: String,
    subtitle: String,
    active: Boolean,
    pulseAlpha: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    if (active)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f * pulseAlpha + 0.10f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PipelineLink(
    active: Boolean,
    pulseAlpha: Float,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = Modifier
            .width(28.dp)
            .height(16.dp),
    ) {
        val strokeW = 3.dp.toPx()
        val y = size.height / 2f
        drawLine(
            color = if (active) activeColor.copy(alpha = pulseAlpha) else inactiveColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
        if (active) {
            drawCircle(
                color = activeColor,
                radius = 3.5.dp.toPx(),
                center = Offset(size.width * 0.5f, y),
            )
        }
    }
}

/**
 * Custom Compose canvas chart drawing real-time sensor transmission history
 * (using DrawScope.size properties for responsive layout).
 */
@Composable
private fun ComposeTelemetryChart(
    points: List<SamsungHealthSyncState.TelemetryPoint>,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Canvas(modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f || points.isEmpty()) return@Canvas

        // Draw horizontal subtle gridlines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = height * (i / gridLines.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Calculate min/max HR for chart normalization
        val minHr = points.minOfOrNull { it.hr }?.coerceAtLeast(50f) ?: 60f
        val maxHr = (points.maxOfOrNull { it.hr } ?: 100f).coerceAtLeast(minHr + 15f)

        val stepX = if (points.size > 1) width / (points.size - 1) else width
        val linePath = Path()
        val fillPath = Path()

        points.forEachIndexed { index, pt ->
            val x = index * stepX
            val normalizedY = (pt.hr - minHr) / (maxHr - minHr)
            val y = height - (normalizedY * (height - 16.dp.toPx()) + 8.dp.toPx())

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevPt = points[index - 1]
                val prevNormalizedY = (prevPt.hr - minHr) / (maxHr - minHr)
                val prevY = height - (prevNormalizedY * (height - 16.dp.toPx()) + 8.dp.toPx())

                val cx1 = prevX + (x - prevX) / 2f
                val cy1 = prevY
                val cx2 = prevX + (x - prevX) / 2f
                val cy2 = y
                linePath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw area fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.28f),
                    primaryColor.copy(alpha = 0.02f),
                ),
            ),
        )

        // Draw line chart
        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )

        // Draw data points
        points.forEachIndexed { index, pt ->
            val x = index * stepX
            val normalizedY = (pt.hr - minHr) / (maxHr - minHr)
            val y = height - (normalizedY * (height - 16.dp.toPx()) + 8.dp.toPx())

            drawCircle(
                color = if (index == points.size - 1) secondaryColor else primaryColor,
                radius = if (index == points.size - 1) 4.5.dp.toPx() else 3.dp.toPx(),
                center = Offset(x, y),
            )
        }
    }
}
