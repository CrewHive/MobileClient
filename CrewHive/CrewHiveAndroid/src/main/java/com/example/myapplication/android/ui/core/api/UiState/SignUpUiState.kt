package com.example.myapplication.android.ui.core.api.UiState

sealed class SignUpUiState {
    // Stato iniziale: nessuna azione in corso
    object Idle : SignUpUiState()

    // Stato di caricamento: chiamata API in corso
    object Loading : SignUpUiState()

    // Stato di successo: login completato
    object Success : SignUpUiState()

    // Stato di errore: backend o eccezione locale
    data class Error(val message: String) : SignUpUiState()
}