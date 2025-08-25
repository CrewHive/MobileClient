package com.example.myapplication.android.ui.core.repo

import com.example.myapplication.android.state.GlobalParticipants
import com.example.myapplication.android.ui.core.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * Repository in-memory: genera eventi demo su un range richiesto.
 * Facile da sostituire in futuro con un RemoteCalendarRepository (Retrofit/API reali).
 */
class InMemoryCalendarRepository : CalendarRepository {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    override val events: StateFlow<List<Event>> = _events.asStateFlow()

    override suspend fun loadRange(from: Calendar, to: Calendar) {
        val out = mutableListOf<Event>()

        val start = (from.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var cursor = start.clone() as Calendar
        while (!cursor.after(to)) {
            out += generateEventsFor(cursor)
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }
        _events.value = out
    }

    override suspend fun upsertEvent(event: Event): Result<Unit> = runCatching {
        val current = _events.value.toMutableList()
        val idx = current.indexOfFirst { it.id == event.id }
        if (idx >= 0) current[idx] = event else current += event
        _events.value = current
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        _events.value = _events.value.filterNot { it.id == eventId }
    }

    // -------------------- Demo generator (ex CalendarScreen) --------------------

    private fun generateEventsFor(date: Calendar): List<Event> {
        val random = kotlin.random.Random(date.get(Calendar.DAY_OF_YEAR))
        val participants = GlobalParticipants.list

        val events = mutableListOf<Event>()

        // Turni standard
        val shifts = listOf(
            Triple("Turno Mattutino", "08:00", "14:00"),
            Triple("Turno Pomeridiano", "14:00", "20:00"),
            Triple("Turno Serale", "20:00", "02:00")
        )

        val shiftColors = listOf(
            0xFF64B5F6, // blu chiaro
            0xFFFFB74D, // arancione
            0xFFBA68C8  // viola
        ).map { it.toLong() }

        // Rotazione settimanale dei turni
        val weekNumber = date.get(Calendar.WEEK_OF_YEAR)
        val participantsPerShift = (participants.size / 3).coerceAtLeast(1)

        shifts.forEachIndexed { index, (title, startTime, endTime) ->
            val shiftOffset = (weekNumber + index) % 3
            val shiftGroup = participants.shuffled(random)
                .drop(shiftOffset * participantsPerShift)
                .take(participantsPerShift)

            val id = buildString {
                append(date.get(Calendar.YEAR))
                append("-")
                append(date.get(Calendar.DAY_OF_YEAR))
                append("-")
                append(index)
            }

            events.add(
                Event(
                    id = id,
                    date = (date.clone() as Calendar),
                    startTime = startTime,
                    endTime = endTime,
                    title = title,
                    description = "Presidio reparto ${'A' + index}",
                    color = shiftColors[index % shiftColors.size],
                    participants = shiftGroup
                )
            )
        }

        // Evento extra nel weekend (random)
        val isSaturday = date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
        if (isSaturday && random.nextBoolean()) {
            events.add(
                Event(
                    id = "${date.get(Calendar.YEAR)}-${date.get(Calendar.DAY_OF_YEAR)}-X",
                    date = (date.clone() as Calendar),
                    startTime = "10:00",
                    endTime = "12:00",
                    title = "Formazione",
                    description = "Briefing settimanale",
                    color = 0xFF9CCC65.toLong(), // verde
                    participants = participants.shuffled(random).take(3)
                )
            )
        }

        return events
    }
}
