package com.example.myapplication.android.ui.components

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
import com.example.myapplication.android.ui.state.getDurationInHours
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
    val participantEvents = userEvents.filter { it.participants.contains(name) }
    val weeklyTotal = participantEvents.sumOf { it.getDurationInHours() }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFFFF3E0) else Color(0xFFFFFFFF))
            .border(0.5.dp, Color(0xFFDDC9A3))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profilo",
                tint = Color(0xFF5D4037),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.width(100.dp)) {
            Text(text = abbreviaNomeCompleto(name), fontSize = 16.sp, color = Color(0xFF5D4037))
            Text(
                text = "$weeklyTotal / $weeklyHours",
                fontSize = 12.sp,
                color = Color(0xFF7D4F16)
            )
        }
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
        nomeCompleto // fallback: restituisce com'è
    }
}

