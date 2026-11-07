package com.bloomodoro.domain

import com.bloomodoro.model.AppLanguage
import com.bloomodoro.model.CalendarSystem
import kotlinx.datetime.LocalDate

/** A calendar-agnostic (year, month, day) triple, so callers never need to know which system it's in. */
data class CalendarDate(val year: Int, val month: Int, val day: Int)

/**
 * Jalali (Iranian solar) <-> Gregorian conversion, using the standard 33-year-cycle algorithm
 * (the same one behind most open-source Jalali calendar libraries). No external dependency
 * needed — it's pure integer arithmetic, accurate for the entire range this app will ever see.
 */
object JalaliCalendar {

    private val gregorianDaysBeforeMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): CalendarDate {
        var jy: Int
        var gyAdj = gy
        if (gyAdj > 1600) {
            jy = 979
            gyAdj -= 1600
        } else {
            jy = 0
            gyAdj -= 621
        }
        val gy2 = if (gm > 2) gyAdj + 1 else gyAdj
        var days = (365 * gyAdj) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) - 80 + gd + gregorianDaysBeforeMonth[gm - 1]
        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + (days % 31)
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + ((days - 186) % 30)
        }
        return CalendarDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): CalendarDate {
        var gy: Int
        var jyAdj = jy
        if (jyAdj > 979) {
            gy = 1600
            jyAdj -= 979
        } else {
            gy = 621
            jyAdj = jy
        }
        var days = 365 * jyAdj + (jyAdj / 33) * 8 + ((jyAdj % 33) + 3) / 4 +
            78 + jd + (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30 + 186))
        gy += 400 * (days / 146097)
        days %= 146097
        var leap = true
        if (days >= 36525) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++ else leap = false
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days >= 366) {
            leap = false
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val gDaysInMonth = intArrayOf(31, if (isGregorianLeap(gy)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        var d = days + 1
        while (gm < 12 && d > gDaysInMonth[gm]) {
            d -= gDaysInMonth[gm]
            gm++
        }
        return CalendarDate(gy, gm + 1, d)
    }

    private fun isGregorianLeap(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    fun isJalaliLeap(jy: Int): Boolean {
        // A year is leap iff the day-count formula above produces day 366 for its last day —
        // simplest robust check: convert Esfand 30 back and forth.
        val g = jalaliToGregorian(jy, 12, 30)
        val backToJalali = gregorianToJalali(g.year, g.month, g.day)
        return backToJalali.month == 12 && backToJalali.day == 30
    }

    fun daysInJalaliMonth(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isJalaliLeap(jy)) 30 else 29
    }

    fun epochDayToJalali(epochDay: Long): CalendarDate {
        val g = LocalDate.fromEpochDays(epochDay.toInt())
        return gregorianToJalali(g.year, g.monthNumber, g.dayOfMonth)
    }

    fun jalaliToEpochDay(date: CalendarDate): Long {
        val g = jalaliToGregorian(date.year, date.month, date.day)
        return LocalDate(g.year, g.month, g.day).toEpochDays().toLong()
    }

    fun epochDayToGregorian(epochDay: Long): CalendarDate {
        val g = LocalDate.fromEpochDays(epochDay.toInt())
        return CalendarDate(g.year, g.monthNumber, g.dayOfMonth)
    }

    fun gregorianToEpochDay(date: CalendarDate): Long =
        LocalDate(date.year, date.month, date.day).toEpochDays().toLong()

    /** Reads/writes whichever system is currently selected, so callers never branch themselves. */
    fun epochDayToDisplay(epochDay: Long, system: CalendarSystem): CalendarDate =
        if (system == CalendarSystem.JALALI) epochDayToJalali(epochDay) else epochDayToGregorian(epochDay)

    fun displayToEpochDay(date: CalendarDate, system: CalendarSystem): Long =
        if (system == CalendarSystem.JALALI) jalaliToEpochDay(date) else gregorianToEpochDay(date)

    val jalaliMonthNamesFa = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )
    val jalaliMonthNamesEn = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand",
    )
    val gregorianMonthNamesFa = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر",
    )
    val gregorianMonthNamesEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    fun monthName(month: Int, system: CalendarSystem, language: AppLanguage): String {
        val list = when (system to language) {
            CalendarSystem.JALALI to AppLanguage.FA -> jalaliMonthNamesFa
            CalendarSystem.JALALI to AppLanguage.EN -> jalaliMonthNamesEn
            CalendarSystem.GREGORIAN to AppLanguage.FA -> gregorianMonthNamesFa
            else -> gregorianMonthNamesEn
        }
        return list[(month - 1).coerceIn(0, 11)]
    }

    /** Weekday header labels, already in the right start-of-week order for the given system. */
    fun weekdayHeaders(system: CalendarSystem, language: AppLanguage): List<String> =
        if (system == CalendarSystem.JALALI) {
            if (language == AppLanguage.FA) listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
            else listOf("Sa", "Su", "Mo", "Tu", "We", "Th", "Fr")
        } else {
            if (language == AppLanguage.FA) listOf("د", "س", "چ", "پ", "ج", "ش", "ی")
            else listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        }

    /** 0-based column index of [epochDay]'s weekday, already aligned to the given system's week start. */
    fun weekdayColumn(epochDay: Long, system: CalendarSystem): Int {
        val isoOrdinal = LocalDate.fromEpochDays(epochDay.toInt()).dayOfWeek.ordinal // Monday = 0
        return if (system == CalendarSystem.JALALI) (isoOrdinal + 2) % 7 else isoOrdinal
    }

    fun formatDate(epochDay: Long, system: CalendarSystem, language: AppLanguage): String {
        val d = epochDayToDisplay(epochDay, system)
        return "${d.day} ${monthName(d.month, system, language)} ${d.year}"
    }
}
