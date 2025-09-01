// com/example/myapplication/android/ui/screens/EmployeeDetailRoute.kt
package com.example.myapplication.android.ui.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.android.ui.components.dialogs.ErrorPopupDialog
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.security.JwtUtils
import com.example.myapplication.android.ui.state.CompanyEmployee
import kotlin.math.roundToInt

@Composable
fun EmployeeDetailRoute(
    employee: CompanyEmployee,
    onBack: () -> Unit,
    onSaveLocal: (CompanyEmployee) -> Unit,
    onRemove: (CompanyEmployee) -> Unit,
    showRemoveButton: Boolean = true,
    vm: EmployeeDetailViewModel = viewModel(factory = EmployeeDetailViewModel.provideFactory())
) {
    val state  by vm.uiState.collectAsState()
    val loaded by vm.loadedDetails.collectAsState()
    val ctx = LocalContext.current

    // Ricava la companyId dal token e carica i dettagli del dipendente via nuova API
    val companyId = remember { JwtUtils.getCompanyId(TokenManager.jwtToken ?: "") }
    LaunchedEffect(employee.userId, companyId) {
        val uid = employee.userId.toLongOrNull()
        if (companyId != null && uid != null) {
            vm.loadEmployeeDetails(companyId, uid)   // usa GET /company/{companyId}/user/{targetId}/info
        }
    }

    // Enrich dei dati UI con quelli restituiti dall’API (nome, contract type, ore/ferie/permessi…)
    val enrichedEmployee = remember(employee, loaded) {
        // Nome visualizzato: username -> base.name -> email -> User {id}
        val displayName = when {
            !loaded?.username.isNullOrBlank() -> loaded!!.username
            employee.name.isNotBlank()        -> employee.name
            !loaded?.email.isNullOrBlank()    -> loaded!!.email!!
            else                              -> "User ${employee.userId}"
        }

        // Contract type: string -> enum dominio (se valido), altrimenti lascia quello già presente
        val domainContract = loaded?.contractType
            ?.let { runCatching { CompanyEmployee.ContractType.valueOf(it) }.getOrNull() }
            ?: employee.contractType

        employee.copy(
            name = displayName,
            contractType = domainContract,
            weeklyHours = loaded?.workableHoursPerWeek ?: employee.weeklyHours,
            // L’overtime lo gestiamo come minuti iniziali separati (vedi sotto); qui lasciamo l’int locale
            vacationDaysAccumulated = (loaded?.vacationDaysAccumulated ?: employee.vacationDaysAccumulated.toDouble()).toFloat(),
            vacationDaysUsed        = (loaded?.vacationDaysTaken       ?: employee.vacationDaysUsed.toDouble()).toFloat(),
            leaveDaysAccumulated    = (loaded?.leaveDaysAccumulated    ?: employee.leaveDaysAccumulated.toDouble()).toFloat(),
            leaveDaysUsed           = (loaded?.leaveDaysTaken          ?: employee.leaveDaysUsed.toDouble()).toFloat()
        )
    }

    // Overtime minuti iniziali (UI step HH:MM) calcolati dal double dell’API
    val initialOvertimeMinutes: Int? = remember(loaded) {
        loaded?.overtimeHours?.let { (it * 60.0).roundToInt().coerceAtLeast(0) }
    }

    EmployeeDetailScreen(
        employee = enrichedEmployee,
        initialOvertimeMinutes = initialOvertimeMinutes,
        onBack = onBack,
        onSave = { updated, overtimeDecimal ->
            // Salva anche con gli straordinari decimali
            vm.saveEmployeeDetails(updated, overtimeDecimal)
        },
        onRemove = onRemove,
        isLoading = state.isLoading,
        showRemoveButton = showRemoveButton
    )

    // Error popup
    if (state.errorMessage != null) {
        ErrorPopupDialog(
            title = "Update employee error",
            message = state.errorMessage ?: "",
            onDismiss = vm::clearError,
            buttonText = "OK"
        )
    }

    // Success -> toast + callback + back
    LaunchedEffect(state.successEvent) {
        if (state.successEvent) {
            Toast.makeText(ctx, "Dettagli dipendente aggiornati", Toast.LENGTH_SHORT).show()
            state.savedEmployee?.let(onSaveLocal)
            vm.consumeSuccess()
            onBack()
        }
    }
}
