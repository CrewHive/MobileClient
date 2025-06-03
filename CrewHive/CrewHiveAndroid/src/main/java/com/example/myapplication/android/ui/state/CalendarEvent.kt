package com.example.myapplication.android.ui.state

import com.example.myapplication.android.ui.components.calendar.CalendarEvent

fun CalendarEvent.getDurationInHours(): Int {
    val startParts = startTime.split(":").map { it.toInt() }
    val endParts = endTime.split(":").map { it.toInt() }
    val start = startParts[0] * 60 + startParts[1]
    val end = endParts[0] * 60 + endParts[1]
    return ((end - start).coerceAtLeast(0)) / 60
}
