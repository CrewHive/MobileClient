// com/example/myapplication/android/ui/components/navigation/EmployeesNavGraph.kt
package com.example.myapplication.android.ui.components.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.android.ui.screens.EmployeeDetailRoute
import com.example.myapplication.android.ui.screens.EmployeesRoute
import com.example.myapplication.android.ui.state.CompanyEmployee

@Composable
fun EmployeesNavGraph(
    navController: NavHostController,
    startDestination: String = "employees"
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Lista dipendenti
        composable("employees") {
            EmployeesRoute(
                navController = navController
                // Il ViewModel viene creato dentro EmployeesRoute via provideFactory()
            )
        }

        // Dettaglio dipendente: passa solo lo userId come argomento
        composable("employeeDetail/{userId}") { backStackEntry ->
            val userIdArg = backStackEntry.arguments?.getString("userId") ?: return@composable

            // Stub minimale: i dati reali arrivano da /user/me (getMeAs) in EmployeeDetailRoute
            val base = CompanyEmployee(
                userId = userIdArg,
                name = "",
                weeklyHours = 0,
                overtimeHours = 0,
                vacationDaysAccumulated = 0f,
                vacationDaysUsed = 0f,
                leaveDaysAccumulated = 0f,
                leaveDaysUsed = 0f,
                contractType = null
            )

            EmployeeDetailRoute(
                employee = base,
                onBack = { navController.popBackStack() },
                onSaveLocal = { /* se mantieni una cache locale, aggiorna qui */ },
                onRemove = { _ -> navController.popBackStack() },
                showRemoveButton = true
            )
        }
    }
}
