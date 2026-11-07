package com.bloomodoro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bloomodoro.data.LocalStore
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.model.AppearanceConfig
import com.bloomodoro.model.CompanionStyle
import com.bloomodoro.theme.Palettes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(private val localStore: LocalStore) {
    private val _appearance = MutableStateFlow(localStore.loadAppearance())
    val appearance: StateFlow<AppearanceConfig> = _appearance

    fun update(transform: (AppearanceConfig) -> AppearanceConfig) {
        val next = transform(_appearance.value)
        _appearance.value = next
        localStore.saveAppearance(next)
    }
}

/**
 * Theme/appearance controls, embedded as one section inside the consolidated Settings screen
 * (it used to be its own bottom-nav tab — now it lives under "تم و ظاهر" in Settings).
 */
@Composable
fun ThemeScreen(viewModel: ThemeViewModel) {
    val appearance by viewModel.appearance.collectAsState()
    val currentTheme = appearance.customTheme ?: Palettes.byId(appearance.themeId)
    val strings = LocalStrings.current

    Column(Modifier.fillMaxWidth()) {
        Text(strings.colorPalette, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(180.dp),
        ) {
            items(Palettes.all) { palette ->
                PaletteSwatch(
                    name = palette.name,
                    color = Color(palette.primary),
                    selected = appearance.themeId == palette.id && appearance.customTheme == null,
                    onClick = { viewModel.update { it.copy(themeId = palette.id, customTheme = null) } },
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Text(strings.fullCustomization, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(strings.customizationHint, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))

        ColorEditRow(strings.backgroundColor, currentTheme.background) { hex ->
            viewModel.update { it.copy(customTheme = currentTheme.copy(id = "custom", background = hex)) }
        }
        ColorEditRow(strings.primaryColor, currentTheme.primary) { hex ->
            viewModel.update { it.copy(customTheme = currentTheme.copy(id = "custom", primary = hex)) }
        }
        ColorEditRow(strings.secondaryColor, currentTheme.secondary) { hex ->
            viewModel.update { it.copy(customTheme = currentTheme.copy(id = "custom", secondary = hex)) }
        }
        ColorEditRow(strings.bloomAccentColor, currentTheme.accentBloom) { hex ->
            viewModel.update { it.copy(customTheme = currentTheme.copy(id = "custom", accentBloom = hex)) }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(strings.darkModeLabel, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(
                checked = appearance.isDarkMode,
                onCheckedChange = { v -> viewModel.update { it.copy(isDarkMode = v) } },
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(strings.companionSection, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompanionStyle.entries.forEach { style ->
                FilterChip(
                    selected = appearance.companionStyle == style,
                    onClick = { viewModel.update { it.copy(companionStyle = style) } },
                    label = { Text(labelFor(style, strings)) },
                )
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

private fun labelFor(style: CompanionStyle, strings: com.bloomodoro.i18n.AppStrings) = when (style) {
    CompanionStyle.NONE -> strings.companionNone
    CompanionStyle.BLOOM -> strings.companionBloom
    CompanionStyle.CAT -> strings.companionCat
    CompanionStyle.BLOB -> strings.companionBlob
}

@Composable
private fun PaletteSwatch(name: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(width = if (selected) 3.dp else 0.dp, color = Color.White, shape = CircleShape),
        )
        Spacer(Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall)
    }
}

/** Simple hex-text colour editor — keeps the dependency list small (no external colour-picker lib needed). */
@Composable
private fun ColorEditRow(label: String, currentArgb: Long, onChange: (Long) -> Unit) {
    var hexText by androidx.compose.runtime.remember(currentArgb) {
        androidx.compose.runtime.mutableStateOf(currentArgb.toULong().toString(16).takeLast(6))
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(currentArgb)),
        )
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = hexText,
            onValueChange = { value ->
                hexText = value
                val parsed = value.removePrefix("#").toLongOrNull(16)
                if (parsed != null && value.length in 1..6) onChange(0xFF000000L or parsed)
            },
            modifier = Modifier.width(110.dp),
            singleLine = true,
            prefix = { Text("#") },
        )
    }
}
