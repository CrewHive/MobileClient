package com.example.myapplication.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.android.state.*
import com.example.myapplication.android.ui.components.dialogs.BrandedLogoutDialog
import com.example.myapplication.android.ui.components.navigation.BottomNavigationBarComponent.BottomNavigationBar
import com.example.myapplication.android.ui.components.navigation.DrawerContent
import com.example.myapplication.android.ui.components.navigation.EmployeesNavGraph
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import com.example.myapplication.android.ui.core.api.UiState.AbstractUiState
import com.example.myapplication.android.ui.core.api.UiState.SignInUiState
import com.example.myapplication.android.ui.core.api.UiState.SignUpUiState
import com.example.myapplication.android.ui.components.navigation.LogOutViewModel
import com.example.myapplication.android.ui.core.api.dto.UserWithTimeParams2DTO
import com.example.myapplication.android.ui.core.security.JwtUtils
import com.example.myapplication.android.ui.core.security.SessionManager
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.screens.*
import com.example.myapplication.android.ui.screens.auth.LogInViewModel
import com.example.myapplication.android.ui.screens.auth.SignUpViewModel
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.theme.CustomTheme
import com.example.myapplication.android.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val colors = CustomTheme.colors

                // ---- App state ----
                var isAuthenticated by remember { mutableStateOf(false) }
                var hasCompany by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("Home") }
                var showingSendScreen by remember { mutableStateOf(false) }
                var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }
                var screenSource by remember { mutableStateOf(NavigationSource.Drawer) }
                var showLogoutConfirm by remember { mutableStateOf(false) }
                var selectedEmployee by remember { mutableStateOf<CompanyEmployee?>(null) }
                val calVm: CalendarViewModel = viewModel()
                var detailFromProfile by remember { mutableStateOf(false) }
                var showRemoveButton by remember { mutableStateOf(false) }
                var profileReloadKey by remember { mutableStateOf(0) }
                var calendarStartInMonthly by remember { mutableStateOf(false) }
                var calendarStartCreate by remember { mutableStateOf(false) }
                var calendarFromDrawer by rememberSaveable { mutableStateOf(false) }


                // ---- Error dialog state ----
                var showErrorDialog by remember { mutableStateOf(false) }
                var errorTitle by remember { mutableStateOf("Error") }
                var errorMessage by remember { mutableStateOf("") }

                SideEffect { android.util.Log.d("AUTH", "isAuthenticated = $isAuthenticated") }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(currentScreen) {
                    if (currentScreen != "Calendar") calendarFromDrawer = false
                }


                // ViewModels (lifecycle-aware)
                val signInViewModel: LogInViewModel = viewModel()
                val signUpViewModel: SignUpViewModel = viewModel()
                val companyCreateViewModel: CompanyRegisterViewModel = viewModel()
                val logOutViewModel: LogOutViewModel = viewModel()


                val ctx = LocalContext.current

                // ---- Helper: membership SOLO dal token ----
                fun hasCompanyFromToken(token: String?): Boolean {
                    if (token.isNullOrBlank()) return false
                    return JwtUtils.getCompanyId(token) != null
                }

                // bootstrap token da storage
                LaunchedEffect(Unit) {
                    val access = SessionManager.getToken(ctx)
                    val refresh = SessionManager.getRefreshToken(ctx)
                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                        TokenManager.jwtToken = access
                        TokenManager.refreshToken = refresh
                        isAuthenticated = true
                        hasCompany = hasCompanyFromToken(access)
                    } else {
                        TokenManager.jwtToken = null
                        TokenManager.refreshToken = null
                        isAuthenticated = false
                        hasCompany = false
                    }
                }

                // ---- FLOW: NON autenticato -> NavHost SignIn/SignUp ----
                if (!isAuthenticated) {
                    val navController = androidx.navigation.compose.rememberNavController()
                    val uiState by signInViewModel.uiState.collectAsState(initial = SignInUiState.Idle)
                    val signUpUiState by signUpViewModel.uiState.collectAsState(initial = SignUpUiState.Idle)

                    LaunchedEffect(uiState) {
                        when (uiState) {
                            is SignInUiState.Success -> {
                                val token = TokenManager.jwtToken
                                val refresh = TokenManager.refreshToken
                                if (!token.isNullOrBlank() && !refresh.isNullOrBlank()) {
                                    logOutViewModel.consume()
                                    showLogoutConfirm = false
                                    SessionManager.saveTokens(ctx, token, refresh)
                                    isAuthenticated = true
                                    hasCompany = hasCompanyFromToken(token)
                                    calVm.onCompanyChanged()
                                }
                                signInViewModel.consume()
                            }
                            else -> Unit
                        }
                    }

                    LaunchedEffect(signUpUiState) {
                        when (signUpUiState) {
                            is SignUpUiState.Success -> {
                                android.util.Log.d("SIGNUP", "Registrazione ok -> torna al login")
                                navController.navigate("signin")
                            }
                            else -> Unit
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "signin"
                    ) {
                        composable("signin") {

                            val isLoadingSignIn = uiState is SignInUiState.Loading

                            SignInScreen(
                                onSignInClick = { email, password -> signInViewModel.doLogin(email, password) },
                                onNavigateToSignUp = { navController.navigate("signup") },
                                isLoading = isLoadingSignIn
                            )

                            when (val state = uiState) {
                                is SignInUiState.Error -> {
                                    errorTitle = "Login error"
                                    errorMessage = state.message.ifBlank { "Si è verificato un errore durante il login." }
                                    showErrorDialog = true
                                    signInViewModel.consume()
                                }
                                // rimuovi il vecchio: is SignInUiState.Loading -> CircularProgressIndicator()
                                else -> Unit
                            }
                        }
                        composable("signup") {
                            val isLoadingSignUp = signUpUiState is SignUpUiState.Loading

                            SignUpScreen(
                                onSignUpClick = { email, username, password ->
                                    signUpViewModel.doSignUp(username, email, password)
                                },
                                onNavigateToSignIn = { navController.navigate("signin") },
                                isLoading = isLoadingSignUp
                            )

                            when (val state = signUpUiState) {
                                is SignUpUiState.Error -> {
                                    errorTitle = "Sign up error"
                                    errorMessage = state.message.ifBlank { "Registrazione non riuscita. Riprova." }
                                    showErrorDialog = true
                                    signUpViewModel.consume()
                                }
                                // rimuovi il vecchio: is SignUpUiState.Loading -> CircularProgressIndicator()
                                else -> Unit
                            }
                        }
                    }

                    if (showErrorDialog) {
                        com.example.myapplication.android.ui.components.dialogs.ErrorPopupDialog(
                            title = errorTitle,
                            message = errorMessage,
                            onDismiss = {
                                showErrorDialog = false
                                signInViewModel.consume()
                                signUpViewModel.consume()
                            }
                        )
                    }

                    // STOP qui nello scenario non autenticato
                    return@MyApplicationTheme
                }

                // ---- FLOW: Autenticato ----
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
                    // osserva lo stato del logout (GLOBAL)
                    val logOutUiState by logOutViewModel.uiState.collectAsState(initial = AbstractUiState.Idle)

                    // user/me nel profilo
                    val userVm: UserViewModel = viewModel(factory = UserViewModel.provideFactory())
                    val userState by userVm.uiState.collectAsState()

                    LaunchedEffect(isAuthenticated, hasCompany) {
                        if (isAuthenticated) userVm.refresh()
                    }

                    LaunchedEffect(logOutUiState) {
                        when (val st = logOutUiState) {
                            is AbstractUiState.Success -> {
                                showLogoutConfirm = false
                                // 1) reset credenziali
                                TokenManager.jwtToken = null
                                TokenManager.refreshToken = null
                                SessionManager.clear(ctx)
                                isAuthenticated = false
                                hasCompany = false
                                // 2) reset calendario solo ora
                                calVm.onCompanyChanged()
                                currentScreen = "Home"
                                signInViewModel.consume()
                                signUpViewModel.consume()
                                logOutViewModel.consume()
                            }
                            is AbstractUiState.Error -> {
                                showLogoutConfirm = false
                                errorTitle = "Logout error"
                                errorMessage = st.message.ifBlank { "Impossibile effettuare il logout. Riprova." }
                                showErrorDialog = true
                            }
                            else -> Unit
                        }
                    }

                    // ========== CONTENUTO APP ==========
                    if (!hasCompany) {
                        // ONBOARDING AREA (autenticato ma senza azienda)
                        when (currentScreen) {
                            "OnboardingScreen" -> CompanyOnboardingScreen(
                                onCreateCompany = { currentScreen = "CreateCompany" },
                                onJoinCompany = { currentScreen = "JoinCompanyCode" },
                                onLogoutClick = { showLogoutConfirm = true }
                            )

                            "JoinCompanyCode" -> {
                                var isRefreshing by remember { mutableStateOf(false) }
                                val scope = rememberCoroutineScope()
                                val ctx2 = LocalContext.current

                                fun launchRefresh() {
                                    scope.launch {
                                        if (isRefreshing) return@launch
                                        isRefreshing = true
                                        val refreshTok = TokenManager.refreshToken
                                        if (!refreshTok.isNullOrBlank()) {
                                            val api = com.example.myapplication.android.ui.core.api.utils.ApiClient
                                                .retrofit.create(
                                                    com.example.myapplication.android.ui.core.api.service.ApiService::class.java
                                                )
                                            val rot = runCatching {
                                                api.rotate(
                                                    com.example.myapplication.android.ui.core.api.dto.RotateRequestDTO(
                                                        refreshTok
                                                    )
                                                )
                                            }.getOrNull()

                                            if (rot?.isSuccessful == true) {
                                                val t = rot.body()
                                                if (t != null) {
                                                    TokenManager.jwtToken = t.accessToken
                                                    TokenManager.refreshToken = t.refreshToken
                                                    SessionManager.saveTokens(ctx2, t.accessToken, t.refreshToken)
                                                    hasCompany = JwtUtils.getCompanyId(t.accessToken) != null
                                                    if (hasCompany) {
                                                        calVm.onCompanyChanged()
                                                        currentScreen = "Home"
                                                        isRefreshing = false
                                                        return@launch
                                                    }
                                                }
                                            }
                                        }
                                        isRefreshing = false
                                    }
                                }

                                JoinCompanyCodeScreen(
                                    userId = (TokenManager.jwtToken?.let { JwtUtils.getUserId(it) } ?: 0L).toString(),
                                    isRefreshing = isRefreshing,
                                    onRefresh = { launchRefresh() },
                                    onBack = { currentScreen = "OnboardingScreen" }
                                )
                            }

                            "CreateCompany" -> {
                                val CompanyCreateUiState by companyCreateViewModel.uiState.collectAsState(
                                    initial = AbstractUiState.Idle
                                )

                                LaunchedEffect(CompanyCreateUiState) {
                                    when (CompanyCreateUiState) {
                                        is AbstractUiState.Success -> {
                                            val acc = TokenManager.jwtToken
                                            val ref = TokenManager.refreshToken
                                            if (!acc.isNullOrBlank() && !ref.isNullOrBlank()) {
                                                SessionManager.saveTokens(ctx, acc, ref)
                                                hasCompany = JwtUtils.getCompanyId(acc) != null
                                                calVm.onCompanyChanged()
                                                currentScreen = "Home"
                                            } else {
                                                hasCompany = false
                                            }
                                        }
                                        is AbstractUiState.Error -> {
                                            errorTitle = "Company creation error"
                                            errorMessage =
                                                (CompanyCreateUiState as AbstractUiState.Error).message
                                                    .ifBlank { "Impossibile creare l'azienda. Riprova." }
                                            showErrorDialog = true
                                        }
                                        else -> Unit
                                    }
                                }

                                when (val st = CompanyCreateUiState) {
                                    is AbstractUiState.Loading -> CircularProgressIndicator()
                                    is AbstractUiState.Error -> {
                                        errorTitle = "Company creation error"
                                        errorMessage =
                                            st.message.ifBlank { "Impossibile creare l'azienda. Riprova." }
                                        showErrorDialog = true
                                    }
                                    AbstractUiState.Idle, is AbstractUiState.Success -> Unit
                                }

                                CreateCompanyScreen(
                                    onBack = { currentScreen = "OnboardingScreen" },
                                    onCreateCompanyDto = { name, type, addressDto ->
                                        companyCreateViewModel.doCompanyRegister(name, type.name, addressDto)
                                    },
                                    onCreateCompany = { name, type, _ ->
                                        companyCreateViewModel.doCompanyRegister(name, type.name, null)
                                    }
                                )
                            }

                            else -> CompanyOnboardingScreen(
                                onCreateCompany = { currentScreen = "CreateCompany" },
                                onJoinCompany = { currentScreen = "JoinCompanyCode" },
                                onLogoutClick = { showLogoutConfirm = true }
                            )
                        }
                    } else {
                        // AREA AUTENTICATA STANDARD
                        val navController = rememberNavController()


                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    userName = userState.me?.username,
                                    userEmail = userState.me?.email,
                                    isManager = JwtUtils.isManager(TokenManager.jwtToken ?: ""),
                                    onClose = { coroutineScope.launch { drawerState.close() } },
                                    currentRoute = currentScreen,
                                    highlightCalendar = calendarFromDrawer,
                                    onCalendarOpenFromDrawer = { calendarFromDrawer = true },
                                    onDestinationSelected = {
                                        currentScreen = it
                                        screenSource = NavigationSource.Drawer
                                        showingSendScreen = false
                                        selectedNotification = null
                                        calendarFromDrawer = (it == "Calendar")
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
                                        highlightCalendar = (currentScreen == "Calendar" && !calendarFromDrawer),
                                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                        onTabSelected = {
                                            currentScreen = it
                                            screenSource = NavigationSource.BottomBar
                                            showingSendScreen = false
                                            selectedNotification = null
                                            calendarFromDrawer = false
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
                                            onCreateCompany = { currentScreen = "CreateCompany" },
                                            onJoinCompany = { currentScreen = "JoinCompanyCode" },
                                            onLogoutClick = { showLogoutConfirm = true }
                                        )

                                        "JoinCompanyCode" -> {
                                            var isRefreshing by remember { mutableStateOf(false) }
                                            val scope = rememberCoroutineScope()
                                            val ctx2 = LocalContext.current

                                            fun launchRefresh() {
                                                scope.launch {
                                                    if (isRefreshing) return@launch
                                                    isRefreshing = true
                                                    val refreshTok = TokenManager.refreshToken
                                                    if (!refreshTok.isNullOrBlank()) {
                                                        val api =
                                                            com.example.myapplication.android.ui.core.api.utils.ApiClient
                                                                .retrofit.create(
                                                                    com.example.myapplication.android.ui.core.api.service.ApiService::class.java
                                                                )
                                                        val rot = runCatching {
                                                            api.rotate(
                                                                com.example.myapplication.android.ui.core.api.dto.RotateRequestDTO(
                                                                    refreshTok
                                                                )
                                                            )
                                                        }.getOrNull()

                                                        if (rot?.isSuccessful == true) {
                                                            val t = rot.body()
                                                            if (t != null) {
                                                                TokenManager.jwtToken = t.accessToken
                                                                TokenManager.refreshToken = t.refreshToken
                                                                SessionManager.saveTokens(
                                                                    ctx2,
                                                                    t.accessToken,
                                                                    t.refreshToken
                                                                )
                                                                hasCompany = hasCompanyFromToken(t.accessToken)
                                                                if (hasCompany) {
                                                                    calVm.onCompanyChanged()
                                                                    currentScreen = "Home"
                                                                    isRefreshing = false
                                                                    return@launch
                                                                }
                                                            }
                                                        }
                                                    }
                                                    isRefreshing = false
                                                }
                                            }

                                            JoinCompanyCodeScreen(
                                                userId = (TokenManager.jwtToken?.let { JwtUtils.getUserId(it) } ?: 0L).toString(),
                                                isRefreshing = isRefreshing,
                                                onRefresh = { launchRefresh() },
                                                onBack = { currentScreen = "OnboardingScreen" }
                                            )
                                        }

                                        "Home" -> HomeRoute(
                                            onOpenMonthlyCalendar = {
                                                calendarStartInMonthly = true
                                                calendarFromDrawer = false         // ⬅️ NEW
                                                currentScreen = "Calendar"
                                            },
                                            onOpenEmployees = { currentScreen = "Employees" },
                                            onStartCreateEvent = {
                                                calendarStartCreate = true
                                                calendarFromDrawer = false         // ⬅️ NEW
                                                currentScreen = "Calendar"
                                            }
                                        )

                                        "Calendar" -> CalendarScreen(
                                            screenSource = screenSource,
                                            startInMonthly = calendarStartInMonthly,
                                            onConsumeStartInMonthly = { calendarStartInMonthly = false },
                                            startInCreateEvent = calendarStartCreate,
                                            onConsumeStartInCreate = { calendarStartCreate = false }
                                        )

                                        "Profile" -> ProfileRoute(
                                            reloadKey = profileReloadKey,
                                            onOpenSelfDetails = { meEmp ->
                                                selectedEmployee = meEmp
                                                detailFromProfile = true
                                                showRemoveButton = false
                                                currentScreen = "EmployeeDetail"
                                            },
                                            onLeftCompany = {
                                                calVm.onCompanyChanged()
                                                hasCompany = false
                                                currentScreen = "JoinCompanyCode"
                                            },
                                            onAccountDeleted = {
                                                showLogoutConfirm = false
                                                isAuthenticated = false
                                                hasCompany = false
                                                currentScreen = "Home"
                                                TokenManager.jwtToken = null
                                                TokenManager.refreshToken = null
                                                SessionManager.clear(this@MainActivity)
                                            }
                                        )

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

                                        "Employees" -> {
                                            EmployeesNavGraph(
                                                navController = navController
                                            )
                                        }

                                        "EmployeeDetail" -> selectedEmployee?.let { emp ->
                                            EmployeeDetailRoute(
                                                employee = emp,
                                                onBack = {
                                                    currentScreen = if (detailFromProfile) "Profile" else "Employees"
                                                },
                                                onSaveLocal = {
                                                    if (detailFromProfile) profileReloadKey++
                                                    currentScreen = if (detailFromProfile) "Profile" else "Employees"
                                                },
                                                onRemove = { _ -> currentScreen = "Employees" },
                                                showRemoveButton = !detailFromProfile
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
                                }
                            }
                        }
                    }

                    // ====== OVERLAY GLOBALI (valide sia in onboarding sia nel resto) ======

                    if (showLogoutConfirm) {
                        val isLoggingOut = logOutUiState is AbstractUiState.Loading

                        BrandedLogoutDialog(
                            visible = showLogoutConfirm,
                            isLoading = isLoggingOut,
                            onConfirm = {
                                val token = TokenManager.jwtToken
                                val refresh = TokenManager.refreshToken
                                val userId = token?.let { JwtUtils.getUserId(it) }
                                if (token.isNullOrBlank() || refresh.isNullOrBlank() || userId == null) {
                                    errorTitle = "Logout error"
                                    errorMessage = "Sessione non valida. Riprova a effettuare il login."
                                    showErrorDialog = true
                                    showLogoutConfirm = false
                                    return@BrandedLogoutDialog
                                }
                                logOutViewModel.doLogout(userId.toString(), refresh)
                            },
                            onDismiss = { showLogoutConfirm = false }
                        )
                    }

                    if (logOutUiState is AbstractUiState.Loading) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color(0x66000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    if (showErrorDialog) {
                        com.example.myapplication.android.ui.components.dialogs.ErrorPopupDialog(
                            title = errorTitle,
                            message = errorMessage,
                            onDismiss = {
                                showErrorDialog = false
                                signInViewModel.consume()
                                signUpViewModel.consume()
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class NavigationSource { Drawer, BottomBar }
enum class OnboardingStep { Welcome, CreateCompany, JoinByCode }

private fun meToCompanyEmployee(
    me: UserWithTimeParams2DTO
): com.example.myapplication.android.ui.state.CompanyEmployee {
    return com.example.myapplication.android.ui.state.CompanyEmployee(
        userId = (me.userId ?: 0L).toString(),
        name = me.username.orEmpty().ifBlank { (me.userId ?: 0L).toString() },
        contractType = null,
        weeklyHours = me.workableHoursPerWeek ?: 0,
        overtimeHours = (me.overtimeHours ?: 0.0).toInt(),
        vacationDaysAccumulated = (me.vacationDaysAccumulated ?: 0.0).toFloat(),
        vacationDaysUsed = (me.vacationDaysTaken ?: 0.0).toFloat(),
        leaveDaysAccumulated = (me.leaveDaysAccumulated ?: 0.0).toFloat(),
        leaveDaysUsed = (me.leaveDaysTaken ?: 0.0).toFloat()
    )
}
