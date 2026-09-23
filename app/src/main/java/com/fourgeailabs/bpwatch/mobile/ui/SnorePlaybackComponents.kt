package com.fourgeailabs.bpwatch.mobile.ui

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fourgeailabs.bpwatch.mobile.data.SnoreEvent
import com.fourgeailabs.bpwatch.mobile.snore.SnoreStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CardBg = Color(0xFF1A1A2E)
private val WavePurple = Color(0xFF7B61FF)
private val SnoreAmber = Color(0xFFFFA726)

/**
 * High-fidelity, interactive audio player card for a detected snoring recording.
 * Supports playing/pausing, real-time position slider, sharing via Intent,
 * downloading to public storage, and deleting with confirmation.
 */
@Composable
fun SnoreRecordingPlayerCard(
    event: SnoreEvent,
    zone: ZoneId = remember { ZoneId.systemDefault() },
    modifier: Modifier = Modifier,
    containerColor: Color = CardBg,
    onDeleted: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var totalDurationMs by remember { mutableIntStateOf(event.durationMs.coerceAtLeast(1000)) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val fileExists = remember(event.clipPath) {
        event.clipPath?.let { File(it).exists() } ?: false
    }

    val fileSizeKb = remember(event.clipPath, fileExists) {
        if (fileExists && event.clipPath != null) {
            File(event.clipPath).length() / 1024
        } else {
            0L
        }
    }

    // Clean up MediaPlayer on composition exit
    DisposableEffect(event.timestamp) {
        onDispose {
            try {
                player?.stop()
                player?.release()
                player = null
            } catch (_: Exception) {
            }
        }
    }

    // Playback progress ticker
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            player?.let { p ->
                try {
                    if (p.isPlaying) {
                        currentPositionMs = p.currentPosition
                    }
                } catch (_: Exception) {
                }
            }
            delay(100)
        }
    }

    fun startPlayback() {
        val path = event.clipPath ?: return
        try {
            if (player == null) {
                val mp = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    val dur = duration
                    if (dur > 0) totalDurationMs = dur
                    setOnCompletionListener {
                        isPlaying = false
                        currentPositionMs = 0
                    }
                }
                player = mp
            }
            player?.start()
            isPlaying = true
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot play audio: ${e.message}", Toast.LENGTH_SHORT).show()
            isPlaying = false
        }
    }

    fun pausePlayback() {
        try {
            player?.pause()
            isPlaying = false
        } catch (_: Exception) {
            isPlaying = false
        }
    }

    fun seekTo(positionMs: Int) {
        currentPositionMs = positionMs
        try {
            player?.seekTo(positionMs)
        } catch (_: Exception) {
        }
    }

    val timeFmt = remember { DateTimeFormatter.ofPattern("h:mm:ss a").withZone(zone) }
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEE, MMM d").withZone(zone) }
    val instant = remember(event.timestamp) { Instant.ofEpochMilli(event.timestamp) }

    // Waveform pulsing animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("snore_player_card_${event.timestamp}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header: Icon, timestamp, and duration badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (isPlaying) WavePurple.copy(alpha = 0.25f * pulseAlpha)
                            else SnoreAmber.copy(alpha = 0.15f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.GraphicEq else Icons.Filled.Mic,
                        contentDescription = "Snore Audio",
                        tint = if (isPlaying) WavePurple else SnoreAmber,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text(
                        text = timeFmt.format(instant),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${dateFmt.format(instant)} · ${event.durationMs / 1000}s burst${if (fileSizeKb > 0) " (${fileSizeKb} KB)" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }

                Surface(
                    color = if (fileExists) WavePurple.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = if (fileExists) "AUDIO CLIPPED" else "NO FILE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (fileExists) WavePurple else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // Audio waveform / progress scrubbing section
            if (fileExists) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Slider(
                        value = currentPositionMs.toFloat().coerceIn(0f, totalDurationMs.toFloat()),
                        onValueChange = { seekTo(it.toInt()) },
                        valueRange = 0f..totalDurationMs.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isPlaying) WavePurple else SnoreAmber,
                            activeTrackColor = if (isPlaying) WavePurple else SnoreAmber,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = formatMs(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Text(
                            text = formatMs(totalDurationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // Playback controls & sharing actions bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Play / Pause button
                FilledTonalButton(
                    onClick = {
                        if (isPlaying) pausePlayback() else startPlayback()
                    },
                    enabled = fileExists,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("play_pause_button_${event.timestamp}"),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isPlaying) "Pause" else "Play Clip")
                }

                // Action buttons: Share, Download, Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Share via Android Intent
                    IconButton(
                        onClick = {
                            SnoreStorage.shareClip(context, event)
                        },
                        enabled = fileExists,
                        modifier = Modifier.testTag("share_clip_button_${event.timestamp}"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share recording via Intent",
                            tint = if (fileExists) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f),
                        )
                    }

                    // Save / Export to Downloads
                    IconButton(
                        onClick = {
                            val ok = SnoreStorage.exportClipToDownloads(context, event)
                            Toast.makeText(
                                context,
                                if (ok) "Saved to Downloads folder" else "Export failed",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        enabled = fileExists,
                        modifier = Modifier.testTag("export_clip_button_${event.timestamp}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Export to Downloads",
                            tint = if (fileExists) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f),
                        )
                    }

                    // Delete recording
                    IconButton(
                        onClick = {
                            showDeleteConfirm = true
                        },
                        modifier = Modifier.testTag("delete_clip_button_${event.timestamp}"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete recording",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog before deleting
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Snore Recording?") },
            text = {
                Text(
                    "This will permanently delete this audio recording from your device storage and remove it from sleep history.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        try {
                            player?.stop()
                            player?.release()
                            player = null
                        } catch (_: Exception) {
                        }
                        isPlaying = false
                        scope.launch(Dispatchers.IO) {
                            SnoreStorage.deleteEvent(context, event)
                        }
                        onDeleted()
                        Toast.makeText(context, "Recording deleted permanently", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

private fun formatMs(ms: Int): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format(Locale.US, "%d:%02d", m, s)
}
