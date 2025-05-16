package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.ui.components.*
import androidx.compose.animation.*
import java.util.*

@Composable
fun CalendarScreen() {
    val initialWeekStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var direction by remember { mutableStateOf(0) }

    val threshold = 50f
    var cumulativeDrag by remember { mutableStateOf(0f) }

    // 🔁 weekOffset calcolato dinamicamente
    val weekOffset = remember(selectedDate.timeInMillis) {
        calculateWeekOffset(initialWeekStart, selectedDate)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAF7C7))
                    .padding(horizontal = 10.dp)
            ) {
                TopBarWithDate(selectedDate = selectedDate)
                Spacer(modifier = Modifier.height(16.dp))

                WeekStrip(
                    selectedDate = selectedDate,
                    initialWeekStart = initialWeekStart,
                    weekOffset = weekOffset,
                    direction = direction,
                    onDateSelected = { newDate, newDirection ->
                        direction = newDirection
                        selectedDate = newDate
                    },
                    onSwipeWeek = { swipeDirection ->
                        direction = swipeDirection
                        selectedDate = selectedDate.cloneAndAddDays(7 * swipeDirection)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                cumulativeDrag += dragAmount
                            },
                            onDragEnd = {
                                val newDate = when {
                                    cumulativeDrag < -threshold -> {
                                        direction = 1
                                        selectedDate.cloneAndAddDays(1)
                                    }
                                    cumulativeDrag > threshold -> {
                                        direction = -1
                                        selectedDate.cloneAndAddDays(-1)
                                    }
                                    else -> selectedDate
                                }

                                if (!newDate.sameDayAs(selectedDate)) {
                                    selectedDate = newDate
                                }
                                cumulativeDrag = 0f
                            }
                        )
                    }
            ) {
                AnimatedContent(
                    targetState = selectedDate.timeInMillis,
                    transitionSpec = {
                        (slideInHorizontally { width -> direction * width } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                    },
                    label = "Day Animation"
                ) { _ ->
                    val events = generateEventsFor(selectedDate)
                    EventList(events = events)
                }
            }
        }

        FloatingAddButton(onClick = {
            // TODO: open add event dialog
        })
    }
}

fun generateEventsFor(date: Calendar): List<CalendarEvent> {
    val random = kotlin.random.Random(date.get(Calendar.DAY_OF_YEAR))

    val possibleColors = listOf(
        Color(0xFF81C784), // green
        Color(0xFF64B5F6), // blue
        Color(0xFFFFB74D), // orange
        Color(0xFFBA68C8), // purple
        Color(0xFFE57373)  // red
    )
    val titles = listOf("Meeting", "Workout", "Review", "Design", "Call", "Sync", "Interview", "Briefing")
    val descriptions = listOf("Team sync", "Strategy", "Follow up", "Feature talk", "Bug review", "Demo prep")

    val events = mutableListOf<CalendarEvent>()

    // Genera tra 2 e 20 eventi
    val count = 2 + random.nextInt(10)

    repeat(count) {
        val startHour = 6 + random.nextInt(16) // 6–21
        val startMinute = listOf(0, 15, 30, 45).random(random)
        val durationMinutes = listOf(30, 45, 60, 90, 120).random(random)
        val endMinutesTotal = startHour * 60 + startMinute + durationMinutes

        val endHour = endMinutesTotal / 60
        val endMinute = endMinutesTotal % 60

        // Blocca evento entro le 22:00
        if (endHour > 22 || (endHour == 22 && endMinute > 0)) return@repeat

        val format = { h: Int, m: Int -> "%02d:%02d".format(h, m) }

        val startTime = format(startHour, startMinute)
        val endTime = format(endHour, endMinute)
        val color = possibleColors.random(random)
        val title = titles.random(random)
        val description = descriptions.random(random)

        events.add(CalendarEvent(startTime, endTime, title, description, color))
    }

    return events
}


fun calculateWeekOffset(start: Calendar, target: Calendar): Int {
    val millisPerWeek = 1000L * 60 * 60 * 24 * 7
    return ((target.timeInMillis - start.timeInMillis) / millisPerWeek).toInt()
}

fun Calendar.cloneAndAddDays(days: Int): Calendar =
    (this.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, days) }

fun Calendar.sameDayAs(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
}
