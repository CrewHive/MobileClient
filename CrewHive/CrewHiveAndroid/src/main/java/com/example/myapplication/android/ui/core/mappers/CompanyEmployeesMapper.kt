package com.example.myapplication.android.ui.core.mappers

import com.example.myapplication.android.ui.core.api.dto.UserIdAndUsernameAndHoursDTO
import com.example.myapplication.android.ui.core.api.dto.UserWithTimeParams2DTO
import com.example.myapplication.android.ui.state.CompanyEmployee

/**
 * Mapper da elenco utenti company -> modello UI compatto.
 * NOTA: questo DTO NON contiene ferie/permessi/straordinari/contratto.
 */
fun UserIdAndUsernameAndHoursDTO.toCompanyEmployee(): CompanyEmployee =
    CompanyEmployee(
        userId = (this.userId).toString(),
        name = this.username,
        weeklyHours = this.workableHoursPerWeek ?: 0,
        overtimeHours = 0,            // il DTO non lo fornisce
        vacationDaysAccumulated = 0f, // il DTO non lo fornisce
        vacationDaysUsed = 0f,        // il DTO non lo fornisce
        leaveDaysAccumulated = 0f,    // il DTO non lo fornisce
        leaveDaysUsed = 0f,           // il DTO non lo fornisce
        contractType = null           // il DTO non lo fornisce
    )

/**
 * Mapper da /user/me o /company/{id}/user/{targetId}/info (ricco) -> modello UI completo.
 * Qui il contractType ESISTE e lo mappiamo correttamente.
 */
fun UserWithTimeParams2DTO.toCompanyEmployee(): CompanyEmployee =
    CompanyEmployee(
        userId = (this.userId ?: 0L).toString(),
        name = this.username?.ifBlank { (this.userId ?: 0L).toString() } ?: (this.userId ?: 0L).toString(),
        weeklyHours = this.workableHoursPerWeek ?: 0,
        overtimeHours = ((this.overtimeHours ?: 0.0).toInt()),
        vacationDaysAccumulated = (this.vacationDaysAccumulated ?: 0.0).toFloat(),
        vacationDaysUsed = (this.vacationDaysTaken ?: 0.0).toFloat(),
        leaveDaysAccumulated = (this.leaveDaysAccumulated ?: 0.0).toFloat(),
        leaveDaysUsed = (this.leaveDaysTaken ?: 0.0).toFloat(),
        contractType = ContractTypeMapper.fromApi(this.contractType) // ⬅️ IMPORTANTE
    )
