package com.example.myapplication.android.ui.core.mappers

import EventDTO
import EventUserDTO
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import com.example.myapplication.android.ui.core.api.dto.CreateEventDTO
import com.example.myapplication.android.ui.core.api.dto.CreateShiftProgrammedDTO
import com.example.myapplication.android.ui.core.api.dto.PatchEventDTO
import com.example.myapplication.android.ui.core.api.dto.PatchShiftProgrammedDTO
import com.example.myapplication.android.ui.core.api.dto.ShiftProgrammedDTO
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.graphics.Color as AndroidColor

object EventMapper {

    // --------- PUBLIC MAPPERS ---------


    @RequiresApi(Build.VERSION_CODES.O)
    fun fromEvent(dto: EventDTO): CalendarEvent = CalendarEvent(
        id = dto.eventId,
        title = dto.eventName,
        description = dto.description,
        startTime = isoToHm(dto.start),
        endTime = isoToHm(dto.end),
        color = parseColorSafe(dto.color, fallback = DEFAULT_EVENT_COLOR),
        date = isoToDayCalendar(dto.start),
        participants = participantNamesFromEventUsers(dto.users), // ⬅️ qui
        kind = CalendarItemKind.EVENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromShift(dto: ShiftProgrammedDTO): CalendarEvent = CalendarEvent(
        id = dto.shiftProgrammedId ?: -1L,
        title = (dto.title ?: dto.shiftName ?: "Shift"),
        description = dto.description,
        startTime = isoToHm(dto.start.toString()),
        endTime = isoToHm(dto.end.toString()),
        color = parseColorSafe(dto.color, fallback = DEFAULT_SHIFT_COLOR),
        date = isoToDayCalendar(dto.start.toString()),
        participants = participantNamesFromEventUsers(dto.users), // ⬅️ qui
        kind = CalendarItemKind.SHIFT
    )


    // --------- DATE HELPERS ---------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isoToHm(iso: String): String {
        val odt = java.time.OffsetDateTime.parse(iso)
        val local = odt.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalTime()
        return "%02d:%02d".format(local.hour, local.minute)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isoToDayCalendar(iso: String): Calendar {
        val odt = java.time.OffsetDateTime.parse(iso)
        val localDate = odt.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDate()
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, localDate.year)
            set(Calendar.MONTH, localDate.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    // --------- COLOR UTILS ---------

    private val DEFAULT_SHIFT_COLOR = Color(0xFFBA68C8) // viola "shift"
    private val DEFAULT_EVENT_COLOR = Color(0xFF64B5F6) // blu "eventi"

    /** Converte varie rappresentazioni di colore (hex, 0x..., rgb/rgba, nomi) in Compose Color, con fallback. */
    fun parseColorSafe(raw: String?, fallback: Color = DEFAULT_SHIFT_COLOR): Color {
        if (raw.isNullOrBlank()) return fallback
        val s = raw.trim()

        // 1) rgb/rgba(r,g,b[,a])
        val rgba = Regex("""rgba?\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})(?:\s*,\s*([01]?\.?\d*))?\s*\)""", RegexOption.IGNORE_CASE)
        rgba.matchEntire(s)?.let { m ->
            val r = m.groupValues[1].toInt().coerceIn(0, 255)
            val g = m.groupValues[2].toInt().coerceIn(0, 255)
            val b = m.groupValues[3].toInt().coerceIn(0, 255)
            val a = m.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.toFloatOrNull()?.coerceIn(0f, 1f) ?: 1f
            return Color(r / 255f, g / 255f, b / 255f, a)
        }

        // 2) numeri decimali (es. "16711680"): prova a costruire direttamente un ARGB Int
        s.toLongOrNull(10)?.let { num ->
            return Color(num.toInt())
        }

        // 3) hex con/ senza prefissi (#, 0x)
        var hex = s.removePrefix("0x").removePrefix("0X")
        if (!hex.startsWith("#")) hex = "#$hex"

        val hexNoHash = hex.removePrefix("#")
        if (hexNoHash.length == 8) {
            // prova diretto (#AARRGGBB?) — se ok, ritorna
            runCatching { return Color(AndroidColor.parseColor("#$hexNoHash")) }
            // altrimenti prova conversione RGBA -> AARRGGBB
            val a = hexNoHash.substring(6, 8)
            val rgb = hexNoHash.substring(0, 6)
            val aarrggbb = "#$a$rgb"
            runCatching { return Color(AndroidColor.parseColor(aarrggbb)) }
        }

        // 4) prova diretta (#RGB/#ARGB/#RRGGBB/#AARRGGBB o nomi CSS standard)
        return runCatching { Color(AndroidColor.parseColor(hex)) }
            .getOrElse { fallback }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val ISO_FMT: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    /** Compose Color -> "RRGGBB" (senza alpha) */
    fun colorTo6Hex(c: Color): String {
        val argb = c.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = (argb) and 0xFF
        return "%02X%02X%02X".format(r, g, b)
    }

    /** (Calendar + "HH:mm") -> ISO con offset locale */
    @RequiresApi(Build.VERSION_CODES.O)
    fun hmToIso(date: Calendar, hm: String): String {
        val (h, m) = hm.split(":").map { it.toInt() }
        val y = date.get(Calendar.YEAR)
        val mo = date.get(Calendar.MONTH) + 1
        val d = date.get(Calendar.DAY_OF_MONTH)
        val zdt = ZonedDateTime.of(y, mo, d, h, m, 0, 0, ZoneId.systemDefault())
        return zdt.format(ISO_FMT)
    }

    // ---------- EVENT ----------
    @RequiresApi(Build.VERSION_CODES.O)
    fun toCreateEventDTO(
        ev: CalendarEvent,
        eventType: String = "PRIVATE",
        userIds: List<Long>? = null
    ): CreateEventDTO = CreateEventDTO(
        name = ev.title,
        description = ev.description,
        start = hmToIso(ev.date, ev.startTime),
        end = hmToIso(ev.date, ev.endTime),
        color = colorTo6Hex(ev.color),
        eventType = eventType,
        userId = userIds
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun toPatchEventDTO(
        ev: CalendarEvent,
        eventType: String = "PRIVATE",
        userIds: List<Long>? = null
    ): PatchEventDTO = PatchEventDTO(
        eventId = ev.id,
        name = ev.title,
        description = ev.description,
        start = hmToIso(ev.date, ev.startTime),
        end = hmToIso(ev.date, ev.endTime),
        color = colorTo6Hex(ev.color),
        eventType = eventType,
        userId = userIds
    )

    // ---------- SHIFT ----------
    @RequiresApi(Build.VERSION_CODES.O)
    fun toCreateShiftDTO(
        shift: CalendarEvent,
        assignedUserIds: List<Long>? = null
    ): CreateShiftProgrammedDTO = CreateShiftProgrammedDTO(
        name = shift.title,
        description = shift.description,
        start = hmToIso(shift.date, shift.startTime),
        end = hmToIso(shift.date, shift.endTime),
        color = colorTo6Hex(shift.color),
        userId = assignedUserIds
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun toPatchShiftDTO(
        shift: CalendarEvent,
        assignedUserIds: List<Long>? = null
    ): PatchShiftProgrammedDTO = PatchShiftProgrammedDTO(
        shiftProgrammedId = shift.id,
        name = shift.title,
        description = shift.description,
        start = hmToIso(shift.date, shift.startTime),
        end = hmToIso(shift.date, shift.endTime),
        color = colorTo6Hex(shift.color),
        userId = assignedUserIds
    )



    private fun participantNamesFromEventUsers(list: List<EventUserDTO>?): List<String> =
        list?.mapNotNull { eu ->
            eu.user?.username?.takeIf { it.isNotBlank() }
                ?: eu.id?.userId?.toString()
        } ?: emptyList()


}
