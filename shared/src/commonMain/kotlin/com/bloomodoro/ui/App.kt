package com.bloomodoro.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.bloomodoro.AppContainer
import com.bloomodoro.i18n.LocalStrings
import com.bloomodoro.i18n.Strings
import com.bloomodoro.model.AppLanguage
import com.bloomodoro.theme.Palettes
import com.bloomodoro.theme.Palettes.withDarkPreference

/** Bottom navigation only ever shows these three — Garden and History are opened from the
 * little logo/history icons on the Timer screen itself, not from the bottom bar. */
private enum class Tab { TIMER, PLANNING, SETTINGS }

/** Full-screen overlays reachable from the Timer screen's top row, with their own back button. */
private enum class Overlay { GARDEN, HISTORY }

@Composable
fun BloomodoroApp(container: AppContainer) {
    var tab by remember { mutableStateOf(Tab.TIMER) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    val themeViewModel = remember { ThemeViewModel(container.localStore) }
    val appearance by themeViewModel.appearance.collectAsState()
    val baseTheme = appearance.customTheme ?: Palettes.byId(appearance.themeId)
    val activeTheme = baseTheme.withDarkPreference(appearance.isDarkMode)
    val strings = Strings.forLanguage(appearance.language)
    val layoutDirection = if (appearance.language == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
    val timerState by container.engine.state.collectAsState()

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDirection,
    ) {
        BloomodoroTheme(theme = activeTheme) {
            Surface {
                Scaffold(
                    bottomBar = {
                        // The nav bar uses MaterialTheme's colours, so it re-colors itself
                        // automatically whenever the palette or light/dark mode changes above.
                        if (overlay == null) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = tab == Tab.TIMER,
                                    onClick = { tab = Tab.TIMER },
                                    icon = { Icon(Icons.Filled.Timer, contentDescription = null) },
                                    label = { Text(strings.tabTimer) },
                                )
                                NavigationBarItem(
                                    selected = tab == Tab.PLANNING,
                                    onClick = { tab = Tab.PLANNING },
                                    icon = { Icon(Icons.Filled.Checklist, contentDescription = null) },
                                    label = { Text(strings.tabPlanning) },
                                )
                                NavigationBarItem(
                                    selected = tab == Tab.SETTINGS,
                                    onClick = { tab = Tab.SETTINGS },
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                    label = { Text(strings.tabSettings) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Surface(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                        when (overlay) {
                            Overlay.GARDEN -> GardenScreen(
                                localStore = container.localStore,
                                mode = timerState.mode,
                                onBack = { overlay = null },
                            )
                            Overlay.HISTORY -> HistoryScreen(
                                localStore = container.localStore,
                                appearance = appearance,
                                onBack = { overlay = null },
                            )
                            null -> when (tab) {
                                Tab.TIMER -> TimerScreen(
                                    engine = container.engine,
                                    appearance = appearance,
                                    calendarSync = container.calendarSync,
                                    onOpenGarden = { overlay = Overlay.GARDEN },
                                    onOpenHistory = { overlay = Overlay.HISTORY },
                                )
                                Tab.PLANNING -> PlanningScreen(planning = container.planningManager, mode = timerState.mode, appearance = appearance)
                                Tab.SETTINGS -> SettingsScreen(
                                    engine = container.engine,
                                    themeViewModel = themeViewModel,
                                    phraseProvider = container.phraseProvider,
                                    cloudSync = container.cloudSync,
                                    backupManager = container.backupManager,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
