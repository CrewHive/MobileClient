package com.example.myapplication.android.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication.android.state.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.NavigationSource
import com.example.myapplication.android.state.LocalCalendarState
import com.example.myapplication.android.ui.components.buttons.FloatingAddButton
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.DailyEventList
import com.example.myapplication.android.ui.components.calendar.EventList
import com.example.myapplication.android.ui.components.calendar.MonthlyCalendarGridView
import com.example.myapplication.android.ui.components.calendar.WeekStrip
import com.example.myapplication.android.ui.components.calendar.WeeklyEventGrid
import com.example.myapplication.android.ui.components.dialogs.EditablePopupDialog
import com.example.myapplication.android.ui.components.dialogs.FullscreenPopupDialog
import com.example.myapplication.android.ui.components.dialogs.TemplatePrivatePopup
import com.example.myapplication.android.ui.components.dialogs.TemplatePublicPopup
import com.example.myapplication.android.ui.components.headers.TopBarWithDate
import com.example.myapplication.android.ui.components.headers.WeeklyHeader
import com.example.myapplication.android.ui.components.navigation.FloatingTemplateMenu
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import com.example.myapplication.android.ui.theme.CustomTheme
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(screenSource: NavigationSource) {
    val colors = CustomTheme.colors

    val calendarState = LocalCalendarState.current
    val currentUser = LocalCurrentUser.current
    var selectedDate by calendarState.selectedDate
    val userEvents = calendarState.userEvents

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
        (selectedDate.clone() as Calendar).apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }

    // Popola eventi una sola volta
    LaunchedEffect(Unit) {
        if (userEvents.isEmpty()) {
            val start = (Calendar.getInstance().clone() as Calendar).apply {
                add(Calendar.MONTH, -6)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            repeat(365) { offset ->
                val date = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
                val generated = generateEventsFor(date)
                generated.forEach { ev ->
                    if (userEvents.none { it.title == ev.title && it.date.sameDayAs(ev.date) }) {
                        userEvents.add(ev)
                    }
                }
            }
        }
    }

    val visibleEvents = if (screenSource == NavigationSource.Drawer) {
        userEvents.filter { it.participants.isNotEmpty() }
    } else {
        userEvents.filter {
            it.participants.isEmpty() || it.participants.contains(currentUser.value)
        }
    }



    val userDayEvents = visibleEvents.filter { it.date.sameDayAs(selectedDate) }
    val userWeekEvents = visibleEvents.filter { it.date.sameWeekAs(selectedDate) }
    val userMonthEvents = visibleEvents.filter {
        it.date.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                it.date.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
    }


    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().background(colors.shade100).padding(horizontal = 8.dp)) {
                TopBarWithDate(
                    selectedDate = selectedDate,
                    onModeChange = { mode -> viewMode = mode },
                    viewMode = viewMode
                )
                Spacer(modifier = Modifier.height(16.dp))

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
                    Spacer(modifier = Modifier.height(16.dp))
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

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    cumulativeMonthDrag += dragAmount
                                },
                                onDragEnd = {
                                    when {
                                        cumulativeMonthDrag < -50f -> {
                                            direction = 1
                                            selectedDate =
                                                (selectedDate.clone() as Calendar).apply {
                                                    add(Calendar.MONTH, 1)
                                                }
                                        }

                                        cumulativeMonthDrag > 50f -> {
                                            direction = -1
                                            selectedDate =
                                                (selectedDate.clone() as Calendar).apply {
                                                    add(Calendar.MONTH, -1)
                                                }
                                        }
                                    }
                                    cumulativeMonthDrag = 0f
                                }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = selectedDate.get(Calendar.MONTH) + selectedDate.get(
                            Calendar.YEAR
                        ) * 12,
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
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .padding(top = 8.dp, end = 8.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount -> cumulativeDrag += dragAmount },
                                onDragEnd = {
                                    val newDate = when {
                                        cumulativeDrag < -threshold -> selectedDate.cloneAndAddDays(1).also { direction = 1 }
                                        cumulativeDrag > threshold -> selectedDate.cloneAndAddDays(-1).also { direction = -1 }
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
                        EventList(
                            events = userDayEvents,
                            showParticipants = screenSource == NavigationSource.Drawer,
                            onDelete = { event -> userEvents.remove(event) },
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
                                onHorizontalDrag = { _, dragAmount ->
                                    cumulativeWeekDrag += dragAmount
                                },
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
                        targetState = selectedDate.get(Calendar.WEEK_OF_YEAR) + selectedDate.get(
                            Calendar.YEAR
                        ) * 100,
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
                                onHorizontalDrag = { _, dragAmount ->
                                    cumulativeDrag += dragAmount
                                },
                                onDragEnd = {
                                    val newDate = when {
                                        cumulativeDrag < -threshold -> {
                                            direction = 1
                                            selectedDate.cloneAndAddDays(1)
                                        }

                                        cumulativeDrag > threshold -> {
                                            direction = -1
                                            selectedDate.cloneAndAddDays(-1)
                                        }

                                        else -> selectedDate
                                    }
                                    if (!newDate.sameDayAs(selectedDate)) {
                                        selectedDate = newDate
                                    }
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

        // Floating Button logica completa
        if (screenSource == NavigationSource.Drawer) {
            FloatingTemplateMenu(
                visible = showTemplateMenu,
                templates = shiftTemplates,
                onTemplateClick = { template ->
                    Log.d("CalendarScreen", "Evento selezionato: $selectedTemplate")
                    selectedTemplate = template
                    showParticipantSelection = true
                    showTemplateMenu = false
                },
                onTemplateLongClick = { template ->
                    selectedTemplate = template
                    showEditTemplateDialog = true
                },
                onNewClick = { showNewTemplateDialog = true },
                onEditClick = { showStandardDialog = true }
            )

            FloatingAddButton(onClick = { showTemplateMenu = !showTemplateMenu })
        } else {
            FloatingAddButton(onClick = { showStandardDialog = true })
        }

        if (showStandardDialog) {
            FullscreenPopupDialog(
                onDismiss = { showStandardDialog = false },
                onAddEvent = { userEvents.add(it); showStandardDialog = false },
                selectedDate = selectedDate
            )
        }
        if (showNewTemplateDialog) {
            TemplatePrivatePopup(
                onDismiss = { showNewTemplateDialog = false },
                onSave = { shiftTemplates.add(it); showNewTemplateDialog = false },
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
                onSave = {
                    shiftTemplates.removeIf { it.title == it.title }
                    shiftTemplates.add(it)
                    showEditTemplateDialog = false
                    selectedTemplate = null
                },
                onDelete = {
                    shiftTemplates.removeIf { it.title == selectedTemplate?.title }
                    showEditTemplateDialog = false
                    selectedTemplate = null
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
                    onClick = {
                        direction = 0
                        selectedDate = Calendar.getInstance()
                    },
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
            onUpdateEvent = { updated ->
                userEvents.removeIf { it == editingEvent }
                userEvents.add(updated)
                showEditDialog = false
            }
        )
    }
    if (showParticipantSelection && selectedTemplate != null) {

        val generatedEvents=visibleEvents.filter { it.date.sameWeekAs(selectedDate) } +
                (0..6).flatMap { offset ->
                    val date = (weekStart.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_MONTH, offset)
                    }
                    generateEventsFor(date)
                }
        val nextWeek = (selectedDate.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
        val generatedEventsDayAfter=visibleEvents.filter { it.date.sameWeekAs(nextWeek) } +
                (0..6).flatMap { offset ->
                    val date = (weekStart.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_MONTH, offset)
                    }
                    generateEventsFor(date)
                }

        val events = generatedEvents + generatedEventsDayAfter




        ShiftParticipantSelectionScreen(
            shiftTemplate = selectedTemplate!!,
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
            onBack = {
                showParticipantSelection = false
                selectedTemplate = null
            },
            onConfirm = { newEvent ->
                userEvents.add(newEvent)
                showParticipantSelection = false
                selectedTemplate = null
            },
            events = userWeekEvents
        )
    }
}


fun generateEventsFor(date: Calendar): List<CalendarEvent> {
    val random = kotlin.random.Random(date.get(Calendar.DAY_OF_YEAR))
    val participants = GlobalParticipants.list

    val events = mutableListOf<CalendarEvent>()

    // Turni standard
    val shifts = listOf(
        Triple("Turno Mattutino", "08:00", "14:00"),
        Triple("Turno Pomeridiano", "14:00", "20:00"),
        Triple("Turno Serale", "20:00", "02:00")
    )

    val shiftColors = listOf(
        Color(0xFF64B5F6), // blu chiaro
        Color(0xFFFFB74D), // arancione
        Color(0xFFBA68C8)  // viola
    )

    // Rotazione settimanale dei turni
    val weekNumber = date.get(Calendar.WEEK_OF_YEAR)
    val participantsPerShift = (participants.size / 3).coerceAtLeast(1)

    shifts.forEachIndexed { index, (title, start, end) ->
        val shiftOffset = (weekNumber + index) % 3
        val shiftGroup = participants.shuffled(random)
            .drop(shiftOffset * participantsPerShift)
            .take(participantsPerShift)

        events.add(
            CalendarEvent(
                startTime = start,
                endTime = end,
                title = title,
                description = "Presidio reparto ${'A' + index}",
                color = shiftColors[index % shiftColors.size],
                date = date.clone() as Calendar,
                participants = shiftGroup
            )
        )
    }

    // Evento extra opzionale
    if (random.nextFloat() < 0.4f) {
        val extraTitles = listOf("Team Meeting", "Briefing", "Formazione", "Aggiornamento Sicurezza")
        val extraDescriptions = listOf(
            "Aggiornamenti sul progetto corrente",
            "Sincronizzazione con il team",
            "Sessione di formazione obbligatoria",
            "Revisione procedure di sicurezza"
        )
        val timeSlots = listOf("10:00" to "11:00", "15:00" to "16:00", "12:00" to "13:00")

        val idx = random.nextInt(extraTitles.size)
        val (start, end) = timeSlots.random(random)
        val extraParticipants = participants.shuffled(random).take(3 + random.nextInt(3))

        events.add(
            CalendarEvent(
                startTime = start,
                endTime = end,
                title = extraTitles[idx],
                description = extraDescriptions[idx],
                color = Color(0xFFE57373), // rosso
                date = date.clone() as Calendar,
                participants = extraParticipants
            )
        )
    }

    return events
}



fun calculateWeekOffset(start: Calendar, target: Calendar): Int {
    val millisPerWeek = 1000L * 60 * 60 * 24 * 7
    return ((target.timeInMillis - start.timeInMillis) / millisPerWeek).toInt()
}

fun Calendar.cloneAndAddDays(days: Int): Calendar =
    (this.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, days) }

fun Calendar.sameDayAs(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
}

fun Calendar.sameWeekAs(other: Calendar): Boolean {
    return this.get(Calendar.WEEK_OF_YEAR) == other.get(Calendar.WEEK_OF_YEAR) &&
            this.get(Calendar.YEAR) == other.get(Calendar.YEAR)
}

