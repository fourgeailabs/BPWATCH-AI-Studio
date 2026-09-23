package com.fourgeailabs.bpwatch.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fourgeailabs.bpwatch.mobile.MainViewModel
import com.fourgeailabs.bpwatch.mobile.prefs.HomeLayoutManager

/**
 * Settings screen allowing users to customize which cards appear on the Home screen
 * and the exact order they are displayed in.
 */
@Composable
fun HomeLayoutSettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val cardOrder by HomeLayoutManager.cardOrder.collectAsState()
    val cardVisibility by HomeLayoutManager.cardVisibility.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("home_layout_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Explanatory card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    Icons.Filled.DashboardCustomize,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Home Layout & Order",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Choose which cards are visible on your Home screen and arrange them in your preferred vertical order. Changes apply immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Text(
            "Cards Order & Visibility",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )

        cardOrder.forEachIndexed { index, cardId ->
            val info = HomeLayoutManager.ALL_CARDS.firstOrNull { it.id == cardId }
            if (info != null) {
                val isVisible = cardVisibility[cardId] ?: true
                val isFirst = index == 0
                val isLast = index == cardOrder.size - 1

                val cardIcon = when (cardId) {
                    HomeLayoutManager.CARD_BP_ESTIMATE -> Icons.Filled.Favorite
                    HomeLayoutManager.CARD_QUICK_ACTIONS -> Icons.Filled.TouchApp
                    HomeLayoutManager.CARD_HC_BANNER -> Icons.Filled.Info
                    HomeLayoutManager.CARD_METRICS_GRID -> Icons.Filled.GridOn
                    HomeLayoutManager.CARD_SNORE_SLEEP -> Icons.Filled.Bedtime
                    else -> Icons.Filled.Warning
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("layout_card_$cardId"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isVisible) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isVisible) MaterialTheme.colorScheme.outlineVariant
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Position indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            IconButton(
                                onClick = { HomeLayoutManager.moveCard(context, cardId, moveUp = true) },
                                enabled = !isFirst,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Move Up",
                                    tint = if (!isFirst) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                )
                            }
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            IconButton(
                                onClick = { HomeLayoutManager.moveCard(context, cardId, moveUp = false) },
                                enabled = !isLast,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Move Down",
                                    tint = if (!isLast) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                )
                            }
                        }

                        // Icon and details
                        Icon(
                            cardIcon,
                            contentDescription = null,
                            tint = if (isVisible) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp),
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                info.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isVisible) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                info.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = if (isVisible) 1f else 0.5f
                                ),
                            )
                        }

                        // Visibility toggle
                        Switch(
                            checked = isVisible,
                            onCheckedChange = { visible ->
                                HomeLayoutManager.setCardVisibility(context, cardId, visible)
                            },
                            modifier = Modifier.testTag("switch_visible_$cardId"),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Reset to default button
        OutlinedButton(
            onClick = { HomeLayoutManager.resetToDefault(context) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_layout_button"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Reset to default order")
        }

        Spacer(Modifier.height(16.dp))
    }
}
