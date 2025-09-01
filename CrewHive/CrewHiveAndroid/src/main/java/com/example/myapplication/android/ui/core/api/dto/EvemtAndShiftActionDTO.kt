package com.example.myapplication.android.ui.core.api.dto

// ---------- EVENT ----------
data class CreateEventDTO(
    val name: String,
    val description: String? = null,
    val start: String,          // ISO_OFFSET_DATE_TIME
    val end: String,            // ISO_OFFSET_DATE_TIME
    val color: String,          // "RRGGBB"
    val eventType: String,      // "PUBLIC" | "PRIVATE"
    val userId: List<Long>? = null
)

data class PatchEventDTO(
    val eventId: Long,
    val name: String,
    val description: String? = null,
    val start: String,          // ISO_OFFSET_DATE_TIME
    val end: String,            // ISO_OFFSET_DATE_TIME
    val color: String,          // "RRGGBB"
    val eventType: String,      // "PUBLIC" | "PRIVATE"
    val userId: List<Long>? = null
)

// ---------- SHIFT PROGRAMMED ----------
data class CreateShiftProgrammedDTO(
    val name: String,
    val description: String? = null,
    val start: String,          // ISO_OFFSET_DATE_TIME
    val end: String,            // ISO_OFFSET_DATE_TIME
    val color: String,          // "RRGGBB"
    val userId: List<Long>? = null
)

data class PatchShiftProgrammedDTO(
    val shiftProgrammedId: Long,
    val name: String,
    val description: String? = null,
    val start: String,          // ISO_OFFSET_DATE_TIME
    val end: String,            // ISO_OFFSET_DATE_TIME
    val color: String,          // "RRGGBB"
    val userId: List<Long>? = null
)