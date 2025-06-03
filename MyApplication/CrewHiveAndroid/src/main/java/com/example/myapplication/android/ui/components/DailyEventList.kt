package com.example.myapplication.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DailyEventList(
    events: List<CalendarEvent>,
    showParticipants: Boolean = false,
    modifier: Modifier = Modifier,
    onEdit: ((CalendarEvent) -> Unit)? = null
) {
    val sortedEvents = events.sortedBy { it.startTime }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        items(sortedEvents) { event ->
            val alt = if (showParticipants) 140.dp else 80.dp
            CalendarEventItem(
                event = event,
                showParticipants = showParticipants,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(alt),
                onEdit = onEdit
            )
        }
    }
}
