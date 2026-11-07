package com.bloomodoro.calendar

import com.bloomodoro.platform.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.EventKit.EKEntityTypeEvent
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpanThisEvent
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970

/**
 * Saves directly to the user's default calendar via EventKit, after asking for access once.
 * Requires the `NSCalendarsUsageDescription` entry in Info.plist (already added in this
 * project's iosApp/iosApp/Info.plist) — without it, iOS kills the app instead of showing the
 * permission prompt.
 */
actual class CalendarSync actual constructor(private val context: PlatformContext) {

    private val store = EKEventStore()

    @OptIn(ExperimentalForeignApi::class)
    actual fun addEvent(title: String, description: String, startEpochMs: Long, endEpochMs: Long) {
        store.requestAccessToEntityType(EKEntityTypeEvent) { granted, _ ->
            if (!granted) return@requestAccessToEntityType
            val event = EKEvent.eventWithEventStore(store)
            event.title = title
            event.notes = description
            event.startDate = NSDate.dateWithTimeIntervalSince1970(startEpochMs / 1000.0)
            event.endDate = NSDate.dateWithTimeIntervalSince1970(endEpochMs / 1000.0)
            event.calendar = store.defaultCalendarForNewEvents
            store.saveEvent(event, span = EKSpanThisEvent, error = null)
        }
    }
}
