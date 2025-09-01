package com.example.myapplication.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.ui.core.api.UiState.SignInUiState
import com.example.myapplication.android.ui.core.api.UiState.SignInUiState.*
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.utils.ApiResult
import com.example.myapplication.android.ui.core.api.utils.MyRepository
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.mappers.AuthErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogInViewModel : ViewModel() {
    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val repository = MyRepository(api)

    private val _uiState = MutableStateFlow<SignInUiState>(Idle)
    val uiState: StateFlow<SignInUiState> = _uiState
    fun doLogin(username: String, password: String) {
        _uiState.value = SignInUiState.Loading

        viewModelScope.launch {
            try {
                when (val result = repository.login(username, password)) {
                    is ApiResult.Success -> {
                        TokenManager.jwtToken = result.data.accessToken
                        TokenManager.refreshToken = result.data.refreshToken
                        _uiState.value = SignInUiState.Success
                    }
                    // 👇 Unico punto di uscita per l’errore: messaggio dal mapper
                    is ApiResult.Unauthorized,
                    is ApiResult.ServerError,
                    is ApiResult.Error,
                    is ApiResult.Exception,
                    is ApiResult.Forbidden,
                    is ApiResult.Conflict -> {
                        val msg = AuthErrorMapper.signInMessage(result)
                        _uiState.value = SignInUiState.Error(msg)
                    }
                }
            } catch (t: Throwable) {
                // se vuoi distinguere rete: if (t is IOException) ...
                _uiState.value = SignInUiState.Error("Errore imprevisto. Riprova.")
            }
        }
    }

    fun consume() { _uiState.value = SignInUiState.Idle }
}
