package com.bloomodoro.notif

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Keeps one ongoing, non-dismissable notification alive for the lifetime of a running session —
 * this is what makes the countdown visible on the lock screen and durable across the app being
 * swiped away. It uses Android's built-in chronometer view (`setUsesChronometer` +
 * `setChronometerCountDown`), so the OS itself repaints the ticking number every second; we never
 * loop or repost from Kotlin, which keeps this both battery-cheap and exactly accurate.
 */
class FocusForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_COUNTDOWN -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Bloomodoro"
                val subtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""
                val endEpochMs = intent.getLongExtra(EXTRA_END_EPOCH_MS, System.currentTimeMillis())
                startForeground(NOTIFICATION_ID, buildCountdownNotification(this, title, subtitle, endEpochMs))
            }
            ACTION_SHOW_PAUSED -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Bloomodoro"
                val subtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: "روی پاز"
                startForeground(NOTIFICATION_ID, buildStaticNotification(this, title, subtitle))
            }
            ACTION_STOP -> {
                // Any launch via startForegroundService must call startForeground() at least once,
                // even one about to be torn down immediately, or the OS treats it as a violation.
                startForeground(NOTIFICATION_ID, buildStaticNotification(this, "Bloomodoro", "جلسه تموم شد"))
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    companion object {
        const val NOTIFICATION_ID = 4200
        const val ACTION_SHOW_COUNTDOWN = "com.bloomodoro.action.SHOW_COUNTDOWN"
        const val ACTION_SHOW_PAUSED = "com.bloomodoro.action.SHOW_PAUSED"
        const val ACTION_STOP = "com.bloomodoro.action.STOP"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_END_EPOCH_MS = "extra_end_epoch_ms"

        private fun openAppPendingIntent(context: Context): PendingIntent {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            return PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        fun buildCountdownNotification(context: Context, title: String, subtitle: String, endEpochMs: Long): Notification =
            NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_LIVE)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openAppPendingIntent(context))
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(endEpochMs)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        fun buildStaticNotification(context: Context, title: String, subtitle: String): Notification =
            NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_LIVE)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openAppPendingIntent(context))
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        fun start(context: Context, title: String, subtitle: String, endEpochMs: Long) {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = ACTION_SHOW_COUNTDOWN
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SUBTITLE, subtitle)
                putExtra(EXTRA_END_EPOCH_MS, endEpochMs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }

        fun showPaused(context: Context, title: String, subtitle: String) {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                action = ACTION_SHOW_PAUSED
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SUBTITLE, subtitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusForegroundService::class.java).apply { action = ACTION_STOP }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }
    }
}
