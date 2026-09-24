package com.fourgeailabs.bpwatch.mobile.ui

import androidx.activity.compose.BackHandler
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fourgeailabs.bpwatch.Link
import com.fourgeailabs.bpwatch.mobile.DashboardMetrics
import com.fourgeailabs.bpwatch.mobile.MainViewModel
import com.fourgeailabs.bpwatch.mobile.data.HealthLog
import com.fourgeailabs.bpwatch.mobile.data.Reading
import com.fourgeailabs.bpwatch.mobile.healthconnect.HcTrendMetric
import com.fourgeailabs.bpwatch.mobile.healthconnect.HcTrendPoint
import com.fourgeailabs.bpwatch.mobile.healthconnect.SleepDiagnosis
import com.fourgeailabs.bpwatch.mobile.wearable.BiaState
import com.fourgeailabs.bpwatch.mobile.wearable.WatchLiveState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Trends metrics. Public so HomeScreen can deep-link here.
 */
enum class TrendMetric(val label: String) {
    HEART_RATE("Heart rate"),
    STRESS("Stress"),
    BLOOD_PRESSURE("Blood pressure"),
    STEPS("Steps"),
    DISTANCE("Distance"),
    CALORIES("Calories"),
    WEIGHT("Weight"),
    SLEEP("Sleep"),
    HYDRATION("Hydration"),
    RESTING_HR("Resting HR"),
    HRV("HRV (RMSSD)"),
    BODY_FAT("Body fat"),
    SKIN_TEMP("Skin temp"),
    BMI("BMI"),
}

fun trendIcon(metric: TrendMetric): ImageVector = when (metric) {
    TrendMetric.HEART_RATE -> Icons.Filled.Favorite
    TrendMetric.STRESS -> Icons.Filled.Psychology
    TrendMetric.BLOOD_PRESSURE -> Icons.Filled.MonitorHeart
    TrendMetric.STEPS -> Icons.Filled.DirectionsWalk
    TrendMetric.DISTANCE -> Icons.Filled.Place
    TrendMetric.CALORIES -> Icons.Filled.LocalFireDepartment
    TrendMetric.WEIGHT -> Icons.Filled.MonitorWeight
    TrendMetric.SLEEP -> Icons.Filled.Bedtime
    TrendMetric.HYDRATION -> Icons.Filled.WaterDrop
    TrendMetric.RESTING_HR -> Icons.Filled.FavoriteBorder
    TrendMetric.HRV -> Icons.Filled.GraphicEq
    TrendMetric.BODY_FAT -> Icons.Filled.Accessibility
    TrendMetric.SKIN_TEMP -> Icons.Filled.Thermostat
    TrendMetric.BMI -> Icons.Filled.Scale
}

fun trendColor(metric: TrendMetric): Color = when (metric) {
    TrendMetric.HEART_RATE -> Color(0xFFD93025)
    TrendMetric.STRESS -> Color(0xFF7B1FA2)
    TrendMetric.BLOOD_PRESSURE -> Color(0xFFC5221F)
    TrendMetric.STEPS -> Color(0xFF1A73E8)
    TrendMetric.DISTANCE -> Color(0xFF9334E6)
    TrendMetric.CALORIES -> Color(0xFFEA8600)
    TrendMetric.WEIGHT -> Color(0xFF0B8043)
    TrendMetric.SLEEP -> Color(0xFF3949AB)
    TrendMetric.HYDRATION -> Color(0xFF039BE5)
    TrendMetric.RESTING_HR -> Color(0xFFD81B60)
    TrendMetric.HRV -> Color(0xFF00897B)
    TrendMetric.BODY_FAT -> Color(0xFF6D4C41)
    TrendMetric.SKIN_TEMP -> Color(0xFF00ACC1)
    TrendMetric.BMI -> Color(0xFF5E35B1)
}

/** True for the Health Connect-backed metrics (STEPS onwards, except BMI which is computed from weight). */
private fun TrendMetric.isHcTrend(): Boolean = when (this) {
    TrendMetric.STEPS, TrendMetric.DISTANCE, TrendMetric.CALORIES,
    TrendMetric.WEIGHT, TrendMetric.SLEEP, TrendMetric.HYDRATION,
    TrendMetric.RESTING_HR, TrendMetric.HRV, TrendMetric.BODY_FAT,
    TrendMetric.SKIN_TEMP -> true
    else -> false
}

private fun TrendMetric.toHcTrendMetric(): HcTrendMetric = when (this) {
    TrendMetric.STEPS -> HcTrendMetric.STEPS
    TrendMetric.DISTANCE -> HcTrendMetric.DISTANCE
    TrendMetric.CALORIES -> HcTrendMetric.CALORIES
    TrendMetric.WEIGHT -> HcTrendMetric.WEIGHT
    TrendMetric.SLEEP -> HcTrendMetric.SLEEP
    TrendMetric.HYDRATION -> HcTrendMetric.HYDRATION
    TrendMetric.RESTING_HR -> HcTrendMetric.RESTING_HR
    TrendMetric.HRV -> HcTrendMetric.HRV
    TrendMetric.BODY_FAT -> HcTrendMetric.BODY_FAT
    TrendMetric.SKIN_TEMP -> HcTrendMetric.SKIN_TEMP
    else -> error("not a Health Connect trend metric: $this")
}

private enum class TrendRange(val label: String, val millis: Long) {
    HOUR("Hour", 24L * 3600_000L),
    DAY("Day", 24L * 3600_000L),
    WEEK("Week", 7L * 24L * 3600_000L),
    MONTH("Month", 30L * 24L * 3600_000L),
    YEAR("Year", 365L * 24L * 3600_000L),
    ALL("All", -1L),
}

