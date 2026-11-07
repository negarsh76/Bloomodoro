package com.bloomodoro.domain

import com.bloomodoro.data.LocalStore
import com.bloomodoro.i18n.Strings
import com.bloomodoro.model.*
import com.bloomodoro.notif.NotificationIds
import com.bloomodoro.notif.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/** Pure, easily-testable snapshot the UI renders every tick. */
data class TimerSnapshot(
    val mode: AppMode,
    val phase: Phase,
    val remainingMs: Long,
    val totalMs: Long,
    val isPaused: Boolean,
    val isRunning: Boolean,
    val completedToday: Int,
    val completedTotal: Int,
    val focusedMinutesToday: Int,
    val dailyTargetMinutes: Int,
) {
    val progress: Float get() = if (totalMs <= 0L) 0f else 1f - (remainingMs.toFloat() / totalMs.toFloat())
}

private const val MS_PER_MIN = 60_000L
private const val MS_PER_DAY = 86_400_000L

class PomodoroEngine(
    private val localStore: LocalStore,
    private val notifier: NotificationScheduler,
    private val phraseProvider: PhraseProvider,
    private val scope: CoroutineScope,
    private val nowMs: () -> Long,
) {
    private val _state = MutableStateFlow(rolledOverIfNewDay(localStore.loadTimerState()))
    val state: StateFlow<TimerState> = _state

    private val _studyConfig = MutableStateFlow(localStore.loadTimerConfig(AppMode.STUDY))
    private val _workConfig = MutableStateFlow(localStore.loadTimerConfig(AppMode.WORK))

    val config: StateFlow<TimerConfig> get() = if (_state.value.mode == AppMode.STUDY) _studyConfig else _workConfig

    private fun configFlowFor(mode: AppMode) = if (mode == AppMode.STUDY) _studyConfig else _workConfig

    fun configFor(mode: AppMode): StateFlow<TimerConfig> = configFlowFor(mode)

    fun updateConfig(transform: (TimerConfig) -> TimerConfig) = updateConfigFor(_state.value.mode, transform)

    fun updateConfigFor(mode: AppMode, transform: (TimerConfig) -> TimerConfig) {
        val flow = configFlowFor(mode)
        flow.update(transform)
        localStore.saveTimerConfig(mode, flow.value)
    }

    /** Switching modes is only allowed while idle — Study and Work never run at the same time. */
    fun switchMode(mode: AppMode) {
        if (_state.value.phase != Phase.IDLE) return
        _state.update { it.copy(mode = mode) }
        persist()
    }

    fun setDailyTargetMinutes(minutes: Int) = updateConfig { it.copy(dailyTargetMinutes = minutes.coerceAtLeast(0)) }

    fun snapshot(): TimerSnapshot {
        val s = _state.value
        val total = s.phaseDurationMs
        val elapsed = if (s.phase == Phase.IDLE) 0L else effectiveElapsed(s)
        val remaining = (total - elapsed).coerceAtLeast(0L)
        return TimerSnapshot(
            mode = s.mode,
            phase = s.phase,
            remainingMs = remaining,
            totalMs = total,
            isPaused = s.isPaused,
            isRunning = s.phase != Phase.IDLE,
            completedToday = s.completedWorkSessionsToday,
            completedTotal = s.completedWorkSessionsTotal,
            focusedMinutesToday = s.focusedMinutesToday,
            dailyTargetMinutes = config.value.dailyTargetMinutes,
        )
    }

    private fun effectiveElapsed(s: TimerState): Long {
        val pausedExtra = if (s.isPaused) nowMs() - s.pausedAtEpochMs else 0L
        return (nowMs() - s.phaseStartEpochMs - s.totalPausedMs - pausedExtra).coerceAtLeast(0L)
    }

    /** Call once when the app opens/resumes: rolls the day over if needed and auto-advances a finished phase. */
    fun reconcile() {
        _state.update { rolledOverIfNewDay(it) }
        val s = _state.value
        if (s.phase == Phase.IDLE || s.isPaused) return
        if (effectiveElapsed(s) >= s.phaseDurationMs) {
            advanceToNextPhase()
        }
    }

    private fun rolledOverIfNewDay(state: TimerState): TimerState {
        val today = nowMs() / MS_PER_DAY
        return if (state.lastResetEpochDay != today) {
            state.copy(completedWorkSessionsToday = 0, focusedMinutesToday = 0, lastResetEpochDay = today)
        } else state
    }

    fun start(phase: Phase = nextPhaseFor(_state.value)) {
        val durationMs = durationFor(phase)
        val start = nowMs()
        _state.update {
            it.copy(
                phase = phase,
                phaseStartEpochMs = start,
                phaseDurationMs = durationMs,
                isPaused = false,
                pausedAtEpochMs = 0L,
                totalPausedMs = 0L,
            )
        }
        persist()
        scheduleNotificationsFor(phase, start, durationMs)
        notifier.showLiveFocus(titleFor(phase), start + durationMs, isPaused = false)
        if (phase == Phase.WORK) scheduleStretchReminders(start, durationMs)
    }

    fun pause() {
        if (_state.value.phase == Phase.IDLE || _state.value.isPaused) return
        _state.update { it.copy(isPaused = true, pausedAtEpochMs = nowMs()) }
        persist()
        cancelScheduledFor(_state.value.phase)
        cancelStretchReminders()
        notifier.showLiveFocus(titleFor(_state.value.phase), 0L, isPaused = true)
    }

    fun resume() {
        val s = _state.value
        if (!s.isPaused) return
        val pausedDuration = nowMs() - s.pausedAtEpochMs
        _state.update { it.copy(isPaused = false, pausedAtEpochMs = 0L, totalPausedMs = it.totalPausedMs + pausedDuration) }
        persist()
        val updated = _state.value
        val remaining = updated.phaseDurationMs - effectiveElapsed(updated)
        rescheduleRemaining(updated.phase, remaining)
        notifier.showLiveFocus(titleFor(updated.phase), nowMs() + remaining, isPaused = false)
        if (updated.phase == Phase.WORK) scheduleStretchReminders(nowMs(), remaining)
    }

    fun skip() = advanceToNextPhase()

    /** Cute "cancel" — stops the current phase early without counting it as completed. */
    fun stopEarly() {
        val s = _state.value
        if (s.phase == Phase.WORK) {
            val minutesFocused = (effectiveElapsed(s) / MS_PER_MIN).toInt()
            if (minutesFocused > 0) recordFocusedMinutes(minutesFocused, wasCompleted = false)
        }
        resetToIdle()
    }

    fun resetToIdle() {
        cancelScheduledFor(_state.value.phase)
        cancelStretchReminders()
        notifier.clearLiveFocus()
        _state.update {
            TimerState(
                mode = it.mode,
                completedWorkSessionsTotal = it.completedWorkSessionsTotal,
                completedWorkSessionsToday = it.completedWorkSessionsToday,
                focusedMinutesToday = it.focusedMinutesToday,
                lastResetEpochDay = it.lastResetEpochDay,
            )
        }
        persist()
    }

    private fun advanceToNextPhase() {
        val finishedPhase = _state.value.phase
        val finishedMode = _state.value.mode
        cancelScheduledFor(finishedPhase)
        cancelStretchReminders()
        var newState = _state.value
        if (finishedPhase == Phase.WORK) {
            val minutes = (newState.phaseDurationMs / MS_PER_MIN).toInt()
            newState = newState.copy(
                completedWorkSessionsToday = newState.completedWorkSessionsToday + 1,
                completedWorkSessionsTotal = newState.completedWorkSessionsTotal + 1,
                focusedMinutesToday = newState.focusedMinutesToday + minutes,
            )
            _state.value = newState
            recordFocusedMinutes(minutes, wasCompleted = true)
            notifier.showSessionSummary(titleFor(Phase.WORK), newState.focusedMinutesToday)
        }
        _state.value = newState
        persist()
        start(nextPhaseFor(newState))
    }

    /** Adds today's finished chunk to the garden (one entry per day per mode) and the full history log. */
    private fun recordFocusedMinutes(minutes: Int, wasCompleted: Boolean) {
        val mode = _state.value.mode
        val today = nowMs() / MS_PER_DAY
        val garden = localStore.loadGarden().toMutableList()
        val idx = garden.indexOfFirst { it.epochDay == today && it.mode == mode }
        if (idx >= 0) {
            val existing = garden[idx]
            garden[idx] = existing.copy(
                focusedMinutes = existing.focusedMinutes + minutes,
                sessionsCompleted = existing.sessionsCompleted + if (wasCompleted) 1 else 0,
            )
        } else {
            garden.add(GardenEntry(epochDay = today, mode = mode, focusedMinutes = minutes, sessionsCompleted = if (wasCompleted) 1 else 0))
        }
        localStore.saveGarden(garden)

        val history = localStore.loadHistory().toMutableList()
        val end = nowMs()
        history.add(
            FocusHistoryEntry(
                id = "h_${Random.nextLong()}",
                mode = mode,
                startEpochMs = end - minutes * MS_PER_MIN,
                endEpochMs = end,
                durationMinutes = minutes,
                wasCompleted = wasCompleted,
            ),
        )
        localStore.saveHistory(history)
    }

    private fun nextPhaseFor(s: TimerState): Phase = when (s.phase) {
        Phase.IDLE, Phase.SHORT_BREAK, Phase.LONG_BREAK -> Phase.WORK
        Phase.WORK -> {
            val justFinished = s.completedWorkSessionsToday
            if (justFinished > 0 && justFinished % config.value.sessionsBeforeLongBreak == 0) Phase.LONG_BREAK
            else Phase.SHORT_BREAK
        }
    }

    private fun durationFor(phase: Phase): Long {
        val cfg = config.value
        return when (phase) {
            Phase.WORK -> cfg.workMinutes * MS_PER_MIN
            Phase.SHORT_BREAK -> cfg.shortBreakMinutes * MS_PER_MIN
            Phase.LONG_BREAK -> cfg.longBreakMinutes * MS_PER_MIN
            Phase.IDLE -> 0L
        }
    }

    private fun scheduleNotificationsFor(phase: Phase, startMs: Long, durationMs: Long) {
        val cfg = config.value
        if (cfg.notifyAtStart) {
            notifier.schedule(com.bloomodoro.notif.ScheduledNotification(
                requestId = NotificationIds.forMoment(phase.ordinal, NotifyMoment.START.ordinal),
                triggerAtEpochMs = startMs,
                title = titleFor(phase),
                body = phraseProvider.pickFor(phase, NotifyMoment.START),
            ))
        }
        if (cfg.notifyAtMidpoint) {
            notifier.schedule(com.bloomodoro.notif.ScheduledNotification(
                requestId = NotificationIds.forMoment(phase.ordinal, NotifyMoment.MIDPOINT.ordinal),
                triggerAtEpochMs = startMs + durationMs / 2,
                title = titleFor(phase),
                body = phraseProvider.pickFor(phase, NotifyMoment.MIDPOINT),
            ))
        }
        if (cfg.notifyAtEnd) {
            notifier.schedule(com.bloomodoro.notif.ScheduledNotification(
                requestId = NotificationIds.forMoment(phase.ordinal, NotifyMoment.END.ordinal),
                triggerAtEpochMs = startMs + durationMs,
                title = titleFor(phase),
                body = phraseProvider.pickFor(phase, NotifyMoment.END),
            ))
        }
    }

    /** Repeating stand-up/stretch pings every N minutes, only while a WORK phase is actually running. */
    private fun scheduleStretchReminders(fromMs: Long, spanMs: Long) {
        val cfg = config.value
        if (!cfg.stretchRemindersEnabled || cfg.stretchIntervalMinutes <= 0) return
        val intervalMs = cfg.stretchIntervalMinutes * MS_PER_MIN
        var t = intervalMs
        var i = 0
        while (t < spanMs) {
            notifier.scheduleStretchReminder(
                requestId = NotificationIds.forStretch(i),
                triggerAtEpochMs = fromMs + t,
                title = "وقت یه تکون خوردنه 🧘",
                body = "یه چند لحظه بلند شو، گردن و کمرتو حرکت بده، بعد برگرد سر درس/کار 🌿",
            )
            t += intervalMs
            i++
        }
    }

    private fun cancelStretchReminders() {
        notifier.cancelAll((0 until 50).map { NotificationIds.forStretch(it) })
    }

    private fun rescheduleRemaining(phase: Phase, remainingMs: Long) {
        val cfg = config.value
        val now = nowMs()
        val total = _state.value.phaseDurationMs
        val elapsed = total - remainingMs
        if (cfg.notifyAtMidpoint && elapsed < total / 2) {
            notifier.schedule(com.bloomodoro.notif.ScheduledNotification(
                requestId = NotificationIds.forMoment(phase.ordinal, NotifyMoment.MIDPOINT.ordinal),
                triggerAtEpochMs = now + (total / 2 - elapsed),
                title = titleFor(phase),
                body = phraseProvider.pickFor(phase, NotifyMoment.MIDPOINT),
            ))
        }
        if (cfg.notifyAtEnd) {
            notifier.schedule(com.bloomodoro.notif.ScheduledNotification(
                requestId = NotificationIds.forMoment(phase.ordinal, NotifyMoment.END.ordinal),
                triggerAtEpochMs = now + remainingMs,
                title = titleFor(phase),
                body = phraseProvider.pickFor(phase, NotifyMoment.END),
            ))
        }
    }

    private fun cancelScheduledFor(phase: Phase) {
        notifier.cancelAll(NotifyMoment.entries.map { NotificationIds.forMoment(phase.ordinal, it.ordinal) })
    }

    private fun titleFor(phase: Phase): String =
        Strings.forLanguage(localStore.loadAppearance().language).phaseTitleFor(phase)

    private fun persist() = localStore.saveTimerState(_state.value)
}
