package com.bloomodoro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloomodoro.calendar.CalendarSync
import com.bloomodoro.domain.PomodoroEngine
import com.bloomodoro.domain.TimerSnapshot
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.AppMode
import com.bloomodoro.model.AppearanceConfig
import com.bloomodoro.model.CompanionStyle
import com.bloomodoro.model.Phase
import androidx.compose.foundation.shape.CircleShape as CircleShapeAlias
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

@Composable
fun TimerScreen(
    engine: PomodoroEngine,
    appearance: AppearanceConfig,
    calendarSync: CalendarSync,
    onOpenGarden: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val strings = LocalStrings.current
    var snapshot by remember { mutableStateOf(engine.snapshot()) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showDurationPickerFor by remember { mutableStateOf<AppMode?>(null) }

    fun refresh() { snapshot = engine.snapshot() }

    LaunchedEffect(Unit) {
        while (true) {
            engine.reconcile()
            refresh()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimerHeader(onOpenGarden = onOpenGarden, onOpenHistory = onOpenHistory)
        Spacer(Modifier.height(4.dp))
        ModeSwitcher(
            current = snapshot.mode,
            enabled = !snapshot.isRunning,
            onSelect = { mode ->
                // Switch immediately and re-read the snapshot right away, instead of waiting
                // for the next 1s tick — this is what made mode switching feel unresponsive.
                engine.switchMode(mode)
                refresh()
                showDurationPickerFor = mode
            },
        )
        Spacer(Modifier.height(16.dp))
        PhaseLabel(snapshot.phase)
        DailyTargetRow(snapshot, onEditTarget = { showTargetDialog = true })

        Spacer(Modifier.weight(1f))

        TimerRing(snapshot = snapshot)

        AnimatedVisibility(visible = snapshot.isPaused, enter = fadeIn(), exit = fadeOut()) {
            CutePauseBadge()
        }

        Spacer(Modifier.height(16.dp))

        if (appearance.companionStyle != CompanionStyle.NONE) {
            BloomCompanion(stage = snapshot.completedToday, style = appearance.companionStyle)
        }

        Spacer(Modifier.weight(1f))

        ControlsRow(
            snapshot = snapshot,
            onStart = { engine.start(); refresh() },
            onPause = { engine.pause(); refresh() },
            onResume = { engine.resume(); refresh() },
            onSkip = { engine.skip(); refresh() },
            onStopEarly = { engine.stopEarly(); refresh() },
        )

        Spacer(Modifier.height(8.dp))

        if (snapshot.isRunning) {
            TextButton(onClick = {
                val now = Clock.System.now().toEpochMilliseconds()
                calendarSync.addEvent(
                    title = strings.phaseTitleFor(snapshot.phase),
                    description = "از بلومودورو",
                    startEpochMs = now,
                    endEpochMs = now + snapshot.remainingMs,
                )
            }) {
                Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.addToCalendarButton)
            }
        }
    }

    if (showTargetDialog) {
        DailyTargetDialog(
            initialMinutes = snapshot.dailyTargetMinutes,
            onDismiss = { showTargetDialog = false },
            onConfirm = { minutes -> engine.setDailyTargetMinutes(minutes); showTargetDialog = false },
        )
    }

    showDurationPickerFor?.let { mode ->
        val currentMinutes by engine.configFor(mode).collectAsState()
        FocusDurationDialog(
            currentMinutes = currentMinutes.workMinutes,
            onDismiss = { showDurationPickerFor = null },
            onPick = { minutes ->
                engine.updateConfigFor(mode) { it.copy(workMinutes = minutes) }
                showDurationPickerFor = null
            },
        )
    }
}

/** Small header row above the mode switcher: a garden "logo" (tap to open the garden) and a
 * history icon — this replaces the bottom-nav Garden/History tabs entirely. */
@Composable
private fun TimerHeader(onOpenGarden: () -> Unit, onOpenHistory: () -> Unit) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShapeAlias)
                .clickable(onClick = onOpenGarden)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShapeAlias)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🌸", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(8.dp))
            Text("Bloomodoro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = onOpenHistory) {
            Icon(Icons.Filled.History, contentDescription = strings.historyIconHint)
        }
    }
}

