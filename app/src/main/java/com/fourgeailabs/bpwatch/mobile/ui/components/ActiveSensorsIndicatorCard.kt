package com.fourgeailabs.bpwatch.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActiveSensorsIndicatorCard(
    isEkgActive: Boolean = true,
    isBpActive: Boolean = true,
    isBodyFatActive: Boolean = true,
    isSkinTempActive: Boolean = true,
    ekgWaveformPackets: FloatArray = floatArrayOf()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("active_sensors_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Sensors Active",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Active Sensor Stream",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "SHM-MOD Live",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Sensor Status Badges: EKG, BP, Body Fat, Skin Temp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SensorStatusPill("EKG", isEkgActive)
                SensorStatusPill("BP", isBpActive)
                SensorStatusPill("Body Fat", isBodyFatActive)
                SensorStatusPill("Skin Temp", isSkinTempActive)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live EKG Waveform Box (D3-style high-frequency canvas render)
            Text(
                text = "Live EKG Waveform Stream (SHM-MOD)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val path = Path()
                    
                    val data = if (ekgWaveformPackets.isNotEmpty()) {
                        ekgWaveformPackets
                    } else {
                        // Default simulated ECG pulse wave stream
                        FloatArray(120) { i ->
                            val cycle = (i % 30) / 30f
                            when {
                                cycle in 0.15f..0.22f -> -0.3f
                                cycle in 0.22f..0.28f -> 1.8f
                                cycle in 0.28f..0.35f -> -0.6f
                                cycle in 0.5f..0.65f -> 0.4f
                                else -> 0f
                            }
                        }
                    }

                    val step = width / (data.size.coerceAtLeast(1))
                    val maxVal = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f
                    val minVal = data.minOrNull()?.coerceAtMost(-1f) ?: -1f
                    val range = (maxVal - minVal).coerceAtLeast(0.1f)

                    data.forEachIndexed { index, value ->
                        val x = index * step
                        val normalized = (value - minVal) / range
                        val y = height - (normalized * (height * 0.8f) + height * 0.1f)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF38BDF8),
                        style = Stroke(width = 2.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun SensorStatusPill(name: String, isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.width(78.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color(0xFF22C55E).copy(alpha = alpha) else Color.Gray)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isActive) "Active" else "Standby",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
