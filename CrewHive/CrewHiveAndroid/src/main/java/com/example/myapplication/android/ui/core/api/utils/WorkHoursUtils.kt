// FILE: ui/core/utils/WorkHoursUtils.kt
package com.example.myapplication.android.ui.core.api.utils

import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private fun startOfWeekMonday(now: Calendar = Calendar.getInstance()): Calendar =
    (now.clone() as Calendar).apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    }

private fun calendarAtTime(baseDate: Calendar, hhmm: String): Calendar {
    val parts = hhmm.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return (baseDate.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, h)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

/** Ore lavorate (intere, arrotondate per difetto) da lunedì 00:00 a ora, considerando SOLO shift. */
fun weeklyWorkedHoursSinceMonday(
    shifts: List<CalendarEvent>,
    now: Calendar = Calendar.getInstance()
): Int {
    val mondayMs = startOfWeekMonday(now).timeInMillis
    val nowMs = now.timeInMillis
    var totalMillis = 0L

    shifts.forEach { ev ->
        if (ev.kind != CalendarItemKind.SHIFT) return@forEach

        val startCal = calendarAtTime(ev.date, ev.startTime)
        var endCal = calendarAtTime(ev.date, ev.endTime)

        // attraversamento mezzanotte
        if (endCal.timeInMillis <= startCal.timeInMillis) {
            endCal = (endCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        }

        val startClamped = max(startCal.timeInMillis, mondayMs)
        val endClamped = min(endCal.timeInMillis, nowMs)
        if (endClamped > startClamped) totalMillis += (endClamped - startClamped)
    }

    return floor(totalMillis / 3_600_000.0).toInt()
}
