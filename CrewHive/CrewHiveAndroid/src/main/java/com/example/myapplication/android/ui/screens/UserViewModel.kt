package com.example.myapplication.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.android.ui.core.api.dto.UserWithTimeParams2DTO
import com.example.myapplication.android.ui.core.api.dto.UpdatePasswordDTO
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

data class UserUiState(
    val me: UserWithTimeParams2DTO? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // eventi one-shot per side-effect nel Route
    val toastMessage: String? = null,
    val leftCompanyEvent: Boolean = false,
    val accountDeletedEvent: Boolean = false
)

class UserViewModel(
    private val api: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState

    fun refresh() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val resp = api.getMe()
            if (resp.isSuccessful) {
                _uiState.update { it.copy(me = resp.body(), isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Impossibile caricare il profilo (${resp.code()})") }
            }
        } catch (t: Throwable) {
            _uiState.update { it.copy(isLoading = false, errorMessage = t.userMessage()) }
        }
    }


    fun changeUsername(newUsername: String) = viewModelScope.launch {
        if (newUsername.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Lo username non può essere vuoto") }
            return@launch
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            // Invia come text/plain senza virgolette
            val body = newUsername.trim().toRequestBody("text/plain".toMediaType())
            val resp = api.updateUsername(body)
            if (!resp.isSuccessful) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Aggiornamento fallito (${resp.code()})")
                }
                return@launch
            }

            val tokens = resp.body() ?: run {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Risposta vuota dal server") }
                return@launch
            }

            // Aggiorna i token ricevuti
            TokenManager.jwtToken = tokens.accessToken
            TokenManager.refreshToken = tokens.refreshToken

            // Poi rileggi /me con l’access token aggiornato
            val meResp = api.getMe()
            if (meResp.isSuccessful) {
                _uiState.update {
                    it.copy(me = meResp.body(), isLoading = false, toastMessage = "Username aggiornato")
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Aggiornato ma rilettura profilo fallita (${meResp.code()})")
                }
            }
        } catch (t: Throwable) {
            _uiState.update { it.copy(isLoading = false, errorMessage = t.userMessage()) }
        }
    }




    fun changePassword(oldPass: String, newPass: String) = viewModelScope.launch {
        if (oldPass.isBlank() || newPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Compila entrambi i campi password") }
            return@launch
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val resp = api.updatePassword(UpdatePasswordDTO(oldPass, newPass))
            if (resp.isSuccessful) {
                _uiState.update { it.copy(isLoading = false, toastMessage = "Password aggiornata") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Aggiornamento fallito (${resp.code()})") }
            }
        } catch (t: Throwable) {
            _uiState.update { it.copy(isLoading = false, errorMessage = t.userMessage()) }
        }
    }

    fun leaveCompany() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val resp = api.leaveCompany() // Response<AuthResponseDTO>
            if (!resp.isSuccessful) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Operazione fallita (${resp.code()})") }
                return@launch
            }

            val body = resp.body() ?: run {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Risposta vuota dal server") }
                return@launch
            }

            // 1) Aggiorna i token IN RAM come già fai in changeUsername
            TokenManager.jwtToken = body.accessToken
            TokenManager.refreshToken = body.refreshToken

            // 2) Rileggi /user/me con i token aggiornati
            val meResp = api.getMe()
            if (meResp.isSuccessful) {
                _uiState.update {
                    it.copy(
                        me = meResp.body(),
                        isLoading = false,
                        leftCompanyEvent = true,
                        toastMessage = "Hai lasciato l'azienda"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Lasciata l'azienda ma rilettura profilo fallita (${meResp.code()})")
                }
            }
        } catch (t: Throwable) {
            _uiState.update { it.copy(isLoading = false, errorMessage = t.userMessage()) }
        }
    }



    fun deleteAccount() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val resp = api.deleteAccount()
            if (resp.isSuccessful) {
                _uiState.update { it.copy(isLoading = false, accountDeletedEvent = true, toastMessage = "Account eliminato") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Eliminazione fallita (${resp.code()})") }
            }
        } catch (t: Throwable) {
            _uiState.update { it.copy(isLoading = false, errorMessage = t.userMessage()) }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun consumeToast() = _uiState.update { it.copy(toastMessage = null) }
    fun consumeLeftCompany() = _uiState.update { it.copy(leftCompanyEvent = false) }
    fun consumeAccountDeleted() = _uiState.update { it.copy(accountDeletedEvent = false) }

    companion object {
        fun provideFactory() = viewModelFactory {
            initializer {
                val api = ApiClient.retrofit.create(ApiService::class.java)
                UserViewModel(api)
            }
        }
    }
}

private fun Throwable.userMessage(): String =
    when (this) {
        is HttpException -> "Errore server (${code()})"
        else -> message ?: "Errore di rete"
    }
