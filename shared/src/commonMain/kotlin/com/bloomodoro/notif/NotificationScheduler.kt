package com.bloomodoro.notif

import com.bloomodoro.platform.PlatformContext

/** One local notification to be delivered at an exact moment, OS-scheduled so it survives the app being killed/backgrounded. */
data class ScheduledNotification(
    val requestId: Int,
    val triggerAtEpochMs: Long,
    val title: String,
    val body: String,
)

/**
 * Implemented per-platform. Schedules OS-level local notifications so timing is correct
 * even if the app process is backgrounded or killed in between — we never rely on a
 * long-running in-process timer for delivery.
 */
expect class NotificationScheduler(context: PlatformContext) {
    fun schedule(notification: ScheduledNotification)
    fun cancel(requestId: Int)
    fun cancelAll(requestIds: List<Int>)
    fun showImmediate(title: String, body: String)
    suspend fun requestPermissionIfNeeded(): Boolean

    /**
     * Live "focus in progress" notification, visible on the lock screen, showing what phase
     * is running and when it ends. Call again to update it (e.g. after pause/resume); it
     * refreshes itself on the system clock, no polling required.
     */
    fun showLiveFocus(phaseTitle: String, endEpochMs: Long, isPaused: Boolean)
    fun clearLiveFocus()

    /** Shown once the app is closed/backgrounded and a session finishes: "You focused N minutes today". */
    fun showSessionSummary(title: String, focusedMinutesToday: Int)

    /** Repeating stand-up/stretch reminder while a work session runs. requestId lets it be cancelled per-session. */
    fun scheduleStretchReminder(requestId: Int, triggerAtEpochMs: Long, title: String, body: String)
}

/** Stable, collision-free request ids. Re-scheduling on pause/resume just overwrites. */
object NotificationIds {
    fun forMoment(phaseOrdinal: Int, momentOrdinal: Int): Int = 9000 + (phaseOrdinal * 10) + momentOrdinal
    fun forStretch(index: Int): Int = 9500 + index
    const val LIVE_FOCUS = 9800
    const val SESSION_SUMMARY = 9801
    fun forDeadline(hash: Int): Int = 20000 + (hash % 9999)
}
