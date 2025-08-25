package com.example.myapplication.android.ui.core.api.utils

import com.example.myapplication.android.ui.core.api.dto.ApiErrorDTO
import com.example.myapplication.android.ui.core.api.dto.LoginRequestDTO
import com.example.myapplication.android.ui.core.api.dto.LoginResponseDTO
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.service.UserDTO
import com.google.gson.Gson
import retrofit2.Response

class MyRepository(private val api: ApiService) {
    private val gson = Gson()

    suspend fun login(username: String, password: String): ApiResult<LoginResponseDTO> {
        return safeApiCall {
            api.login(LoginRequestDTO(username, password))
        }
    }

    suspend fun getCurrentUser(): ApiResult<UserDTO> {
        return safeApiCall { api.getCurrentUser() }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Risposta vuota dal server", response.code())
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorDto = try {
                    gson.fromJson(errorBody, ApiErrorDTO::class.java)
                } catch (e: Exception) {
                    null
                }

                when (response.code()) {
                    400 -> ApiResult.Error(errorDto?.message ?: "Richiesta non valida", 400)
                    401 -> ApiResult.Unauthorized(errorDto?.message ?: "Non autorizzato")
                    403 -> ApiResult.Forbidden(errorDto?.message ?: "Accesso negato")
                    404 -> ApiResult.Error(errorDto?.message ?: "Risorsa non trovata", 404)
                    409 -> ApiResult.Conflict(errorDto?.message ?: "Risorsa già esistente")
                    500 -> ApiResult.ServerError(errorDto?.message ?: "Errore interno", 500)
                    else -> ApiResult.Error(errorDto?.message ?: "Errore sconosciuto", response.code())
                }
            }
        } catch (e: Exception) {
            ApiResult.Exception("Eccezione locale: ${e.localizedMessage}")
        }
    }
}
