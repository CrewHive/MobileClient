package com.example.myapplication.android.ui.components.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.ui.core.api.UiState.AbstractUiState
import com.example.myapplication.android.ui.core.api.UiState.AbstractUiState.Error
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.utils.ApiResult
import com.example.myapplication.android.ui.core.api.utils.MyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogOutViewModel : ViewModel() {
    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val repository = MyRepository(api)

    private val _uiState = MutableStateFlow< AbstractUiState>(AbstractUiState.Idle)
    val uiState: StateFlow<AbstractUiState> = _uiState

    fun doLogout(userId: String, refreshToken: String) {
        // metti subito Loading
        _uiState.value = AbstractUiState.Loading

        viewModelScope.launch {
            try {
                when (val result = repository.logout(userId, refreshToken)) {
                    is ApiResult.Success -> {
                        _uiState.value = AbstractUiState.Success
                    }
                    is ApiResult.Unauthorized -> {
                        _uiState.value = Error(result.message ?: "Credenziali non valide")
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

                    is ApiResult.Conflict -> TODO()
                    is ApiResult.Forbidden -> TODO()
                }
            } catch (t: Throwable) {
                _uiState.value = Error(t.message ?: "Errore imprevisto")
            }
        }
    }

    fun consume() { _uiState.value = AbstractUiState.Idle }

}