package com.example.myapplication.android.ui.core.mappers

import com.example.myapplication.android.ui.core.api.dto.UserIdAndUsernameAndHoursDTO
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.core.api.dto.UserWithTimeParams2DTO

/**
 * Mapper da elenco utenti company -> modello UI compatto.
 * NOTA: questo DTO NON contiene ferie/permessi/straordinari -> default a 0.
 */
fun UserIdAndUsernameAndHoursDTO.toCompanyEmployee(): CompanyEmployee =
    CompanyEmployee(
        userId = (this.userId).toString(),
        name = this.username,
        weeklyHours = this.workableHoursPerWeek ?: 0,
        overtimeHours = 0,                       // <- default safe: il DTO non lo fornisce
        vacationDaysAccumulated = 0f,            // <- default safe
        vacationDaysUsed = 0f,                   // <- default safe
        leaveDaysAccumulated = 0f,               // <- default safe
        leaveDaysUsed = 0f,                      // <- default safe
        contractType = null                      // <- se hai un enum, puoi mapparlo quando disponibile
    )

/**
 * Mapper da /user/me (ricco) -> modello UI completo.
 */
fun UserWithTimeParams2DTO.toCompanyEmployee(): CompanyEmployee =
    CompanyEmployee(
        userId = (this.userId).toString(),
        name = this.username.ifBlank { this.userId.toString() },
        weeklyHours = this.workableHoursPerWeek ?: 0,
        // DTO usa Double? per le ore di straordinario → UI ha Int (ore intere)
        overtimeHours = ((this.overtimeHours ?: 0.0).toInt()),
        vacationDaysAccumulated = (this.vacationDaysAccumulated ?: 0.0).toFloat(),
        vacationDaysUsed = (this.vacationDaysTaken ?: 0.0).toFloat(),
        leaveDaysAccumulated = (this.leaveDaysAccumulated ?: 0.0).toFloat(),
        leaveDaysUsed = (this.leaveDaysTaken ?: 0.0).toFloat(),
        // Se hai un enum per il contratto, qui puoi mappare la stringa del backend all’enum.
        contractType = null
    )
