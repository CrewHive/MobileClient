package com.example.myapplication.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.data.repository.CompanyRepository
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.state.CompanyEmployee
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.create

data class EmployeesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successEvent: Boolean = false,
    val lastRequestedId: Long? = null,
    val employees: List<CompanyEmployee> = emptyList() // <- AGGIUNTO
)

class EmployeesViewModel(
    private val repo: CompanyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeesUiState())
    val uiState: StateFlow<EmployeesUiState> = _uiState

    fun addByUserId(input: String) {
        val id = input.toLongOrNull()
        if (id == null) {
            _uiState.update { it.copy(errorMessage = "User ID non valido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successEvent = false, lastRequestedId = id) }
            val res = repo.addUserToCurrentCompany(id)
            if (res.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successEvent = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = res.exceptionOrNull()?.message) }
            }
        }
    }

    /** Carica/ricarica la lista dipendenti della company e popola uiState.employees */
    fun refreshCompanyEmployees(companyId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = repo.getCompanyUsers(companyId)
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successEvent = false,
                        employees = res.getOrThrow() // <- POPOLA LA LISTA
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = res.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun consumeSuccess() = _uiState.update { it.copy(successEvent = false) }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val api = ApiClient.retrofit.create<ApiService>()
                EmployeesViewModel(CompanyRepository(api))
            }
        }
    }
}
