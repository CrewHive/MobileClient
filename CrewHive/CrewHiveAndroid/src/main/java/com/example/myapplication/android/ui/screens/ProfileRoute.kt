// FILE: ProfileRoute.kt
package com.example.myapplication.android.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.android.ui.components.dialogs.ChangePasswordPopupDialog
import com.example.myapplication.android.ui.components.dialogs.ConfirmPopupDialog
import com.example.myapplication.android.ui.components.dialogs.ErrorPopupDialog
import com.example.myapplication.android.ui.components.dialogs.TextInputPopupDialog
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.security.JwtUtils
import com.example.myapplication.android.ui.core.security.SessionManager
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.state.EmployeeContractType

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileRoute(
    vm: UserViewModel = viewModel(factory = UserViewModel.provideFactory()),
    onOpenSelfDetails: (CompanyEmployee) -> Unit = {},
    onLeftCompany: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    reloadKey: Int = 0
) {
    val state by vm.uiState.collectAsState()
    val ctx = LocalContext.current

    // Carica i dati al primo ingresso o quando forzi reload
    LaunchedEffect(Unit) {
        if (state.me == null && !state.isLoading) vm.refresh()
    }
    LaunchedEffect(reloadKey) { vm.refresh() }

    val contractLabel: String? = when (state.me?.contractType) {
        "FULL_TIME" -> "Full time"
        "PART_TIME_HORIZONTAL" -> "Part time orizzontale"
        "PART_TIME_VERTICAL" -> "Part time verticale"
        null -> null
        else -> state.me?.contractType
    }
    val uiContractType = runCatching {
        state.me?.contractType?.let { EmployeeContractType.valueOf(it) }
    }.getOrNull()

    // --- Token/isManager reattivi ---
    var accessToken by remember {
        mutableStateOf(SessionManager.getToken(ctx) ?: TokenManager.jwtToken ?: "")
    }
    LaunchedEffect(state.me, state.leftCompanyEvent, state.accountDeletedEvent, reloadKey) {
        accessToken = SessionManager.getToken(ctx) ?: TokenManager.jwtToken ?: ""
    }
    val isManagerNow = remember(accessToken) { JwtUtils.isManager(accessToken) }

    // --- dialog flags ---
    var showChangeUsername by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showLeaveCompany   by remember { mutableStateOf(false) }
    var showDeleteAccount  by remember { mutableStateOf(false) }

    ProfileScreen(
        username      = state.me?.username ?: "—",
        email         = state.me?.email ?: "—",
        weeklyTarget  = state.me?.workableHoursPerWeek ?: 40,
        overtimeHours = state.me?.overtimeHours ?: 0.0,
        vacationAcc   = state.me?.vacationDaysAccumulated ?: 0.0,
        vacationUsed  = state.me?.vacationDaysTaken ?: 0.0,
        leaveAcc      = state.me?.leaveDaysAccumulated ?: 0.0,
        leaveUsed     = state.me?.leaveDaysTaken ?: 0.0,
        contractTypeLabel = contractLabel,
        isManager     = isManagerNow,
        onEditSelf = {
            val me = state.me ?: return@ProfileScreen
            onOpenSelfDetails(
                CompanyEmployee(
                    userId = (me.userId ?: 0L).toString(),
                    name = me.username.orEmpty().ifBlank { me.email ?: (me.userId ?: 0L).toString() },
                    contractType = uiContractType.toDomainContract(), // <-- FIX: conversione verso il dominio
                    weeklyHours = me.workableHoursPerWeek ?: 0,
                    overtimeHours = (me.overtimeHours ?: 0.0).toInt(),
                    vacationDaysAccumulated = (me.vacationDaysAccumulated ?: 0.0).toFloat(),
                    vacationDaysUsed = (me.vacationDaysTaken ?: 0.0).toFloat(),
                    leaveDaysAccumulated = (me.leaveDaysAccumulated ?: 0.0).toFloat(),
                    leaveDaysUsed = (me.leaveDaysTaken ?: 0.0).toFloat()
                )
            )
        },
        onClickChangeUsername = { showChangeUsername = true },
        onClickChangePassword = { showChangePassword = true },
        onClickLeaveCompany   = { showLeaveCompany = true },
        onClickDeleteAccount  = { showDeleteAccount = true },
        isBusy = state.isLoading
    )

    // --- POPUP: Cambia username ---
    if (showChangeUsername) {
        TextInputPopupDialog(
            title = "Cambia username",
            label = "Nuovo username",
            initialValue = state.me?.username.orEmpty(),
            confirmText = "Aggiorna",
            isLoading = state.isLoading,
            onConfirm = { newUsername ->
                showChangeUsername = false
                vm.changeUsername(newUsername)
            },
            onDismiss = { showChangeUsername = false }
        )
    }

    // --- POPUP: Cambia password ---
    if (showChangePassword) {
        ChangePasswordPopupDialog(
            title = "Cambia password",
            confirmText = "Aggiorna",
            isLoading = state.isLoading,
            onConfirm = { oldPwd, newPwd ->
                showChangePassword = false
                vm.changePassword(oldPwd, newPwd)
            },
            onDismiss = { showChangePassword = false }
        )
    }

    // --- POPUP: Lascia azienda ---
    if (showLeaveCompany) {
        ConfirmPopupDialog(
            title = "Lascia l'azienda",
            message = "Sei sicuro di voler lasciare la tua azienda? Per rientrare servirà un nuovo invito.",
            confirmText = "Conferma",
            cancelText  = "Annulla",
            onConfirm = {
                showLeaveCompany = false
                vm.leaveCompany()
            },
            onDismiss = { showLeaveCompany = false }
        )
    }

    // --- POPUP: Elimina account ---
    if (showDeleteAccount) {
        ConfirmPopupDialog(
            title = "Elimina account",
            message = "Questa operazione è irreversibile. Vuoi procedere?",
            confirmText = "Elimina",
            cancelText  = "Annulla",
            isDestructive = true,
            onConfirm = {
                showDeleteAccount = false
                vm.deleteAccount()
            },
            onDismiss = { showDeleteAccount = false }
        )
    }

    // --- Side effects (toast) ---
    LaunchedEffect(state.toastMessage) {
        val msg = state.toastMessage
        if (!msg.isNullOrBlank()) {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            vm.consumeToast()
        }
    }

    LaunchedEffect(state.leftCompanyEvent) {
        if (state.leftCompanyEvent) {
            val access = TokenManager.jwtToken
            val refresh = TokenManager.refreshToken
            if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                SessionManager.saveTokens(ctx, access, refresh)
            }
            onLeftCompany()
            vm.consumeLeftCompany()
        }
    }

    LaunchedEffect(state.accountDeletedEvent) {
        if (state.accountDeletedEvent) {
            onAccountDeleted()
            vm.consumeAccountDeleted()
        }
    }

    if (state.errorMessage != null) {
        ErrorPopupDialog(
            title = "Operazione profilo",
            message = state.errorMessage ?: "Errore",
            onDismiss = vm::clearError
        )
    }

    if (state.isLoading && state.me == null) {
        CircularProgressIndicator()
    }
}

/* ---------- Adapter UI -> Dominio ---------- */
private fun EmployeeContractType?.toDomainContract(): CompanyEmployee.ContractType? =
    this?.name?.let { runCatching { CompanyEmployee.ContractType.valueOf(it) }.getOrNull() }
