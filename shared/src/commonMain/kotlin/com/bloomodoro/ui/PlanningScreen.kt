package com.bloomodoro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloomodoro.domain.JalaliCalendar
import com.bloomodoro.domain.PlanningManager
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private enum class PlanTab { TODO, SCHEDULE, DEADLINES, GOALS, JOURNEY }

@Composable
fun PlanningScreen(planning: PlanningManager, mode: AppMode, appearance: AppearanceConfig) {
    val strings = LocalStrings.current
    var tab by remember { mutableStateOf(PlanTab.TODO) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScrollableTabRow(selectedTabIndex = tab.ordinal, edgePadding = 0.dp) {
            Tab(selected = tab == PlanTab.TODO, onClick = { tab = PlanTab.TODO }, text = { Text(strings.planningTodoTab) })
            Tab(selected = tab == PlanTab.SCHEDULE, onClick = { tab = PlanTab.SCHEDULE }, text = { Text(strings.planningScheduleTab) })
            Tab(selected = tab == PlanTab.DEADLINES, onClick = { tab = PlanTab.DEADLINES }, text = { Text(strings.planningDeadlinesTab) })
            Tab(selected = tab == PlanTab.GOALS, onClick = { tab = PlanTab.GOALS }, text = { Text(strings.planningGoalsTab) })
            Tab(selected = tab == PlanTab.JOURNEY, onClick = { tab = PlanTab.JOURNEY }, text = { Text(strings.planningJourneyTab) })
        }
        Spacer(Modifier.height(12.dp))
        when (tab) {
            PlanTab.TODO -> TodoTab(planning, mode)
            PlanTab.SCHEDULE -> ScheduleTab(planning, mode)
            PlanTab.DEADLINES -> DeadlinesTab(planning, mode, appearance)
            PlanTab.GOALS -> GoalsPyramidTab(planning, mode)
            PlanTab.JOURNEY -> GoalJourneyTab(planning, mode)
        }
    }
}

// ---------------------------------------------------------------------------
// Todo list
// ---------------------------------------------------------------------------

