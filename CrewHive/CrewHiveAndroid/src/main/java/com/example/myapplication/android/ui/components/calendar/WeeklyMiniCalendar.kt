package com.example.myapplication.android.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeeklyMiniCalendar(
    weekStart: Calendar,
    events: List<CalendarEvent>,
    selectedDate: Calendar,
    highlightColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val colors = CustomTheme.colors
    val dayFormatter = SimpleDateFormat("E", Locale.getDefault())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (0..6).forEach { offset ->
            val day = (weekStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, offset)
            }

            val isSelectedDay = day.sameDayAs(selectedDate)

            val dayLabel = SimpleDateFormat("EEEEE", Locale("it", "IT"))
                .format(day.time).uppercase().first().toString()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .background(
                        if (isSelectedDay) colors.shade300 else Color.Transparent, // Evidenziato
                        RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 2.dp)
            ) {
                Text(dayLabel, fontSize = 12.sp, color = colors.shade950)
                MiniDayBar(day = day, events = events, highlightColor = highlightColor)
            }
        }
    }

}

@Composable
fun MiniDayBar(
    day: Calendar,
    events: List<CalendarEvent>,
    highlightColor: Color?
) {
    val slots = listOf(
        6 to 14,
        14 to 22,
        22 to 6 // attenzione: questa fascia è "notturna"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        slots.forEach { (start, end) ->
            val slotEvents = events.filter { event ->
                val eventStart = parseTimeToMinutes(event.startTime)
                val eventEnd = parseTimeToMinutes(event.endTime)
                val dayCheck = when {
                    start == 22 -> {
                        // Fascia 22–06: includi eventi del giorno corrente (dalle 22) e del giorno dopo (fino alle 6)
                        event.date.sameDayAs(day) || event.date.sameDayAs((day.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) })
                    }
                    else -> {
                        event.date.sameDayAs(day)
                    }
                }


                dayCheck && overlapsWithSlot(eventStart, eventEnd, start, end)
            }

            val dominantColor = slotEvents.maxByOrNull { durationInSlot(it, start, end) }?.color
            val slotWidth = Modifier
                .height(10.dp) // più alta
                .width(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(dominantColor ?: Color.LightGray.copy(alpha = 0.3f))

            Box(modifier = slotWidth)
        }

        // Highlight preview (es. turno selezionato)
        highlightColor?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(it)
            )
        }
    }
}

fun parseTimeToMinutes(time: String): Int {
    val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
    return (parts.getOrElse(0) { 0 } * 60) + parts.getOrElse(1) { 0 }
}

fun overlapsWithSlot(start: Int, end: Int, slotStart: Int, slotEnd: Int): Boolean {
    val slotStartMin = slotStart * 60
    val slotEndMin = if (slotEnd > slotStart) slotEnd * 60 else (24 * 60 + slotEnd * 60)
    val eventStart = start
    val eventEnd = if (end > start) end else (24 * 60 + end)
    return eventStart < slotEndMin && eventEnd > slotStartMin
}

fun durationInSlot(event: CalendarEvent, slotStart: Int, slotEnd: Int): Int {
    val start = parseTimeToMinutes(event.startTime)
    val end = parseTimeToMinutes(event.endTime).let {
        if (it <= start) it + 24 * 60 else it
    }

    val slotStartMin = slotStart * 60
    val slotEndMin = if (slotEnd > slotStart) slotEnd * 60 else (24 * 60 + slotEnd * 60)

    return (minOf(end, slotEndMin) - maxOf(start, slotStartMin)).coerceAtLeast(0)
}


