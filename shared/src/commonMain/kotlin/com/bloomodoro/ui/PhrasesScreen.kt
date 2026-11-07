package com.bloomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bloomodoro.domain.PhraseProvider
import com.bloomodoro.i18n.AppStrings
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.CustomPhrase
import com.bloomodoro.model.NotifyMoment
import com.bloomodoro.model.Phase

/**
 * Motivational-phrases editor, embedded as one section inside the consolidated Settings screen
 * (it used to be its own bottom-nav tab — now it lives under "جمله‌های انگیزشی" in Settings).
 * Uses a plain Column (not LazyColumn) since Settings itself already scrolls.
 */
@Composable
fun PhrasesScreen(phraseProvider: PhraseProvider) {
    val strings = LocalStrings.current
    var phrases by remember { mutableStateOf(phraseProvider.userPhrases()) }
    var showAddSheet by remember { mutableStateOf(false) }

    fun refresh() { phrases = phraseProvider.userPhrases() }

    Column(Modifier.fillMaxWidth()) {
        Text(strings.phrasesHint, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        if (phrases.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Text(strings.noPhrasesYet, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                phrases.forEach { phrase ->
                    PhraseCard(
                        phrase = phrase,
                        strings = strings,
                        onDelete = { phraseProvider.deletePhrase(phrase.id); refresh() },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { showAddSheet = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.addPhraseTitle)
        }
    }

    if (showAddSheet) {
        AddPhraseSheet(
            strings = strings,
            onDismiss = { showAddSheet = false },
            onSave = { text, phase, moment ->
                phraseProvider.addPhrase(text, moment, phase)
                refresh()
                showAddSheet = false
            },
        )
    }
}

@Composable
private fun PhraseCard(phrase: CustomPhrase, strings: AppStrings, onDelete: () -> Unit) {
    ElevatedCard(shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(phrase.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${strings.phaseLabelFor(phrase.phase)} • ${strings.momentLabelFor(phrase.moment)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = strings.delete) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPhraseSheet(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (text: String, phase: Phase, moment: NotifyMoment) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf(Phase.WORK) }
    var moment by remember { mutableStateOf(NotifyMoment.START) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(strings.addPhraseTitle, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(strings.phrasePlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(Modifier.height(16.dp))
            Text(strings.whenToShow, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Phase.WORK, Phase.SHORT_BREAK, Phase.LONG_BREAK).forEach { p ->
                    FilterChip(selected = phase == p, onClick = { phase = p }, label = { Text(strings.phaseLabelFor(p)) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotifyMoment.entries.forEach { m ->
                    FilterChip(selected = moment == m, onClick = { moment = m }, label = { Text(strings.momentLabelFor(m)) })
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { if (text.isNotBlank()) onSave(text.trim(), phase, moment) },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank(),
            ) { Text(strings.save) }
            Spacer(Modifier.height(12.dp))
        }
    }
}
