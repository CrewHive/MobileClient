package com.example.myapplication.android.ui.core.api.dto

data class LogoutRequestDTO (
    val userId: String,
    val refreshToken: String
)