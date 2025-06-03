// FILE: TodaySectionComponent.kt
package com.example.myapplication.android.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import com.example.myapplication.android.state.LocalCalendarState
import com.example.myapplication.android.state.LocalCurrentUser
import com.example.myapplication.android.ui.screens.generateEventsFor
import com.example.myapplication.android.ui.theme.CustomTheme
import java.util.*

object TodaySectionComponent {

    @Composable
    fun TodaySection() {
        val colors = CustomTheme.colors

        val calendarState = LocalCalendarState.current
        val currentUser = LocalCurrentUser.current
        val today = remember { Calendar.getInstance() }

        val generated = generateEventsFor(today)
        val userEvents = calendarState.userEvents.filter { it.date.sameDayAs(today) }

        val filteredGenerated = generated.filter { gen ->
            userEvents.none {
                it.title == gen.title &&
                        it.startTime == gen.startTime &&
                        it.endTime == gen.endTime &&
                        it.date.sameDayAs(gen.date)
            }
        }

        val allEvents = userEvents + filteredGenerated

        val visibleEvents = allEvents.filter { it.participants.contains(currentUser.value) }


        Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Oggi",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = colors.shade950
            )

            Spacer(modifier = Modifier.height(8.dp))

            visibleEvents.sortedBy { it.startTime }.forEach {
                CalendarEventItem1(it, modifier = Modifier.height(120.dp).padding(bottom = 8.dp))
            }
        }
    }

    @Composable
    fun CalendarEventItem1(
        event: CalendarEvent,
        modifier: Modifier = Modifier
    ) {
        val colors = CustomTheme.colors

        Box(modifier = modifier) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(
                        color = event.color,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    )
                    .zIndex(1f)
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .border(6.dp, colors.shade100, RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(20.dp, 8.dp, 6.dp, 6.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = event.title,
                        fontSize = 16.sp,
                        color = colors.shade950
                    )
                    Text(
                        text = "${event.startTime} - ${event.endTime}",
                        fontSize = 12.sp,
                        color = colors.shade800.copy(alpha = 0.84f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.description,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
