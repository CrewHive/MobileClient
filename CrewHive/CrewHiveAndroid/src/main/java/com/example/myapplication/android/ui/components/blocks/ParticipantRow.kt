package com.example.myapplication.android.ui.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.WeeklyMiniCalendar
import com.example.myapplication.android.ui.state.getDurationInHours
import com.example.myapplication.android.ui.theme.CustomTheme
import java.util.*

@Composable
fun ParticipantRow(
    name: String,
    weeklyHours: Int,
    userEvents: List<CalendarEvent>,
    selectedDate: Calendar,
    miniCalendarWeekStart: Calendar,
    isSelected: Boolean,
    onClick: () -> Unit,
    currentTemplatePreview: CalendarEvent? = null
) {
    val colors = CustomTheme.colors

    // Eventi effettivi dell’utente nella settimana
    val participantEvents = userEvents.filter { it.participants.contains(name) }
    val weeklyTotal = participantEvents.sumOf { it.getDurationInHours() }

    // Se la riga è selezionata e c’è il "preview" dello shift, somma la durata del preview
    val extraHours = if (isSelected && currentTemplatePreview != null) {
        currentTemplatePreview.getDurationInHours()
    } else 0
    val displayTotal = weeklyTotal + extraHours

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.shade100 else colors.background)
            .border(0.5.dp, colors.shade200)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profilo",
                tint = colors.shade950,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.width(100.dp)) {
            Text(text = abbreviaNomeCompleto(name), fontSize = 16.sp, color = colors.shade950)
            Text(
                text = "$displayTotal / $weeklyHours",
                fontSize = 12.sp,
                color = if (displayTotal <= weeklyHours) colors.shade800 else colors.error
            )
        }

        // Mostra il mini-calendario includendo il preview se selezionato
        val previewEvent = if (isSelected && currentTemplatePreview != null) {
            currentTemplatePreview.copy(participants = listOf(name))
        } else null

        WeeklyMiniCalendar(
            events = participantEvents + listOfNotNull(previewEvent),
            highlightColor = previewEvent?.color,
            selectedDate = selectedDate,
            weekStart = miniCalendarWeekStart,
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
        )
    }
}

fun abbreviaNomeCompleto(nomeCompleto: String): String {
    val parts = nomeCompleto.trim().split(" ")
    return if (parts.size >= 2) {
        val inizialeNome = parts[0].firstOrNull()?.uppercaseChar() ?: ""
        val cognome = parts.subList(1, parts.size).joinToString(" ")
        "$inizialeNome. $cognome"
    } else {
        nomeCompleto
    }
}
