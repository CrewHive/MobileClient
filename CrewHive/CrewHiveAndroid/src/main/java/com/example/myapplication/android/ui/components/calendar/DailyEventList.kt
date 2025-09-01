package com.example.myapplication.android.ui.components.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyEventList(
    events: List<CalendarEvent>,
    showParticipants: Boolean = false,
    modifier: Modifier = Modifier,
    onEdit: ((CalendarEvent) -> Unit)? = null,
    onDelete: ((CalendarEvent) -> Unit)? = null
) {
    val sortedEvents = events.sortedBy { it.startTime }
    val hasActions = (onEdit != null || onDelete != null)
    var pendingDelete by remember { mutableStateOf<CalendarEvent?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        items(sortedEvents, key = { it.id }) { event ->
            val alt = if (showParticipants) 140.dp else 80.dp

            val baseMod = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(alt)

            val clickableMod = if (hasActions) {
                baseMod.combinedClickable(
                    onClick = { onEdit?.invoke(event) },
                    onLongClick = { if (onDelete != null) pendingDelete = event }
                )
            } else {
                baseMod // nessun click/long-press → nessun dialog/bottone
            }

            Box(clickableMod) {
                CalendarEventItem(
                    event = event,
                    showParticipants = showParticipants,
                    modifier = Modifier.fillMaxSize(),
                    onEdit = null  // niente pulsanti interni
                )
            }
        }
    }

    if (hasActions && pendingDelete != null) {  // ← mostra il dialog solo se servono azioni
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Elimina evento") },
            text = { Text("Vuoi davvero eliminare questo elemento?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete?.let { onDelete?.invoke(it) }
                    pendingDelete = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Annulla") }
            }
        )
    }
}
