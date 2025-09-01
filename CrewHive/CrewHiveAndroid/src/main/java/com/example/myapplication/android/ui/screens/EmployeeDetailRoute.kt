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
        val ctx = LocalContext.current   // <-- AGGIUNGI QUESTO


    val companyId = remember { JwtUtils.getCompanyId(TokenManager.jwtToken ?: "") }
        val targetUid = remember(employee.userId) { employee.userId.toLongOrNull() }


        val safeLoaded = remember(loaded, targetUid) {
            if (loaded != null && loaded!!.userId == targetUid) loaded else null
        }

        LaunchedEffect(employee.userId, companyId) {
            val uid = employee.userId.toLongOrNull()
            if (companyId != null && uid != null) {
                vm.loadEmployeeDetails(companyId, uid)
            }
        }

    val detailsLoaded = safeLoaded != null

    val domainContract = safeLoaded?.contractType
        ?.let { runCatching { CompanyEmployee.ContractType.valueOf(it) }.getOrNull() }

    val enrichedEmployee = employee.copy(
        name = when {
            !safeLoaded?.username.isNullOrBlank() -> safeLoaded!!.username
            employee.name.isNotBlank()            -> employee.name
            !safeLoaded?.email.isNullOrBlank()    -> safeLoaded!!.email!!
            else                                  -> "User ${employee.userId}"
        },
        // ⬇️ SOLO dal server; finché non carica lascia null (mostrerai “Seleziona…”)
        contractType = domainContract,
        // ⬇️ SOLO dal server; finché non carica 0 (placeholder visivo)
        weeklyHours = safeLoaded?.workableHoursPerWeek ?: 0,
        vacationDaysAccumulated = (safeLoaded?.vacationDaysAccumulated ?: employee.vacationDaysAccumulated.toDouble()).toFloat(),
        vacationDaysUsed        = (safeLoaded?.vacationDaysTaken       ?: employee.vacationDaysUsed.toDouble()).toFloat(),
        leaveDaysAccumulated    = (safeLoaded?.leaveDaysAccumulated    ?: employee.leaveDaysAccumulated.toDouble()).toFloat(),
        leaveDaysUsed           = (safeLoaded?.leaveDaysTaken          ?: employee.leaveDaysUsed.toDouble()).toFloat()
    )


    val initialOvertimeMinutes: Int? = remember(safeLoaded) {
            safeLoaded?.overtimeHours?.let { (it * 60.0).roundToInt().coerceAtLeast(0) }
        }

    EmployeeDetailScreen(
        employee = enrichedEmployee,
        initialOvertimeMinutes = initialOvertimeMinutes,
        onBack = onBack,
        onSave = { updated, overtimeDecimal -> vm.saveEmployeeDetails(updated, overtimeDecimal) },
        onRemove = { emp ->
            val uid = emp.userId.toLongOrNull() ?: return@EmployeeDetailScreen
            val cid = companyId ?: return@EmployeeDetailScreen
            vm.removeEmployeeFromCompany(cid, uid)
        },
        // ⬇️ disabilita UI finché i dettagli non sono arrivati
        isLoading = state.isLoading || !detailsLoaded,
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


    LaunchedEffect(state.removedSuccess) {
        if (state.removedSuccess) {
            android.widget.Toast
                .makeText(ctx, "Dipendente rimosso dall'azienda", android.widget.Toast.LENGTH_SHORT)
                .show()
            vm.consumeSuccess()
            onRemove(enrichedEmployee) // qui fai solo navigazione (es. currentScreen = "Employees")
        }
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
