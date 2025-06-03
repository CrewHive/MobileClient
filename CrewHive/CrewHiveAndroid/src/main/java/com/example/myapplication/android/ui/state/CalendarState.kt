package com.example.myapplication.android.state

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import java.util.*

data class CalendarState(
    val selectedDate: MutableState<Calendar>,
    val userEvents: SnapshotStateList<CalendarEvent>
)

val LocalCalendarState = compositionLocalOf<CalendarState> {
    error("CalendarState not provided")
}

