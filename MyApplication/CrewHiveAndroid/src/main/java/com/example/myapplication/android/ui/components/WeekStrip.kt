package com.example.myapplication.android.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeekStrip(
    selectedDate: Calendar,
    initialWeekStart: Calendar,
    weekOffset: Int,
    direction: Int,
    onDateSelected: (Calendar, Int) -> Unit,
    onSwipeWeek: (Int) -> Unit
) {
    val sdfDayName = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val today = remember { Calendar.getInstance() }

    val threshold = 50f
    var cumulativeDrag by remember { mutableStateOf(0f) }

    val startDate = remember(initialWeekStart.timeInMillis, weekOffset) {
        (initialWeekStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, weekOffset * 7)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF7C7))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        cumulativeDrag += dragAmount
                    },
                    onDragEnd = {
                        when {
                            cumulativeDrag < -threshold -> onSwipeWeek(1)
                            cumulativeDrag > threshold -> onSwipeWeek(-1)
                        }
                        cumulativeDrag = 0f
                    }
                )
            }
    ) {
        AnimatedContent(
            targetState = weekOffset,
            transitionSpec = {
                (slideInHorizontally { width -> direction * width } + fadeIn())
                    .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
            },
            label = "Week Animation"
        ) { _ ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..6) {
                    val dayCalendar = (startDate.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_MONTH, i)
                    }

                    val isSelected = dayCalendar.sameDayAs(selectedDate)
                    val isToday = dayCalendar.sameDayAs(today)

                    Column(
                        modifier = Modifier
                            .width(48.dp)
                            .clickable {
                                val newDirection = when {
                                    dayCalendar.before(selectedDate) -> -1
                                    dayCalendar.after(selectedDate) -> 1
                                    else -> 0
                                }
                                onDateSelected(dayCalendar, newDirection)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .background(
                                    when {
                                        isSelected -> Color(0xFFF0D954)
                                        isToday -> Color(0xFFE0E0E0)
                                        else -> Color.Transparent
                                    }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = sdfDayName.format(dayCalendar.time),
                                color = Color(0xFF5D4037),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 5.dp, start = 5.dp, end = 5.dp)
                            )
                            Text(
                                text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString(),
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(bottom = 5.dp, start = 5.dp, end = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}




// 🔧 Estensioni

fun Calendar.sameDayAs(other: Calendar): Boolean {
    return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            this.get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            this.get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
}

fun Calendar.cloneAndAddDays(days: Int): Calendar =
    (this.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, days) }

fun Calendar.sameWeekAs(other: Calendar): Boolean {
    return this.get(Calendar.WEEK_OF_YEAR) == other.get(Calendar.WEEK_OF_YEAR) &&
            this.get(Calendar.YEAR) == other.get(Calendar.YEAR)
}
