package com.bloomodoro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloomodoro.domain.PlanningManager
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.AppMode
import com.bloomodoro.model.GoalJourney
import com.bloomodoro.model.JourneyStep
import kotlinx.datetime.Clock
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.*
@Composable
fun GoalJourneyTab(planning: PlanningManager, mode: AppMode) {
    val strings = LocalStrings.current
    val journeys by planning.journeys.collectAsState()
    val steps by planning.journeySteps.collectAsState()
    val forMode = journeys.filter { it.mode == mode }
    var showAdd by remember { mutableStateOf(false) }
    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        Text(strings.journeyTitle, style = MaterialTheme.typography.titleMedium)
        Text(strings.journeyHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(10.dp))
        Button(onClick = { showAdd = true }) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.addJourneyButton)
        }
        Spacer(Modifier.height(14.dp))

        if (forMode.isEmpty()) {
            Text(strings.journeyEmpty, modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(forMode, key = { it.id }) { journey ->
                    JourneyCard(
                        journey = journey,
                        steps = steps.filter { it.journeyId == journey.id }.sortedBy { it.order },
                        isExpanded = expandedId == journey.id,
                        onToggleExpand = { expandedId = if (expandedId == journey.id) null else journey.id },
                        onAddStep = { title -> planning.addJourneyStep(journey.id, title) },
                        onToggleStep = { planning.toggleJourneyStep(it) },
                        onDeleteStep = { planning.deleteJourneyStep(it) },
                        onDeleteJourney = { planning.deleteJourney(journey.id) },
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddJourneyDialog(
            onDismiss = { showAdd = false },
            onConfirm = { title, icon ->
                planning.addJourney(
                    GoalJourney(
                        id = "jrn_${Clock.System.now().toEpochMilliseconds()}",
                        title = title,
                        mode = mode,
                        icon = icon,
                        createdAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
                showAdd = false
            },
        )
    }
}

@Composable
private fun JourneyCard(
    journey: GoalJourney,
    steps: List<JourneyStep>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddStep: (String) -> Unit,
    onToggleStep: (String) -> Unit,
    onDeleteStep: (String) -> Unit,
    onDeleteJourney: () -> Unit,
) {
    val strings = LocalStrings.current
    val doneCount = steps.count { it.isDone }
    val progress = if (steps.isEmpty()) 0f else doneCount.toFloat() / steps.size
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(600))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).clickable(onClick = onToggleExpand),
                ) {
                    Text(journey.icon, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(journey.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (steps.isEmpty()) "" else strings.journeyProgress(doneCount, steps.size),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                IconButton(onClick = onDeleteJourney) { Icon(Icons.Filled.Delete, contentDescription = null) }
            }

            if (steps.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                )
                if (doneCount == steps.size) {
                    Spacer(Modifier.height(6.dp))
                    CelebrationBanner(strings.journeyAllDone)
                }
            }

            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    JourneyPath(steps = steps, onToggleStep = onToggleStep, onDeleteStep = onDeleteStep)
                    Spacer(Modifier.height(10.dp))
                    AddStepRow(onAdd = onAddStep)
                }
            }
        }
    }
}

@Composable
private fun CelebrationBanner(text: String) {
    val infinite = rememberInfiniteTransition(label = "celebrate")
    val scale by infinite.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseOutBack), RepeatMode.Reverse),
        label = "celebrateScale",
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * The animated path itself: a vertical line of step nodes, gently zigzagging left/right for an
 * organic "trail" feel. The connecting line segments already completed are solid, the ones
 * ahead are dashed; the first not-yet-done step pulses so it always reads as "you are here".
 */
@Composable
private fun JourneyPath(steps: List<JourneyStep>, onToggleStep: (String) -> Unit, onDeleteStep: (String) -> Unit) {
    val activeIndex = steps.indexOfFirst { !it.isDone }.let { if (it == -1) steps.size else it }
    val doneColor = MaterialTheme.colorScheme.primary
    val pendingColor = MaterialTheme.colorScheme.outlineVariant

    Column {
        steps.forEachIndexed { index, step ->
            val isDone = step.isDone
            val isActive = index == activeIndex
            val indent = if (index % 2 == 0) 0.dp else 18.dp

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = indent),
            ) {
                StepNode(isDone = isDone, isActive = isActive, onClick = { onToggleStep(step.id) })
                Spacer(Modifier.width(12.dp))
                Text(
                    step.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onDeleteStep(step.id) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            if (index < steps.size - 1) {
                Box(modifier = Modifier.height(22.dp).padding(start = indent + 14.dp)) {
                    Canvas(modifier = Modifier.width(4.dp).fillMaxHeight()) {
                        val color = if (index < activeIndex) doneColor else pendingColor
                        drawLine(
                            color = color,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = size.width,
                            pathEffect = if (index >= activeIndex) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepNode(isDone: Boolean, isActive: Boolean, onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "stepPulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(1100, easing = EaseOutBack), RepeatMode.Reverse),
        label = "stepPulseValue",
    )
    val activeScale = if (isActive && !isDone) pulse else 1f
    val baseColor = when {
        isDone -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .graphicsLayer { scaleX = activeScale; scaleY = activeScale }
            .clip(CircleShape)
            .background(baseColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isDone) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AddStepRow(onAdd: (String) -> Unit) {
    val strings = LocalStrings.current
    var text by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(strings.stepNamePlaceholder) },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        IconButton(onClick = {
            if (text.isNotBlank()) {
                onAdd(text.trim())
                text = ""
            }
        }) { Icon(Icons.Filled.Add, contentDescription = strings.addStepButton) }
    }
}

@Composable
private fun AddJourneyDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf("") }
    val icons = listOf("🎯", "🎓", "💼", "✈️", "🏆", "📚", "💪", "🚀")
    var icon by remember { mutableStateOf(icons.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addJourneyTitle) },
        text = {
            Column {
                Row {
                    icons.forEach { i ->
                        FilterChip(selected = icon == i, onClick = { icon = i }, label = { Text(i) })
                        Spacer(Modifier.width(4.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text(strings.journeyNamePlaceholder) })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onConfirm(title.trim(), icon) }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}
