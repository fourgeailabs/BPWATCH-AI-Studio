package com.fourgeailabs.bpwatch.mobile.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeCardInfo(
    val id: String,
    val title: String,
    val description: String,
    val defaultVisible: Boolean = true,
)

/**
 * Manages Home Screen card ordering and visibility customization.
 * Allows users to choose which cards are displayed and reorder them via "Home layout" in Settings.
 */
object HomeLayoutManager {
    const val CARD_BP_ESTIMATE = "bp_estimate"
    const val CARD_QUICK_ACTIONS = "quick_actions"
    const val CARD_HC_BANNER = "hc_banner"
    const val CARD_METRICS_GRID = "metrics_grid"
    const val CARD_SNORE_SLEEP = "snore_sleep"
    const val CARD_EKG = "ekg_card"
    const val CARD_ACTIVE_SENSORS = "active_sensors"
    const val CARD_DISCLAIMER = "disclaimer"

    val ALL_CARDS = listOf(
        HomeCardInfo(CARD_BP_ESTIMATE, "Blood Pressure Estimate", "Live estimate hero card with clinical posture context and check trigger"),
        HomeCardInfo(CARD_QUICK_ACTIONS, "Quick Actions", "Direct shortcuts for manual data entry (+ Log) and Sleep overview"),
        HomeCardInfo(CARD_HC_BANNER, "Health Connect Banner", "Status banner prompting connection when Health Connect permissions are missing"),
        HomeCardInfo(CARD_METRICS_GRID, "Health Metrics Tiles", "13-tile health grid including Steps, HR, Weight, Body Fat, Sleep, HRV, and Skin Temp"),
        HomeCardInfo(CARD_SNORE_SLEEP, "Snoring & Sleep Summary", "Overnight sleep duration and snore monitoring with 7-night interactive trend"),
        HomeCardInfo(CARD_EKG, "Electrocardiogram (EKG)", "Real-time EKG waveform and rhythm classification via SHM-MOD reflection ECG sensor"),
        HomeCardInfo(CARD_ACTIVE_SENSORS, "Active Sensors Stream Status", "Visual status indicator and live D3-style waveform stream for EKG, BP, Body Fat, and Skin Temp"),
        HomeCardInfo(CARD_DISCLAIMER, "Medical Disclaimer", "Clinical guidance on blood pressure estimation and cuff calibration"),
    )

    private const val PREFS_NAME = "bpwatch_home_layout"
    private const val KEY_CARD_ORDER = "card_order"
    private const val KEY_CARD_VISIBLE_PREFIX = "card_visible_"

    private val _cardOrder = MutableStateFlow<List<String>>(ALL_CARDS.map { it.id })
    val cardOrder: StateFlow<List<String>> = _cardOrder.asStateFlow()

    private val _cardVisibility = MutableStateFlow<Map<String, Boolean>>(ALL_CARDS.associate { it.id to true })
    val cardVisibility: StateFlow<Map<String, Boolean>> = _cardVisibility.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedOrderStr = prefs.getString(KEY_CARD_ORDER, null)
        val order = if (!savedOrderStr.isNullOrBlank()) {
            val list = savedOrderStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val existing = list.filter { id -> ALL_CARDS.any { it.id == id } }
            val missing = ALL_CARDS.map { it.id }.filter { it !in existing }
            existing + missing
        } else {
            ALL_CARDS.map { it.id }
        }
        _cardOrder.value = order

        val visibility = ALL_CARDS.associate { card ->
            card.id to prefs.getBoolean(KEY_CARD_VISIBLE_PREFIX + card.id, card.defaultVisible)
        }
        _cardVisibility.value = visibility
    }

    fun setCardVisibility(context: Context, cardId: String, visible: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CARD_VISIBLE_PREFIX + cardId, visible).apply()
        _cardVisibility.value = _cardVisibility.value + (cardId to visible)
    }

    fun moveCard(context: Context, cardId: String, moveUp: Boolean) {
        val current = _cardOrder.value.toMutableList()
        val index = current.indexOf(cardId)
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex in 0 until current.size) {
            current.removeAt(index)
            current.add(targetIndex, cardId)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_CARD_ORDER, current.joinToString(",")).apply()
            _cardOrder.value = current
        }
    }

    operator fun invoke(context: Context): HomeLayoutManager {
        init(context)
        return this
    }

    fun resetToDefault(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _cardOrder.value = ALL_CARDS.map { it.id }
        _cardVisibility.value = ALL_CARDS.associate { it.id to true }
    }
}

object HomeCardId {
    const val HERO_BP = HomeLayoutManager.CARD_BP_ESTIMATE
    const val PRIMARY_ACTIONS = HomeLayoutManager.CARD_QUICK_ACTIONS
    const val HC_STATUS = HomeLayoutManager.CARD_HC_BANNER
    const val METRIC_GRID = HomeLayoutManager.CARD_METRICS_GRID
    const val SNORE_CARD = HomeLayoutManager.CARD_SNORE_SLEEP
    const val EKG_CARD = HomeLayoutManager.CARD_EKG
    const val ACTIVE_SENSORS = HomeLayoutManager.CARD_ACTIVE_SENSORS
    const val DISCLAIMER = HomeLayoutManager.CARD_DISCLAIMER
}