private data class ChartPoint(val x: Long, val y: Float)

private data class ChartSeries(
    val label: String,
    val color: Color,
    val unit: String,
    val points: List<ChartPoint>,
)

data class TrendCardData(
    val metric: TrendMetric,
    val valueText: String,
    val subText: String,
    val hasData: Boolean,
)

/**
 * Computes the most current headline data for a given trend metric from all sources:
 * local readings, Health Connect, live watch telemetry, and BIA scans.
 */
fun resolveTrendCardData(
    metric: TrendMetric,
    dashboard: DashboardMetrics,
    readings: List<Reading>,
    healthLogs: List<HealthLog>,
    biaResult: BiaState.BiaResult?,
    liveHr: Float?,
    liveHrAt: Long,
    liveHrv: Float?,
): TrendCardData {
    val liveFresh = liveHr != null && liveHrAt > 0L && WatchLiveState.isLiveHrFresh()
    return when (metric) {
        TrendMetric.HEART_RATE -> {
            val bpm = when {
                liveFresh -> "${liveHr!!.toInt()} bpm"
                dashboard.heartRateBpm != null -> "${dashboard.heartRateBpm} bpm"
                else -> readings.filter { it.heartRate != null }.maxByOrNull { it.timestamp }?.heartRate?.let { "${it.toInt()} bpm" }
            }
            TrendCardData(
                metric = metric,
                valueText = bpm ?: "No data",
                subText = if (liveFresh) "Live from watch" else "Daily heart rate",
                hasData = bpm != null,
            )
        }
        TrendMetric.STRESS -> {
            val score = dashboard.stress
                ?: readings.filter { it.stress != null && it.stress >= 0 }.maxByOrNull { it.timestamp }?.stress
            TrendCardData(
                metric = metric,
                valueText = if (score != null) "$score / 100" else "No data",
                subText = if (score != null) when {
                    score < 25 -> "Low · Restful state"
                    score < 50 -> "Mild · Balanced"
                    score < 75 -> "Moderate tension"
                    else -> "High stress"
                } else "Estimated during BP checks",
                hasData = score != null,
            )
        }
        TrendMetric.BLOOD_PRESSURE -> {
            val latest = readings.maxByOrNull { it.timestamp }
            val sys = latest?.sysEstimate ?: latest?.sysCuff
            val dia = latest?.diaEstimate ?: latest?.diaCuff
            val bpStr = if (sys != null && dia != null) "$sys / $dia mmHg" else null
            TrendCardData(
                metric = metric,
                valueText = bpStr ?: "No data",
                subText = if (latest?.sysEstimate != null) "Latest estimate" else if (latest?.sysCuff != null) "Cuff reading" else "Calibrate to begin",
                hasData = bpStr != null,
            )
        }
        TrendMetric.STEPS -> {
            val s = dashboard.steps
            TrendCardData(
                metric = metric,
                valueText = if (s != null) "%,d steps".format(Locale.US, s) else "No data",
                subText = "Today's step count",
                hasData = s != null,
            )
        }
        TrendMetric.DISTANCE -> {
            val d = dashboard.distanceMi
            TrendCardData(
                metric = metric,
                valueText = if (d != null) "%.2f mi".format(Locale.US, d) else "No data",
                subText = "Distance covered today",
                hasData = d != null,
            )
        }
        TrendMetric.CALORIES -> {
            val c = dashboard.caloriesKcal
            TrendCardData(
                metric = metric,
                valueText = if (c != null) "${c.toInt()} kcal" else "No data",
                subText = "Active burn today",
                hasData = c != null,
            )
        }
        TrendMetric.WEIGHT -> {
            val w = dashboard.weightLb
                ?: healthLogs.filter { it.kind == "weight" }.maxByOrNull { it.timestamp }?.value
            TrendCardData(
                metric = metric,
                valueText = if (w != null) "%.1f lb".format(Locale.US, w) else "No data",
                subText = "Latest body weight",
                hasData = w != null,
            )
        }
        TrendMetric.SLEEP -> {
            val s = dashboard.sleepHours
            val sleepStr = if (s != null && s > 0) {
                val h = s.toInt()
                val m = ((s - h) * 60).toInt()
                if (h > 0) "${h}h ${m}m" else "${m}m"
            } else null
            TrendCardData(
                metric = metric,
                valueText = sleepStr ?: "No data",
                subText = "Last night's recorded sleep",
                hasData = sleepStr != null,
            )
        }
        TrendMetric.HYDRATION -> {
            val l = dashboard.hydrationMl?.let { it / 1000.0 }
                ?: healthLogs.filter { it.kind == "hydration" }.takeIf { it.isNotEmpty() }?.let { it.sumOf { h -> h.value } / 1000.0 }
            TrendCardData(
                metric = metric,
                valueText = if (l != null) "%.1f L".format(Locale.US, l) else "No data",
                subText = "Fluid consumption today",
                hasData = l != null,
            )
        }
        TrendMetric.RESTING_HR -> {
            val r = dashboard.restingHeartRateBpm
                ?: readings.filter { it.heartRate != null && (it.activity == "sitting" || it.activity == "sleeping" || it.bodyPosition == Link.Posture.SITTING_DOWN) }.maxByOrNull { it.timestamp }?.heartRate?.toLong()
            TrendCardData(
                metric = metric,
                valueText = if (r != null) "$r bpm" else "No data",
                subText = "Baseline resting pulse",
                hasData = r != null,
            )
        }
        TrendMetric.HRV -> {
            val hrv = when {
                liveHrv != null && WatchLiveState.isLiveHrvFresh() -> "${liveHrv.toInt()} ms"
                dashboard.hrvRmssd != null -> "${dashboard.hrvRmssd.toInt()} ms"
                else -> readings.filter { it.hrvRmssd != null }.maxByOrNull { it.timestamp }?.hrvRmssd?.let { "${it.toInt()} ms" }
                    ?: healthLogs.filter { it.kind == "hrv" }.maxByOrNull { it.timestamp }?.value?.let { "${it.toInt()} ms" }
            }
            TrendCardData(
                metric = metric,
                valueText = hrv ?: "No data",
                subText = "RMSSD autonomic balance",
                hasData = hrv != null,
            )
        }
        TrendMetric.BODY_FAT -> {
            val bf = biaResult?.bodyFatPct
                ?: dashboard.bodyFatPercentage
                ?: healthLogs.filter { it.kind == "body_fat" || it.kind == "bodyFat" }.maxByOrNull { it.timestamp }?.value
            TrendCardData(
                metric = metric,
                valueText = if (bf != null) "%.1f%%".format(Locale.US, bf) else "No data",
                subText = if (bf != null) "BIA composition index" else "Tap to scan or view history",
                hasData = bf != null,
            )
        }
        TrendMetric.SKIN_TEMP -> {
            val temp = dashboard.skinTempC?.let { "%.1f°C".format(Locale.US, it) }
                ?: readings.filter { it.skinTempC != null }.maxByOrNull { it.timestamp }?.skinTempC?.let { "%.1f°C".format(Locale.US, it) }
            TrendCardData(
                metric = metric,
                valueText = temp ?: "No data",
                subText = "Wrist optical temperature",
                hasData = temp != null,
            )
        }
        TrendMetric.BMI -> {
            val bmi = dashboard.bmi
            val lbl = dashboard.bmiLabel
            TrendCardData(
                metric = metric,
                valueText = if (bmi != null) "%.1f".format(Locale.US, bmi) else "No data",
                subText = lbl ?: "Body Mass Index",
                hasData = bmi != null,
            )
        }
    }
}

