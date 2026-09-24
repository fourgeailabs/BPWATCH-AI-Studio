package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.fourgeailabs.bpwatch.BuildConfig
import com.fourgeailabs.bpwatch.mobile.MainViewModel
import com.fourgeailabs.bpwatch.mobile.calibration.CalibrationEngine
import com.fourgeailabs.bpwatch.mobile.healthconnect.HealthConnectManager
import com.fourgeailabs.bpwatch.mobile.healthconnect.SleepDiagnosis
import com.fourgeailabs.bpwatch.mobile.monitoring.MonitoringConfig
import com.fourgeailabs.bpwatch.mobile.profile.UserProfile
import com.fourgeailabs.bpwatch.mobile.snore.SnoreScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    wrist: String = "left",
    onWristChange: (String) -> Unit = {},
    onOpenWatch: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenConnections: () -> Unit,
    onOpenMonitoring: () -> Unit,
    onOpenSleep: () -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenReminders: () -> Unit = {},
    onOpenHomeLayout: () -> Unit = {},
) {
    // v2.4.0: Settings is a hub — every section is a clickable card that
    // opens that section's own screen (back button returns here).
    val sections = listOf(
        Quad(Icons.Filled.Watch, "Watch app", "Watch location, calibrations, sync & updates", onOpenWatch),
        Quad(Icons.Filled.Person, "Body profile", "Height, weight, age, sex and BMI", onOpenProfile),
        Quad(Icons.Filled.Favorite, "Connections", "Samsung Health and Health Connect", onOpenConnections),
        Quad(Icons.Filled.MonitorHeart, "Monitoring & alerts", "Check schedule, thresholds, alerts", onOpenMonitoring),
        Quad(Icons.Filled.Bedtime, "Sleep", "Snore detection, sleep times and diagnostics", onOpenSleep),
        Quad(Icons.Filled.Notifications, "Reminders", "Daily and custom-day weight check alerts", onOpenReminders),
        Quad(Icons.Filled.Home, "Home", "Choose visible cards and arrange home screen order", onOpenHomeLayout),
        Quad(Icons.Filled.Tune, "Calibration", "Cuff readings and model status", onOpenCalibration),
        Quad(Icons.Filled.Info, "About", "Version, credits and links", onOpenAbout),
        Quad(Icons.Filled.NewReleases, "What's new", "Release history, newest first", onOpenChangelog),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        // Wrist sensor orientation bias toggle in settings menu
        WristOrientationCard(
            wrist = wrist,
            onWristChange = onWristChange,
        )

        sections.forEach { (icon, title, subtitle, onClick) ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "BPWatch ${BuildConfig.VERSION_NAME} — built for Galaxy Watch Ultra + Pixel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Explicit toggle in the settings menu allowing the user to select 'Left' or 'Right'
 * wrist to adjust the sensor orientation bias in the data processing algorithms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WristOrientationCard(
    wrist: String,
    onWristChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TintedIcon(
                    icon = Icons.Filled.Tune,
                    contentDescription = null,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sensor orientation bias",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Watch wrist: Left or Right",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                "Explicitly select 'Left' or 'Right' wrist to adjust the sensor orientation bias in the motion and posture data processing algorithms, and log the correct body location in Health Connect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                val isLeft = !wrist.equals("right", ignoreCase = true)
                SegmentedButton(
                    selected = isLeft,
                    onClick = { onWristChange("left") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { SegmentedButtonDefaults.Icon(active = isLeft) },
                    label = { Text("Left wrist") },
                )
                SegmentedButton(
                    selected = !isLeft,
                    onClick = { onWristChange("right") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { SegmentedButtonDefaults.Icon(active = !isLeft) },
                    label = { Text("Right wrist") },
                )
            }
            val currentWristLabel = if (wrist.equals("right", ignoreCase = true))
                "Right wrist (mirrored lateral axis correction active)"
            else
                "Left wrist (standard 3-axis orientation active)"
            Text(
                "Current bias: $currentWristLabel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** One row of the Settings hub. */
private data class Quad(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(profile: UserProfile, onSave: (UserProfile) -> Unit) {
    var height by remember(profile) { mutableStateOf(profile.heightCm?.toString() ?: "") }
    var weight by remember(profile) { mutableStateOf(profile.weightKg?.toString() ?: "") }
    var age by remember(profile) { mutableStateOf(profile.age?.toString() ?: "") }
    var sex by remember(profile) { mutableStateOf(profile.sex ?: "") }
    var sexExpanded by remember { mutableStateOf(false) }
    val sexOptions = listOf("Female", "Male", "Other", "Prefer not to say")

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Height, weight, age and sex — used for BMI and health context. " +
                    "Your BP estimate comes from your own cuff calibration, so your " +
                    "physiology is already baked in; this just rounds out the picture.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = sex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sex") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sexExpanded) },
                        modifier = Modifier.menuAnchor(),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false },
                    ) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    sex = option
                                    sexExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            val preview = UserProfile(
                heightCm = height.toFloatOrNull(),
                weightKg = weight.toFloatOrNull(),
                age = age.toIntOrNull(),
                sex = sex.ifBlank { null },
            )
            preview.bmi?.let { bmi ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "BMI: ${"%.1f".format(bmi)} (${preview.bmiLabel})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
            Button(
                onClick = {
                    onSave(preview)
                },
            ) {
                Text("Save profile")
            }
        }
    }
}

@Composable
fun SamsungHealthCard(
    hcAvailable: Boolean,
    hcGranted: Boolean,
    hcStatusText: String,
    hcNeedsUpdate: Boolean,
    hcGrantedSet: Set<String>,
    hcPermissions: Set<String>,
    hcReadPermissions: Set<String>,
    onRequestPermissions: (Set<String>) -> Unit,
) {
    val context = LocalContext.current
    val hcInstalled = remember {
        HealthConnectManager.isHealthConnectInstalled(context)
    }
    val samsungInstalled = remember {
        HealthConnectManager.isSamsungHealthInstalled(context)
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TintedIcon(icon = Icons.Filled.Favorite, contentDescription = null)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Samsung Health", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Watch data via Samsung Health → Health Connect",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "1. In Samsung Health: Settings → Health Connect → allow sharing " +
                    "(Sleep has its own toggle there — it must be on too).\n" +
                    "2. Below: grant BPWatch permission to read Health Connect.",
                style = MaterialTheme.typography.bodyMedium,
            )

            // Connection status row.
            val statusText = when {
                hcGranted -> "Status: connected ✓"
                !hcInstalled -> "Status: Health Connect isn't installed."
                hcNeedsUpdate -> "Status: Health Connect needs an update before apps can use it."
                !hcAvailable -> "Status: Health Connect $hcStatusText."
                else -> "Status: not connected (Health Connect $hcStatusText)"
            }
            ListItem(
                headlineContent = {
                    Text(statusText, style = MaterialTheme.typography.bodyMedium)
                },
                leadingContent = {
                    Icon(
                        if (hcGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (hcGranted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth(),
            )

            // Per-permission status: which declared permissions Android
            // reports as granted, each with its own re-request button.
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                hcPermissions.sorted().forEach { perm ->
                    val granted = perm in hcGrantedSet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            if (granted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (granted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = perm.substringAfterLast('.'),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (granted) "granted" else "not granted",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (granted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                        )
                        if (!granted) {
                            TextButton(onClick = { onRequestPermissions(setOf(perm)) }) {
                                Text("Request")
                            }
                        }
                    }
                }
            }

            if (samsungInstalled) {
                OutlinedButton(
                    onClick = { HealthConnectManager.openSamsungHealth(context) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open Samsung Health")
                }
            }
            if (!hcInstalled || hcNeedsUpdate) {
                Button(
                    onClick = { HealthConnectManager.openHealthConnectInPlayStore(context) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (hcNeedsUpdate) "Update Health Connect" else "Install Health Connect")
                }
            } else if (!hcGranted) {
                Button(
                    onClick = { onRequestPermissions(hcPermissions) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Connect Health Connect")
                }
                OutlinedButton(
                    onClick = { onRequestPermissions(hcReadPermissions) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Try read-only request")
                }
                OutlinedButton(
                    onClick = {
                        val ok = HealthConnectManager.openAppHealthPermissions(context)
                        Toast.makeText(
                            context,
                            if (ok) "Opening BPWatch's Health Connect toggles — " +
                                "switch them on, then come back and tap Refresh below."
                            else "Couldn't open Health Connect settings.",
                            Toast.LENGTH_LONG,
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Grant permissions manually")
                }
                OutlinedButton(
                    onClick = { HealthConnectManager.openHealthConnectSettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Health Connect settings")
                }
            }
            // v2.3.2: one-tap re-request even when everything looks granted —
            // covers silently-revoked or stuck grants without a Settings hunt.
            // The result toast + refreshed list below confirm what changed.
            if (hcGranted && hcInstalled && !hcNeedsUpdate) {
                OutlinedButton(
                    onClick = { onRequestPermissions(hcPermissions) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Re-request permissions")
                }
            }
        }
    }
}

/**
 * "Monitoring & alerts" — tells the watch what to keep an eye on. Every
 * change is pushed to the watch immediately (see MainViewModel.updateMonitoring).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringCard(
    config: MonitoringConfig,
    onUpdate: ((MonitoringConfig) -> MonitoringConfig) -> Unit,
    recordHr: Boolean,
    onRecordHrChange: (Boolean) -> Unit,
    wrist: String = "left",
    onWristChange: (String) -> Unit = {},
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TintedIcon(icon = Icons.Filled.MonitorHeart, contentDescription = null)
                Text(
                    "Your watch does the measuring — these settings tell it what " +
                        "to watch for. Changes are sent to your watch straight away.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))

            MonitoringSubHeader("Sensor orientation bias (wrist)")
            Text(
                "Explicitly select 'Left' or 'Right' wrist to adjust the sensor orientation bias in the motion and posture data processing algorithms, and log the correct body side in Health Connect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                val isLeft = !wrist.equals("right", ignoreCase = true)
                SegmentedButton(
                    selected = isLeft,
                    onClick = { onWristChange("left") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { SegmentedButtonDefaults.Icon(active = isLeft) },
                    label = { Text("Left wrist") },
                )
                SegmentedButton(
                    selected = !isLeft,
                    onClick = { onWristChange("right") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { SegmentedButtonDefaults.Icon(active = !isLeft) },
                    label = { Text("Right wrist") },
                )
            }
            val currentWristLabel = if (wrist.equals("right", ignoreCase = true))
                "Right wrist (mirrored lateral axis active)"
            else
                "Left wrist (standard orientation active)"
            Text(
                "Active algorithm bias: $currentWristLabel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(4.dp))

            SwitchRow(
                headline = "Continuous heart rate",
                subtitle = "Keeps the heart-rate sensor on all day. Uses noticeably more battery.",
                checked = config.continuousHr,
                onCheckedChange = { checked -> onUpdate { cfg -> cfg.copy(continuousHr = checked) } },
            )

            SwitchRow(
                headline = "Record heart rate continuously",
                subtitle = "Samples heart rate and stress every 10 minutes for your Trends " +
                    "graphs. Off by default — it uses more battery.",
                checked = recordHr,
                onCheckedChange = onRecordHrChange,
            )

            MonitoringSubHeader("Heart-rate alert")
            SwitchRow(
                headline = "High heart-rate alert",
                subtitle = "Buzz your watch when your heart rate goes over the limit.",
                checked = config.hrHighEnabled,
                onCheckedChange = { enabled -> onUpdate { cfg -> cfg.copy(hrHighEnabled = enabled) } },
            )
            if (config.hrHighEnabled) {
                ThresholdField(
                    label = "Alert above",
                    unit = "bpm",
                    value = config.hrHighThreshold,
                    range = MonitoringConfig.HR_THRESHOLD_MIN..MonitoringConfig.HR_THRESHOLD_MAX,
                    onCommit = { value -> onUpdate { cfg -> cfg.copy(hrHighThreshold = value) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            MonitoringSubHeader("Blood-pressure checks")
            var freqExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = freqExpanded,
                onExpandedChange = { freqExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = MonitoringConfig.intervalLabel(config.bpIntervalMinutes),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Check frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(freqExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true,
                )
                ExposedDropdownMenu(
                    expanded = freqExpanded,
                    onDismissRequest = { freqExpanded = false },
                ) {
                    MonitoringConfig.INTERVAL_OPTIONS.forEach { minutes ->
                        DropdownMenuItem(
                            text = { Text(MonitoringConfig.intervalLabel(minutes)) },
                            onClick = {
                                onUpdate { it.copy(bpIntervalMinutes = minutes) }
                                freqExpanded = false
                            },
                        )
                    }
                }
            }
            Text(
                "Each check samples your heart rate in the background and " +
                    "estimates blood pressure from your calibration.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MonitoringSubHeader("Blood-pressure alerts")
            SwitchRow(
                headline = "High blood-pressure alert",
                subtitle = "Buzz when systolic or diastolic reaches your high limit.",
                checked = config.bpHighEnabled,
                onCheckedChange = { enabled -> onUpdate { cfg -> cfg.copy(bpHighEnabled = enabled) } },
            )
            if (config.bpHighEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdField(
                        label = "Sys ≥",
                        unit = "mmHg",
                        value = config.sysHigh,
                        range = MonitoringConfig.BP_SYS_MIN..MonitoringConfig.BP_SYS_MAX,
                        onCommit = { value -> onUpdate { cfg -> cfg.copy(sysHigh = value) } },
                        modifier = Modifier.weight(1f),
                    )
                    ThresholdField(
                        label = "Dia ≥",
                        unit = "mmHg",
                        value = config.diaHigh,
                        range = MonitoringConfig.BP_DIA_MIN..MonitoringConfig.BP_DIA_MAX,
                        onCommit = { value -> onUpdate { cfg -> cfg.copy(diaHigh = value) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            SwitchRow(
                headline = "Low blood-pressure alert",
                subtitle = "Buzz when systolic or diastolic drops to your low limit.",
                checked = config.bpLowEnabled,
                onCheckedChange = { enabled -> onUpdate { cfg -> cfg.copy(bpLowEnabled = enabled) } },
            )
            if (config.bpLowEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThresholdField(
                        label = "Sys ≤",
                        unit = "mmHg",
                        value = config.sysLow,
                        range = MonitoringConfig.BP_SYS_MIN..MonitoringConfig.BP_SYS_MAX,
                        onCommit = { value -> onUpdate { cfg -> cfg.copy(sysLow = value) } },
                        modifier = Modifier.weight(1f),
                    )
                    ThresholdField(
                        label = "Dia ≤",
                        unit = "mmHg",
                        value = config.diaLow,
                        range = MonitoringConfig.BP_DIA_MIN..MonitoringConfig.BP_DIA_MAX,
                        onCommit = { value -> onUpdate { cfg -> cfg.copy(diaLow = value) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Alerts buzz on your watch and show the reading that tripped them. " +
                    "Each alert type waits 15 minutes before buzzing again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun MonitoringSubHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
fun SwitchRow(
    headline: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(headline, style = MaterialTheme.typography.titleSmall)
        },
        supportingContent = {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * A numeric threshold field that commits (validates + clamps) when the user
 * taps Done or moves focus away.
 */
@Composable
fun ThresholdField(
    label: String,
    unit: String,
    value: Int,
    range: IntRange,
    onCommit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    fun commit() {
        val parsed = text.toIntOrNull()?.coerceIn(range) ?: value
        text = parsed.toString()
        if (parsed != value) onCommit(parsed)
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it.filter { c -> c.isDigit() }.take(3) },
        label = { Text(label) },
        suffix = { Text(unit) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(onDone = { commit() }),
        singleLine = true,
        modifier = modifier.onFocusChanged { if (!it.isFocused) commit() },
    )
}

/**
 * Snore detection (v2.3): opt-in overnight listening on the phone
 * microphone. The copy is explicit that the microphone records overnight
 * and that it costs extra battery, and that clips stay on the phone.
 */
@Composable
fun SnoreCard(
    enabled: Boolean,
    listening: Boolean,
    status: String?,
    onToggle: (Boolean) -> Unit,
    onStartNow: () -> Unit,
    onRequestMicPermission: () -> Unit,
    isMicGranted: () -> Boolean,
    sleepStartHour: Int = 22,
    sleepStartMinute: Int = 0,
    sleepEndHour: Int = 7,
    sleepEndMinute: Int = 0,
    onUpdateSleepSchedule: (startH: Int, startM: Int, endH: Int, endM: Int) -> Unit = { _, _, _, _ -> },
) {
    var startH by remember(sleepStartHour) { mutableStateOf(sleepStartHour) }
    var startM by remember(sleepStartMinute) { mutableStateOf(sleepStartMinute) }
    var endH by remember(sleepEndHour) { mutableStateOf(sleepEndHour) }
    var endM by remember(sleepEndMinute) { mutableStateOf(sleepEndMinute) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TintedIcon(icon = Icons.Filled.Bedtime, contentDescription = null)
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("Snore detection & sleep window", style = MaterialTheme.typography.titleMedium)
                    val sFormatted = "%02d:%02d".format(startH, startM)
                    val eFormatted = "%02d:%02d".format(endH, endM)
                    Text(
                        "Listens with the microphone during sleep window ($sFormatted to $eFormatted). " +
                            "Uses extra battery overnight.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { want ->
                        if (want && !isMicGranted()) {
                            onRequestMicPermission()
                        } else {
                            onToggle(want)
                        }
                    },
                )
            }

            Text(
                "Adjust sleep tracking times to let the phone know when to monitor sleep and start listening for snoring:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Bedtime 12h Picker
            TimePicker12hSection(
                title = "Bedtime (start)",
                hour24 = startH,
                minute = startM,
                onTimeChanged = { h, m ->
                    startH = h
                    startM = m
                    onUpdateSleepSchedule(startH, startM, endH, endM)
                },
                presets = listOf(21 to 0, 22 to 0, 22 to 30, 23 to 0, 23 to 30, 0 to 0),
            )

            Spacer(Modifier.height(4.dp))

            // Wakeup 12h Picker
            TimePicker12hSection(
                title = "Wakeup (end)",
                hour24 = endH,
                minute = endM,
                onTimeChanged = { h, m ->
                    endH = h
                    endM = m
                    onUpdateSleepSchedule(startH, startM, endH, endM)
                },
                presets = listOf(6 to 0, 6 to 30, 7 to 0, 7 to 30, 8 to 0, 8 to 30),
            )

            if (enabled && !listening) {
                // Manual start only makes sense inside the overnight window;
                // outside it the status line already says when listening begins.
                val inWindow = remember { SnoreScheduler.inWindow() }
                if (inWindow) {
                    OutlinedButton(onClick = onStartNow, modifier = Modifier.fillMaxWidth()) {
                        Text("Start listening now")
                    }
                }
            }
            if (status != null) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "Clips stay on this phone — nothing is sent or uploaded anywhere else.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Reusable 12-hour time picker section with AM/PM segmented buttons, steppers, and quick-set presets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker12hSection(
    title: String,
    hour24: Int,
    minute: Int,
    onTimeChanged: (hour24: Int, minute: Int) -> Unit,
    presets: List<Pair<Int, Int>> = emptyList(),
) {
    fun from24h(h24: Int): Pair<Int, Boolean> {
        val pm = h24 >= 12
        val h12 = when (val h = h24 % 12) {
            0 -> 12
            else -> h
        }
        return Pair(h12, pm)
    }

    fun to24h(h12: Int, pm: Boolean): Int {
        val h = h12.coerceIn(1, 12)
        return if (pm) {
            if (h == 12) 12 else h + 12
        } else {
            if (h == 12) 0 else h
        }
    }

    val (currentH12, currentIsPm) = from24h(hour24)
    var hour12 by remember(hour24) { androidx.compose.runtime.mutableIntStateOf(currentH12) }
    var isPm by remember(hour24) { mutableStateOf(currentIsPm) }
    var curMin by remember(minute) { androidx.compose.runtime.mutableIntStateOf(minute.coerceIn(0, 59)) }
    var hourInput by remember(hour24) { mutableStateOf(currentH12.toString()) }
    var minInput by remember(minute) { mutableStateOf("%02d".format(minute.coerceIn(0, 59))) }

    fun emit(h: Int, m: Int, pm: Boolean) {
        val safeH = h.coerceIn(1, 12)
        val safeM = m.coerceIn(0, 59)
        hour12 = safeH
        curMin = safeM
        isPm = pm
        hourInput = safeH.toString()
        minInput = "%02d".format(safeM)
        onTimeChanged(to24h(safeH, pm), safeM)
    }

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "%d:%02d %s".format(hour12, curMin, if (isPm) "PM" else "AM"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // AM / PM Segmented control
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = !isPm,
                        onClick = { emit(hour12, curMin, false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("AM", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    )
                    SegmentedButton(
                        selected = isPm,
                        onClick = { emit(hour12, curMin, true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("PM", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    )
                }
            }

            // Hour and Minute stepper controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hour
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hour (1–12)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                val next = if (hour12 <= 1) 12 else hour12 - 1
                                emit(next, curMin, isPm)
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Text("-", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = hourInput,
                            onValueChange = { s ->
                                val digits = s.filter { it.isDigit() }.take(2)
                                hourInput = digits
                                digits.toIntOrNull()?.let { if (it in 1..12) emit(it, curMin, isPm) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = {
                                val next = if (hour12 >= 12) 1 else hour12 + 1
                                emit(next, curMin, isPm)
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Text("+", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }

                // Minute
                Column(modifier = Modifier.weight(1f)) {
                    Text("Minute (00–59)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                val next = (curMin - 5 + 60) % 60
                                emit(hour12, next, isPm)
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Text("-", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = minInput,
                            onValueChange = { s ->
                                val digits = s.filter { it.isDigit() }.take(2)
                                minInput = digits
                                digits.toIntOrNull()?.let { if (it in 0..59) emit(hour12, it, isPm) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = {
                                val next = (curMin + 5) % 60
                                emit(hour12, next, isPm)
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Text("+", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }

            if (presets.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    presets.forEach { (presetH24, presetM) ->
                        val (pH12, pPm) = from24h(presetH24)
                        val isSelected = hour24 == presetH24 && minute == presetM
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                emit(pH12, presetM, pPm)
                            },
                            label = {
                                Text("%d:%02d %s".format(pH12, presetM, if (pPm) "PM" else "AM"), style = MaterialTheme.typography.labelSmall)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Weight check and health routine reminders card.
 * Supports full 12-hour time format with AM/PM selection, direct manual time entry,
 * steppers, and quick-set presets.
 */
@Composable
fun RemindersCard(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    daysCsv: String,
    onSave: (enabled: Boolean, hour: Int, minute: Int, daysCsv: String) -> Unit,
) {
    var isEnabled by remember(enabled) { mutableStateOf(enabled) }
    var curHour by remember(hour) { androidx.compose.runtime.mutableIntStateOf(hour.coerceIn(0, 23)) }
    var curMinute by remember(minute) { androidx.compose.runtime.mutableIntStateOf(minute.coerceIn(0, 59)) }

    var selectedDays by remember(daysCsv) {
        mutableStateOf(daysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet())
    }

    val dayLabels = listOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed",
        4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
    )

    val presets = listOf(
        7 to 0,   // 7:00 AM
        8 to 0,   // 8:00 AM
        12 to 0,  // 12:00 PM
        18 to 0,  // 6:00 PM
        21 to 0,  // 9:00 PM
    )

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TintedIcon(icon = Icons.Filled.Notifications, contentDescription = null)
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("Weight Check Reminders", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Receive daily or selected-day reminders to measure and record your weight.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        val csv = selectedDays.sorted().joinToString(",")
                        onSave(isEnabled, curHour, curMinute, csv)
                    },
                )
            }

            if (isEnabled) {
                // Scheduled Time 12h Picker
                TimePicker12hSection(
                    title = "Scheduled Time",
                    hour24 = curHour,
                    minute = curMinute,
                    onTimeChanged = { h24, m ->
                        curHour = h24
                        curMinute = m
                        val csv = selectedDays.sorted().joinToString(",")
                        onSave(isEnabled, h24, m, csv)
                    },
                    presets = presets,
                )

                // Repeat Days selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Repeat Days",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = {
                                    selectedDays = (1..7).toSet()
                                    val csv = selectedDays.sorted().joinToString(",")
                                    onSave(isEnabled, curHour, curMinute, csv)
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text("Every day", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(
                                onClick = {
                                    selectedDays = setOf(1, 2, 3, 4, 5)
                                    val csv = selectedDays.sorted().joinToString(",")
                                    onSave(isEnabled, curHour, curMinute, csv)
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text("Weekdays", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        dayLabels.forEach { (dayInt, label) ->
                            val isSelected = selectedDays.contains(dayInt)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - dayInt else selectedDays
                                    } else {
                                        selectedDays + dayInt
                                    }
                                    val csv = selectedDays.sorted().joinToString(",")
                                    onSave(isEnabled, curHour, curMinute, csv)
                                },
                                label = {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else null,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
