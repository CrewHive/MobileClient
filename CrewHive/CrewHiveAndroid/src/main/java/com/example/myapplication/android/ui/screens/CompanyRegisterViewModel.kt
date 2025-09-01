package com.example.myapplication.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.ui.core.api.UiState.AbstractUiState
import com.example.myapplication.android.ui.core.api.UiState.AbstractUiState.Error
import com.example.myapplication.android.ui.core.api.dto.AddressDTO
import com.example.myapplication.android.ui.core.api.dto.LoginResponseDTO
import com.example.myapplication.android.ui.core.api.utils.ApiResult
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.MyRepository
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompanyRegisterViewModel : ViewModel() {
    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val repository = MyRepository(api)

    private val _uiState = MutableStateFlow<AbstractUiState>(AbstractUiState.Idle)
    val uiState: StateFlow<AbstractUiState> = _uiState

    // espongo l’ultimo payload token per permettere alla UI di persistere su SessionManager
    private val _lastTokens = MutableStateFlow<LoginResponseDTO?>(null)
    val lastTokens: StateFlow<LoginResponseDTO?> = _lastTokens

    fun doCompanyRegister(companyName: String, companyType: String, address: AddressDTO?) {
        _uiState.value = AbstractUiState.Loading
        viewModelScope.launch {
            try {
                when (val result: ApiResult<LoginResponseDTO> =
                    repository.companyRegister(companyName, companyType, address)
                ) {
                    is ApiResult.Success -> {
                        val tokens = result.data
                        // 1) metti SUBITO in memoria volatile
                        TokenManager.jwtToken = tokens.accessToken
                        TokenManager.refreshToken = tokens.refreshToken
                        // 2) esponi alla UI per il salvataggio persistente
                        _lastTokens.value = tokens
                        _uiState.value = AbstractUiState.Success
                    }

                    is ApiResult.Unauthorized -> {
                        _uiState.value = Error(result.message ?: "Credenziali non valide")
                    }
                    is ApiResult.Forbidden -> {
                        _uiState.value = Error(result.message ?: "Operazione non consentita")
                    }
                    is ApiResult.Conflict -> {
                        _uiState.value = Error(result.message ?: "Azienda già esistente")
                    }
                    is ApiResult.ServerError -> {
                        _uiState.value = Error(result.message ?: "Errore del server")
                    }
                    is ApiResult.Error -> {
                        _uiState.value = Error(result.message ?: "Errore sconosciuto")
                    }
                    is ApiResult.Exception -> {
                        _uiState.value = Error(result.message ?: "Eccezione imprevista")
                    }
                }
            } catch (t: Throwable) {
                _uiState.value = Error(t.message ?: "Errore imprevisto")
            }
        }
    }

    /** Chiamala dopo aver gestito Success/Error in UI per riportare lo stato a Idle e azzerare i token temporanei. */
    fun consume() {
        _uiState.value = AbstractUiState.Idle
        _lastTokens.value = null
    }
}
