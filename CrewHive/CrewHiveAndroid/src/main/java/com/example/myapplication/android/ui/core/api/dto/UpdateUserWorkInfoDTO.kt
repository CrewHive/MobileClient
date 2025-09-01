package com.example.myapplication.android.ui.core.api.dto

data class UpdateUserWorkInfoDTO(
    val targetUserId: Long,
    val contractType: String,              // FULL_TIME | PART_TIME_HORIZONTAL | PART_TIME_VERTICAL
    val workableHoursPerWeek: Int? = null, // opzionale
    val overtimeHours: Double? = null,     // opzionale
    val vacationDaysAccumulated: Double? = null,
    val vacationDaysTaken: Double? = null,
    val leaveDaysAccumulated: Double? = null,
    val leaveDaysTaken: Double? = null
)