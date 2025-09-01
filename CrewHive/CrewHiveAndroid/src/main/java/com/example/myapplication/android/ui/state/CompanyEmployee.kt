package com.example.myapplication.android.ui.state

enum class EmployeeContractType(val displayName: String) {
    FULL_TIME("Full time"),
    PART_TIME_HORIZONTAL("Part time orizzontale"),
    PART_TIME_VERTICAL("Part time verticale")
}

data class CompanyEmployee(
    val userId: String,
    val name: String,
    val weeklyHours: Int,
    val overtimeHours: Int,
    val vacationDaysAccumulated: Float,
    val vacationDaysUsed: Float,
    val leaveDaysAccumulated: Float,
    val leaveDaysUsed: Float,
    val contractType: ContractType? = null
) {
    enum class ContractType(val displayName: String) {
        FULL_TIME("Full time"),
        PART_TIME("Part time"),
        INTERNSHIP("Internship"),
        OTHER("Other")
    }
}
