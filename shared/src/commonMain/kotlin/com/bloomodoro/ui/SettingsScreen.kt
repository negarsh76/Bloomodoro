package com.bloomodoro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.bloomodoro.domain.PhraseProvider
import com.bloomodoro.domain.PomodoroEngine
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.AppMode

/**
 * Everything about the app in one consolidated, collapsible place. Theme/appearance and the
 * motivational-phrases editor — which each used to be their own bottom-nav tab — now live here
 * as sections, so the bottom bar only ever has Timer / Planning / Settings.
 *
 * Study and Work each get their own full timing/notification settings — switching the segmented
 * control at the top edits a completely different TimerConfig, the other mode's numbers are
 * untouched.
 */
@Composable
fun SettingsScreen(
    engine: PomodoroEngine,
    themeViewModel: ThemeViewModel,
    phraseProvider: PhraseProvider,
    cloudSync: com.bloomodoro.data.CloudSync,
    backupManager: com.bloomodoro.data.BackupManager,
) {
    val strings = LocalStrings.current
    val appearance by themeViewModel.appearance.collectAsState()
    var editingMode by remember { mutableStateOf(AppMode.STUDY) }
    val config by engine.configFor(editingMode).collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Spacer(Modifier.height(12.dp)) }

        item {
            ExpandableSection(title = strings.sectionLanguageRegion, initiallyExpanded = true) {
                Text(strings.languageLabel, style = MaterialTheme.typography.bodyLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = appearance.language == com.bloomodoro.model.AppLanguage.FA,
                        onClick = { themeViewModel.update { it.copy(language = com.bloomodoro.model.AppLanguage.FA) } },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text(strings.languageFa) }
                    SegmentedButton(
                        selected = appearance.language == com.bloomodoro.model.AppLanguage.EN,
                        onClick = { themeViewModel.update { it.copy(language = com.bloomodoro.model.AppLanguage.EN) } },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text(strings.languageEn) }
                }
                Spacer(Modifier.height(12.dp))
                Text(strings.calendarSystemLabel, style = MaterialTheme.typography.bodyLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = appearance.calendarSystem == com.bloomodoro.model.CalendarSystem.JALALI,
                        onClick = { themeViewModel.update { it.copy(calendarSystem = com.bloomodoro.model.CalendarSystem.JALALI) } },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text(strings.calendarJalali) }
                    SegmentedButton(
                        selected = appearance.calendarSystem == com.bloomodoro.model.CalendarSystem.GREGORIAN,
                        onClick = { themeViewModel.update { it.copy(calendarSystem = com.bloomodoro.model.CalendarSystem.GREGORIAN) } },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text(strings.calendarGregorian) }
                }
            }
        }

        item {
            ExpandableSection(title = strings.sectionTiming, initiallyExpanded = true) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = editingMode == AppMode.STUDY,
                        onClick = { editingMode = AppMode.STUDY },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text(strings.modeStudy) }
                    SegmentedButton(
                        selected = editingMode == AppMode.WORK,
                        onClick = { editingMode = AppMode.WORK },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text(strings.modeWork) }
                }
                Spacer(Modifier.height(4.dp))
                Text(strings.editingSettingsFor(strings.modeLabelFor(editingMode)), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                MinutesSlider(strings.focusDuration, config.workMinutes, 5f..90f, strings.minutesUnit) {
                    engine.updateConfigFor(editingMode) { c -> c.copy(workMinutes = it) }
                }
                MinutesSlider(strings.shortBreakDuration, config.shortBreakMinutes, 1f..30f, strings.minutesUnit) {
                    engine.updateConfigFor(editingMode) { c -> c.copy(shortBreakMinutes = it) }
                }
                MinutesSlider(strings.longBreakDuration, config.longBreakMinutes, 5f..60f, strings.minutesUnit) {
                    engine.updateConfigFor(editingMode) { c -> c.copy(longBreakMinutes = it) }
                }
                MinutesSlider(strings.sessionsBeforeLongBreak, config.sessionsBeforeLongBreak, 2f..8f, "") {
                    engine.updateConfigFor(editingMode) { c -> c.copy(sessionsBeforeLongBreak = it) }
                }
            }
        }

        item {
            ExpandableSection(title = strings.sectionMyNotifications) {
                Text(
                    strings.editingSettingsFor(strings.modeLabelFor(editingMode)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(4.dp))
                ToggleRow(strings.notifyStart, config.notifyAtStart) { v -> engine.updateConfigFor(editingMode) { it.copy(notifyAtStart = v) } }
                ToggleRow(strings.notifyMidpoint, config.notifyAtMidpoint) { v -> engine.updateConfigFor(editingMode) { it.copy(notifyAtMidpoint = v) } }
                ToggleRow(strings.notifyEnd, config.notifyAtEnd) { v -> engine.updateConfigFor(editingMode) { it.copy(notifyAtEnd = v) } }
            }
        }

        item {
            ExpandableSection(title = strings.sectionStretch) {
                ToggleRow(strings.stretchEnabled, config.stretchRemindersEnabled) { v ->
                    engine.updateConfigFor(editingMode) { it.copy(stretchRemindersEnabled = v) }
                }
                if (config.stretchRemindersEnabled) {
                    MinutesSlider(strings.stretchInterval, config.stretchIntervalMinutes, 10f..90f, strings.minutesUnit) {
                        engine.updateConfigFor(editingMode) { c -> c.copy(stretchIntervalMinutes = it) }
                    }
                }
            }
        }

        item {
            ExpandableSection(title = strings.sectionLiveFocus) {
                ToggleRow(strings.liveFocusEnabled, config.liveFocusNotificationEnabled) { v ->
                    engine.updateConfigFor(editingMode) { it.copy(liveFocusNotificationEnabled = v) }
                }
            }
        }

        item {
            ExpandableSection(title = strings.sectionAppearance) {
                ThemeScreen(viewModel = themeViewModel)
            }
        }

        item {
            ExpandableSection(title = strings.sectionPhrasesInSettings) {
                PhrasesScreen(phraseProvider = phraseProvider)
            }
        }

        item {
            ExpandableSection(title = strings.sectionBackup) {
                Text(strings.cloudBackupHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(12.dp))
                BackupSection(cloudSync = cloudSync, backupManager = backupManager)
                Spacer(Modifier.height(16.dp))
                LocalBackupCard(backupManager = backupManager)
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

/** A collapsible card — used for every settings section so the whole screen isn't one huge
 * wall of sliders/switches at once. */
@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Card(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            }
            if (expanded) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

/** Copy/paste based backup that works fully offline — no Firebase project required. Complements
 * the email/cloud backup above, which only works once a real Firebase project is connected. */
@Composable
private fun LocalBackupCard(backupManager: com.bloomodoro.data.BackupManager) {
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    var pasteText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Text(strings.localBackupTitle, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(4.dp))
    Text(strings.localBackupHint, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(10.dp))
    Button(
        onClick = {
            clipboard.setText(AnnotatedString(backupManager.exportSnapshotJson()))
            statusMessage = strings.copiedToClipboard
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(strings.exportToClipboard) }
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = pasteText,
        onValueChange = { pasteText = it },
        placeholder = { Text(strings.pasteHere) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedButton(
        onClick = {
            backupManager.importSnapshotJson(pasteText).fold(
                onSuccess = { statusMessage = strings.importedSuccessfully },
                onFailure = { statusMessage = strings.importFailed },
            )
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = pasteText.isNotBlank(),
    ) { Text(strings.importFromClipboard) }
    statusMessage?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MinutesSlider(
    label: String,
    minutes: Int,
    range: ClosedFloatingPointRange<Float>,
    unitSuffix: String = " min",
    onChange: (Int) -> Unit,
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text("$label: $minutes$unitSuffix", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = minutes.toFloat(),
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1,
            onValueChange = { onChange(it.toInt()) },
        )
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}