/**
 * Trends Screen:
 * Displays all health trends as interactive cards showing the most current data for each metric.
 * Clicking any trend card opens its detailed historical trend chart, multi-range controls, and stats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    viewModel: MainViewModel,
    onRequestHcPermissions: (Set<String>) -> Unit,
    initialMetric: TrendMetric? = null,
    onInitialMetricConsumed: () -> Unit = {},
    onDiagnoseSleep: (suspend () -> SleepDiagnosis)? = null,
) {
    // When selectedMetric is null, the Overview of cards is displayed.
    // When selectedMetric is set, the detailed trend chart and statistics for that metric are shown.
    var selectedMetric by remember { mutableStateOf<TrendMetric?>(null) }
    var range by remember { mutableStateOf(TrendRange.WEEK) }
    var refreshTick by remember { mutableStateOf(0) }
    var lastUpdatedMs by remember { mutableStateOf<Long?>(null) }
    var showBodyFatReader by remember { mutableStateOf(false) }

    val dashboard by viewModel.dashboardMetrics.collectAsState()
    val readings by viewModel.readings.collectAsState()
    val healthLogs by viewModel.healthLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val biaResult by BiaState.latestResult.collectAsState()
    val liveHr by WatchLiveState.liveHr.collectAsState()
    val liveHrAt by WatchLiveState.liveHrAt.collectAsState()
    val liveHrv by WatchLiveState.liveHrv.collectAsState()

    // Deep-link from Home tiles: preselect the metric and open detail directly.
    LaunchedEffect(initialMetric) {
        if (initialMetric != null) {
            selectedMetric = initialMetric
            onInitialMetricConsumed()
        }
    }

    val now = remember(range, refreshTick) { System.currentTimeMillis() }
    val start = if (range == TrendRange.ALL) {
        java.time.LocalDate.of(2015, 1, 1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    } else {
        now - range.millis
    }

    val hrSamples by remember(range, refreshTick) {
        viewModel.observeHrRange(start, now)
    }.collectAsState(initial = emptyList())
    val stressSamples by remember(range, refreshTick) {
        viewModel.observeStressRange(start, now)
    }.collectAsState(initial = emptyList())

    var hcAvailable by remember { mutableStateOf(false) }
    var hcTrend by remember { mutableStateOf<List<HcTrendPoint>>(emptyList()) }
    var hcTrendLoading by remember { mutableStateOf(false) }

    LaunchedEffect(range, selectedMetric, refreshTick) {
        val current = selectedMetric ?: return@LaunchedEffect
        if (current.isHcTrend()) {
            hcTrend = emptyList()
            hcTrendLoading = true
            hcAvailable = viewModel.isHcTrendsAvailable()
            hcTrend = viewModel.loadHcTrendRange(
                current.toHcTrendMetric(),
                Instant.ofEpochMilli(start),
                Instant.ofEpochMilli(now),
                bucketHours = if (range == TrendRange.HOUR || range == TrendRange.DAY) 1L else 24L,
            )
            hcTrendLoading = false
            lastUpdatedMs = System.currentTimeMillis()
        } else if (current == TrendMetric.BMI) {
            hcTrend = emptyList()
            hcTrendLoading = true
            hcAvailable = viewModel.isHcTrendsAvailable()
            hcTrend = viewModel.loadHcTrendRange(
                HcTrendMetric.WEIGHT,
                Instant.ofEpochMilli(start),
                Instant.ofEpochMilli(now),
                bucketHours = if (range == TrendRange.HOUR || range == TrendRange.DAY) 1L else 24L,
            )
            hcTrendLoading = false
            lastUpdatedMs = System.currentTimeMillis()
        }
    }

    val series: List<ChartSeries> = remember(
        selectedMetric, range, hrSamples, stressSamples, readings, hcTrend, profile, start, now,
        healthLogs, biaResult, dashboard
    ) {
        val current = selectedMetric ?: return@remember emptyList()
        val hrPoints = hrSamples.map { ChartPoint(it.timestamp, it.bpm) }
            .let { if (range == TrendRange.HOUR) bucketHourly(it, start) else it }
        val stressPoints = (
            stressSamples.map { ChartPoint(it.timestamp, it.score.toFloat()) } +
                readings.mapNotNull { r ->
                    r.stress?.takeIf { it >= 0 && r.timestamp in start..now }
                        ?.let { ChartPoint(r.timestamp, it.toFloat()) }
                }
            ).sortedBy { it.x }
            .let { if (range == TrendRange.HOUR) bucketHourly(it, start) else it }

        when (current) {
            TrendMetric.HEART_RATE -> listOf(
                ChartSeries(
                    label = "Heart rate",
                    color = Color(0xFFD93025),
                    unit = "bpm",
                    points = hrPoints,
                )
            )
            TrendMetric.STRESS -> listOf(
                ChartSeries(
                    label = "Stress",
                    color = Color(0xFF9334E6),
                    unit = "",
                    points = stressPoints,
                )
            )
            TrendMetric.BLOOD_PRESSURE -> {
                val inRange = readings.filter { it.timestamp in start..now }
                listOf(
                    ChartSeries(
                        label = "Systolic",
                        color = Color(0xFFD93025),
                        unit = "mmHg",
                        points = inRange.mapNotNull { r ->
                            (r.sysEstimate ?: r.sysCuff)?.let {
                                ChartPoint(r.timestamp, it.toFloat())
                            }
                        },
                    ),
                    ChartSeries(
                        label = "Diastolic",
                        color = Color(0xFF1A73E8),
                        unit = "mmHg",
                        points = inRange.mapNotNull { r ->
                            (r.diaEstimate ?: r.diaCuff)?.let {
                                ChartPoint(r.timestamp, it.toFloat())
                            }
                        },
                    ),
                )
            }
            TrendMetric.STEPS -> hcSeries("Steps", Color(0xFF1A73E8), "", hcTrend)
            TrendMetric.DISTANCE -> hcSeries("Distance", Color(0xFF9334E6), "mi", hcTrend)
            TrendMetric.CALORIES -> hcSeries("Calories", Color(0xFFEA8600), "kcal", hcTrend)
            TrendMetric.WEIGHT -> {
                val localWeight = healthLogs.filter { it.kind == "weight" && it.timestamp in start..now }
                    .map { ChartPoint(it.timestamp, it.value.toFloat()) }
                val hcWeight = hcSeries("Weight", Color(0xFF0B8043), "lb", hcTrend).firstOrNull()?.points ?: emptyList()
                val dashboardWeight = dashboard.weightLb?.let {
                    listOf(ChartPoint(now, it.toFloat()))
                } ?: emptyList()
                val merged = (localWeight + hcWeight + dashboardWeight).distinctBy { it.x }.sortedBy { it.x }
                listOf(ChartSeries("Weight", Color(0xFF0B8043), "lb", merged))
            }
            TrendMetric.SLEEP -> {
                val hcSleep = hcSeries("Sleep", Color(0xFF3949AB), "h", hcTrend).firstOrNull()?.points ?: emptyList()
                val dashboardSleep = dashboard.sleepHours?.takeIf { it > 0 }?.let {
                    listOf(ChartPoint(now, it.toFloat()))
                } ?: emptyList()
                val merged = (hcSleep + dashboardSleep).distinctBy { it.x }.sortedBy { it.x }
                listOf(ChartSeries("Sleep", Color(0xFF3949AB), "h", merged))
            }
            TrendMetric.HYDRATION -> {
                val localHydration = healthLogs.filter { it.kind == "hydration" && it.timestamp in start..now }
                    .map { ChartPoint(it.timestamp, (it.value / 1000.0).toFloat()) }
                val hcHydration = hcSeries("Hydration", Color(0xFF039BE5), "L", hcTrend).firstOrNull()?.points ?: emptyList()
                val dashboardHydration = dashboard.hydrationMl?.let {
                    listOf(ChartPoint(now, (it / 1000.0).toFloat()))
                } ?: emptyList()
                val merged = (localHydration + hcHydration + dashboardHydration).distinctBy { it.x }.sortedBy { it.x }
                listOf(ChartSeries("Hydration", Color(0xFF039BE5), "L", merged))
            }
            TrendMetric.RESTING_HR -> {
                val hcResting = hcSeries("Resting HR", Color(0xFFD81B60), "bpm", hcTrend).firstOrNull()?.points ?: emptyList()
                val localResting = readings.filter { it.timestamp in start..now && it.heartRate != null }
                    .filter { it.activity == "sitting" || it.activity == "sleeping" || it.bodyPosition == Link.Posture.SITTING_DOWN || (it.stress != null && it.stress < 30) }
                    .map { ChartPoint(it.timestamp, it.heartRate!!) }
                val dashboardResting = dashboard.restingHeartRateBpm?.let {
                    listOf(ChartPoint(now, it.toFloat()))
                } ?: emptyList()
                val merged = (localResting + hcResting + dashboardResting).distinctBy { it.x }.sortedBy { it.x }
                listOf(ChartSeries("Resting HR", Color(0xFFD81B60), "bpm", merged))
            }
            TrendMetric.HRV -> {
                val localHrv = readings.filter { it.timestamp in start..now && it.hrvRmssd != null }
                    .map { ChartPoint(it.timestamp, it.hrvRmssd!!) }
                val localLogs = healthLogs.filter { it.kind == "hrv" && it.timestamp in start..now }
                    .map { ChartPoint(it.timestamp, it.value.toFloat()) }
                val hcHrv = hcSeries("HRV", Color(0xFF00897B), "ms", hcTrend).firstOrNull()?.points ?: emptyList()
                val dashboardHrv = dashboard.hrvRmssd?.let {
                    listOf(ChartPoint(now, it.toFloat()))
                } ?: emptyList()
                val merged = (localHrv + localLogs + hcHrv + dashboardHrv).distinctBy { it.x }.sortedBy { it.x }
                listOf(ChartSeries("HRV", Color(0xFF00897B), "ms", merged))
            }
            TrendMetric.BODY_FAT -> {
                val localBia = biaResult?.let {
                    if (it.timestamp in start..now) listOf(ChartPoint(it.timestamp, it.bodyFatPct.toFloat())) else null
                } ?: emptyList()
                val localLogs = healthLogs.filter { (it.kind == "body_fat" || it.kind == "bodyFat") && it.timestamp in start..now }
                    .map { ChartPoint(it.timestamp, it.value.toFloat()) }
                val hcBf = hcSeries("Body fat", Color(0xFF6D4C41), "%", hcTrend).firstOrNull()?.points ?: emptyList()
                val dashboardBf = dashboard.bodyFatPercentage?.let {
                    listOf(ChartPoint(now, it.toFloat()))
                } ?: emptyList()
                val merged = (localBia + localLogs + hcBf + dashboardBf).distinctBy { it.x }.sortedBy { it.x }
                listOf(ChartSeries("Body fat", Color(0xFF6D4C41), "%", merged))
            }
            TrendMetric.SKIN_TEMP -> {
                val localTemp = readings.filter { it.timestamp in start..now && it.skinTempC != null }
                    .map { ChartPoint(it.timestamp, it.skinTempC!!) }
                val hcTemp = hcSeries("Skin temp", Color(0xFF00ACC1), "°C", hcTrend).firstOrNull()?.points ?: emptyList()
                val dashboardTemp = dashboard.skinTempC?.let {
                    listOf(ChartPoint(now, it.toFloat()))
                } ?: emptyList()
                val merged = (localTemp + hcTemp + dashboardTemp).distinctBy { it.x }.sortedBy { it.x }
                listOf(ChartSeries("Skin temp", Color(0xFF00ACC1), "°C", merged))
            }
            TrendMetric.BMI -> {
                val heightM = profile.heightCm?.takeIf { it > 0f }?.div(100f)
                val bmiPoints = if (heightM != null) {
                    hcTrend.map { p ->
                        val kg = p.value / 2.20462f
                        ChartPoint(p.timestamp, kg / (heightM * heightM))
                    }
                } else {
                    emptyList()
                }
                listOf(ChartSeries("BMI", Color(0xFF5E35B1), "", bmiPoints))
            }
        }
    }

    val currentMetric = selectedMetric
    if (currentMetric == null) {
        // =================================================================
        // TRENDS OVERVIEW: All trends displayed as clickable cards with
        // the most current data on each card.
        // =================================================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Health Trends",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Tap any metric card to open its full trend chart, custom time spans, and analysis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            TrendMetric.entries.forEach { m ->
                val cardData = resolveTrendCardData(
                    metric = m,
                    dashboard = dashboard,
                    readings = readings,
                    healthLogs = healthLogs,
                    biaResult = biaResult,
                    liveHr = liveHr,
                    liveHrAt = liveHrAt,
                    liveHrv = liveHrv,
                )
                TrendOverviewCard(
                    cardData = cardData,
                    onClick = { selectedMetric = m },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    } else {
        // =================================================================
        // TREND DETAIL: Dedicated detail view for the selected trend.
        // Handled as its own destination screen with independent scroll state.
        // =================================================================
        BackHandler(enabled = true) {
            selectedMetric = null
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { selectedMetric = null },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back to all trends",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${currentMetric.label} Trend",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Historical data, charts & analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (currentMetric == TrendMetric.BODY_FAT) {
                    Button(onClick = { showBodyFatReader = true }) {
                        Icon(Icons.Filled.Accessibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Scan BIA")
                    }
                }
            }

            // Hero Card displaying current headline data for this metric
            val currentData = resolveTrendCardData(
                metric = currentMetric,
                dashboard = dashboard,
                readings = readings,
                healthLogs = healthLogs,
                biaResult = biaResult,
                liveHr = liveHr,
                liveHrAt = liveHrAt,
                liveHrv = liveHrv,
            )
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (currentData.hasData) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(trendColor(currentMetric).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            trendIcon(currentMetric),
                            contentDescription = null,
                            tint = trendColor(currentMetric),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Current ${currentMetric.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (currentData.hasData) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            currentData.valueText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentData.hasData) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            currentData.subText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentData.hasData) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Range switcher
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TrendRange.entries.forEachIndexed { index, r ->
                    SegmentedButton(
                        selected = range == r,
                        onClick = { range = r },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TrendRange.entries.size,
                        ),
                        label = { Text(r.label) },
                    )
                }
            }

            // Refresh bar
            val hasData = series.any { it.points.isNotEmpty() }
            val hcLoading = hcTrendLoading
            val updatedFmt = remember {
                DateTimeFormatter.ofPattern("h:mm:ss a").withZone(ZoneId.systemDefault())
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = when {
                        hcLoading -> "Updating ${currentMetric.label.lowercase()}…"
                        currentMetric.isHcTrend() || currentMetric == TrendMetric.BMI ->
                            if (lastUpdatedMs != null)
                                "Updated ${updatedFmt.format(Instant.ofEpochMilli(lastUpdatedMs!!))}"
                            else "Not updated yet"
                        currentMetric == TrendMetric.HEART_RATE || currentMetric == TrendMetric.STRESS ->
                            "Live from your watch"
                        else -> "Live from your BP checks"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (hcLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    OutlinedButton(onClick = {
                        lastUpdatedMs = System.currentTimeMillis()
                        refreshTick++
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refresh")
                    }
                }
            }

            // Chart or empty states
            when {
                currentMetric.isHcTrend() && !hcAvailable && !hcLoading -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "${currentMetric.label} history lives in Health Connect",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                "Connect Health Connect to pull in Samsung Health and watch measurements.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(onClick = {
                                onRequestHcPermissions(viewModel.hcPermissions)
                            }) {
                                Text("Connect Health Connect")
                            }
                        }
                    }
                }
                !hasData -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                when (currentMetric) {
                                    TrendMetric.HEART_RATE ->
                                        "No heart rate recordings in this range yet. Continuous recording tracks samples every 10 minutes."
                                    TrendMetric.STRESS ->
                                        "No stress data yet. Take a BP check on the watch to record an autonomic stress reading."
                                    TrendMetric.SLEEP ->
                                        "No sleep sessions recorded for this range."
                                    TrendMetric.BODY_FAT ->
                                        "No body fat scans yet. Tap \"Scan BIA\" above with both fingers touching the watch buttons."
                                    else -> "No data available for this range yet."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }
                        if (currentMetric == TrendMetric.SLEEP && onDiagnoseSleep != null) {
                            SleepDiagnosticsCard(onDiagnose = onDiagnoseSleep)
                        }
                    }
                }
                else -> {
                    TrendChart(
                        series = series,
                        xMin = start,
                        xMax = now,
                        range = range,
                    )
                    // Min / max / avg statistical summary
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        series.forEach { s ->
                            if (s.points.isNotEmpty()) {
                                val ys = s.points.map { it.y }
                                val avg = ys.average()
                                val min = ys.min()
                                val max = ys.max()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .width(24.dp)
                                            .height(4.dp)
                                    ) {
                                        drawLine(
                                            s.color,
                                            Offset(0f, size.height / 2),
                                            Offset(size.width, size.height / 2),
                                            strokeWidth = size.height,
                                            cap = StrokeCap.Round,
                                        )
                                    }
                                    Text(
                                        "${s.label}: avg ${fmt(avg)}${s.unit} · min ${fmt(min)} · max ${fmt(max)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Clinical Guidelines & Interpretation Card for the open metric
            MetricGuidelineCard(metric = currentMetric)

            // History Log of recorded data points
            TrendDataPointsLog(series = series)

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showBodyFatReader) {
        BodyFatReaderDialog(
            viewModel = viewModel,
            onDismiss = { showBodyFatReader = false },
        )
    }
}

/**
 * Clickable card for each trend in the overview list, displaying the metric's icon,
 * label, most current data point, and description.
 */
