package com.example.myapplication.android.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme
import java.util.*

@Composable
fun MonthlyCalendarGridView(
    monthCalendar: Calendar, // ← cambia qui
    selectedDate: Calendar,
    events: List<CalendarEvent>,
    onDayClick: (Calendar) -> Unit
) {
    val colors = CustomTheme.colors
    val daysInMonth = remember(monthCalendar) {
        val calendar = monthCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val cells = (firstDayOfWeek + totalDays).let {
            if (it % 7 == 0) it else it + (7 - it % 7)
        }

        (0 until cells).map { index ->
            val day = index - firstDayOfWeek + 1
            if (day in 1..totalDays) {
                val dayCalendar = calendar.clone() as Calendar
                dayCalendar.set(Calendar.DAY_OF_MONTH, day)
                dayCalendar
            } else null
        }
    }
    val today = remember { Calendar.getInstance() }


    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(
                    text = it,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(vertical = 4.dp),
                    color = colors.shade950,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        daysInMonth.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {

                week.forEach { day ->
                    val backgroundColor = when {
                        day?.sameDayAs(selectedDate) == true -> colors.shade300 // selezionato
                        day?.sameDayAs(today) == true -> colors.shade50 // giorno corrente
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                backgroundColor
                            )
                            .clickable(enabled = day != null) {
                                if (day != null) onDayClick(day)
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            if (day != null) {
                                val dayEvents = events.filter { event ->
                                    day.let { event.date.sameDayAs(it) }
                                }

                                Text(
                                    text = day.get(Calendar.DAY_OF_MONTH).toString(),
                                    fontSize = 14.sp,
                                    color = colors.shade950
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    dayEvents.sortedBy { it.startTime }.take(5).forEach { event ->
                                    Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .padding(horizontal = 1.dp)
                                                .clip(CircleShape)
                                                .background(event.color)
                                        )
                                    }
                                }
                            }


                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthlyCalendarGridViewPreview() {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2025)
        set(Calendar.MONTH, Calendar.MAY)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val selectedDate = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2025)
        set(Calendar.MONTH, Calendar.MAY)
        set(Calendar.DAY_OF_MONTH, 15)
    }

    val sampleEvents = listOf(
        CalendarEvent(
            startTime = "08:00",
            endTime = "10:00",
            title = "Riunione",
            description = "Discussione progetto",
            color = Color.Red,
            date = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2025)
                set(Calendar.MONTH, Calendar.MAY)
                set(Calendar.DAY_OF_MONTH, 15)
            }
        ),
        CalendarEvent(
            startTime = "14:00",
            endTime = "15:00",
            title = "Chiamata",
            description = "Call con cliente",
            color = Color.Blue,
            date = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2025)
                set(Calendar.MONTH, Calendar.MAY)
                set(Calendar.DAY_OF_MONTH, 18)
            }
        )
    )

    MonthlyCalendarGridView(
        monthCalendar = calendar,
        selectedDate = selectedDate,
        events = sampleEvents,
        onDayClick = {}
    )
}

