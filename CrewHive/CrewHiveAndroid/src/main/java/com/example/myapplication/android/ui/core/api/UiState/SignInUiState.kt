package com.example.myapplication.android.ui.core.api.UiState

// sealed class che rappresenta gli stati della schermata di login
sealed class SignInUiState {
    // Stato iniziale: nessuna azione in corso
    object Idle : SignInUiState()

    // Stato di caricamento: chiamata API in corso
    object Loading : SignInUiState()

    // Stato di successo: login completato
    object Success : SignInUiState()

    // Stato di errore: backend o eccezione locale
    data class Error(val message: String) : SignInUiState()
}





