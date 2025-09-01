package com.example.myapplication.android.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.android.ui.components.dialogs.ErrorPopupDialog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeRoute(
    vm: UserViewModel = viewModel(factory = UserViewModel.provideFactory()),
    onOpenMonthlyCalendar: () -> Unit = {},
    onOpenEmployees: () -> Unit = {},
    onStartCreateEvent: (() -> Unit)? = null
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (state.me == null && !state.isLoading) vm.refresh()
    }

    HomeScreen(weeklyTarget = state.me?.workableHoursPerWeek ?: 40,
        onOpenMonthlyCalendar = onOpenMonthlyCalendar,
        onOpenEmployees = onOpenEmployees,
        onStartCreateEvent = onStartCreateEvent)

    if (state.isLoading && state.me == null) {
        CircularProgressIndicator()
    }
    if (state.errorMessage != null) {
        ErrorPopupDialog(
            title = "Caricamento utente",
            message = state.errorMessage ?: "Errore nel recupero dati utente",
            onDismiss = vm::clearError
        )
    }
}
