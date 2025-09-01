// FILE: HomeScreen.kt
package com.example.myapplication.android.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.android.state.CalendarState
import com.example.myapplication.android.state.LocalCalendarState
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import com.example.myapplication.android.ui.components.calendar.TodaySectionComponent
import com.example.myapplication.android.ui.components.charts.TotalWeekHoursComponent
import com.example.myapplication.android.ui.components.dialogs.EditablePopupDialog
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.components.headers.TopBarComponent
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.api.utils.filterByCompanyId
import com.example.myapplication.android.ui.core.api.utils.rememberCompanyId
import com.example.myapplication.android.ui.core.security.JwtUtils
import com.example.myapplication.android.ui.theme.CustomTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/* ---------- Utils ---------- */

private fun Calendar.isSameDay(other: Calendar): Boolean =
    get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)

private fun startOfWeekMonday(now: Calendar = Calendar.getInstance()): Calendar =
    (now.clone() as Calendar).apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    }

private fun calendarAtTime(baseDate: Calendar, hhmm: String): Calendar {
    val (h, m) = hhmm.split(":").let {
        val hh = it.getOrNull(0)?.toIntOrNull() ?: 0
        val mm = it.getOrNull(1)?.toIntOrNull() ?: 0
        hh to mm
    }
    return (baseDate.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, h)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

/** Somma ore (solo SHIFT) da lun 00:00 a ora, clampando e arrotondando per difetto. */
private fun weeklyWorkedHoursSinceMonday(
    shifts: List<CalendarEvent>,
    now: Calendar = Calendar.getInstance()
): Int {
    val monday = startOfWeekMonday(now)
    val mondayMs = monday.timeInMillis
    val nowMs = now.timeInMillis
    var totalMillis = 0L

    shifts.forEach { ev ->
        if (ev.kind != CalendarItemKind.SHIFT) return@forEach
        val s = calendarAtTime(ev.date, ev.startTime)
        var e = calendarAtTime(ev.date, ev.endTime)
        if (e.timeInMillis <= s.timeInMillis) {
            e = (e.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        }
        val sClamp = max(s.timeInMillis, mondayMs)
        val eClamp = min(e.timeInMillis, nowMs)
        if (eClamp > sClamp) totalMillis += (eClamp - sClamp)
    }
    return floor(totalMillis / 3_600_000.0).toInt()
}

/* ---------- Screen ---------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    weeklyTarget: Int = 40,
    onOpenMonthlyCalendar: () -> Unit = {},
    onOpenEmployees: () -> Unit = {},
    onStartCreateEvent: (() -> Unit)? = null
) {
    val colors = CustomTheme.colors

    // ✅ stessa ViewModel dell'Activity (condivisa con CalendarScreen)
    val owner = LocalContext.current as ComponentActivity
    val vm: CalendarViewModel = viewModel(owner)

    val companyId = rememberCompanyId()
    val isManager = remember { JwtUtils.isManager(TokenManager.jwtToken ?: "") }

    val selectedDate = remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (vm.userId == null) { kotlinx.coroutines.delay(50) }
        vm.refreshHomeToday()
        vm.ensureUserShiftsYearLoaded()
    }

    LaunchedEffect(vm.userId, vm.companyId) {
        if (vm.userId != null) vm.refreshHomeToday()
    }


    // Refresh al rientro in foreground (solo oggi)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) vm.refreshHomeToday()
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }


    // elementi di OGGI già filtrati dalle API
    val todayItems by remember(vm.todayUserEvents, vm.todayUserShifts, companyId) {
        derivedStateOf { (vm.todayUserEvents + vm.todayUserShifts).filterByCompanyId(companyId) }
    }

    val weeklyHoursWorked by remember(vm.todayUserShifts, companyId) {
        derivedStateOf { weeklyWorkedHoursSinceMonday(vm.todayUserShifts.filterByCompanyId(companyId)) }
    }


    // Stato locale per dialog edit
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Stato per TodaySection (usa solo i dati del giorno)
    val todayState = remember {
        CalendarState(
            selectedDate = selectedDate,
            userEvents = mutableStateListOf()
        )
    }
    // Mantiene la lista "Oggi" in sync
    SideEffect {
        todayState.userEvents.clear()
        todayState.userEvents.addAll(todayItems)
    }

    /* ---------- UI ---------- */

    val brandCream = Color(0xFFFFF8E1)
    val brandBrown = Color(0xFF7D4F16)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarComponent.TopBar()
        Spacer(Modifier.height(12.dp))

        HeaderCard(
            background = brandCream,
            textColor = Color(0xFF5D4037),
            accent = brandBrown
        )

        Spacer(Modifier.height(14.dp))

        CompositionLocalProvider(LocalCalendarState provides todayState) {
            TodaySectionComponent.TodaySection(
                showParticipants = false,
                onDelete = { ev ->
                    if (ev.kind == CalendarItemKind.SHIFT) vm.deleteUserShift(ev) else vm.deleteEvent(ev)
                    vm.refreshHomeToday() // update immediato
                },
                onEdit = { ev ->
                    editingEvent = ev
                    showEditDialog = true
                },
                onReport = { /* opzionale */ }
            )
        }

        Spacer(Modifier.height(18.dp))
        Divider(
            color = colors.shade600.copy(alpha = 0.3f),
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
        )
        Spacer(Modifier.height(18.dp))

        QuickActionsGrid(
            isManager = isManager,
            onNewEvent = { onStartCreateEvent?.invoke() },
            onOpenCalendar = onOpenMonthlyCalendar,
            onOvertime = onOpenEmployees
        )

        Spacer(Modifier.height(16.dp))

        TipsCarousel(isManager = isManager, tint = brandBrown, bg = brandCream)

        Spacer(Modifier.height(20.dp))
        Divider(
            color = colors.shade600.copy(alpha = 0.3f),
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
        )
        Spacer(Modifier.height(20.dp))

        TotalWeekHoursComponent.TotalWeekHours(
            current = weeklyHoursWorked,
            max = weeklyTarget
        )

        Spacer(Modifier.height(24.dp))
    }

    // Dialog di edit
    if (showEditDialog && editingEvent != null) {
        EditablePopupDialog(
            eventToEdit = editingEvent!!,
            onDismiss = { showEditDialog = false; editingEvent = null },
            allEmployees = vm.companyEmployees,
            canEditParticipants = false,
            onUpdateEvent = { updated, userIds ->
                if (updated.kind == CalendarItemKind.SHIFT) {
                    vm.patchUserShift(updated, maybeAssignedUserIds = userIds)
                } else {
                    vm.patchEvent(updated)
                }
                vm.refreshHomeToday()   // riallinea la slice "Oggi"
                showEditDialog = false
                editingEvent = null
            }
        )
    }
}

