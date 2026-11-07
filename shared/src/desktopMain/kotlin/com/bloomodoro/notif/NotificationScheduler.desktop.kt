package com.bloomodoro.notif

import com.bloomodoro.platform.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

actual class NotificationScheduler actual constructor(context: PlatformContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val jobs = mutableMapOf<Int, Job>()
    private val trayIcon: TrayIcon? = runCatching {
        if (!SystemTray.isSupported()) return@runCatching null
        val image = Toolkit.getDefaultToolkit().createImage(ByteArray(0))
        TrayIcon(image, "Bloomodoro").also { SystemTray.getSystemTray().add(it) }
    }.getOrNull()

    actual fun schedule(notification: ScheduledNotification) {
        jobs[notification.requestId]?.cancel()
        jobs[notification.requestId] = scope.launch {
            val delayMs = (notification.triggerAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(delayMs)
            showImmediate(notification.title, notification.body)
        }
    }

    actual fun scheduleStretchReminder(requestId: Int, triggerAtEpochMs: Long, title: String, body: String) {
        schedule(ScheduledNotification(requestId, triggerAtEpochMs, title, body))
    }

    actual fun cancel(requestId: Int) {
        jobs.remove(requestId)?.cancel()
    }

    actual fun cancelAll(requestIds: List<Int>) = requestIds.forEach { cancel(it) }

    actual fun showImmediate(title: String, body: String) {
        trayIcon?.displayMessage(title, body, TrayIcon.MessageType.INFO)
    }

    actual fun showLiveFocus(phaseTitle: String, endEpochMs: Long, isPaused: Boolean) {
        // Desktop keeps the live countdown in the window UI itself (always visible while running),
        // so we don't duplicate it as a tray popup — only nudge once when it starts.
    }

    actual fun clearLiveFocus() = Unit

    actual fun showSessionSummary(title: String, focusedMinutesToday: Int) {
        val hours = focusedMinutesToday / 60
        val mins = focusedMinutesToday % 60
        val timeText = if (hours > 0) "$hours ساعت و $mins دقیقه" else "$mins دقیقه"
        showImmediate(title, "امروز مجموعاً $timeText تمرکز کردی 🌸")
    }

    actual suspend fun requestPermissionIfNeeded(): Boolean = true
}
