package com.example.myapplication.android.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.myapplication.android.state.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.android.NavigationSource
import com.example.myapplication.android.state.LocalCalendarState
import com.example.myapplication.android.ui.components.buttons.FloatingAddButton
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import com.example.myapplication.android.ui.components.calendar.DailyEventList
import com.example.myapplication.android.ui.components.calendar.EventList
import com.example.myapplication.android.ui.components.calendar.MonthlyCalendarGridView
import com.example.myapplication.android.ui.components.calendar.WeekStrip
import com.example.myapplication.android.ui.components.calendar.WeeklyEventGrid
import com.example.myapplication.android.ui.components.dialogs.CreateMode
import com.example.myapplication.android.ui.components.dialogs.EditablePopupDialog
import com.example.myapplication.android.ui.components.dialogs.FullscreenPopupDialog
import com.example.myapplication.android.ui.components.dialogs.TemplatePrivatePopup
import com.example.myapplication.android.ui.components.dialogs.TemplatePublicPopup
import com.example.myapplication.android.ui.components.headers.TopBarWithDate
import com.example.myapplication.android.ui.components.headers.WeeklyHeader
import com.example.myapplication.android.ui.components.navigation.FloatingTemplateMenu
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.utils.TemplateRepository
import com.example.myapplication.android.ui.core.api.utils.filterByCompanyId
import com.example.myapplication.android.ui.core.api.utils.rememberCompanyId
import com.example.myapplication.android.ui.core.security.JwtUtils
import com.example.myapplication.android.ui.core.security.SessionManager
import com.example.myapplication.android.ui.theme.CustomTheme
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
    screenSource: NavigationSource,
    startInMonthly: Boolean = false,
    onConsumeStartInMonthly: () -> Unit = {},
    startInCreateEvent: Boolean = false,
    onConsumeStartInCreate: () -> Unit = {}
) {
    val colors = CustomTheme.colors

    val calendarState = LocalCalendarState.current
    var selectedDate by calendarState.selectedDate

    val today = remember { Calendar.getInstance() }
    var direction by remember { mutableStateOf(0) }
    var viewMode by remember { mutableStateOf("D") }
    val threshold = 50f
    var cumulativeDrag by remember { mutableStateOf(0f) }

    val shiftTemplates = LocalTemplateState.current
    var selectedTemplate by remember { mutableStateOf<ShiftTemplate?>(null) }
    var showParticipantSelection by remember { mutableStateOf(false) }
    var showStandardDialog by remember { mutableStateOf(false) }
    var showTemplateMenu by remember { mutableStateOf(false) }
    var showNewTemplateDialog by remember { mutableStateOf(false) }
    var showEditTemplateDialog by remember { mutableStateOf(false) }

    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val weekStart = remember(selectedDate.timeInMillis) {
        (selectedDate.clone() as Calendar).apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
    }


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val api = remember { ApiClient.retrofit.create(ApiService::class.java) }
    val templateRepo = remember { TemplateRepository(api) }
    val cid = rememberCompanyId()

    LaunchedEffect(startInMonthly) {
        if (startInMonthly) {
            viewMode = "M"
            onConsumeStartInMonthly()
        }
    }
    LaunchedEffect(startInCreateEvent) {
        if (startInCreateEvent) {
            showStandardDialog = true
            onConsumeStartInCreate()
        }
    }

    // ✅ usa la stessa ViewModel dell'Activity (condivisa con Home)
    val owner = LocalContext.current as ComponentActivity
    val vm: CalendarViewModel = viewModel(owner)

    val managerColor = Color(0xFF7D4F16)
    val isManagerMode = (screenSource == NavigationSource.Drawer && vm.isManager)

    // Prefetch iniziale + slicing
    LaunchedEffect(vm.userId, vm.companyId, screenSource) {
        val y = selectedDate.get(Calendar.YEAR)
        val m = selectedDate.get(Calendar.MONTH)
        vm.userEventYear().join()
        vm.ensureUserShiftsYearLoaded().join()
        if (screenSource == NavigationSource.Drawer) vm.ensureCompanyYearLoadedIfManager().join()
        if (screenSource == NavigationSource.Drawer && vm.isManager) vm.loadCompanyUsersIfManager()
        vm.showMonth(y, m)
        vm.showUserShiftMonth(y, m)
        if (screenSource == NavigationSource.Drawer && vm.isManager) vm.showCompanyMonth(y, m)
    }

    // Slicing su cambio mese/anno
    LaunchedEffect(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), screenSource) {
        vm.showMonth(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH))
        vm.showUserShiftMonth(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH))
        if (screenSource == NavigationSource.Drawer && vm.isManager) {
            vm.showCompanyMonth(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH))
        }
    }

    val userEvents = vm.userEvents
    val companyEvents = vm.companyEvents
    val userShifts = vm.userShifts

    fun mergeDistinct(a: List<CalendarEvent>, b: List<CalendarEvent>): List<CalendarEvent> {
        val merged = a + b
        return merged.distinctBy { ev ->
            val kindKey = when (ev.kind) {
                CalendarItemKind.SHIFT -> "S"
                else -> "E"
            }
            if (ev.id > 0) {
                "$kindKey|ID:${ev.id}"
            } else {
                val d = ev.date
                val dayKey = d.get(Calendar.YEAR) * 400 + d.get(Calendar.DAY_OF_YEAR)
                "$kindKey|D:$dayKey|${ev.startTime}-${ev.endTime}|${ev.title}"
            }
        }
    }


    // derivedStateOf: grafica invariata, refresh immediato
    val visibleEvents by remember(cid, screenSource, vm.isManager) {
        derivedStateOf {
            val raw = if (screenSource == NavigationSource.Drawer && vm.isManager) companyEvents
            else mergeDistinct(userEvents, userShifts)
            raw.filterByCompanyId(cid)
        }
    }

    val userDayEvents by remember(visibleEvents, selectedDate.timeInMillis) {
        derivedStateOf { visibleEvents.filter { it.date.sameDayAs(selectedDate) } }
    }
    val userWeekEvents by remember(visibleEvents, selectedDate.timeInMillis) {
        derivedStateOf { visibleEvents.filter { it.date.sameWeekAs(selectedDate) } }
    }
    val userMonthEvents by remember(visibleEvents, selectedDate.timeInMillis) {
        derivedStateOf {
            visibleEvents.filter {
                it.date.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                        it.date.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.shade100)
                    .padding(horizontal = 8.dp)
            ) {


                TopBarWithDate(
                    selectedDate = selectedDate,
                    onModeChange = { mode -> viewMode = mode },
                    viewMode = viewMode
                )

                // NEW: pill/etichetta evidenziata per modalità manager
                if (isManagerMode) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(managerColor.copy(alpha = 0.10f))
                            .border(1.dp, managerColor, RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = managerColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Modalità manager • Gestione turni",
                                color = managerColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Stai vedendo e gestendo i turni aziendali.",
                        color = managerColor.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))


                if (viewMode == "D") {
                    WeekStrip(
                        selectedDate = selectedDate,
                        direction = direction,
                        onDateSelected = { newDate, newDirection ->
                            direction = newDirection
                            selectedDate = newDate
                        },
                        onSwipeWeek = { swipeDirection ->
                            direction = swipeDirection
                            selectedDate = selectedDate.cloneAndAddDays(7 * swipeDirection)
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                } else if (viewMode == "W") {
                    AnimatedContent(
                        targetState = selectedDate.get(Calendar.WEEK_OF_YEAR) + selectedDate.get(Calendar.YEAR) * 100,
                        transitionSpec = {
                            (slideInHorizontally { width -> direction * width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                        },
                        label = "Weekly Header Animation"
                    ) {
                        WeeklyHeader(
                            weekStart = weekStart,
                            selectedDate = selectedDate,
                            onDayClick = { selectedDate = it },
                            onSwipeWeek = { delta ->
                                direction = delta
                                selectedDate = selectedDate.cloneAndAddDays(7 * delta)
                            }
                        )
                    }
                } else if (viewMode == "M") {
                    var cumulativeMonthDrag by remember { mutableStateOf(0f) }
                    val calendarForMonth = (selectedDate.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onHorizontalDrag = { _, dragAmount -> cumulativeMonthDrag += dragAmount },
                                        onDragEnd = {
                                            when {
                                                cumulativeMonthDrag < -50f -> {
                                                    direction = 1
                                                    selectedDate = (selectedDate.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                                                }
                                                cumulativeMonthDrag > 50f -> {
                                                    direction = -1
                                                    selectedDate = (selectedDate.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                                                }
                                            }
                                            cumulativeMonthDrag = 0f
                                        }
                                    )
                                }
                        ) {
                            AnimatedContent(
                                targetState = selectedDate.get(Calendar.MONTH) + selectedDate.get(Calendar.YEAR) * 12,
                                transitionSpec = {
                                    (slideInHorizontally { width -> direction * width } + fadeIn())
                                        .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                                },
                                label = "Monthly Swipe"
                            ) {
                                MonthlyCalendarGridView(
                                    monthCalendar = calendarForMonth,
                                    selectedDate = selectedDate,
                                    events = userMonthEvents,
                                    onDayClick = { clickedDate -> selectedDate = clickedDate }
                                )
                            }
                        }
                    }
                }
            }

            if (viewMode == "D") {
                var cumulativeDayDrag by remember { mutableStateOf(0f) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 8.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount -> cumulativeDayDrag += dragAmount },
                                onDragEnd = {
                                    val newDate = when {
                                        cumulativeDayDrag < -threshold -> selectedDate.cloneAndAddDays(1).also { direction = 1 }
                                        cumulativeDayDrag > threshold -> selectedDate.cloneAndAddDays(-1).also { direction = -1 }
                                        else -> selectedDate
                                    }
                                    if (!newDate.sameDayAs(selectedDate)) selectedDate = newDate
                                    cumulativeDayDrag = 0f
                                }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = selectedDate.timeInMillis,
                        transitionSpec = {
                            (slideInHorizontally { width -> direction * width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                        },
                        label = "Day Animation"
                    ) {
                        EventList(
                            events = userDayEvents,
                            showParticipants = screenSource == NavigationSource.Drawer,
                            onDelete = { event ->
                                if (event.kind == CalendarItemKind.SHIFT) {
                                    if (screenSource == NavigationSource.Drawer && vm.isManager) {
                                        vm.deleteCompanyShift(event)
                                    } else {
                                        vm.deleteUserShift(event)
                                    }
                                } else {
                                    vm.deleteEvent(event)
                                }
                                // ✅ allinea subito la Home
                                vm.refreshHomeToday()
                            },
                            onReport = {},
                            onEdit = { selectedEvent ->
                                editingEvent = selectedEvent
                                showEditDialog = true
                            }
                        )
                    }
                }
            } else if (viewMode == "W") {
                var cumulativeWeekDrag by remember { mutableStateOf(0f) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount -> cumulativeWeekDrag += dragAmount },
                                onDragEnd = {
                                    when {
                                        cumulativeWeekDrag < -threshold -> {
                                            direction = 1
                                            selectedDate = selectedDate.cloneAndAddDays(7)
                                        }
                                        cumulativeWeekDrag > threshold -> {
                                            direction = -1
                                            selectedDate = selectedDate.cloneAndAddDays(-7)
                                        }
                                    }
                                    cumulativeWeekDrag = 0f
                                }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = selectedDate.get(Calendar.WEEK_OF_YEAR) + selectedDate.get(Calendar.YEAR) * 100,
                        transitionSpec = {
                            (slideInHorizontally { width -> direction * width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                        },
                        label = "Weekly Grid Animation"
                    ) {
                        WeeklyEventGrid(
                            weekStart = weekStart,
                            events = userWeekEvents,
                            showParticipants = screenSource == NavigationSource.Drawer,
                            onEdit = { selectedEvent ->
                                editingEvent = selectedEvent
                                showEditDialog = true
                            }
                        )
                    }
                }
            } else if (viewMode == "M") {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount -> cumulativeDrag += dragAmount },
                                onDragEnd = {
                                    val newDate = when {
                                        cumulativeDrag < -threshold -> { direction = 1; selectedDate.cloneAndAddDays(1) }
                                        cumulativeDrag > threshold -> { direction = -1; selectedDate.cloneAndAddDays(-1) }
                                        else -> selectedDate
                                    }
                                    if (!newDate.sameDayAs(selectedDate)) selectedDate = newDate
                                    cumulativeDrag = 0f
                                }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = selectedDate.timeInMillis,
                        transitionSpec = {
                            (slideInHorizontally { width -> direction * width } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                        },
                        label = "Day Animation"
                    ) {
                        DailyEventList(
                            events = userDayEvents,
                            showParticipants = screenSource == NavigationSource.Drawer,
                            onEdit = { event ->
                                editingEvent = event
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }

        // FAB e Template
        if (screenSource == NavigationSource.Drawer) {
            // Se in futuro riattivi il menu template, lascio qui il blocco commentato originale
            // FloatingTemplateMenu(...)
            FloatingAddButton(onClick = { showStandardDialog = true })
        } else {
            FloatingAddButton(onClick = { showStandardDialog = true })
        }

        if (showStandardDialog) {
            val mode = if (screenSource == NavigationSource.Drawer && vm.isManager)
                CreateMode.PUBLIC_SHIFT else CreateMode.PRIVATE_EVENT

            FullscreenPopupDialog(
                mode = mode,
                onDismiss = { showStandardDialog = false },
                onAddEvent = { ev, userIds, isPublic ->
                    if (isPublic) {
                        // turno pubblico: usa gli ID selezionati
                        vm.createCompanyShift(ev, assignedUserIds = userIds)
                        vm.refreshHomeToday()
                        showStandardDialog = false
                    } else {
                        // evento privato (comportamento già esistente)
                        vm.createEvent(ev, eventType = "PRIVATE")
                        vm.refreshHomeToday()
                        showStandardDialog = false
                    }
                },
                onAddEmployeesClick = { /* non usato più qui, mantenuto per compatibilità */ },
                selectedDate = selectedDate,
                allEmployees = vm.companyEmployees
            )
        }

        if (showNewTemplateDialog) {
            TemplatePrivatePopup(
                onDismiss = { showNewTemplateDialog = false },
                onSave = { newTpl ->
                    scope.launch {
                        val cid = vm.companyId ?: JwtUtils.getCompanyId(SessionManager.getToken(context) ?: "") ?: return@launch
                        val res = templateRepo.create(cid, newTpl)
                        res.onSuccess { created ->
                            shiftTemplates.add(created)
                            showNewTemplateDialog = false
                        }.onFailure { e -> Log.e("Template", "create failed", e) }
                    }
                },
                selectedDate = selectedDate
            )
        }

        if (showEditTemplateDialog && selectedTemplate != null) {
            TemplatePublicPopup(
                template = selectedTemplate!!,
                onDismiss = {
                    showEditTemplateDialog = false
                    selectedTemplate = null
                },
                onSave = { edited ->
                    val oldName = selectedTemplate?.title ?: edited.title
                    scope.launch {
                        val cid = vm.companyId ?: JwtUtils.getCompanyId(SessionManager.getToken(context) ?: "") ?: return@launch
                        val res = templateRepo.update(cid, oldName, edited)
                        res.onSuccess { updated ->
                            shiftTemplates.removeIf { it.title == oldName }
                            shiftTemplates.add(updated)
                            showEditTemplateDialog = false
                            selectedTemplate = null
                        }.onFailure { e -> Log.e("Template", "update failed", e) }
                    }
                },
                onDelete = {
                    val nameToDelete = selectedTemplate?.title ?: return@TemplatePublicPopup
                    scope.launch {
                        val cid = vm.companyId ?: JwtUtils.getCompanyId(SessionManager.getToken(context) ?: "") ?: return@launch
                        val res = templateRepo.delete(cid, nameToDelete)
                        res.onSuccess {
                            shiftTemplates.removeIf { it.title == nameToDelete }
                            showEditTemplateDialog = false
                            selectedTemplate = null
                        }.onFailure { e -> Log.e("Template", "delete failed", e) }
                    }
                }
            )
        }

        val showResetButton = remember(viewMode, selectedDate.timeInMillis) {
            when (viewMode) {
                "D" -> !selectedDate.sameWeekAs(today)
                "W" -> selectedDate.get(Calendar.WEEK_OF_YEAR) != today.get(Calendar.WEEK_OF_YEAR)
                "M" -> !(selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR))
                else -> false
            }
        }
        if (showResetButton) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, bottom = 16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                FloatingActionButton(
                    onClick = { direction = 0; selectedDate = Calendar.getInstance() },
                    containerColor = colors.shade950,
                    shape = CircleShape
                ) {
                    Text("Oggi", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }

    if (showEditDialog && editingEvent != null) {
        EditablePopupDialog(
            eventToEdit = editingEvent!!,
            onDismiss = { showEditDialog = false },
            allEmployees = vm.companyEmployees,
            canEditParticipants = (screenSource == NavigationSource.Drawer && vm.isManager),
            onUpdateEvent = { updated, userIds ->
                if (updated.kind == CalendarItemKind.SHIFT) {
                    if (screenSource == NavigationSource.Drawer && vm.isManager) {
                        vm.patchCompanyShift(updated, maybeAssignedUserIds = userIds)
                    } else {
                        vm.patchUserShift(updated, maybeAssignedUserIds = userIds)
                    }
                } else {
                    vm.patchEvent(updated)
                }
                // ✅ allinea anche la Home (se l’evento/turno è “oggi” la vedrai aggiornata)
                vm.refreshHomeToday()
                showEditDialog = false
            }
        )
    }

    if (showParticipantSelection && selectedTemplate != null) {
        ShiftParticipantSelectionScreen(
            shiftTemplate = selectedTemplate!!,
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
            onBack = {
                showParticipantSelection = false
                selectedTemplate = null
            },
            onConfirm = { newShift, selectedUserIds ->
                if (screenSource == NavigationSource.Drawer && vm.isManager) {
                    vm.createCompanyShift(newShift, assignedUserIds = selectedUserIds)
                } else {
                    vm.createUserShift(newShift, alsoAssignOwner = true)
                }
                // ✅ se crei un turno per oggi, la Home si aggiorna subito
                vm.refreshHomeToday()
                showParticipantSelection = false
                selectedTemplate = null
            },
            allEmployees = vm.companyEmployees,
            events = userWeekEvents
        )
    }
}

/* utils */
fun Calendar.cloneAndAddDays(days: Int): Calendar = (this.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, days) }
fun Calendar.sameDayAs(other: Calendar): Boolean =
    get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
fun Calendar.sameWeekAs(other: Calendar): Boolean =
    this.get(Calendar.WEEK_OF_YEAR) == other.get(Calendar.WEEK_OF_YEAR) &&
            this.get(Calendar.YEAR) == other.get(Calendar.YEAR)
