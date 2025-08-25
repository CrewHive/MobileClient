package com.example.myapplication.android.ui.core.repo

import com.example.myapplication.android.ui.core.model.Event
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

interface CalendarRepository {
    val events: StateFlow<List<Event>>
    suspend fun loadRange(from: Calendar, to: Calendar) // per API con pagination
    suspend fun upsertEvent(event: Event): Result<Unit>
    suspend fun deleteEvent(eventId: String): Result<Unit>
}