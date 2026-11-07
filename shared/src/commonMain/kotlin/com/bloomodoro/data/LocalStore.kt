package com.bloomodoro.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.russhwolf.settings.get
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.bloomodoro.model.*

/**
 * Single source of truth for everything that must work fully offline.
 * Backed by [Settings] (russhwolf/multiplatform-settings), which has a real
 * implementation on Android (SharedPreferences/DataStore), iOS (NSUserDefaults)
 * and Desktop (java.util.prefs) — no custom expect/actual needed here.
 */
class LocalStore(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // --- Timer state (shared) + one config PER MODE (Study vs Work are fully independent) ---
    fun saveTimerState(state: TimerState) = settings.set(KEY_TIMER_STATE, json.encodeToString(state))
    fun loadTimerState(): TimerState =
        settings.get<String>(KEY_TIMER_STATE)?.let { runCatching { json.decodeFromString<TimerState>(it) }.getOrNull() }
            ?: TimerState()

    fun saveTimerConfig(mode: AppMode, config: TimerConfig) =
        settings.set(configKey(mode), json.encodeToString(config))

    fun loadTimerConfig(mode: AppMode): TimerConfig =
        settings.get<String>(configKey(mode))?.let { runCatching { json.decodeFromString<TimerConfig>(it) }.getOrNull() }
            ?: TimerConfig()

    private fun configKey(mode: AppMode) = "timer_config_v2_${mode.name}"

    // --- Appearance ---
    fun saveAppearance(config: AppearanceConfig) = settings.set(KEY_APPEARANCE, json.encodeToString(config))
    fun loadAppearance(): AppearanceConfig =
        settings.get<String>(KEY_APPEARANCE)?.let { runCatching { json.decodeFromString<AppearanceConfig>(it) }.getOrNull() }
            ?: AppearanceConfig()

    // --- Custom phrases ---
    fun savePhrases(phrases: List<CustomPhrase>) = settings.set(KEY_PHRASES, json.encodeToString(phrases))
    fun loadPhrases(): List<CustomPhrase> =
        settings.get<String>(KEY_PHRASES)?.let { runCatching { json.decodeFromString<List<CustomPhrase>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Garden (per-day growth, forest of blooms) ---
    fun saveGarden(entries: List<GardenEntry>) = settings.set(KEY_GARDEN, json.encodeToString(entries))
    fun loadGarden(): List<GardenEntry> =
        settings.get<String>(KEY_GARDEN)?.let { runCatching { json.decodeFromString<List<GardenEntry>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Full session history ---
    fun saveHistory(entries: List<FocusHistoryEntry>) = settings.set(KEY_HISTORY, json.encodeToString(entries))
    fun loadHistory(): List<FocusHistoryEntry> =
        settings.get<String>(KEY_HISTORY)?.let { runCatching { json.decodeFromString<List<FocusHistoryEntry>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Todos ---
    fun saveTodos(todos: List<TodoItem>) = settings.set(KEY_TODOS, json.encodeToString(todos))
    fun loadTodos(): List<TodoItem> =
        settings.get<String>(KEY_TODOS)?.let { runCatching { json.decodeFromString<List<TodoItem>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Weekly / daily schedule ---
    fun saveSchedule(blocks: List<ScheduleBlock>) = settings.set(KEY_SCHEDULE, json.encodeToString(blocks))
    fun loadSchedule(): List<ScheduleBlock> =
        settings.get<String>(KEY_SCHEDULE)?.let { runCatching { json.decodeFromString<List<ScheduleBlock>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Classes & deadlines (alarms) ---
    fun saveDeadlines(items: List<ClassOrDeadline>) = settings.set(KEY_DEADLINES, json.encodeToString(items))
    fun loadDeadlines(): List<ClassOrDeadline> =
        settings.get<String>(KEY_DEADLINES)?.let { runCatching { json.decodeFromString<List<ClassOrDeadline>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Goal pyramid (year -> season -> month -> week) ---
    fun saveGoals(goals: List<Goal>) = settings.set(KEY_GOALS, json.encodeToString(goals))
    fun loadGoals(): List<Goal> =
        settings.get<String>(KEY_GOALS)?.let { runCatching { json.decodeFromString<List<Goal>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Goal Journeys (free-form ordered step paths) ---
    fun saveJourneys(journeys: List<GoalJourney>) = settings.set(KEY_JOURNEYS, json.encodeToString(journeys))
    fun loadJourneys(): List<GoalJourney> =
        settings.get<String>(KEY_JOURNEYS)?.let { runCatching { json.decodeFromString<List<GoalJourney>>(it) }.getOrNull() }
            ?: emptyList()

    fun saveJourneySteps(steps: List<JourneyStep>) = settings.set(KEY_JOURNEY_STEPS, json.encodeToString(steps))
    fun loadJourneySteps(): List<JourneyStep> =
        settings.get<String>(KEY_JOURNEY_STEPS)?.let { runCatching { json.decodeFromString<List<JourneyStep>>(it) }.getOrNull() }
            ?: emptyList()

    // --- Identity (cached, refreshed from CloudSync when online — used for future email backup) ---
    var cachedDisplayName: String? by NullableStringPref(settings, KEY_DISPLAY_NAME)
    var cachedEmail: String? by NullableStringPref(settings, KEY_EMAIL)

    companion object {
        private const val KEY_TIMER_STATE = "timer_state_v2"
        private const val KEY_APPEARANCE = "appearance_v1"
        private const val KEY_PHRASES = "phrases_v1"
        private const val KEY_GARDEN = "garden_v1"
        private const val KEY_HISTORY = "history_v1"
        private const val KEY_TODOS = "todos_v1"
        private const val KEY_SCHEDULE = "schedule_v1"
        private const val KEY_DEADLINES = "deadlines_v1"
        private const val KEY_GOALS = "goals_v1"
        private const val KEY_JOURNEYS = "journeys_v1"
        private const val KEY_JOURNEY_STEPS = "journey_steps_v1"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_EMAIL = "cached_email"
    }
}

private class NullableStringPref(private val settings: Settings, private val key: String) {
    operator fun getValue(thisRef: Any?, prop: Any?): String? = settings.get<String>(key)
    operator fun setValue(thisRef: Any?, prop: Any?, value: String?) {
        if (value == null) settings.remove(key) else settings.set(key, value)
    }
}
