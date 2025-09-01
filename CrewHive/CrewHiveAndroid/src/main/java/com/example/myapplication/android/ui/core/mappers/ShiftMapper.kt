package com.example.myapplication.android.ui.core.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.core.api.dto.CreateShiftProgrammedDTO
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private fun Color.toHexRGB6(): String {
    val r = (red * 255).roundToInt().coerceIn(0, 255)
    val g = (green * 255).roundToInt().coerceIn(0, 255)
    val b = (blue * 255).roundToInt().coerceIn(0, 255)
    return "%02X%02X%02X".format(r, g, b)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun composeIsoDateTime(dateCal: java.util.Calendar, hhmm: String): String {
    val zone = ZoneId.systemDefault()
    val (h, m) = hhmm.split(":").map { it.toInt() }
    val ldt = LocalDateTime.of(
        dateCal.get(java.util.Calendar.YEAR),
        dateCal.get(java.util.Calendar.MONTH) + 1, // Calendar è 0-based
        dateCal.get(java.util.Calendar.DAY_OF_MONTH),
        h, m, 0
    )
    // ISO_OFFSET_DATE_TIME es: 2025-08-30T14:00:00+02:00
    return ldt.atZone(zone).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

@RequiresApi(Build.VERSION_CODES.O)
fun CalendarEvent.toCreateShiftDTO(assignedUserIds: List<Long>?): CreateShiftProgrammedDTO {
    val nameSafe = title.trim().ifBlank { "Shift" }.let {
        when {
            it.length < 3 -> it.padEnd(3, '_')
            it.length > 32 -> it.take(32)
            else -> it
        }
    }
    val descSafe = (description ?: "").let { if (it.length > 256) it.take(256) else it }

    val startIso = composeIsoDateTime(date, startTime)
    val endIso   = composeIsoDateTime(date, endTime)

    // (facoltativo) protezione: se end <= start, spostiamo end di +1h
    val startDT = OffsetDateTime.parse(startIso)
    val endDT   = OffsetDateTime.parse(endIso)
    val endFixed = if (!endDT.isAfter(startDT)) startDT.plusHours(1) else endDT

    return CreateShiftProgrammedDTO(
        name = nameSafe,
        description = descSafe,
        start = startDT.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        end = endFixed.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        color = color.toHexRGB6(),
        userId = assignedUserIds?.distinct()
    )
}
