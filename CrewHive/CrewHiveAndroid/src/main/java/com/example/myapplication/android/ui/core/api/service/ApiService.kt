package com.example.myapplication.android.ui.core.api.service

import com.example.myapplication.android.ui.core.api.dto.LoginRequestDTO
import com.example.myapplication.android.ui.core.api.dto.LoginResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDTO): Response<LoginResponseDTO>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDTO>
}

// DTO utente di esempio
data class UserDTO(
    val id: Long,
    val username: String,
    val email: String
)
