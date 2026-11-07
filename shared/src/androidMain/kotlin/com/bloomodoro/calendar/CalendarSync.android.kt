package com.bloomodoro.calendar

import android.content.Intent
import android.provider.CalendarContract
import com.bloomodoro.platform.PlatformContext

/**
 * Delegates to whatever calendar app is installed via ACTION_INSERT — no runtime permission
 * needed (the calendar app itself owns the write), and the user sees/can edit the event before
 * it's actually saved.
 */
actual class CalendarSync actual constructor(context: PlatformContext) {
    // Named differently from the constructor parameter — see NotificationScheduler.android.kt
    // for why reusing the exact name 'context' for both is ambiguous and breaks the build.
    private val androidContext = context.context

    actual fun addEvent(title: String, description: String, startEpochMs: Long, endEpochMs: Long) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startEpochMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endEpochMs)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        androidContext.startActivity(intent)
    }
}
