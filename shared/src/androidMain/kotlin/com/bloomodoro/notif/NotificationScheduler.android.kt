package com.bloomodoro.notif

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bloomodoro.platform.PlatformContext
import kotlinx.coroutines.suspendCancellableCoroutine

actual class NotificationScheduler actual constructor(context: PlatformContext) {

    // Named differently from the constructor parameter on purpose: a property sharing the
    // exact name of a same-primary-constructor parameter is ambiguous in Kotlin and was
    // resolving back to the parameter (PlatformContext, which has no getSystemService/etc.)
    // instead of this unwrapped android.content.Context — that's what broke the build.
    private val androidContext: Context = context.context

    init {
        val manager = androidContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SESSIONS, "Bloomodoro", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for the start, midpoint, and end of each session"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_LIVE, "Focus in progress", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Ongoing focus timer, visible on the lock screen while a session runs"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_STRETCH, "Stand up & stretch", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders to get up and move your neck/back during a work session"
            },
        )
    }

    @SuppressLint("ScheduleExactAlarm")
    actual fun schedule(notification: ScheduledNotification) {
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(androidContext, NotificationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REQUEST_ID, notification.requestId)
            putExtra(EXTRA_TITLE, notification.title)
            putExtra(EXTRA_BODY, notification.body)
            putExtra(EXTRA_CHANNEL, CHANNEL_SESSIONS)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            androidContext, notification.requestId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        scheduleAlarm(alarmManager, notification.triggerAtEpochMs, pendingIntent)
    }

    actual fun scheduleStretchReminder(requestId: Int, triggerAtEpochMs: Long, title: String, body: String) {
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(androidContext, NotificationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REQUEST_ID, requestId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_CHANNEL, CHANNEL_STRETCH)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            androidContext, requestId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        scheduleAlarm(alarmManager, triggerAtEpochMs, pendingIntent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(alarmManager: AlarmManager, triggerAtEpochMs: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtEpochMs, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtEpochMs, pendingIntent)
        }
    }

    actual fun cancel(requestId: Int) {
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(androidContext, NotificationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            androidContext, requestId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    actual fun cancelAll(requestIds: List<Int>) = requestIds.forEach { cancel(it) }

    actual fun showImmediate(title: String, body: String) {
        showNotification(androidContext, CHANNEL_SESSIONS, title, body, 8000, ongoing = false)
    }

    actual fun showLiveFocus(phaseTitle: String, endEpochMs: Long, isPaused: Boolean) {
        if (isPaused) {
            FocusForegroundService.showPaused(androidContext, phaseTitle, "روی حالت پاز ⏸️ — هر وقت خواستی ادامه بده")
        } else {
            FocusForegroundService.start(androidContext, phaseTitle, "تا پایان این بخش، باقی مانده", endEpochMs)
        }
    }

    actual fun clearLiveFocus() {
        FocusForegroundService.stop(androidContext)
    }

    actual fun showSessionSummary(title: String, focusedMinutesToday: Int) {
        val hours = focusedMinutesToday / 60
        val mins = focusedMinutesToday % 60
        val timeText = if (hours > 0) "$hours ساعت و $mins دقیقه" else "$mins دقیقه"
        showNotification(
            androidContext, CHANNEL_SESSIONS, title,
            "امروز مجموعاً $timeText تمرکز کردی 🌸", NotificationIds.SESSION_SUMMARY, ongoing = false,
        )
    }

    actual suspend fun requestPermissionIfNeeded(): Boolean = suspendCancellableCoroutine { cont ->
        // Actual runtime POST_NOTIFICATIONS permission request is wired from MainActivity
        // (needs an Activity for the system dialog) — see MainActivity.kt in androidApp.
        cont.resume(true) { _, _, _ -> }
    }

    companion object {
        const val CHANNEL_SESSIONS = "bloomodoro_sessions"
        const val CHANNEL_LIVE = "bloomodoro_live_focus"
        const val CHANNEL_STRETCH = "bloomodoro_stretch"
        const val EXTRA_REQUEST_ID = "request_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_CHANNEL = "channel"

        fun showNotification(context: Context, channel: String, title: String, body: String, requestId: Int, ongoing: Boolean) {
            val notification = NotificationCompat.Builder(context, channel)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(!ongoing)
                .setOngoing(ongoing)
                .build()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(requestId, notification)
        }
    }
}

/** Fires when an AlarmManager alarm goes off — works even if the app process was killed. */
class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestId = intent.getIntExtra(NotificationScheduler.EXTRA_REQUEST_ID, 0)
        val title = intent.getStringExtra(NotificationScheduler.EXTRA_TITLE) ?: "Bloomodoro"
        val body = intent.getStringExtra(NotificationScheduler.EXTRA_BODY) ?: ""
        val channel = intent.getStringExtra(NotificationScheduler.EXTRA_CHANNEL) ?: NotificationScheduler.CHANNEL_SESSIONS
        NotificationScheduler.showNotification(context, channel, title, body, requestId, ongoing = false)
    }
}
