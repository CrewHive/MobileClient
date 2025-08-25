package com.example.myapplication.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.myapplication.android.state.*
import com.example.myapplication.android.ui.components.navigation.BottomNavigationBarComponent.BottomNavigationBar
import com.example.myapplication.android.ui.components.navigation.DrawerContent
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import com.example.myapplication.android.ui.core.api.UiState.SignInUiState
import com.example.myapplication.android.ui.screens.*
import com.example.myapplication.android.ui.screens.auth.LogInViewModel
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.theme.CustomTheme
import com.example.myapplication.android.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val colors = CustomTheme.colors

                // ---- App state ----
                var isAuthenticated by remember { mutableStateOf(false) }
                var hasCompany by remember { mutableStateOf(false) } // <-- metti false per testare l’onboarding
                var currentScreen by remember { mutableStateOf("Home") }
                var showingSendScreen by remember { mutableStateOf(false) }
                var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }
                var screenSource by remember { mutableStateOf(NavigationSource.Drawer) }
                var showLogoutConfirm by remember { mutableStateOf(false) }
                // stati (dentro setContent, insieme agli altri)
                val employees = remember { mutableStateListOf<CompanyEmployee>() }
                var selectedEmployee by remember { mutableStateOf<CompanyEmployee?>(null) }


                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                val signInViewModel: LogInViewModel = viewModel()

                // ---- FLOW: NON autenticato -> NavHost SignIn/SignUp ----
                if (!isAuthenticated) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "signin") {
                        composable("signin") {
                            val uiState by signInViewModel.uiState.collectAsState(initial = SignInUiState.Idle)
                            SignInScreen(
                                onSignInClick = { email, password ->
                                    signInViewModel.doLogin(email, password)
                                    // qui potresti caricare il profilo e settare hasCompany = (companyId != null)
                                },
                                onNavigateToSignUp = { navController.navigate("signup") }
                            )
                            when (uiState) {
                                is SignInUiState.Success -> {
                                    isAuthenticated = true
                                }
                                is SignInUiState.Error -> {
                                    val errorMessage = (uiState as SignInUiState.Error).message
                                    // Snackbar/Dialog con errorMessage
                                }
                                is SignInUiState.Loading -> {
                                    CircularProgressIndicator()
                                }
                                SignInUiState.Idle -> { /* non fare nulla */ }
                            }
                        }
                        composable("signup") {
                            SignUpScreen(
                                onSignUpClick = { _, _, _ ->
                                    isAuthenticated = true
                                    // idem: valuta hasCompany in base ai dati reali
                                },
                                onNavigateToSignIn = { navController.navigate("signin") }
                            )
                        }
                    }
                    return@MyApplicationTheme
                }

                // ---- FLOW: Autenticato ----
                // Stati condivisi usati nell’area autenticata
                val calendarState = remember {
                    CalendarState(
                        selectedDate = mutableStateOf(Calendar.getInstance()),
                        userEvents = mutableStateListOf()
                    )
                }
                val templateState = remember { mutableStateListOf<ShiftTemplate>() }
                val currentUser = remember { mutableStateOf("Giulia Verdi") }

                CompositionLocalProvider(
                    LocalCalendarState provides calendarState,
                    LocalTemplateState provides templateState,
                    LocalCurrentUser provides currentUser
                ) {
                    // Gate: utente autenticato ma senza azienda -> onboarding
                    if (!hasCompany) {
                        when (currentScreen) {
                            "OnboardingScreen" -> CompanyOnboardingScreen(
                                onCreateCompany = { currentScreen = "CreateCompany" },
                                onJoinCompany   = { currentScreen = "JoinCompanyCode" }
                            )
                            "JoinCompanyCode" -> JoinCompanyCodeScreen(
                                userId = "USR-8F2C1A" // TODO: prendi lo userId reale
                            )
                            "CreateCompany" -> CreateCompanyScreen(
                                    onBack = { "OnboardingScreen" },
                        onCreateCompany = { name, type, address ->
                            // TODO: chiama la tua API
                            // es.: viewModel.createCompany(name, type.name, address) { success -> ... }

                            // Se OK:
                            hasCompany = true      // esci dall’onboarding e vai all’app principale
                            // In alternativa: onboardingStep = OnboardingStep.Choose per tornare indietro
                        }
                            )
                            else -> CompanyOnboardingScreen(
                                onCreateCompany = { currentScreen = "CreateCompany" },
                                onJoinCompany   = { currentScreen = "JoinCompanyCode" }
                            )
                        }
                        return@CompositionLocalProvider
                    }

                    // Area autenticata standard: Drawer + BottomBar + contenuti
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(
                                onClose = { coroutineScope.launch { drawerState.close() } },
                                onDestinationSelected = {
                                    currentScreen = it
                                    screenSource = NavigationSource.Drawer
                                    showingSendScreen = false
                                    selectedNotification = null
                                },
                                onLogoutClicked = {
                                    showLogoutConfirm = true
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            bottomBar = {
                                BottomNavigationBar(
                                    currentScreen = currentScreen,
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                    onTabSelected = {
                                        currentScreen = it
                                        screenSource = NavigationSource.BottomBar
                                        showingSendScreen = false
                                        selectedNotification = null
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .background(Color.White)
                            ) {
                                when (currentScreen) {
                                    "OnboardingScreen" -> CompanyOnboardingScreen(
                                        onCreateCompany = {
                                            // TODO: naviga alla tua schermata reale di creazione
                                            // currentScreen = "CreateCompany"
                                        },
                                        onJoinCompany = {
                                            currentScreen = "JoinCompanyCode"
                                        }
                                    )
                                    "JoinCompanyCode" -> JoinCompanyCodeScreen(
                                        userId = /* prendi il vero ID dell’utente, es. da sessione/profilo */ "USR-8F2C1A"
                                    )
                                    "Home" -> HomeScreen()
                                    "Calendar" -> CalendarScreen(screenSource = screenSource)
                                    "Profile" -> ProfileScreen()
                                    "Notifications" -> {
                                        if (selectedNotification != null) {
                                            NotificationDetailScreen(
                                                notification = selectedNotification!!,
                                                onBackClick = { selectedNotification = null }
                                            )
                                        } else {
                                            NotificationScreen(
                                                onEditClick = { showingSendScreen = true },
                                                onNotificationClick = { notification ->
                                                    selectedNotification = notification
                                                }
                                            )
                                        }
                                    }
                                    "Employees" -> EmployeesScreen(
                                        employees = employees,
                                        onAddByUserId = { userId ->
                                            if (employees.none { it.userId == userId }) {
                                                employees.add(
                                                    CompanyEmployee(
                                                        userId = userId,
                                                        name = userId // o il nome reale quando farai la fetch
                                                    )
                                                )
                                            }
                                        },
                                        onOpenDetails = { emp ->
                                            selectedEmployee = emp
                                            currentScreen = "EmployeeDetail"
                                        }
                                    )

                                    "EmployeeDetail" -> selectedEmployee?.let { emp ->
                                        EmployeeDetailScreen(
                                            employee = emp,
                                            onBack = { currentScreen = "Employees" },
                                            onSave = { updated ->
                                                val idx = employees.indexOfFirst { it.userId == updated.userId }
                                                if (idx >= 0) employees[idx] = updated
                                                currentScreen = "Employees"
                                            },
                                            onRemove = { toRemove ->
                                                employees.removeAll { it.userId == toRemove.userId }
                                                currentScreen = "Employees"
                                            }
                                        )
                                    }

                                }

                                if (showingSendScreen) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White)
                                    ) {
                                        NotificationSendScreen(
                                            onBackClick = { showingSendScreen = false },
                                            onSendClick = { showingSendScreen = false }
                                        )
                                    }
                                }

                                if (showLogoutConfirm) {
                                    AlertDialog(
                                        modifier = Modifier.background(colors.background),
                                        onDismissRequest = { showLogoutConfirm = false },
                                        title = { Text("Conferma logout") },
                                        text = { Text("Sei sicuro di voler uscire?") },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                isAuthenticated = false
                                                showLogoutConfirm = false
                                            }) { Text("Conferma") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showLogoutConfirm = false }) {
                                                Text("Annulla")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class NavigationSource { Drawer, BottomBar }
