package com.example.myapplication.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.ui.core.api.UiState.SignUpUiState.Error
import com.example.myapplication.android.ui.core.api.UiState.SignUpUiState
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.utils.ApiResult
import com.example.myapplication.android.ui.core.api.utils.MyRepository
import com.example.myapplication.android.ui.core.mappers.AuthErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val repository = MyRepository(api)

    private val _uiState = MutableStateFlow< SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun doSignUp(username: String, email: String, password: String) {
        _uiState.value = SignUpUiState.Loading

        viewModelScope.launch {
            try {
                when (val result = repository.signup(username, email, password)) {
                    is ApiResult.Success -> {
                        _uiState.value = SignUpUiState.Success
                    }
                    is ApiResult.Unauthorized,
                    is ApiResult.ServerError,
                    is ApiResult.Error,
                    is ApiResult.Exception,
                    is ApiResult.Forbidden,
                    is ApiResult.Conflict -> {
                        val msg = AuthErrorMapper.signUpMessage(result)
                        _uiState.value = SignUpUiState.Error(msg)
                    }
                }
            } catch (t: Throwable) {
                _uiState.value = SignUpUiState.Error("Errore imprevisto. Riprova.")
            }
        }
    }

    fun consume() { _uiState.value = SignUpUiState.Idle }
}