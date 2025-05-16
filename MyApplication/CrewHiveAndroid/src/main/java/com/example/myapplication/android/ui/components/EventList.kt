package com.example.myapplication.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Alignment

data class CalendarEvent(
    val startTime: String,
    val endTime: String,
    val title: String,
    val description: String,
    val color: Color
)

data class PositionedEvent(
    val event: CalendarEvent,
    val columnIndex: Int,
    val totalColumns: Int
)

private val hourRange = 6..22
private val hourHeight = 80.dp

@Composable
fun EventList(events: List<CalendarEvent>) {
    val scrollState = rememberScrollState()
    val positionedEvents = resolveEventPositionsWithGrouping(events)
    val density = LocalDensity.current

    val totalHeight = hourHeight * hourRange.count()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        val containerWidth = (constraints.maxWidth - with(density) { 40.dp.toPx() })

        Row(modifier = Modifier.height(totalHeight)) {

            // COLONNA ORARIA (08:00, 09:00, ...)
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
            ) {
                hourRange.forEach { hour ->
                    Box(
                        modifier = Modifier
                            .height(hourHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "${"%02d".format(hour)}:00",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // COLONNA EVENTI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight)
            ) {
                // LINEE ORARIE DI SFONDO (sotto agli eventi)
                hourRange.forEach { hour ->
                    Box(
                        modifier = Modifier
                            .offset(y = hourHeight * (hour - hourRange.first))
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )
                }

                // EVENTI POSIZIONATI
                positionedEvents.forEach { positioned ->
                    val (startMin, durationMin) = calculateDuration(positioned.event)
                    val topOffsetDp = ((startMin - hourRange.first * 60) / 60f) * hourHeight.value
                    val heightDp = (durationMin / 60f) * hourHeight.value
                    val topOffset = with(density) { topOffsetDp.dp }
                    val height = with(density) { heightDp.dp }

                    val columnWidth = containerWidth / positioned.totalColumns
                    val xOffsetPx = positioned.columnIndex * columnWidth
                    val xOffset = with(density) { xOffsetPx.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = topOffset)
                            .width(with(density) { columnWidth.toDp() })
                            .height(height.coerceAtLeast(32.dp)) // altezza minima evento
                            .padding(1.dp)
                    ) {
                        CalendarEventItem(
                            event = positioned.event,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


private fun resolveEventPositionsWithGrouping(events: List<CalendarEvent>): List<PositionedEvent> {
    val visited = mutableSetOf<CalendarEvent>()
    val result = mutableListOf<PositionedEvent>()

    for (event in events) {
        if (visited.contains(event)) continue

        val group = mutableListOf<CalendarEvent>()
        collectConnectedEvents(event, events, visited, group)

        // Calcolo colonne per questo gruppo
        val groupWindows = group.map { it to calculateTimeWindow(it) }
            .sortedBy { it.second.first }

        val columns = mutableListOf<MutableList<Pair<CalendarEvent, Pair<Int, Int>>>>()

        for ((e, window) in groupWindows) {
            var placed = false

            for ((colIdx, col) in columns.withIndex()) {
                if (col.none { overlap(it.second, window) }) {
                    col.add(e to window)
                    result.add(PositionedEvent(e, colIdx, 0)) // temp totalColumns
                    placed = true
                    break
                }
            }

            if (!placed) {
                columns.add(mutableListOf(e to window))
                result.add(PositionedEvent(e, columns.lastIndex, 0))
            }
        }

        // Ora correggo totalColumns per tutti gli eventi del gruppo
        result.replaceAll { positioned ->
            if (group.contains(positioned.event)) {
                positioned.copy(totalColumns = columns.size)
            } else positioned
        }
    }

    return result
}

private fun collectConnectedEvents(
    current: CalendarEvent,
    all: List<CalendarEvent>,
    visited: MutableSet<CalendarEvent>,
    group: MutableList<CalendarEvent>
) {
    visited.add(current)
    group.add(current)

    for (other in all) {
        if (!visited.contains(other) && overlap(current, other)) {
            collectConnectedEvents(other, all, visited, group)
        }
    }
}

private fun calculateDuration(event: CalendarEvent): Pair<Int, Float> {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return try {
        val start = format.parse(event.startTime)
        val end = format.parse(event.endTime)
        val startCal = Calendar.getInstance().apply { time = start!! }
        val endCal = Calendar.getInstance().apply { time = end!! }

        val startMinutes = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
        val endMinutes = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)

        val duration = (endMinutes - startMinutes).coerceAtLeast(15)
        startMinutes to duration.toFloat()
    } catch (e: Exception) {
        0 to 60f
    }
}

private fun calculateTimeWindow(event: CalendarEvent): Pair<Int, Int> {
    val (start, dur) = calculateDuration(event)
    return start to (start + dur.roundToInt())
}

private fun overlap(e1: CalendarEvent, e2: CalendarEvent, margin: Int = 1): Boolean {
    val (s1, e1end) = calculateTimeWindow(e1)
    val (s2, e2end) = calculateTimeWindow(e2)
    return !(e1end <= s2 + margin || e2end <= s1 + margin)
}

private fun overlap(a: Pair<Int, Int>, b: Pair<Int, Int>, margin: Int = 1): Boolean {
    return !(a.second <= b.first + margin || b.second <= a.first + margin)
}
