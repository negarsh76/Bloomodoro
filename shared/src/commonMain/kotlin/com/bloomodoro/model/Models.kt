package com.bloomodoro.model

import kotlinx.serialization.Serializable

/** The three phases of a classic focus cycle. */
@Serializable
enum class Phase { WORK, SHORT_BREAK, LONG_BREAK, IDLE }

/** Where in a phase a notification should fire. */
@Serializable
enum class NotifyMoment { START, MIDPOINT, END }

/** Two independent modes with their own timing & reminder rules. */
@Serializable
enum class AppMode { STUDY, WORK }

/** Fully user-adjustable durations & behaviour for ONE mode (Study or Work) — nothing hard-coded. */
@Serializable
data class TimerConfig(
    val workMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 20,
    val sessionsBeforeLongBreak: Int = 4,
    val notifyAtStart: Boolean = true,
    val notifyAtMidpoint: Boolean = true,
    val notifyAtEnd: Boolean = true,
    val soundEnabled: Boolean = true,
    // Stand up / stretch reminders fired repeatedly WHILE a work session is running.
    val stretchRemindersEnabled: Boolean = true,
    val stretchIntervalMinutes: Int = 30,
    // Manually set target for "how many hours today" — 0 means not set.
    val dailyTargetMinutes: Int = 0,
    // Ongoing lock-screen / notification-shade live focus display.
    val liveFocusNotificationEnabled: Boolean = true,
)

/** The full, persistable snapshot of "where the timer is right now", per mode. */
@Serializable
data class TimerState(
    val mode: AppMode = AppMode.STUDY,
    val phase: Phase = Phase.IDLE,
    val phaseStartEpochMs: Long = 0L,
    val phaseDurationMs: Long = 0L,
    val isPaused: Boolean = false,
    val pausedAtEpochMs: Long = 0L,
    val totalPausedMs: Long = 0L,
    val completedWorkSessionsToday: Int = 0,
    val completedWorkSessionsTotal: Int = 0,
    val focusedMinutesToday: Int = 0,
    val lastResetEpochDay: Long = 0L,
)

/** A motivational line the user wrote for themself, shown/notified at a given moment. */
@Serializable
data class CustomPhrase(
    val id: String,
    val text: String,
    val moment: NotifyMoment,
    val phase: Phase,
    val isUserWritten: Boolean = true,
)

/** A named, fully custom colour palette. Presets are just defaults — every value is editable. */
@Serializable
data class ColorTheme(
    val id: String,
    val name: String,
    val background: Long,
    val surface: Long,
    val primary: Long,
    val secondary: Long,
    val accentBloom: Long,
    val isDark: Boolean = false,
)

/** Visual companion that grows as sessions complete. Fully optional / swappable. */
@Serializable
enum class CompanionStyle { NONE, BLOOM, CAT, BLOB }

@Serializable
enum class AppLanguage { FA, EN }

@Serializable
enum class CalendarSystem { JALALI, GREGORIAN }

@Serializable
data class AppearanceConfig(
    val themeId: String = "peach_cream",
    val customTheme: ColorTheme? = null,
    /** Manual dark-mode switch — applied on top of whichever palette/custom theme is chosen. */
    val isDarkMode: Boolean = false,
    val companionStyle: CompanionStyle = CompanionStyle.BLOOM,
    val language: AppLanguage = AppLanguage.FA,
    val calendarSystem: CalendarSystem = CalendarSystem.JALALI,
)

// ---------------------------------------------------------------------------
// Garden / forest: one entry per calendar day (epoch-day), tracked per mode.
// ---------------------------------------------------------------------------

@Serializable
data class GardenEntry(
    /** Days since epoch (UTC), stable regardless of timezone drift within a session. */
    val epochDay: Long,
    val mode: AppMode,
    val focusedMinutes: Int = 0,
    val sessionsCompleted: Int = 0,
)

// ---------------------------------------------------------------------------
// A single logged focus session — the full history, independent of the garden roll-up.
// ---------------------------------------------------------------------------

@Serializable
data class FocusHistoryEntry(
    val id: String,
    val mode: AppMode,
    val startEpochMs: Long,
    val endEpochMs: Long,
    val durationMinutes: Int,
    val wasCompleted: Boolean,
)

// ---------------------------------------------------------------------------
// Planning suite: todos, schedule, goal pyramid, class/deadline alarms.
// ---------------------------------------------------------------------------

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val mode: AppMode,
    val isDone: Boolean = false,
    val dueEpochDay: Long? = null,
    val createdAtEpochMs: Long = 0L,
)

@Serializable
enum class RepeatRule { NONE, DAILY, WEEKLY }

/** A block on the daily/weekly schedule. If [specificEpochDay] is null it repeats every [dayOfWeek]. */
@Serializable
data class ScheduleBlock(
    val id: String,
    val title: String,
    val mode: AppMode,
    /** 1 = Monday .. 7 = Sunday, ISO-8601 style. */
    val dayOfWeek: Int,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val repeat: RepeatRule = RepeatRule.WEEKLY,
    val specificEpochDay: Long? = null,
    val remindMeAlarm: Boolean = false,
)

/** A class session or deadline that should ring an alarm on the day it's due. */
@Serializable
data class ClassOrDeadline(
    val id: String,
    val title: String,
    val mode: AppMode,
    val kind: DeadlineKind,
    val dueEpochMs: Long,
    val notes: String = "",
    val isDone: Boolean = false,
)

@Serializable
enum class DeadlineKind { CLASS, DEADLINE, EXAM, OTHER }

/** One level of the goal pyramid: Year sits at top, Season/Month/Week are the steps beneath it. */
@Serializable
enum class GoalLevel { YEAR, SEASON, MONTH, WEEK }

/**
 * A single node in the goal pyramid. [parentId] links a step up to the broader goal it serves,
 * so "read 12 books this year" (YEAR) can have "3 books this season" (SEASON, parentId = year's id)
 * which can have "1 book this month" (MONTH) etc. This is what gets rendered as a pyramid.
 */
@Serializable
data class Goal(
    val id: String,
    val title: String,
    val mode: AppMode,
    val level: GoalLevel,
    val parentId: String? = null,
    val year: Int,
    /** 1..4 for SEASON, 1..12 for MONTH, ISO week number for WEEK. Null for YEAR. */
    val periodIndex: Int? = null,
    val isDone: Boolean = false,
    val progressPercent: Int = 0,
    val notes: String = "",
)

// ---------------------------------------------------------------------------
// Goal Journey: a free-form, ordered path of concrete steps toward one destination goal —
// e.g. "Apply to grad school" -> 1) Language exam  2) Two articles  3) Recommendation letters.
// Unlike the Year/Season/Month/Week pyramid above, steps here aren't tied to time periods;
// they're just an ordered checklist rendered as an animated path/roadmap.
// ---------------------------------------------------------------------------

@Serializable
data class GoalJourney(
    val id: String,
    val title: String,
    val mode: AppMode,
    val icon: String = "🎯",
    val createdAtEpochMs: Long = 0L,
)

@Serializable
data class JourneyStep(
    val id: String,
    val journeyId: String,
    val title: String,
    val order: Int,
    val isDone: Boolean = false,
    val note: String = "",
)
