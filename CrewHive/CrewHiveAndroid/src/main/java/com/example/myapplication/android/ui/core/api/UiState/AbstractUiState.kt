package com.example.myapplication.android.ui.core.api.UiState

sealed class AbstractUiState {

    object Idle : AbstractUiState()

    object Loading : AbstractUiState()

    object Success : AbstractUiState()

    // Stato di errore: backend o eccezione locale
    data class Error(val message: String) : AbstractUiState()
}