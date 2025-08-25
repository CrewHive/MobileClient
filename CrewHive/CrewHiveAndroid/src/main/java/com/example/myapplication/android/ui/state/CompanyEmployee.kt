package com.example.myapplication.android.ui.state

enum class EmployeeContractType(val displayName: String) {
    FULL_TIME("Full time"),
    PART_TIME_HORIZONTAL("Part time horizontal"),
    PART_TIME_VERTICAL("Part time vertical")
}

data class CompanyEmployee(
    val userId: String,
    val name: String,                 // per ora puoi usare userId o un nome reale se lo recuperi dal backend
    var contractType: EmployeeContractType? = null,
    var weeklyHours: Int = 40,
    var overtimeHours: Int = 0,
    var vacationDaysAccumulated: Float = 0f,
    var vacationDaysUsed: Float = 0f,
    var leaveDaysAccumulated: Float = 0f,
    var leaveDaysUsed: Float = 0f
)
