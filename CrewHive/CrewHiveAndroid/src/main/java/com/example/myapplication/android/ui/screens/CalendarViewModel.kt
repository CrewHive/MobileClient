package com.example.myapplication.android.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.android.ui.core.api.dto.UserIdAndUsernameAndHoursDTO
import com.example.myapplication.android.data.repository.EventRepository
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.ApiClient
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.security.JwtUtils
import com.example.myapplication.android.ui.core.security.SessionManager
import com.example.myapplication.android.ui.state.CompanyEmployee
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(
    private val api: ApiService = ApiClient.retrofit.create(ApiService::class.java),

    private val repo: EventRepository = EventRepository(
        ApiClient.retrofit.create(ApiService::class.java)
    ),
    private val session: SessionManager = SessionManager,
    private val jwt: JwtUtils = JwtUtils
) : ViewModel() {

    companion object {
        private const val TAG = "CalendarVM"
    }

    val userEvents = mutableStateListOf<CalendarEvent>()
    val userShifts = mutableStateListOf<CalendarEvent>()
    val companyEvents = mutableStateListOf<CalendarEvent>()

    val companyEmployees = mutableStateListOf<CompanyEmployee>()
    var isLoadingCompanyUsers by mutableStateOf(false)
    var companyUsersError by mutableStateOf<String?>(null)

    private val token: String get() = TokenManager.jwtToken ?: ""

    val isManager: Boolean
        get() = JwtUtils.isManager(token)

    val userId: Long?
        get() = JwtUtils.getUserId(token)

    val companyId: Long?
        get() = JwtUtils.getCompanyId(token)

    // --------------------------------------------
    // Stato per slicing precedente (per patch)
    // --------------------------------------------
    private var lastUserYearMonth: Pair<Int, Int>? = null
    private var lastShiftYearMonth: Pair<Int, Int>? = null
    private var lastCompanyYearMonth: Pair<Int, Int>? = null

    // --------------------------------------------
    // PENDING CACHES (created/deleted) per mese
    // --------------------------------------------
    private val pendingUserShiftsByMonth =
        mutableMapOf<Pair<Int, Int>, MutableList<CalendarEvent>>()
    private val pendingCompanyShiftsByMonth =
        mutableMapOf<Pair<Int, Int>, MutableList<CalendarEvent>>()

    private val pendingDeletedUserShiftIdsByMonth =
        mutableMapOf<Pair<Int, Int>, MutableSet<Long>>()
    private val pendingDeletedCompanyShiftIdsByMonth =
        mutableMapOf<Pair<Int, Int>, MutableSet<Long>>()

    // --- sotto agli altri pending ---
    private val pendingUserEventsByMonth =
        mutableMapOf<Pair<Int, Int>, MutableList<CalendarEvent>>()

    private val pendingDeletedUserEventIdsByMonth =
        mutableMapOf<Pair<Int, Int>, MutableSet<Long>>()

    // --- Stato "oggi" DEDICATO (NUOVO) ---
    val todayUserEvents = mutableStateListOf<CalendarEvent>()
    val todayUserShifts = mutableStateListOf<CalendarEvent>()


    /* ---------- helpers ---------- */

    private fun ymKey(cal: Calendar): Pair<Int, Int> =
        cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)

    private fun isToday2(cal: Calendar): Boolean {
        val t = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == t.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR)
    }

    private fun removeFromPendingBuckets(shift: CalendarEvent) {
        val key = ymKey(shift.date)
        pendingUserShiftsByMonth[key]?.removeAll { it.id == shift.id }
        pendingCompanyShiftsByMonth[key]?.removeAll { it.id == shift.id }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshSlicesAfterMutation() {
        Log.d(
            TAG,
            "refreshSlicesAfterMutation: lastUser=$lastUserYearMonth lastShift=$lastShiftYearMonth lastCompany=$lastCompanyYearMonth"
        )
        lastUserYearMonth?.let { (y, m0) -> showMonth(y, m0) }
        lastShiftYearMonth?.let { (y, m0) -> showUserShiftMonth(y, m0) }
        if (isManager) lastCompanyYearMonth?.let { (y, m0) -> showCompanyMonth(y, m0) }
    }

    // ---------------------- Caricamenti legacy ---------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadUserWindow(center: Calendar) = viewModelScope.launch {
        val uid = userId ?: return@launch
        val events = repo.loadUser3Months(center, uid)
        userEvents.clear(); userEvents.addAll(events)
        Log.d(TAG, "loadUserWindow -> userEvents=${userEvents.size}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadCompanyWindowIfManager(center: Calendar) = viewModelScope.launch {
        if (!isManager) return@launch
        val cid = companyId ?: return@launch
        val events = repo.loadCompany3Months(center, cid)
        companyEvents.clear(); companyEvents.addAll(events)
        Log.d(TAG, "loadCompanyWindow -> companyEvents=${companyEvents.size}")
    }

    // ---------------------- Prefetch anno eventi -------------------------

    private var didPrefetchYear = false

    @RequiresApi(Build.VERSION_CODES.O)
    fun userEventYear() = viewModelScope.launch {
        if (didPrefetchYear) return@launch
        val token = TokenManager.jwtToken ?: return@launch
        val uid = JwtUtils.getUserId(token) ?: return@launch
        repo.prefetchUserEventYear(uid)
        didPrefetchYear = true
        Log.d(TAG, "userEventYear prefetched")
    }

    fun showMonth(year: Int, month0: Int) {
        val token = TokenManager.jwtToken ?: return
        val uid = JwtUtils.getUserId(token) ?: return
        lastUserYearMonth = year to month0

        val base = repo.monthSlice(uid, year, month0).toMutableList()
        val key = year to month0

        // 1) filtra gli ID marcati come deleted pendenti
        pendingDeletedUserEventIdsByMonth[key]?.let { deleted ->
            if (deleted.isNotEmpty()) {
                base.removeAll { it.id in deleted }
                val stillThere = base.any { it.id in deleted }
                if (!stillThere) deleted.clear()
            }
        }

        // 2) unisci i pending-created non ancora presenti nello slice repo
        pendingUserEventsByMonth[key]?.let { pendings ->
            if (pendings.isNotEmpty()) {
                val idsInBase = base.asSequence().map { it.id }.toSet()
                pendings.removeAll { it.id in idsInBase } // già arrivati dal repo
                base += pendings
            }
        }

        userEvents.clear()
        userEvents.addAll(base)

        Log.d(TAG, "showMonth y=$year m0=$month0 -> userEvents=${userEvents.size}")
    }

    // ---------------------- Company year --------------------------------

    private var didLoadCompanyYear = false

    @RequiresApi(Build.VERSION_CODES.O)
    fun ensureCompanyYearLoadedIfManager() = viewModelScope.launch {
        if (!isManager) return@launch
        if (didLoadCompanyYear) return@launch
        val cid = companyId ?: return@launch
        val all = repo.loadCompany1YearOnce(cid)
        companyEvents.clear(); companyEvents.addAll(all)
        didLoadCompanyYear = true
        Log.d(TAG, "ensureCompanyYearLoadedIfManager -> companyEvents=${companyEvents.size}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showCompanyMonth(year: Int, month0: Int) = viewModelScope.launch {
        if (!isManager) return@launch
        val cid = companyId ?: return@launch
        lastCompanyYearMonth = year to month0

        val cached = repo.getCompanyMonth(year, month0)
            ?: run {
                repo.loadCompany1YearOnce(cid)
                repo.getCompanyMonth(year, month0)
            }

        val base = (cached ?: emptyList()).toMutableList()
        val key = year to month0

        // Filtra gli ID marcati come pending-deleted per questo mese
        pendingDeletedCompanyShiftIdsByMonth[key]?.let { deleted ->
            if (deleted.isNotEmpty()) {
                base.removeAll { it.id in deleted }
                val stillThere = base.any { it.id in deleted }
                if (!stillThere) deleted.clear()
            }
        }

        // Unisci i pending created non ancora presenti nel repo
        pendingCompanyShiftsByMonth[key]?.let { pendings ->
            if (pendings.isNotEmpty()) {
                val idsInBase = base.asSequence().map { it.id }.toSet()
                pendings.removeAll { it.id in idsInBase } // già arrivati dal repo
                base += pendings
            }
        }

        companyEvents.clear(); companyEvents.addAll(base)
        Log.d(TAG, "showCompanyMonth y=$year m0=$month0 -> companyEvents=${companyEvents.size}")
    }

    // ---------------------- Shifts utente -------------------------------

    private var didPrefetchUserShiftsYear = false

    @RequiresApi(Build.VERSION_CODES.O)
    fun ensureUserShiftsYearLoaded() = viewModelScope.launch {
        if (didPrefetchUserShiftsYear) return@launch
        val uid = userId ?: return@launch
        repo.prefetchUserShiftsYear(uid)
        val now = Calendar.getInstance()
        showUserShiftMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        didPrefetchUserShiftsYear = true
        Log.d(TAG, "ensureUserShiftsYearLoaded done")
    }

    fun showUserShiftMonth(year: Int, month0: Int) {
        val uid = userId ?: return
        lastShiftYearMonth = year to month0

        val base = repo.userShiftMonthSlice(uid, year, month0).toMutableList()
        val key = year to month0

        // Filtra gli ID marcati come pending-deleted per questo mese
        pendingDeletedUserShiftIdsByMonth[key]?.let { deleted ->
            if (deleted.isNotEmpty()) {
                base.removeAll { it.id in deleted }
                val stillThere = base.any { it.id in deleted }
                if (!stillThere) deleted.clear()
            }
        }

        // Unisci i pending created non ancora presenti nel repo
        pendingUserShiftsByMonth[key]?.let { pendings ->
            if (pendings.isNotEmpty()) {
                val idsInBase = base.asSequence().map { it.id }.toSet()
                pendings.removeAll { it.id in idsInBase } // già arrivati dal repo
                base += pendings
            }
        }

        userShifts.clear(); userShifts.addAll(base)
        Log.d(TAG, "showUserShiftMonth y=$year m0=$month0 -> userShifts=${userShifts.size}")
    }

    // ----------------------------- CRUD EVENTI ---------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun createEvent(newEvent: CalendarEvent, eventType: String = "PRIVATE") = viewModelScope.launch {
        val uid = userId ?: return@launch
        val temp = newEvent.copy(id = -System.currentTimeMillis())
        userEvents.add(temp)
        touchTodayOnCreate(temp) // ottimistico

        runCatching {
            val created = repo.createUserEvent(uid, newEvent, eventType, alsoAssignOwner = true)
            val idx = userEvents.indexOfFirst { it.id == temp.id }
            if (idx >= 0) userEvents[idx] = created else userEvents.add(created)
            touchTodayOnPatch(temp, created) // allinea “oggi”
            val key = ymKey(created.date)
            val bucket = pendingUserEventsByMonth.getOrPut(key) { mutableListOf() }
            bucket.removeAll { it.id == created.id }
            bucket.add(created)
            showMonth(created.date.get(Calendar.YEAR), created.date.get(Calendar.MONTH))
            refreshHomeToday()
        }.onFailure {
            userEvents.removeAll { it.id == temp.id }
            touchTodayOnDelete(temp) // rollback “oggi”
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun patchEvent(updated: CalendarEvent, eventType: String = "PRIVATE") = viewModelScope.launch {
        val uid = userId ?: return@launch
        val idx = userEvents.indexOfFirst { it.id == updated.id }
        if (idx < 0) return@launch
        val old = userEvents[idx]
        userEvents[idx] = updated
        touchTodayOnPatch(old, updated) // ottimistico

        runCatching {
            repo.updateUserEvent(uid, updated, eventType)
            val oldKey = ymKey(old.date)
            val newKey = ymKey(updated.date)
            pendingUserEventsByMonth[oldKey]?.removeAll { it.id == updated.id }
            val b = pendingUserEventsByMonth.getOrPut(newKey) { mutableListOf() }
            b.removeAll { it.id == updated.id }
            b.add(updated)
            showMonth(updated.date.get(Calendar.YEAR), updated.date.get(Calendar.MONTH))
            refreshHomeToday()
        }.onFailure {
            userEvents[idx] = old
            touchTodayOnPatch(updated, old) // rollback “oggi”
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteEvent(ev: CalendarEvent) = viewModelScope.launch {
        val uid = userId ?: return@launch
        val removed = userEvents.removeAll { it.id == ev.id }
        touchTodayOnDelete(ev) // ottimistico

        val key = ymKey(ev.date)
        pendingUserEventsByMonth[key]?.removeAll { it.id == ev.id }
        val deletedSet = pendingDeletedUserEventIdsByMonth.getOrPut(key) { mutableSetOf() }
        deletedSet += ev.id
        refreshHomeToday()


        runCatching {
            repo.deleteUserEvent(uid, ev.id)
            showMonth(ev.date.get(Calendar.YEAR), ev.date.get(Calendar.MONTH))
        }.onFailure {
            if (removed) userEvents.add(ev)
            pendingDeletedUserEventIdsByMonth[key]?.remove(ev.id)
            touchTodayOnCreate(ev) // rollback “oggi”
        }
    }


    // ----------------------------- CRUD SHIFTS (USER) --------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun createUserShift(newShift: CalendarEvent, alsoAssignOwner: Boolean = true) = viewModelScope.launch {
        val uid = userId ?: return@launch
        val temp = newShift.copy(id = -System.currentTimeMillis(), kind = CalendarItemKind.SHIFT)
        userShifts.add(temp)
        touchTodayOnCreate(temp)

        runCatching {
            val created = repo.createShiftForUser(uid, newShift, alsoAssignOwner).copy(kind = CalendarItemKind.SHIFT)
            val idx = userShifts.indexOfFirst { it.id == temp.id }
            if (idx >= 0) userShifts[idx] = created else userShifts.add(created)
            touchTodayOnPatch(temp, created)
            val key = ymKey(created.date)
            val bucket = pendingUserShiftsByMonth.getOrPut(key) { mutableListOf() }
            bucket.removeAll { it.id == created.id }
            bucket.add(created)
            showUserShiftMonth(created.date.get(Calendar.YEAR), created.date.get(Calendar.MONTH))
            refreshHomeToday()

        }.onFailure {
            userShifts.removeAll { it.id == temp.id }
            touchTodayOnDelete(temp)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun patchUserShift(updated: CalendarEvent, maybeAssignedUserIds: List<Long>? = null) = viewModelScope.launch {
        val uid = userId ?: return@launch
        val idx = userShifts.indexOfFirst { it.id == updated.id }
        if (idx < 0) return@launch
        val old = userShifts[idx]
        userShifts[idx] = updated
        touchTodayOnPatch(old, updated)

        runCatching {
            repo.updateShiftForUser(uid, updated, maybeAssignedUserIds)
            refreshSlicesAfterMutation()
            refreshHomeToday()
        }.onFailure {
            userShifts[idx] = old
            touchTodayOnPatch(updated, old) // rollback
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteUserShift(shift: CalendarEvent) = viewModelScope.launch {
        val uid = userId ?: return@launch
        val removed = userShifts.removeAll { it.id == shift.id }
        touchTodayOnDelete(shift)

        removeFromPendingBuckets(shift)
        val key = ymKey(shift.date)
        val deletedSet = pendingDeletedUserShiftIdsByMonth.getOrPut(key) { mutableSetOf() }
        deletedSet += shift.id

        runCatching {
            repo.deleteShiftForUser(uid, shift.id)
            showUserShiftMonth(shift.date.get(Calendar.YEAR), shift.date.get(Calendar.MONTH))
        }.onFailure {
            if (removed) userShifts.add(shift)
            pendingDeletedUserShiftIdsByMonth[key]?.remove(shift.id)
            touchTodayOnCreate(shift) // rollback
        }
    }

    // ----------------------- COMPANY SHIFTS (manager) --------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun createCompanyShift(shift: CalendarEvent, assignedUserIds: List<Long>) =
        viewModelScope.launch {
            if (!isManager) return@launch
            val temp = shift.copy(id = -System.currentTimeMillis(), kind = CalendarItemKind.SHIFT)
            companyEvents.add(temp)

            runCatching {
                val created = repo.createCompanyShift(assignedUserIds, shift)
                    .copy(kind = CalendarItemKind.SHIFT)
                val idx = companyEvents.indexOfFirst { it.id == temp.id }
                if (idx >= 0) companyEvents[idx] = created else companyEvents.add(created)

                // Se il manager è anche assegnato, riflettilo nella lista userShifts e nei pending user
                val uid = userId
                if (uid != null && assignedUserIds.contains(uid)) {
                    val uidx = userShifts.indexOfFirst { it.id == temp.id }
                    if (uidx >= 0) userShifts[uidx] = created else userShifts.add(created)

                    val kUser = ymKey(created.date)
                    val bUser = pendingUserShiftsByMonth.getOrPut(kUser) { mutableListOf() }
                    bUser.removeAll { it.id == created.id }
                    bUser.add(created)
                }

                // pending company
                val kCompany = ymKey(created.date)
                val bCompany = pendingCompanyShiftsByMonth.getOrPut(kCompany) { mutableListOf() }
                bCompany.removeAll { it.id == created.id }
                bCompany.add(created)

                // slicing locale
                showCompanyMonth(created.date.get(Calendar.YEAR), created.date.get(Calendar.MONTH))

                // ✅ se tocca "oggi" e l'utente è assegnato, aggiorna Home
                if (isToday2(created.date) && userId?.let { assignedUserIds.contains(it) } == true) {
                    refreshHomeToday()
                }
            }.onFailure {
                companyEvents.removeAll { it.id == temp.id }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun patchCompanyShift(updated: CalendarEvent, maybeAssignedUserIds: List<Long>? = null) =
        viewModelScope.launch {
            if (!isManager) return@launch
            val idx = companyEvents.indexOfFirst { it.id == updated.id }
            if (idx < 0) return@launch
            val old = companyEvents[idx]
            companyEvents[idx] = updated
            Log.d(TAG, "patchCompanyShift optimistic id=${updated.id}")

            runCatching {
                repo.updateCompanyShift(updated, maybeAssignedUserIds)
                Log.d(TAG, "patchCompanyShift server ok id=${updated.id}")
                refreshSlicesAfterMutation()
                // ✅ se tocca "oggi", riallinea Home (nel caso l'utente sia tra gli assegnati)
                if (isToday2(old.date) || isToday2(updated.date)) refreshHomeToday()
            }.onFailure {
                Log.e(TAG, "patchCompanyShift FAILED", it)
                companyEvents[idx] = old
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteCompanyShift(shift: CalendarEvent, maybeAssignedUserIds: List<Long>? = null) =
        viewModelScope.launch {
            if (!isManager) return@launch

            val removed = companyEvents.removeAll { it.id == shift.id }
            removeFromPendingBuckets(shift)

            val key = ymKey(shift.date)
            val deletedSet = pendingDeletedCompanyShiftIdsByMonth.getOrPut(key) { mutableSetOf() }
            deletedSet += shift.id

            runCatching {
                repo.deleteCompanyShift(shift.id, maybeAssignedUserIds)
                showCompanyMonth(shift.date.get(Calendar.YEAR), shift.date.get(Calendar.MONTH))
                // ✅ se era oggi, riallinea Home (se l'utente era assegnato, verrà riflesso)
                if (isToday2(shift.date)) refreshHomeToday()
            }.onFailure {
                if (removed) companyEvents.add(shift)
                pendingDeletedCompanyShiftIdsByMonth[key]?.remove(shift.id)
            }
        }

    // ----------------------- Company users --------------------------------

    fun loadCompanyUsersIfManager() = viewModelScope.launch {
        if (!isManager) return@launch
        val cid = companyId ?: return@launch
        isLoadingCompanyUsers = true
        companyUsersError = null

        runCatching {
            val list: List<UserIdAndUsernameAndHoursDTO> = api.getCompanyUsers(cid)

            // aggiorna rubrica ID -> nome nel repository
            repo.updateUserDirectory(list)

            companyEmployees.clear()
            companyEmployees.addAll(
                list.map { dto ->
                    CompanyEmployee(
                        userId = dto.userId.toString(),
                        name = dto.username,
                        weeklyHours = dto.workableHoursPerWeek ?: 0,


                        overtimeHours = 0,
                        vacationDaysAccumulated = 0f,
                        vacationDaysUsed = 0f,
                        leaveDaysAccumulated = 0f,
                        leaveDaysUsed = 0f,

                        contractType = null
                    )
                }
            )

        }.onFailure {
            companyUsersError = it.message
        }

        isLoadingCompanyUsers = false
    }

    fun updateUserDirectory(users: List<UserIdAndUsernameAndHoursDTO>) {
        repo.updateUserDirectory(users)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshHomeToday() = viewModelScope.launch {
        val uid = userId ?: return@launch

        runCatching { repo.loadUserDayEvents(uid) }
            .onSuccess { list ->
                todayUserEvents.clear()
                todayUserEvents.addAll(list)
            }

        runCatching { repo.loadUserDayShifts(uid) }
            .onSuccess { list ->
                todayUserShifts.clear()
                todayUserShifts.addAll(list)
            }
    }





    private fun clearVmState() {
        userEvents.clear()
        userShifts.clear()
        companyEvents.clear()

        pendingUserShiftsByMonth.clear()
        pendingCompanyShiftsByMonth.clear()
        pendingDeletedUserShiftIdsByMonth.clear()
        pendingDeletedCompanyShiftIdsByMonth.clear()
        pendingUserEventsByMonth.clear()
        pendingDeletedUserEventIdsByMonth.clear()

        lastUserYearMonth = null
        lastShiftYearMonth = null
        lastCompanyYearMonth = null

        didPrefetchYear = false
        didLoadCompanyYear = false
        didPrefetchUserShiftsYear = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onCompanyChanged() = viewModelScope.launch {
        // svuota repository + stato locale
        repo.clearAllCaches()

        clearVmState()

        // ricarica lo stretto necessario per “oggi” e mese corrente
        val now = Calendar.getInstance()

        userEventYear().join()
        ensureUserShiftsYearLoaded().join()
        if (isManager) ensureCompanyYearLoadedIfManager().join()

        showMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        showUserShiftMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        if (isManager) showCompanyMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))

        // ✅ popola subito la Home (oggi)
        refreshHomeToday()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun workedHoursThisWeekFloor(): Int {
        val uid = userId ?: return 0

        // assicurati di avere in cache tutti gli shift dell'anno (usiamo le slice mensili della repo)
        ensureUserShiftsYearLoaded().join()

        val now = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // lunedì ore 00:00 della settimana corrente
        val mondayStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        }

        // raccogli al massimo i 2 mesi coinvolti (settimana può attraversare il cambio mese/anno)
        val monthsToScan = mutableSetOf<Pair<Int, Int>>() // (year, month0)
        run {
            val c = (mondayStart.clone() as Calendar)
            while (c.timeInMillis <= now.timeInMillis) {
                monthsToScan += c.get(Calendar.YEAR) to c.get(Calendar.MONTH)
                c.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // prendi gli shift dell’utente dalle slice del/i mese/i
        val shifts = monthsToScan.flatMap { (y, m0) ->
            repo.userShiftMonthSlice(uid, y, m0)
        }

        fun parseHm(hm: String): Pair<Int, Int> {
            val p = hm.split(":")
            val h = p.getOrNull(0)?.toIntOrNull() ?: 0
            val m = p.getOrNull(1)?.toIntOrNull() ?: 0
            return h to m
        }

        fun Calendar.at(h: Int, m: Int): Calendar =
            (clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        var totalMinutes = 0L
        val mondayMs = mondayStart.timeInMillis
        val nowMs = now.timeInMillis

        for (ev in shifts) {
            // Consideriamo SOLO shift (ma userShiftMonthSlice ritorna già turni dell’utente)
            val (sh, sm) = parseHm(ev.startTime)
            val (eh, em) = parseHm(ev.endTime)

            val evStart = ev.date.at(sh, sm)
            val evEnd = ev.date.at(eh, em).apply {
                // se finisce dopo la mezzanotte, porta a giorno successivo
                if (timeInMillis <= evStart.timeInMillis) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            // clamping su finestra [mondayStart, now]
            val startClamped = maxOf(evStart.timeInMillis, mondayMs)
            val endClamped   = minOf(evEnd.timeInMillis, nowMs)

            if (endClamped > startClamped) {
                val minutes = (endClamped - startClamped) / (1000L * 60L)
                totalMinutes += minutes
            }
        }

        // arrotonda per difetto alle ore intere
        return (totalMinutes / 60L).toInt()
    }
    private fun Calendar.isToday(): Boolean {
        val now = Calendar.getInstance()
        return get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }

    private fun MutableList<CalendarEvent>.replaceById(item: CalendarEvent) {
        val i = indexOfFirst { it.id == item.id }
        if (i >= 0) this[i] = item else add(item)
    }

    private fun touchTodayOnCreate(ev: CalendarEvent) {
        if (!ev.date.isToday()) return
        if (ev.kind == CalendarItemKind.SHIFT) todayUserShifts.replaceById(ev)
        else todayUserEvents.replaceById(ev)
    }

    private fun touchTodayOnPatch(old: CalendarEvent, updated: CalendarEvent) {
        if (old.date.isToday()) {
            if (old.kind == CalendarItemKind.SHIFT) todayUserShifts.removeAll { it.id == old.id }
            else todayUserEvents.removeAll { it.id == old.id }
        }
        if (updated.date.isToday()) {
            if (updated.kind == CalendarItemKind.SHIFT) todayUserShifts.replaceById(updated)
            else todayUserEvents.replaceById(updated)
        }
    }

    private fun touchTodayOnDelete(ev: CalendarEvent) {
        if (!ev.date.isToday()) return
        if (ev.kind == CalendarItemKind.SHIFT) todayUserShifts.removeAll { it.id == ev.id }
        else todayUserEvents.removeAll { it.id == ev.id }
    }


    fun companyUserPairs(): List<Pair<Long, String>> =
        companyEmployees.mapNotNull { ce ->
            ce.userId.toLongOrNull()?.let { it to ce.name }
        }
}