@Composable
private fun TodoTab(planning: PlanningManager, mode: AppMode) {
    val strings = LocalStrings.current
    val todos by planning.todos.collectAsState()
    val forMode = todos.filter { it.mode == mode }
    var newTitle by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newTitle, onValueChange = { newTitle = it },
                placeholder = { Text(strings.todoAddPlaceholder) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(onClick = {
                if (newTitle.isNotBlank()) {
                    planning.addTodo(newTitle.trim(), mode, null, Clock.System.now().toEpochMilliseconds())
                    newTitle = ""
                }
            }) { Icon(Icons.Filled.Add, contentDescription = null) }
        }
        Spacer(Modifier.height(8.dp))
        if (forMode.isEmpty()) {
            Text(strings.todoEmpty, modifier = Modifier.padding(top = 24.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(forMode.sortedBy { it.isDone }) { todo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = todo.isDone, onCheckedChange = { planning.toggleTodo(todo.id) })
                            Text(todo.title, textDecoration = if (todo.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                        }
                        IconButton(onClick = { planning.deleteTodo(todo.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Weekly / daily schedule
// ---------------------------------------------------------------------------

@Composable
private fun ScheduleTab(planning: PlanningManager, mode: AppMode) {
    val strings = LocalStrings.current
    val blocks by planning.schedule.collectAsState()
    val forMode = blocks.filter { it.mode == mode }.sortedBy { it.dayOfWeek * 1440 + it.startMinuteOfDay }
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Button(onClick = { showAdd = true }) { Text(strings.scheduleAddButton) }
        Spacer(Modifier.height(8.dp))
        if (forMode.isEmpty()) {
            Text(strings.scheduleEmpty, modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(forMode) { block ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(block.title, fontWeight = FontWeight.SemiBold)
                                Text("${strings.weekdayFullNames[block.dayOfWeek - 1]} — ${minuteToClock(block.startMinuteOfDay)}\u2013${minuteToClock(block.endMinuteOfDay)}")
                            }
                            IconButton(onClick = { planning.deleteScheduleBlock(block.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddScheduleDialog(
            onDismiss = { showAdd = false },
            onConfirm = { title, day, startMin, endMin ->
                planning.addScheduleBlock(
                    ScheduleBlock(
                        id = "sch_${Clock.System.now().toEpochMilliseconds()}",
                        title = title, mode = mode, dayOfWeek = day,
                        startMinuteOfDay = startMin, endMinuteOfDay = endMin,
                    ),
                )
                showAdd = false
            },
        )
    }
}

private fun minuteToClock(m: Int): String = "${(m / 60).toString().padStart(2, '0')}:${(m % 60).toString().padStart(2, '0')}"

@Composable
private fun AddScheduleDialog(onDismiss: () -> Unit, onConfirm: (String, Int, Int, Int) -> Unit) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(1) }
    var startHour by remember { mutableStateOf(9f) }
    var endHour by remember { mutableStateOf(11f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.scheduleAddButton) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("عنوان") })
                Spacer(Modifier.height(8.dp))
                Text("روز: ${strings.weekdayFullNames[day - 1]}")
                Slider(value = day.toFloat(), valueRange = 1f..7f, steps = 5, onValueChange = { day = it.toInt() })
                Text("شروع: ${startHour.toInt()}:00")
                Slider(value = startHour, valueRange = 0f..23f, onValueChange = { startHour = it })
                Text("پایان: ${endHour.toInt()}:00")
                Slider(value = endHour, valueRange = 0f..23f, onValueChange = { endHour = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title.ifBlank { "بدون عنوان" }, day, startHour.toInt() * 60, endHour.toInt() * 60) }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}

// ---------------------------------------------------------------------------
// Classes & deadlines (ring an alarm)
// ---------------------------------------------------------------------------

@Composable
private fun DeadlinesTab(planning: PlanningManager, mode: AppMode, appearance: AppearanceConfig) {
    val strings = LocalStrings.current
    val items by planning.deadlines.collectAsState()
    val forMode = items.filter { it.mode == mode }.sortedBy { it.dueEpochMs }
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Button(onClick = { showAdd = true }) { Text(strings.deadlineAddButton) }
        Spacer(Modifier.height(8.dp))
        if (forMode.isEmpty()) {
            Text(strings.deadlinesEmpty, modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(forMode) { item ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = item.isDone, onCheckedChange = { planning.toggleDeadlineDone(item.id) })
                                Column {
                                    Text("${kindEmoji(item.kind)} ${item.title}")
                                    Text(
                                        JalaliCalendar.formatDate(item.dueEpochMs / 86_400_000L, appearance.calendarSystem, appearance.language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                    if (item.notes.isNotBlank()) Text(item.notes, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            IconButton(onClick = { planning.deleteDeadline(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddDeadlineDialog(
            onDismiss = { showAdd = false },
            onConfirm = { title, kind, notes, epochMs ->
                planning.addDeadline(
                    ClassOrDeadline(
                        id = "dl_${Clock.System.now().toEpochMilliseconds()}",
                        title = title, mode = mode, kind = kind, dueEpochMs = epochMs, notes = notes,
                    ),
                )
                showAdd = false
            },
        )
    }
}

private fun kindEmoji(kind: DeadlineKind): String = when (kind) {
    DeadlineKind.CLASS -> "🎓"
    DeadlineKind.EXAM -> "📝"
    DeadlineKind.DEADLINE -> "⏰"
    DeadlineKind.OTHER -> "📌"
}

@Composable
private fun AddDeadlineDialog(onDismiss: () -> Unit, onConfirm: (String, DeadlineKind, String, Long) -> Unit) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(DeadlineKind.CLASS) }
    // Simple "in N hours from now" picker keeps this usable without a full date-picker dependency.
    var hoursFromNow by remember { mutableStateOf(24f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.deadlineAddButton) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("عنوان") })
                Spacer(Modifier.height(8.dp))
                Row {
                    DeadlineKind.entries.forEach { k ->
                        FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(kindEmoji(k)) })
                        Spacer(Modifier.width(4.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("چند ساعت دیگه: ${hoursFromNow.toInt()}")
                Slider(value = hoursFromNow, valueRange = 1f..240f, onValueChange = { hoursFromNow = it })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, placeholder = { Text("یادداشت (اختیاری)") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val due = Clock.System.now().toEpochMilliseconds() + (hoursFromNow.toLong() * 3_600_000L)
                onConfirm(title.ifBlank { "بدون عنوان" }, kind, notes, due)
            }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}

// ---------------------------------------------------------------------------
// Goal pyramid: Year -> Season -> Month -> Week
// ---------------------------------------------------------------------------

@Composable
private fun GoalsPyramidTab(planning: PlanningManager, mode: AppMode) {
    val strings = LocalStrings.current
    val goals by planning.goals.collectAsState()
    val yearGoals = goals.filter { it.mode == mode && it.level == GoalLevel.YEAR }
    var showAddYear by remember { mutableStateOf(false) }
    var addStepUnder by remember { mutableStateOf<Goal?>(null) }

    Column(Modifier.fillMaxSize()) {
        Text(strings.goalsPyramidTitle, style = MaterialTheme.typography.titleMedium)
        Text(strings.goalsPyramidHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { showAddYear = true }) { Text(strings.addYearGoal) }
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(yearGoals) { year ->
                PyramidBranch(
                    goal = year,
                    depth = 0,
                    goals = goals,
                    onAddStep = { addStepUnder = it },
                    onProgress = { id, pct -> planning.updateGoalProgress(id, pct) },
                    onDelete = { planning.deleteGoal(it) },
                )
            }
        }
    }

    if (showAddYear) {
        AddGoalDialog(
            title = strings.addYearGoal,
            onDismiss = { showAddYear = false },
            onConfirm = { text ->
                planning.addGoal(
                    Goal(
                        id = "g_${Clock.System.now().toEpochMilliseconds()}",
                        title = text, mode = mode, level = GoalLevel.YEAR,
                        year = currentYear(),
                    ),
                )
                showAddYear = false
            },
        )
    }

    addStepUnder?.let { parent ->
        val nextLevel = when (parent.level) {
            GoalLevel.YEAR -> GoalLevel.SEASON
            GoalLevel.SEASON -> GoalLevel.MONTH
            GoalLevel.MONTH -> GoalLevel.WEEK
            GoalLevel.WEEK -> GoalLevel.WEEK
        }
        AddGoalDialog(
            title = strings.addStepUnder,
            onDismiss = { addStepUnder = null },
            onConfirm = { text ->
                planning.addGoal(
                    Goal(
                        id = "g_${Clock.System.now().toEpochMilliseconds()}",
                        title = text, mode = mode, level = nextLevel, parentId = parent.id,
                        year = parent.year,
                    ),
                )
                addStepUnder = null
            },
        )
    }
}

private fun currentYear(): Int =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

@Composable
private fun PyramidBranch(
    goal: Goal,
    depth: Int,
    goals: List<Goal>,
    onAddStep: (Goal) -> Unit,
    onProgress: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val children = goals.filter { it.parentId == goal.id }
    val levelColor = when (goal.level) {
        GoalLevel.YEAR -> MaterialTheme.colorScheme.primaryContainer
        GoalLevel.SEASON -> MaterialTheme.colorScheme.secondaryContainer
        GoalLevel.MONTH -> MaterialTheme.colorScheme.tertiaryContainer
        GoalLevel.WEEK -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column(modifier = Modifier.padding(start = (depth * 16).dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = levelColor)) {
            Column(Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(goal.title, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { onDelete(goal.id) }) { Icon(Icons.Filled.Delete, contentDescription = null) }
                }
                Spacer(Modifier.height(4.dp))
                Slider(
                    value = goal.progressPercent.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { onProgress(goal.id, it.toInt()) },
                )
                Text("${goal.progressPercent}%", style = MaterialTheme.typography.labelSmall)
                if (goal.level != GoalLevel.WEEK) {
                    TextButton(onClick = { onAddStep(goal) }) { Text(strings.addStepUnder) }
                }
            }
        }
        children.forEach { child ->
            Spacer(Modifier.height(8.dp))
            PyramidBranch(child, depth + 1, goals, onAddStep, onProgress, onDelete)
        }
    }
}

@Composable
private fun AddGoalDialog(title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val strings = LocalStrings.current
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("عنوان هدف") }) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) { Text(strings.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}