/* ---------- Partials ---------- */

@Composable
private fun HeaderCard(
    background: Color,
    textColor: Color,
    accent: Color
) {
    val shape = RoundedCornerShape(20.dp)
    val dateFmt = remember { SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()) }
    val today = remember { Calendar.getInstance() }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = background),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                color = accent.copy(alpha = 0.12f),
                tonalElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.ThumbUp,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Benvenuto!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                )
                Text(
                    text = dateFmt.format(today.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge.copy(color = textColor.copy(alpha = 0.8f))
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    isManager: Boolean,
    onNewEvent: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOvertime: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Nuovo evento",
                icon = Icons.Filled.Notifications,
                modifier = Modifier.weight(1f),
                shape = shape,
                onClick = onNewEvent
            )
            ActionCard(
                title = "Calendario (mensile)",
                icon = Icons.Filled.DateRange,
                modifier = Modifier.weight(1f),
                shape = shape,
                onClick = onOpenCalendar
            )
        }

        if (isManager) {
            ActionCard(
                title = "Gestisci dipendenti",
                icon = Icons.Filled.Face,
                shape = shape,
                modifier = Modifier.fillMaxWidth(),
                onClick = onOvertime
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    onClick: () -> Unit
) {
    val brandCream = Color(0xFFFFF8E1)
    val brandBrown = Color(0xFF7D4F16)

    ElevatedCard(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = brandCream,
                tonalElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = brandBrown,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun TipsCarousel(
    isManager: Boolean,
    tint: Color,
    bg: Color
) {
    // Suggerimenti validi per tutti
    val commonTips = remember {
        listOf(
            "Suggerimento: tocca un elemento di \"Oggi\" per aprire modifica rapida.",
            "Gli straordinari accettano formato HH:MM: scrivi 1:30 e ci pensiamo noi.",
            "Ferie e permessi si inseriscono anche a step di 0.1 giorni.",
            "Il riepilogo \"Ore settimana\" si aggiorna automaticamente dopo ogni modifica.",
            "Usa la lente in \"Dipendenti\" per filtrare velocemente per nome.",
            "Dalla schermata mensile e settimanale puoi avere il quadro di tutta la settimana in un colpo.",
            "La lista \"Oggi\" si aggiorna da sola quando rientri nell’app.",
            "Nella selezione partecipanti la somma ore mostra anche il turno selezionato."
        )
    }

    // Suggerimenti aggiuntivi per manager (senza menzionare i modelli)
    val managerTips = remember {
        listOf(
            "Solo manager: in \"Dipendenti\" aggiorni contratto, ore, ferie, permessi e straordinari.",
            "Solo manager: nella selezione partecipanti vedi le ore settimanali con il turno proposto.",
            "Solo manager: puoi modificare o annullare i turni dalla vista Gestionale turni.",
            "Solo manager: i valori devono essere positivi con massimo 2 decimali (es. 9.4).",
            "Solo manager: assegna più persone allo stesso turno in un’unica conferma.",
            "Solo manager: dopo aver salvato i dettagli di un dipendente, il profilo si ricarica da solo.",
            "Solo manager: usa la ricerca per evitare conflitti e sovrapposizioni di orario."
        )
    }
    val pool = remember(isManager) { if (isManager) commonTips + managerTips else commonTips }

    var idx by remember { mutableStateOf(0) }
    LaunchedEffect(pool) {
        while (true) {
            delay(6500)
            idx = (idx + 1) % pool.size
        }
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clickable { idx = (idx + 1) % pool.size }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                color = tint.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource( id = R.drawable.suggeriti),
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = pool[idx],
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
