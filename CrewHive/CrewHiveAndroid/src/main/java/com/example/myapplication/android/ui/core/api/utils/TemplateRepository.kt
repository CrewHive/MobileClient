package com.example.myapplication.android.ui.core.api.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate as UiShiftTemplate
import com.example.myapplication.android.ui.core.api.dto.CreateShiftTemplateDTO
import com.example.myapplication.android.ui.core.api.dto.PatchShiftTemplateDTO
import com.example.myapplication.android.ui.core.api.dto.ShiftTemplateDTO
import com.example.myapplication.android.ui.core.api.service.ApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.runCatching

class TemplateRepository(
    private val api: ApiService
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun create(companyId: Long, tpl: UiShiftTemplate): Result<UiShiftTemplate> = runCatching {
        api.createShiftTemplate(tpl.toCreateDto(companyId)).toUi()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun update(companyId: Long, oldName: String, tpl: UiShiftTemplate): Result<UiShiftTemplate> = runCatching {
        api.updateShiftTemplate(tpl.toPatchDto(companyId, oldName)).toUi()
    }

    suspend fun get(companyId: Long, name: String): Result<UiShiftTemplate> = runCatching {
        api.getShiftTemplate(name, companyId).toUi()
    }

    suspend fun delete(companyId: Long, name: String): Result<Unit> = runCatching {
        api.deleteShiftTemplate(name, companyId)
    }

}

/* ---------- MAPPERS ---------- */


@RequiresApi(Build.VERSION_CODES.O)
private fun UiShiftTemplate.toCreateDto(companyId: Long) = CreateShiftTemplateDTO(
    shiftName   = title.trim(),
    description = description.orEmpty().trim(),
    color       = color.toHex6(),                 // "RRGGBB"
    start        = hmToIsoLocalOffsetNoMillis(startTime.trim(), LocalDate.now()),
    end          = hmToIsoLocalOffsetNoMillis(endTime.trim(),   LocalDate.now()),
    companyId   = companyId.toInt()
)

@RequiresApi(Build.VERSION_CODES.O)
private fun UiShiftTemplate.toPatchDto(companyId: Long, oldName: String) = PatchShiftTemplateDTO(
    shiftName    = title.trim(),
    description  = description.orEmpty().trim(),
    color        = color.toHex6(),
    start        = hmToIsoLocalOffsetNoMillis(startTime.trim(), LocalDate.now()),
    end          = hmToIsoLocalOffsetNoMillis(endTime.trim(),   LocalDate.now()),
    companyId    = companyId,
    oldShiftName = oldName
)

private fun ShiftTemplateDTO.toUi() = UiShiftTemplate(
    title       = shiftName,
    startTime   = start,    // il server risponde in questi campi
    endTime     = end,
    color       = color.hexToColor(),
    description = description.orEmpty()
)

/* ---------- COLOR HELPERS ---------- */

/** Restituisce "RRGGBB" (senza #, uppercase) dalla Color Compose */
private fun Color.toHex7(): String = "#%06X".format(toArgb() and 0x00FFFFFF)

fun Color.toHex6(): String {
    val argb = this.toArgb()
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = (argb) and 0xFF
    return "%02X%02X%02X".format(r, g, b)
}

/** Converte "RRGGBB" o "#RRGGBB" in Color(0xFFRRGGBB) */
private fun String.hexToColor(): Color {
    val clean = this.trim().removePrefix("#")
    val rgb = clean.toLong(16).toInt() and 0x00FFFFFF
    val argb = 0xFF000000.toInt() or rgb
    return Color(argb)
}

private fun String.normalizeTimeToSeconds(): String =
    when (length) {
        5  -> this + ":00"       // "09:00" -> "09:00:00"
        8  -> this               // già "HH:mm:ss"
        else -> runCatching {
            // “0900” o “900” ecc.
            val d = filter(Char::isDigit)
            val hh = d.padEnd(2, '0').substring(0, 2)
            val mm = d.padEnd(4, '0').substring(2, 4)
            val ss = if (d.length >= 6) d.substring(4, 6) else "00"
            "$hh:$mm:$ss"
        }.getOrDefault("00:00:00")
    }


@RequiresApi(Build.VERSION_CODES.O)
private fun hmToIsoLocalOffsetNoMillis(hm: String, date: LocalDate = LocalDate.now()): String {
    val parts = hm.trim().split(":")
    require(parts.size >= 2) { "Orario non valido: $hm" }
    val h = parts[0].toInt()
    val m = parts[1].toInt()
    val s = if (parts.size >= 3) parts[2].toInt() else 0

    val zone = ZoneId.systemDefault()
    val ldt = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, h, m, s)
    val offset = ldt.atZone(zone).offset
    val total = kotlin.math.abs(offset.totalSeconds)
    val sign = if (offset.totalSeconds >= 0) "+" else "-"
    val oh = total / 3600
    val om = (total % 3600) / 60

    // yyyy-MM-dd'T'HH:mm:ss+HH:mm (senza .000)
    return String.format(
        "%04d-%02d-%02dT%02d:%02d:%02d%s%02d:%02d",
        date.year, date.monthValue, date.dayOfMonth,
        h, m, s,
        sign, oh, om
    )
}
@RequiresApi(Build.VERSION_CODES.O)
private val DATE_TIME_NO_MS_WITH_OFFSET: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")