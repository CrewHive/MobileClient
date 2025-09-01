package com.example.myapplication.android.ui.core.api.dto

import com.google.gson.annotations.SerializedName

// CreateShiftTemplateDTO.kt
data class CreateShiftTemplateDTO(
    @SerializedName("shiftName") val shiftName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("color") val color: String,       // "RRGGBB"
    @SerializedName("start") val start: String,       // <-- chiave JSON: start
    @SerializedName("end")   val end: String,         // <-- chiave JSON: end
    @SerializedName("companyId") val companyId: Int
)


data class PatchShiftTemplateDTO(
    val shiftName: String,
    val description: String,
    /** Hex senza #, 6 caratteri */
    val color: String,
    val start: String,
    val end: String,
    val companyId: Long,
    /** Nome precedente del template da rinominare */
    val oldShiftName: String
)

/** Risposta backend */
data class ShiftTemplateDTO(
    val shiftId: Long,
    val shiftName: String,
    /** "HH:mm" */
    val start: String,
    /** "HH:mm" */
    val end: String,
    val description: String?,
    /** Hex senza #, 6 caratteri */
    val color: String
)
