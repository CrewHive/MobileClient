package com.example.myapplication.android.ui.core.mappers

import com.example.myapplication.android.ui.core.api.utils.ApiResult

object AuthErrorMapper {

    // Messaggi generici da ignorare se arrivano dal backend
    private val genericBackendMsgs = setOf(
        "Bad Request",
        "richiesta non valida",
        "Request failed with status code 400",
        "Internal Server Error",
        "Errore interno del server",
        "Unauthorized",
        "Forbidden"
    )

    private fun String?.usefulOrNull(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() && it !in genericBackendMsgs }

    fun signInMessage(result: ApiResult<*>): String = when (result) {
        is ApiResult.Unauthorized -> "Username o password non corretti."
        is ApiResult.Forbidden    -> "Account non autorizzato o non verificato."
        is ApiResult.Conflict     -> "Conflitto di credenziali. Riprova."
        is ApiResult.ServerError  -> "Servizio non disponibile. Riprova più tardi."
        is ApiResult.Exception    -> "Problema di connessione. Controlla la rete e riprova."
        is ApiResult.Error        -> result.message.usefulOrNull()
            ?: "Richiesta non valida. Controlla i campi e riprova."
        is ApiResult.Success<*>   -> ""
    }

    fun signUpMessage(result: ApiResult<*>): String = when (result) {
        is ApiResult.Conflict     -> "Username o email già in uso."
        is ApiResult.Forbidden    -> "Non hai i permessi per completare la registrazione."
        is ApiResult.Unauthorized -> "Dati non validi. Controlla i campi e riprova."
        is ApiResult.ServerError  -> "Servizio non disponibile. Riprova più tardi."
        is ApiResult.Exception    -> "Problema di connessione. Controlla la rete e riprova."
        is ApiResult.Error        -> result.message.usefulOrNull()
            ?: "Richiesta non valida. Controlla i campi e riprova."
        is ApiResult.Success<*>   -> ""
    }
}