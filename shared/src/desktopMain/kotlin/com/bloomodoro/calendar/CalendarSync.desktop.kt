package com.bloomodoro.calendar

import com.bloomodoro.platform.PlatformContext
import java.awt.Desktop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Desktop has no single native "Calendar app" API the way Android/iOS do, so this writes a
 * standard .ics file and asks the OS to open it — that hands the event to whatever the user
 * already has as their default calendar app (Calendar.app, Outlook, Thunderbird, GNOME
 * Calendar, etc.) without Bloomodoro needing to integrate with any of them directly.
 */
actual class CalendarSync actual constructor(private val context: PlatformContext) {
    actual fun addEvent(title: String, description: String, startEpochMs: Long, endEpochMs: Long) {
        val stampFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val ics = buildString {
            appendLine("BEGIN:VCALENDAR")
            appendLine("VERSION:2.0")
            appendLine("PRODID:-//Bloomodoro//EN")
            appendLine("BEGIN:VEVENT")
            appendLine("UID:${startEpochMs}-bloomodoro@local")
            appendLine("DTSTAMP:${stampFormat.format(Date())}")
            appendLine("DTSTART:${stampFormat.format(Date(startEpochMs))}")
            appendLine("DTEND:${stampFormat.format(Date(endEpochMs))}")
            appendLine("SUMMARY:$title")
            appendLine("DESCRIPTION:$description")
            appendLine("END:VEVENT")
            appendLine("END:VCALENDAR")
        }
        val file = File.createTempFile("bloomodoro-session", ".ics")
        file.writeText(ics)
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file)
        }
    }
}
