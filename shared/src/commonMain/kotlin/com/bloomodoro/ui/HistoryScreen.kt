package com.bloomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bloomodoro.data.LocalStore
import com.bloomodoro.domain.JalaliCalendar
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.AppMode
import com.bloomodoro.model.AppearanceConfig
import com.bloomodoro.model.FocusHistoryEntry
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Every focus session ever logged, newest first, filterable by Study/Work.
 * Opened as a full-screen overlay from the Timer screen, so it carries its own back button. */
@Composable
fun HistoryScreen(localStore: LocalStore, appearance: AppearanceConfig, onBack: () -> Unit) {
    val strings = LocalStrings.current
    var filter by remember { mutableStateOf<AppMode?>(null) }
    val all = remember { localStore.loadHistory().sortedByDescending { it.endEpochMs } }
    val shown = if (filter == null) all else all.filter { it.mode == filter }
    val totalMinutes = shown.sumOf { it.durationMinutes }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backButton)
            }
            Spacer(Modifier.width(4.dp))
            Text(strings.historyTitle, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(selected = filter == null, onClick = { filter = null }, shape = SegmentedButtonDefaults.itemShape(0, 3)) { Text(strings.filterAll) }
            SegmentedButton(selected = filter == AppMode.STUDY, onClick = { filter = AppMode.STUDY }, shape = SegmentedButtonDefaults.itemShape(1, 3)) { Text(strings.modeStudy) }
            SegmentedButton(selected = filter == AppMode.WORK, onClick = { filter = AppMode.WORK }, shape = SegmentedButtonDefaults.itemShape(2, 3)) { Text(strings.modeWork) }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            strings.historyTotalLabel(strings.hoursMinutesText(totalMinutes / 60, totalMinutes % 60)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))

        if (shown.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.historyEmpty)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(shown) { entry -> HistoryRow(entry, appearance) }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: FocusHistoryEntry, appearance: AppearanceConfig) {
    val strings = LocalStrings.current
    val epochDay = entry.endEpochMs / 86_400_000L
    val dt = Instant.fromEpochMilliseconds(entry.endEpochMs).toLocalDateTime(TimeZone.currentSystemDefault())
    val datePart = JalaliCalendar.formatDate(epochDay, appearance.calendarSystem, appearance.language)
    val timePart = "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"

    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(strings.modeLabelFor(entry.mode), style = MaterialTheme.typography.bodyLarge)
                Text("$datePart  $timePart", style = MaterialTheme.typography.labelSmall)
            }
            Text(
                "${entry.durationMinutes}${strings.minutesUnit}" + if (!entry.wasCompleted) strings.notCompletedSuffix else "",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
