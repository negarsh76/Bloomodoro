package com.bloomodoro.ui

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.bloomodoro.data.LocalStore
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.AppMode
import com.bloomodoro.model.GardenEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.sin
import androidx.compose.animation.core.animateFloat
/**
 * The "forest": one little plant per day you focused, for the currently selected mode.
 * The bigger the day's total, the bigger the flower. Each tile grows in with a soft pop
 * animation and gently sways forever after — a living garden rather than static stickers.
 * The sway's phase/speed is derived from each entry's date, so neighboring plants never
 * move in perfect unison; it reads as wind, not a loop.
 *
 * Opened as a full-screen overlay from the little flower logo on the Timer screen, so it
 * carries its own back button instead of living in the bottom navigation bar.
 */
@Composable
fun GardenScreen(localStore: LocalStore, mode: AppMode, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val entries = remember(mode) { MutableStateFlow(localStore.loadGarden()) }
    val allEntries by entries.collectAsState()
    val forMode = allEntries.filter { it.mode == mode }.sortedByDescending { it.epochDay }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backButton)
            }
            Spacer(Modifier.width(4.dp))
            Text(strings.gardenTitle, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            strings.gardenSubtitle(forMode.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(16.dp))

        if (forMode.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.gardenEmpty, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 72.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(forMode, key = { it.epochDay }) { entry -> FlowerTile(entry) }
            }
        }
    }
}

@Composable
private fun FlowerTile(entry: GardenEntry) {
    val emoji = when {
        entry.focusedMinutes >= 240 -> "🌳"
        entry.focusedMinutes >= 120 -> "🌸"
        entry.focusedMinutes >= 60 -> "🌷"
        entry.focusedMinutes >= 25 -> "🌿"
        else -> "🌱"
    }

    // Grow-in pop, once, when the tile first appears.
    val scale = remember { androidx.compose.animation.core.Animatable(0.4f) }
    LaunchedEffect(entry.epochDay) {
        scale.animateTo(1f, animationSpec = tween(durationMillis = 550, easing = EaseOutBack))
    }

    // Continuous, gentle, non-synchronized sway — phase and period both derived from the date
    // so every plant in the garden moves a little differently, like real leaves in a breeze.
    val phase = remember(entry.epochDay) { (entry.epochDay % 628).toFloat() / 100f }
    val periodMs = remember(entry.epochDay) { 2600 + (entry.epochDay % 900).toInt() }
    val infinite = rememberInfiniteTransition(label = "sway")
    val swayT by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing), RepeatMode.Restart),
        label = "swayT",
    )
    val angle = sin(swayT + phase) * 4f // degrees, small and organic
    val bob = sin(swayT * 1.3f + phase) * 1.5f // px-ish vertical drift via dp below

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            emoji,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationY = bob.dp.toPx()
                }
                .rotate(angle),
        )
        Spacer(Modifier.height(4.dp))
        Text("${entry.focusedMinutes}′", style = MaterialTheme.typography.labelSmall)
    }
}
