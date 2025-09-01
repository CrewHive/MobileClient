package com.example.myapplication.android.ui.state

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.android.state.CalendarState
import com.example.myapplication.android.state.LocalCalendarState
import com.example.myapplication.android.state.LocalCurrentUser
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.screens.CalendarViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProvideCalendarState(
    // Se usi lo stesso VM in più schermate, questo viewModel() punta allo stesso store della NavHost
    vm: CalendarViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    // Oggi non è modificabile qui, ma teniamo selectedDate per compatibilità con CalendarState
    val selectedDateState = remember { mutableStateOf(Calendar.getInstance()) }
    val todayEvents: SnapshotStateList<CalendarEvent> = remember { mutableStateListOf() }

    // Carica i dati minimi per avere eventi/shift del mese corrente (se non già in cache)
    LaunchedEffect(Unit) {
        // prefetch year per eventi/shift (sono one-shot nel VM)
        vm.userEventYear().join()
        vm.ensureUserShiftsYearLoaded().join()

        val now = Calendar.getInstance()
        vm.showMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        vm.showUserShiftMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        if (vm.isManager && vm.companyId != null) {
            vm.ensureCompanyYearLoadedIfManager().join()
            vm.showCompanyMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        }
    }

    // Osserva i cambiamenti nelle liste del VM e aggiorna *solo gli eventi di oggi*
    val me = LocalCurrentUser.current
    LaunchedEffect(me.value) {
        snapshotFlow { vm.userEvents.toList() to vm.userShifts.toList() }
            .collectLatest { (events, shifts) ->
                val today = Calendar.getInstance()
                val merged = (events + shifts).distinctBy { e ->
                    // chiave di deduplica per stesso giorno/fascia/titolo
                    val d = e.date
                    val dayKey = d.get(Calendar.YEAR) * 400 + d.get(Calendar.DAY_OF_YEAR)
                    "$dayKey|${e.startTime}-${e.endTime}|${e.title}"
                }

                val visible = merged.filter { e ->
                    e.date.sameDayAs(today) &&
                            (e.participants.isEmpty() || e.participants.contains(me.value))
                }.sortedBy { it.startTime }

                todayEvents.clear()
                todayEvents.addAll(visible)
            }
    }

    CompositionLocalProvider(
        LocalCalendarState provides CalendarState(
            selectedDate = selectedDateState,
            userEvents = todayEvents // <-- qui mettiamo SOLO gli eventi di oggi
        )
    ) {
        content()
    }
}

// Helper se non l’hai già da qualche parte
private fun Calendar.sameDayAs(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
}