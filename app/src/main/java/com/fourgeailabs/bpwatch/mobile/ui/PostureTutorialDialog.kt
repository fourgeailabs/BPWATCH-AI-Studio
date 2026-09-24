package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val PrimaryBlue = Color(0xFF2196F3)
private val DeepNavy = Color(0xFF0F172A)
private val CardSurface = Color(0xFF1E293B)
private val AccentGreen = Color(0xFF4CAF50)
private val LightCyan = Color(0xFF38BDF8)

data class PostureStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val keyPoints: List<String>,
    val clinicalReasoning: String,
    val doList: List<String>,
    val dontList: List<String>,
)

val POSTURE_TUTORIAL_STEPS = listOf(
    PostureStep(
        title = "1. Rest & Seated Position",
        subtitle = "Sit comfortably with back support for 5 minutes",
        icon = Icons.Filled.SelfImprovement,
        keyPoints = listOf(
            "Sit in an upright chair with full back support.",
            "Keep both feet flat on the floor — do not cross legs or ankles.",
            "Rest quietly in a relaxed environment for 3-5 minutes prior to checking.",
        ),
        clinicalReasoning = "Crossing legs or physical tension can elevate systolic blood pressure by 2 to 8 mmHg due to increased vascular resistance.",
        doList = listOf("Back supported by chair", "Feet flat on floor", "Rest for 5 mins prior"),
        dontList = listOf("Do not cross legs", "Do not slump forward", "Avoid cold temperatures"),
    ),
    PostureStep(
        title = "2. Wrist Positioned at Heart Level",
        subtitle = "Align Galaxy Watch level with your right atrium",
        icon = Icons.Filled.Height,
        keyPoints = listOf(
            "Rest your arm on a desk, table, or lap cushion.",
            "Raise or lower your wrist so the Galaxy Watch sits exactly at heart level.",
            "Keep palm face up and relax your hand — do not clench your fist.",
        ),
        clinicalReasoning = "Hydrostatic pressure error: holding wrist below heart level artificially inflates BP by ~0.8 mmHg per cm below heart height (~10 mmHg error if arm hangs at side).",
        doList = listOf("Rest arm on table/desk", "Watch level with heart", "Hand & palm relaxed"),
        dontList = listOf("Do not let arm dangle", "Do not hold arm unsupported in mid-air", "Do not clench fist"),
    ),
    PostureStep(
        title = "3. Stillness & Silence",
        subtitle = "Remain motionless and silent during the 30s scan",
        icon = Icons.Filled.MicOff,
        keyPoints = listOf(
            "Remain completely still for the full 30-second optical pulse measurement.",
            "Do not speak, swallow repeatedly, or answer phone calls.",
            "Breathe naturally and steadily — do not hold your breath.",
        ),
        clinicalReasoning = "Vocal cord vibration and arm muscle micro-tremors distort optical pulse wave signal quality (PPG) and cause reading retries or noise spikes.",
        doList = listOf("Remain motionless", "Breathe normally", "Keep lips closed"),
        dontList = listOf("Do not speak or talk", "Do not move fingers or arm", "Do not hold breath"),
    ),
    PostureStep(
        title = "4. Matching Cuff Calibration Posture",
        subtitle = "Use the exact same posture during monthly cuff calibrations",
        icon = Icons.Filled.MonitorHeart,
        keyPoints = listOf(
            "When performing mandatory 28-day arm cuff calibrations, sit in this exact same posture.",
            "Wear the upper arm cuff on the arm opposite your watch or follow cuff instructions.",
            "Ensure baseline model calibration reflects your relaxed resting hemodynamics.",
        ),
        clinicalReasoning = "Model accuracy depends on consistency. Calibrating in one posture and checking in another shifts arterial stiffness baselines.",
        doList = listOf("Calibrate in same chair", "Use validated upper arm cuff", "Log exact cuff values"),
        dontList = listOf("Do not calibrate while standing", "Do not use loose cuff", "Do not calibrate after caffeine/exercise"),
    ),
)

/**
 * Interactive tutorial overlay guiding users step-by-step on the optimal posture
 * for accurate, reliable blood pressure estimation using wrist optical sensors.
 */
@Composable
fun PostureTutorialDialog(
    onDismiss: () -> Unit,
    onStartCheck: (() -> Unit)? = null,
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val totalSteps = POSTURE_TUTORIAL_STEPS.size
    val currentStep = POSTURE_TUTORIAL_STEPS[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("posture_tutorial_dialog"),
            color = DeepNavy,
            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlue.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Filled.SelfImprovement,
                                contentDescription = null,
                                tint = LightCyan,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                "Measurement Posture",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                "Step ${currentStepIndex + 1} of $totalSteps",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightCyan,
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }

                // Step progress bar
                LinearProgressIndicator(
                    progress = { (currentStepIndex + 1) / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryBlue,
                    trackColor = Color.White.copy(alpha = 0.1f),
                )

                // Main step card container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Title & Subtitle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    currentStep.icon,
                                    contentDescription = null,
                                    tint = LightCyan,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(24.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    currentStep.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    currentStep.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                )
                            }
                        }

                        // Key points
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            currentStep.keyPoints.forEach { point ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(16.dp),
                                    )
                                    Text(
                                        point,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f),
                                    )
                                }
                            }
                        }

                        // Do vs Don't comparison grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Do list
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = AccentGreen.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f)),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "✓ DO",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGreen,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    currentStep.doList.forEach { item ->
                                        Text(
                                            "• $item",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                        )
                                    }
                                }
                            }

                            // Don't list
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "✕ DON'T",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF87171),
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    currentStep.dontList.forEach { item ->
                                        Text(
                                            "• $item",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                        )
                                    }
                                }
                            }
                        }

                        // Clinical reasoning box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.25f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = LightCyan,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(16.dp),
                                )
                                Text(
                                    "Clinical Impact: ${currentStep.clinicalReasoning}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LightCyan,
                                )
                            }
                        }
                    }
                }

                // Step dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(totalSteps) { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == currentStepIndex) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentStepIndex) PrimaryBlue else Color.White.copy(alpha = 0.3f)
                                ),
                        )
                    }
                }

                // Navigation action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Back")
                        }
                    }

                    if (currentStepIndex < totalSteps - 1) {
                        Button(
                            onClick = { currentStepIndex++ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("Next Step")
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else {
                        // Final step
                        Button(
                            onClick = {
                                onDismiss()
                                onStartCheck?.invoke()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGreen,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Got It! Done")
                        }
                    }
                }
            }
        }
    }
}
