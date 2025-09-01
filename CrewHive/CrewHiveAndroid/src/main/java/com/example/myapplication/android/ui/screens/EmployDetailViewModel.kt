package com.example.myapplication.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.ui.core.api.dto.UserWithTimeParams2DTO
import com.example.myapplication.android.ui.core.api.dto.UpdateUserWorkInfoDTO
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.state.CompanyEmployee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class EmployeeDetailViewModel(
    private val api: ApiService
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val successEvent: Boolean = false,            // successo salvataggio
        val savedEmployee: CompanyEmployee? = null,
        val removedSuccess: Boolean = false           // successo rimozione
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _loadedDetails = MutableStateFlow<UserWithTimeParams2DTO?>(null)
    val loadedDetails = _loadedDetails.asStateFlow()

    // Ricordo l'ultimo companyId usato per poter ricaricare dopo il salvataggio
    private var lastCompanyId: Long? = null
    fun rememberCompany(companyId: Long) { lastCompanyId = companyId }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(
            successEvent = false,
            savedEmployee = null,
            removedSuccess = false                 // ⬅️ reset anche la rimozione
        )
    }

    /** ---------- Helpers numerici ---------- */
    private fun round2OrNull(x: Double?): Double? =
        x?.let { BigDecimal.valueOf(it).setScale(2, RoundingMode.HALF_UP).toDouble() }

    private fun round2NonNeg(x: Double): Double =
        BigDecimal.valueOf(x.coerceAtLeast(0.0)).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun clearLoadedDetails() {
        _loadedDetails.value = null
    }

    fun loadEmployeeDetails(companyId: Long, userId: Long) {
        if (companyId <= 0L || userId <= 0L) return
        viewModelScope.launch {
            // ⬇️ svuoto i vecchi dettagli se sto cambiando utente
            if (_loadedDetails.value?.userId != userId) {
                _loadedDetails.value = null
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                lastCompanyId = companyId
                val resp = api.getUserInformation(companyId, userId)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    _loadedDetails.value = if (body != null && body.userId == userId) body else null
                    _uiState.value = _uiState.value.copy(isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossibile caricare i dettagli (${resp.code()})"
                    )
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = t.message ?: "Errore di rete."
                )
            }
        }
    }

    fun reloadEmployeeDetails(companyId: Long, userId: Long) {
        loadEmployeeDetails(companyId, userId)
    }

    /** Compat: salva prendendo l’overtime intero dalla UI */
    fun saveEmployeeDetails(updated: CompanyEmployee) {
        saveEmployeeDetails(updated, updated.overtimeHours.toDouble())
    }

    /** Salva con overtime decimale preciso (ARROTONDATO a 2 decimali) e poi ricarica */
    fun saveEmployeeDetails(updated: CompanyEmployee, overtimeHoursDecimal: Double) {
        val currentContractFromServer = _loadedDetails.value?.contractType
        val contractApi = (updated.contractType?.name)
            ?: currentContractFromServer
            ?: return run {
                _uiState.value = _uiState.value.copy(errorMessage = "Tipo di contratto mancante")
            }


        val dto = UpdateUserWorkInfoDTO(
            targetUserId = updated.userId.toLongOrNull() ?: -1L,
            contractType = contractApi,
            workableHoursPerWeek = updated.weeklyHours,
            overtimeHours = round2NonNeg(overtimeHoursDecimal),
            vacationDaysAccumulated = round2NonNeg(updated.vacationDaysAccumulated.toDouble()),
            vacationDaysTaken = round2NonNeg(updated.vacationDaysUsed.toDouble()),
            leaveDaysAccumulated = round2NonNeg(updated.leaveDaysAccumulated.toDouble()),
            leaveDaysTaken = round2NonNeg(updated.leaveDaysUsed.toDouble())
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successEvent = false)
            try {
                val resp = api.updateUserWorkInfo(dto)
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successEvent = true,
                        savedEmployee = updated
                    )
                    // Ricarica con la nuova API, usando l’ultimo companyId memorizzato
                    val uid = updated.userId.toLongOrNull()
                    val cid = lastCompanyId
                    if (cid != null && uid != null) reloadEmployeeDetails(cid, uid)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Aggiornamento non riuscito (${resp.code()})."
                    )
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = t.message ?: "Errore di rete."
                )
            }
        }
    }

    /** ⬇️ NUOVO: rimuove l'utente dall'azienda */
    fun removeEmployeeFromCompany(companyId: Long, userId: Long) {
        if (companyId <= 0L || userId <= 0L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, removedSuccess = false)
            try {
                val resp = api.removeUserFromCompany(companyId, userId)
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, removedSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Rimozione non riuscita (${resp.code()})."
                    )
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = t.message ?: "Errore di rete."
                )
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val api = ApiClient.retrofit.create(ApiService::class.java)
                return EmployeeDetailViewModel(api) as T
            }
        }
    }
}
