package com.example.myapplication.android.ui.core.api.dto

data class UserWithTimeParamsDTO(
    val userId: Long,
    val username: String,
    val email: String,
    val companyName: String?,            // può essere null se non associato
    val workableHoursPerWeek: Int?,      // <-- questo è il campo che usi in Home/Profile
    val overtimeHours: Double?,          // numerici come da schema OpenAPI
    val vacationDaysAccumulated: Double?,
    val vacationDaysTaken: Double?,
    val leaveDaysAccumulated: Double?,
    val leaveDaysTaken: Double?
)