package com.example.myapplication.android.ui.state


enum class EmployeeContractType {
    FULL_TIME,
    PART_TIME_HORIZONTAL,
    PART_TIME_VERTICAL;

    val displayName: String get() = when (this) {
        FULL_TIME -> "Full time"
        PART_TIME_HORIZONTAL -> "Part-time orizzontale"
        PART_TIME_VERTICAL -> "Part-time verticale"
    }
}



data class CompanyEmployee(
    val userId: String,
    val name: String,
    val contractType: ContractType?,     // <- usa questo enum
    val weeklyHours: Int,
    val overtimeHours: Int,
    val vacationDaysAccumulated: Float,
    val vacationDaysUsed: Float,
    val leaveDaysAccumulated: Float,
    val leaveDaysUsed: Float
) {
    enum class ContractType {
        FULL_TIME,
        PART_TIME_HORIZONTAL,   // <- aggiungi
        PART_TIME_VERTICAL      // <- aggiungi
    }
}