@Composable
private fun TrendOverviewCard(
    cardData: TrendCardData,
    onClick: () -> Unit,
) {
    val m = cardData.metric
    val accent = trendColor(m)
    val hasData = cardData.hasData

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trend_card_${m.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (hasData) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    trendIcon(m),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    m.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    cardData.valueText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (hasData) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    cardData.subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open ${m.label} trend",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun fmt(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString()
    else String.format(Locale.US, "%.1f", v)

private fun fmt(v: Float): String = fmt(v.toDouble())

/**
 * Hand-rolled line chart: axes, gridlines, line + fill. Downsamples to at
 * most 240 points per series so a year of 10-minute samples stays smooth.
 */
@Composable
private fun TrendChart(
    series: List<ChartSeries>,
    xMin: Long,
    xMax: Long,
    range: TrendRange,
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val drawn = remember(series) {
        series.map { s -> s.copy(points = downsample(s.points)) }
    }
    val allY = drawn.flatMap { s -> s.points.map { it.y } }
    val rawMin = allY.minOrNull() ?: 0f
    val rawMax = allY.maxOrNull() ?: 1f
    val pad = (rawMax - rawMin).takeIf { it > 0 }?.times(0.12f) ?: 1f
    val yMin = rawMin - pad
    val yMax = rawMax + pad

    val zone = remember { ZoneId.systemDefault() }
    val xFmt = remember(range) {
        when (range) {
            TrendRange.HOUR, TrendRange.DAY -> DateTimeFormatter.ofPattern("ha")
            TrendRange.WEEK -> DateTimeFormatter.ofPattern("EEE")
            TrendRange.MONTH -> DateTimeFormatter.ofPattern("d MMM")
            TrendRange.YEAR, TrendRange.ALL -> DateTimeFormatter.ofPattern("MMM yy")
        }.withZone(zone)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(8.dp)
        ) {
            val padL = 40.dp.toPx()
            val padB = 26.dp.toPx()
            val padT = 10.dp.toPx()
            val padR = 10.dp.toPx()
            val w = (size.width - padL - padR).coerceAtLeast(1f)
            val h = (size.height - padT - padB).coerceAtLeast(1f)

            fun xToPx(t: Long): Float =
                padL + w * ((t - xMin).toFloat() / (xMax - xMin).toFloat().coerceAtLeast(1f))
            fun yToPx(v: Float): Float =
                padT + h * (1f - (v - yMin) / (yMax - yMin).coerceAtLeast(0.001f))

            val paint = Paint().apply {
                color = labelColor.toArgb()
                textSize = 11.sp.toPx()
                isAntiAlias = true
            }

            // Horizontal gridlines + y labels.
            for (i in 0..4) {
                val y = padT + h * i / 4f
                val value = yMax - (yMax - yMin) * i / 4f
                drawLine(
                    gridColor,
                    Offset(padL, y),
                    Offset(padL + w, y),
                    strokeWidth = 1.dp.toPx(),
                )
                paint.textAlign = Paint.Align.RIGHT
                drawContext.canvas.nativeCanvas.drawText(
                    fmt(value),
                    padL - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint,
                )
            }
            // X ticks.
            paint.textAlign = Paint.Align.CENTER
            for (i in 0..4) {
                val t = xMin + (xMax - xMin) * i / 4
                drawContext.canvas.nativeCanvas.drawText(
                    xFmt.format(Instant.ofEpochMilli(t)),
                    xToPx(t).coerceIn(padL, padL + w),
                    padT + h + 18.dp.toPx(),
                    paint,
                )
            }

            // Series: line, plus a soft fill under a lone series.
            drawn.forEach { s ->
                if (s.points.size < 2) return@forEach
                val path = Path()
                s.points.forEachIndexed { pi, p ->
                    val x = xToPx(p.x)
                    val y = yToPx(p.y)
                    if (pi == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                if (drawn.size == 1) {
                    val fill = Path().apply {
                        addPath(path)
                        lineTo(xToPx(s.points.last().x), padT + h)
                        lineTo(xToPx(s.points.first().x), padT + h)
                        close()
                    }
                    drawPath(fill, s.color.copy(alpha = 0.16f))
                }
                drawPath(
                    path,
                    s.color,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                )
                // Dots only when sparse enough to read.
                if (s.points.size <= 60) {
                    s.points.forEach { p ->
                        drawCircle(s.color, radius = 3.dp.toPx(), center = Offset(xToPx(p.x), yToPx(p.y)))
                    }
                }
            }
        }

        // Legend.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            drawn.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .width(20.dp)
                            .height(4.dp)
                    ) {
                        drawLine(
                            s.color,
                            Offset(0f, size.height / 2),
                            Offset(size.width, size.height / 2),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round,
                        )
                    }
                    Text(s.label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/** Wraps Health Connect trend points in a ChartSeries. */
private fun hcSeries(
    label: String,
    color: Color,
    unit: String,
    points: List<HcTrendPoint>,
): List<ChartSeries> = listOf(
    ChartSeries(
        label = label,
        color = color,
        unit = unit,
        points = points.map { ChartPoint(it.timestamp, it.value) },
    )
)

/** Averages points into hour buckets anchored at [start] (Hour range). */
private fun bucketHourly(points: List<ChartPoint>, start: Long): List<ChartPoint> {
    val hourMs = 3600_000L
    return points.groupBy { ((it.x - start).coerceAtLeast(0) / hourMs) }
        .map { (idx, ps) ->
            ChartPoint(
                x = start + idx * hourMs,
                y = ps.map { it.y }.average().toFloat(),
            )
        }
        .sortedBy { it.x }
}

/** Bucket-average downsampling so dense ranges stay smooth. */
private fun downsample(points: List<ChartPoint>, max: Int = 240): List<ChartPoint> {
    if (points.size <= max) return points
    val bucket = points.size / max.toFloat()
    return (0 until max).map { i ->
        val from = (i * bucket).toInt()
        val to = ((i + 1) * bucket).toInt().coerceAtMost(points.size)
        val slice = points.subList(from, to)
        ChartPoint(
            x = slice.map { it.x }.average().toLong(),
            y = slice.map { it.y }.average().toFloat(),
        )
    }
}

/**
 * Guideline and clinical reference card for the opened trend.
 */
@Composable
private fun MetricGuidelineCard(metric: TrendMetric) {
    val (title, description, ranges) = when (metric) {
        TrendMetric.BLOOD_PRESSURE -> Triple(
            "Blood Pressure Guidelines (AHA/ACC)",
            "Blood pressure is recorded as systolic over diastolic pressure (mmHg). Track consistently to monitor cardiovascular health.",
            listOf(
                "Normal: Systolic < 120 and Diastolic < 80 mmHg",
                "Elevated: Systolic 120-129 and Diastolic < 80 mmHg",
                "Hypertension Stage 1: Systolic 130-139 or Diastolic 80-89 mmHg",
                "Hypertension Stage 2: Systolic ≥ 140 or Diastolic ≥ 90 mmHg"
            )
        )
        TrendMetric.HEART_RATE -> Triple(
            "Heart Rate Information",
            "Heart rate measures beats per minute (bpm). It fluctuates naturally with activity, posture, stress, and autonomic state.",
            listOf(
                "Normal Resting (Adults): 60 – 100 bpm",
                "Athletic Baseline: 40 – 60 bpm",
                "Tachycardia (Resting): > 100 bpm",
                "Bradycardia (Resting non-athletic): < 60 bpm"
            )
        )
        TrendMetric.RESTING_HR -> Triple(
            "Resting Heart Rate (RHR)",
            "Measured during quiet rest or sleep. RHR is a key indicator of cardiovascular fitness and autonomic recovery.",
            listOf(
                "Excellent: < 60 bpm",
                "Good: 60 – 69 bpm",
                "Average: 70 – 79 bpm",
                "Higher: ≥ 80 bpm"
            )
        )
        TrendMetric.HRV -> Triple(
            "Heart Rate Variability (RMSSD)",
            "RMSSD measures variation between consecutive heartbeats in milliseconds. Higher values generally indicate resilient parasympathetic tone and recovery.",
            listOf(
                "Higher RMSSD: Indicates healthy autonomic balance and recovery",
                "Lower RMSSD: May reflect physical fatigue, illness, or acute stress",
                "Personal Baseline: HRV is highly individual; compare against your own average"
            )
        )
        TrendMetric.STRESS -> Triple(
            "Autonomic Stress Index",
            "Evaluates autonomic nervous system balance derived from pulse wave analysis and pulse rate dynamics during checkups.",
            listOf(
                "Relaxed: 0 – 29 (Parasympathetic dominance)",
                "Moderate: 30 – 59 (Balanced physiological activity)",
                "Elevated: 60 – 79 (Sympathetic activation / exertion)",
                "High: 80 – 100 (High physiological or mental tension)"
            )
        )
        TrendMetric.SLEEP -> Triple(
            "Sleep Duration & Quality",
            "Monitors total sleep time, stages, and restfulness synced from Health Connect and Samsung Health sleep tracking.",
            listOf(
                "Optimal (Adults): 7 – 9 hours per night",
                "Short Sleep: < 6 hours (associated with elevated BP risk)",
                "Consistency: Regular sleep/wake schedules support healthy circadian rhythm"
            )
        )
        TrendMetric.BODY_FAT -> Triple(
            "Body Composition (BIA)",
            "Bioelectrical Impedance Analysis measured using the watch dual side-button electrodes with both fingers touching.",
            listOf(
                "Athletic: Men 6-13% · Women 14-20%",
                "Fitness: Men 14-17% · Women 21-24%",
                "Acceptable: Men 18-24% · Women 25-31%",
                "Measurement tip: Keep fingers on electrodes for the full 15s scan"
            )
        )
        TrendMetric.WEIGHT -> Triple(
            "Weight Tracking",
            "Body weight trend over time. Day-to-day fluctuations reflect hydration, glycogen storage, and sodium balance.",
            listOf(
                "Tracking: Log consistently at the same time (e.g. morning after waking)",
                "Trends: Focus on the weekly moving average rather than single-day spikes"
            )
        )
        TrendMetric.BMI -> Triple(
            "Body Mass Index (BMI)",
            "Screening metric calculated from height and weight (kg/m²).",
            listOf(
                "Underweight: < 18.5",
                "Normal weight: 18.5 – 24.9",
                "Overweight: 25.0 – 29.9",
                "Obese: ≥ 30.0"
            )
        )
        TrendMetric.STEPS -> Triple(
            "Daily Step Activity",
            "Step count recorded by the watch accelerometer and pedometer sensor throughout the day.",
            listOf(
                "Active Target: 8,000 – 10,000 steps/day",
                "Moderate: 5,000 – 7,999 steps/day",
                "Sedentary: < 5,000 steps/day"
            )
        )
        TrendMetric.DISTANCE -> Triple(
            "Distance Traveled",
            "Calculated from pedometer cadence and stride length metrics across active movement.",
            listOf(
                "Daily Target: 3.0 – 5.0 miles (5 – 8 km)",
                "Encourages steady aerobic conditioning"
            )
        )
        TrendMetric.CALORIES -> Triple(
            "Active Calories (kcal)",
            "Active energy expenditure estimated from heart rate, movement intensity, and user body profile.",
            listOf(
                "Active Burn: Represents energy burned through exercise and physical movement",
                "Total Burn: Includes Basal Metabolic Rate (BMR) plus active burn"
            )
        )
        TrendMetric.HYDRATION -> Triple(
            "Fluid Consumption",
            "Daily water and fluid intake logged or synced with Health Connect.",
            listOf(
                "General Guideline: 2.0 – 3.0 Liters daily (68 – 100 fl oz)",
                "Adjust for exercise duration, ambient heat, and sweating"
            )
        )
        TrendMetric.SKIN_TEMP -> Triple(
            "Wrist Optical Skin Temperature",
            "Monitors peripheral skin temperature relative to baseline. Valuable for tracking circadian rhythm, sleep environments, and recovery.",
            listOf(
                "Nighttime Shift: Skin temperature typically increases slightly during deep sleep",
                "Environmental: Subject to ambient temperature and watch band tightness"
            )
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ranges.forEach { r ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text(r, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

/**
 * Log card showing recorded individual data points for the open trend.
 */
@Composable
private fun TrendDataPointsLog(series: List<ChartSeries>) {
    val allPoints = remember(series) {
        series.flatMap { s -> s.points.map { pt -> Triple(pt.x, pt.y, s.unit) } }
            .sortedByDescending { it.first }
            .take(15)
    }
    if (allPoints.isEmpty()) return

    val formatter = remember {
        DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.US).withZone(ZoneId.systemDefault())
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Recorded History (${allPoints.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            allPoints.forEach { (ts, value, unit) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        formatter.format(Instant.ofEpochMilli(ts)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${if (value % 1 == 0f) value.toInt().toString() else "%.1f".format(Locale.US, value)} $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}
