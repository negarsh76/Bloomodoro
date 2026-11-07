package com.bloomodoro.calendar

import com.bloomodoro.platform.PlatformContext

/**
 * Adds a focus/break block to the device's own calendar app, instead of maintaining a calendar
 * UI inside Bloomodoro. On Android/iOS this hands off to the native Calendar app (so it syncs
 * with whatever account — Google, iCloud — the user already has configured there). On Desktop
 * it writes a standard .ics file and opens it with the OS's default calendar handler.
 */
expect class CalendarSync(context: PlatformContext) {
    fun addEvent(title: String, description: String, startEpochMs: Long, endEpochMs: Long)
}
