package com.bloomodoro.notif

import com.bloomodoro.platform.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.UserNotifications.*

@OptIn(ExperimentalForeignApi::class)
actual class NotificationScheduler actual constructor(context: PlatformContext) {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    actual fun schedule(notification: ScheduledNotification) {
        val nowMs = (NSDate().timeIntervalSince1970 * 1000).toLong()
        val intervalSeconds = ((notification.triggerAtEpochMs - nowMs) / 1000.0).coerceAtLeast(1.0)
        val content = UNMutableNotificationContent().apply {
            title = notification.title
            body = notification.body
            sound = UNNotificationSound.defaultSound
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(intervalSeconds, false)
        val request = UNNotificationRequest.requestWithIdentifier(notification.requestId.toString(), content, trigger)
        center.addNotificationRequest(request) { }
    }

    actual fun scheduleStretchReminder(requestId: Int, triggerAtEpochMs: Long, title: String, body: String) {
        schedule(ScheduledNotification(requestId, triggerAtEpochMs, title, body))
    }

    actual fun cancel(requestId: Int) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(requestId.toString()))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(requestId.toString()))
    }

    actual fun cancelAll(requestIds: List<Int>) = requestIds.forEach { cancel(it) }

    actual fun showImmediate(title: String, body: String) {
        val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
        schedule(ScheduledNotification(requestId = 8000, triggerAtEpochMs = now + 500, title = title, body = body))
    }

    // iOS has no "chronometer" style ongoing notification like Android; the closest native
    // equivalent (a Live Activity / Dynamic Island widget) needs a small ActivityKit widget
    // extension written in Swift/Xcode — see iosApp/NOTES.md for the drop-in snippet.
    actual fun showLiveFocus(phaseTitle: String, endEpochMs: Long, isPaused: Boolean) {
        if (isPaused) return
        val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
        schedule(
            ScheduledNotification(
                requestId = NotificationIds.LIVE_FOCUS,
                triggerAtEpochMs = endEpochMs.coerceAtLeast(now + 1000),
                title = phaseTitle,
                body = "زمان تموم شد",
            ),
        )
    }

    actual fun clearLiveFocus() = cancel(NotificationIds.LIVE_FOCUS)

    actual fun showSessionSummary(title: String, focusedMinutesToday: Int) {
        val hours = focusedMinutesToday / 60
        val mins = focusedMinutesToday % 60
        val timeText = if (hours > 0) "$hours ساعت و $mins دقیقه" else "$mins دقیقه"
        showImmediate(title, "امروز مجموعاً $timeText تمرکز کردی 🌸")
    }

    actual suspend fun requestPermissionIfNeeded(): Boolean = suspendCancellableCoroutine { cont ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            cont.resume(granted) { _, _, _ -> }
        }
    }
}
