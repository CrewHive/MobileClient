package com.example.myapplication.android.ui.core

import androidx.compose.ui.graphics.Color
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.core.model.Event

/**
 * Mapper tra Event (core/model) e CalendarEvent (UI).
 * Manteniamo colore come Long in core per serializzazione, e come Color in UI.
 */
object EventMapper {
    fun toUi(e: Event): CalendarEvent = CalendarEvent(
        startTime = e.startTime,
        endTime = e.endTime,
        title = e.title,
        description = e.description,
        color = Color(e.color.toULong()),
        date = e.date,
        participants = e.participants
    )
}
