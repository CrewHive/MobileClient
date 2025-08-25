package com.example.myapplication.android.ui.core.model

data class Employee(
    val userId: String,
    val displayName: String,
    val contractType: ContractType? = null,
    val weeklyHours: Int = 40,
    val overtimeHours: Int = 0,
    val vacationDaysAccumulated: Float = 0f,
    val vacationDaysUsed: Float = 0f,
    val leaveDaysAccumulated: Float = 0f,
    val leaveDaysUsed: Float = 0f
)
enum class ContractType { FULL_TIME, PART_TIME_HORIZONTAL, PART_TIME_VERTICAL }
