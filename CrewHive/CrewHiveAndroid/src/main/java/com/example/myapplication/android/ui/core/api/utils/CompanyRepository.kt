package com.example.myapplication.android.data.repository

import android.util.Log
import com.example.myapplication.android.ui.core.api.dto.SetCompanyDTO
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.mappers.toCompanyEmployee
import com.example.myapplication.android.ui.state.CompanyEmployee

class CompanyRepository(
    private val api: ApiService
) {
    companion object {
        private const val TAG = "CompanyRepoDebug"
        private const val DEBUG = true
    }

    /**
     * Aggiunge l'utente [targetUserId] all'azienda corrente (ricavata da /user/me).
     * - Usa companyName (non companyId)
     */
    suspend fun addUserToCurrentCompany(targetUserId: Long): Result<Unit> {
        // 1) leggo il profilo corrente
        val meResp = api.getMe()
        if (!meResp.isSuccessful) {
            return Result.failure(Exception("Errore /user/me (${meResp.code()})"))
        }
        val companyName = meResp.body()?.companyName
            ?: return Result.failure(IllegalStateException("Nessuna azienda trovata per l’utente"))

        if (DEBUG) Log.d(TAG, "setCompany -> userId=$targetUserId companyName=$companyName")

        // 2) chiamo il backend con companyName + userId
        val setResp = api.setCompany(SetCompanyDTO(companyName = companyName, userId = targetUserId))
        return when {
            setResp.isSuccessful -> Result.success(Unit)
            setResp.code() == 404 -> Result.failure(Exception("Utente non trovato"))
            setResp.code() == 401 -> Result.failure(Exception("Non autorizzato"))
            setResp.code() == 403 -> Result.failure(Exception("Operazione non consentita"))
            else -> Result.failure(Exception("Errore ${setResp.code()}"))
        }
    }

    /**
     * Lista dipendenti di una company.
     * Usa mapper -> CompanyEmployee
     */
    suspend fun getCompanyUsers(companyId: Long): Result<List<CompanyEmployee>> = runCatching {
        api.getCompanyUsers(companyId).map { it.toCompanyEmployee() }
    }
}
