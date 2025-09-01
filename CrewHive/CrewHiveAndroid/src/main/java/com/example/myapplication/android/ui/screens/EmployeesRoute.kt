package com.example.myapplication.android.ui.screens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.android.ui.components.dialogs.ErrorPopupDialog

@Composable
fun EmployeesRoute(
    navController: NavController,
    vm: EmployeesViewModel = viewModel(factory = EmployeesViewModel.provideFactory()),
    calVm: CalendarViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val ctx = LocalContext.current

    // Quando conosco/cambia la companyId -> ricarico la lista dipendenti
    LaunchedEffect(calVm.companyId) {
        calVm.companyId?.let { companyId ->
            vm.refreshCompanyEmployees(companyId)
        }
    }

    // Dopo l’aggiunta per userId, fai refresh e toast
    LaunchedEffect(ui.successEvent) {
        if (ui.successEvent) {
            calVm.companyId?.let { vm.refreshCompanyEmployees(it) }
            Toast.makeText(ctx, "Utente aggiunto all’azienda", Toast.LENGTH_SHORT).show()
            vm.consumeSuccess()
        }
    }

    EmployeesScreen(
        employees = ui.employees,
        isLoading = ui.isLoading,
        onAddByUserId = { id -> vm.addByUserId(id) },
        onOpenDetails = { emp ->
            navController.navigate("employeeDetail/${emp.userId}")
        }
    )

    // Error popup coerente col resto dell’app
    ui.errorMessage?.let { msg ->
        ErrorPopupDialog(
            title = "Add employee error",
            message = msg,
            onDismiss = vm::clearError,
            buttonText = "OK"
        )
    }
}