@Composable
private fun FocusDurationDialog(currentMinutes: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val strings = LocalStrings.current
    val presets = listOf(15, 25, 45, 60, 90)
    var customText by remember { mutableStateOf(currentMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.pickDurationTitle) },
        text = {
            Column {
                presets.forEach { minutes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPick(minutes) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = minutes == currentMinutes, onClick = { onPick(minutes) })
                        Spacer(Modifier.width(8.dp))
                        Text("$minutes${strings.minutesUnit}")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text(strings.customDurationLabel, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it.filter(Char::isDigit).take(3) },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        suffix = { Text(strings.minutesUnit) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val minutes = customText.toIntOrNull()?.coerceIn(1, 300)
                if (minutes != null) onPick(minutes)
            }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}

@Composable
private fun ModeSwitcher(current: AppMode, enabled: Boolean, onSelect: (AppMode) -> Unit) {
    val strings = LocalStrings.current
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            selected = current == AppMode.STUDY,
            onClick = { if (enabled) onSelect(AppMode.STUDY) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            enabled = enabled,
        ) { Text(strings.modeStudy) }
        SegmentedButton(
            selected = current == AppMode.WORK,
            onClick = { if (enabled) onSelect(AppMode.WORK) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            enabled = enabled,
        ) { Text(strings.modeWork) }
    }
}

@Composable
private fun DailyTargetRow(snapshot: TimerSnapshot, onEditTarget: () -> Unit) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (snapshot.dailyTargetMinutes > 0) {
            val progress = (snapshot.focusedMinutesToday.toFloat() / snapshot.dailyTargetMinutes.toFloat()).coerceIn(0f, 1f)
            Text(
                "${strings.dailyTargetLabel}: ${snapshot.focusedMinutesToday}/${snapshot.dailyTargetMinutes} دقیقه",
                style = MaterialTheme.typography.bodySmall,
            )
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.width(80.dp))
        }
        TextButton(onClick = onEditTarget) { Text(strings.dailyTargetSet) }
    }
}

@Composable
private fun DailyTargetDialog(initialMinutes: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val strings = LocalStrings.current
    var minutes by remember { mutableStateOf(initialMinutes.toFloat().coerceAtLeast(0f)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.setTodayTargetTitle) },
        text = {
            Column {
                Text("${minutes.toInt()} دقیقه (${(minutes / 60)} ساعت)")
                Slider(value = minutes, valueRange = 0f..600f, onValueChange = { minutes = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(minutes.toInt()) }) { Text(strings.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.delete) } },
    )
}

@Composable
private fun PhaseLabel(phase: Phase) {
    val strings = LocalStrings.current
    val text = if (phase == Phase.IDLE) strings.readyQuestion else strings.phaseTitleFor(phase)
    val color = when (phase) {
        Phase.WORK -> MaterialTheme.colorScheme.primary
        Phase.SHORT_BREAK, Phase.LONG_BREAK -> MaterialTheme.colorScheme.secondary
        Phase.IDLE -> MaterialTheme.colorScheme.onBackground
    }
    Text(text, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun TimerRing(snapshot: TimerSnapshot) {
    val animatedProgress by animateFloatAsState(targetValue = snapshot.progress)
    val totalSeconds = (snapshot.remainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val ringColor = if (snapshot.isPaused) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            drawArc(
                color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(size.width - stroke, size.height - stroke),
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
            )
            drawArc(
                color = ringColor, startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(size.width - stroke, size.height - stroke),
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
            )
        }
        Text(
            text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
            style = TimerDigitsStyle,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/** A cute little "روی پازه" chip that pops in whenever the timer is paused — no scary red icon. */
@Composable
private fun CutePauseBadge() {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text("😴 یه لحظه استراحت... هر وقت آماده بودی برگرد", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ControlsRow(
    snapshot: TimerSnapshot,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStopEarly: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (snapshot.isRunning) {
            FilledTonalIconButton(onClick = onStopEarly, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Filled.Stop, contentDescription = "Stop")
            }
        }
        val mainAction = when {
            !snapshot.isRunning -> onStart
            snapshot.isPaused -> onResume
            else -> onPause
        }
        val mainIcon = if (snapshot.isRunning && !snapshot.isPaused) Icons.Filled.Pause else Icons.Filled.PlayArrow
        FilledIconButton(
            onClick = mainAction,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(mainIcon, contentDescription = "Start/Pause", modifier = Modifier.size(28.dp))
        }
        if (snapshot.isRunning) {
            FilledTonalIconButton(onClick = onSkip, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Skip")
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

/** A tiny companion that visibly grows with each completed session today — the app's one signature delight. */
@Composable
private fun BloomCompanion(stage: Int, style: CompanionStyle) {
    val emoji = when (style) {
        CompanionStyle.BLOOM -> when {
            stage <= 0 -> "🌱"
            stage in 1..2 -> "🌿"
            stage in 3..4 -> "🌷"
            else -> "🌸"
        }
        CompanionStyle.CAT -> when {
            stage <= 0 -> "🐣"
            stage in 1..2 -> "🐱"
            else -> "😻"
        }
        CompanionStyle.BLOB -> when {
            stage <= 0 -> "🫧"
            stage in 1..2 -> "🟢"
            else -> "💚"
        }
        CompanionStyle.NONE -> ""
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShapeAlias)
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, style = MaterialTheme.typography.displayMedium)
    }
}
