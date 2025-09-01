// FILE: com/example/myapplication/android/ui/screens/EmployeeDetailRoute.kt
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
import com.example.myapplication.android.ui.core.mappers.ContractTypeMapper
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

    val companyId = remember { JwtUtils.getCompanyId(TokenManager.jwtToken ?: "") }
    val targetUid = remember(employee.userId) { employee.userId.toLongOrNull() }

    // usa i dettagli solo se appartengono davvero all'utente richiesto
    val safeLoaded = remember(loaded, targetUid) {
        if (loaded != null && loaded!!.userId == targetUid) loaded else null
    }

    // carico/ricarico i dettagli quando cambia l'utente o la company
    LaunchedEffect(employee.userId, companyId) {
        val uid = employee.userId.toLongOrNull()
        if (companyId != null && uid != null) {
            vm.loadEmployeeDetails(companyId, uid)
        }
    }

    val detailsLoaded = safeLoaded != null

    // mappa il contratto API -> dominio; se assente, fallback ai dati lista
    val domainContract =
        ContractTypeMapper.fromApi(safeLoaded?.contractType) ?: employee.contractType

    // arricchisci i dati visualizzati con quelli del server (senza sovrascrivere tutto a 0)
    val enrichedEmployee = remember(employee, safeLoaded) {
        employee.copy(
            name = when {
                !safeLoaded?.username.isNullOrBlank() -> safeLoaded!!.username
                employee.name.isNotBlank()            -> employee.name
                !safeLoaded?.email.isNullOrBlank()    -> safeLoaded!!.email!!
                else                                  -> "User ${employee.userId}"
            },
            contractType = domainContract,
            weeklyHours = safeLoaded?.workableHoursPerWeek ?: employee.weeklyHours,
            vacationDaysAccumulated = (safeLoaded?.vacationDaysAccumulated ?: employee.vacationDaysAccumulated.toDouble()).toFloat(),
            vacationDaysUsed        = (safeLoaded?.vacationDaysTaken       ?: employee.vacationDaysUsed.toDouble()).toFloat(),
            leaveDaysAccumulated    = (safeLoaded?.leaveDaysAccumulated    ?: employee.leaveDaysAccumulated.toDouble()).toFloat(),
            leaveDaysUsed           = (safeLoaded?.leaveDaysTaken          ?: employee.leaveDaysUsed.toDouble()).toFloat()
        )
    }

    val initialOvertimeMinutes: Int? = remember(safeLoaded) {
        safeLoaded?.overtimeHours?.let { (it * 60.0).roundToInt().coerceAtLeast(0) }
    }

    EmployeeDetailScreen(
        employee = enrichedEmployee,
        initialOvertimeMinutes = initialOvertimeMinutes,
        onBack = onBack,
        onSave = { updated, overtimeDecimal ->
            vm.saveEmployeeDetails(updated, overtimeDecimal)
        },
        onRemove = { emp ->
            val uid = emp.userId.toLongOrNull() ?: return@EmployeeDetailScreen
            val cid = companyId ?: return@EmployeeDetailScreen
            vm.removeEmployeeFromCompany(cid, uid)
        },
        // disabilita la UI finché i dettagli non sono arrivati o mentre salvi
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

    // Rimozione avvenuta
    LaunchedEffect(state.removedSuccess) {
        if (state.removedSuccess) {
            Toast.makeText(ctx, "Dipendente rimosso dall'azienda", Toast.LENGTH_SHORT).show()
            vm.consumeSuccess()
            onRemove(enrichedEmployee)
        }
    }

    // Salvataggio riuscito
    LaunchedEffect(state.successEvent) {
        if (state.successEvent) {
            Toast.makeText(ctx, "Dettagli dipendente aggiornati", Toast.LENGTH_SHORT).show()
            state.savedEmployee?.let(onSaveLocal)
            vm.consumeSuccess()
            onBack()
        }
    }
}
