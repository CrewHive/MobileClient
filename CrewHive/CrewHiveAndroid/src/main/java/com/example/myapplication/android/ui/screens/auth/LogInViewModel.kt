package com.example.myapplication.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.ui.core.api.UiState.SignInUiState
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.utils.ApiResult
import com.example.myapplication.android.ui.core.api.utils.MyRepository
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogInViewModel : ViewModel() {
    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val repository = MyRepository(api)
    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState

    fun doLogin(username: String, password: String) {
        viewModelScope.launch {
            when (val result = repository.login(username, password)) {
                is ApiResult.Success -> {
                    println("✅ Login OK: ${result.data}")
                    TokenManager.jwtToken = result.data.token
                    TokenManager.refreshToken = result.data.refreshToken
                }
                is ApiResult.Unauthorized -> println("❌ Login fallito: ${result.message}")
                is ApiResult.Error -> println("⚠️ Errore: ${result.message}")
                is ApiResult.ServerError -> println("💥 Server Error: ${result.message}")
                is ApiResult.Exception -> println("🔥 Eccezione: ${result.message}")
                else -> {}
            }
        }
    }
}
