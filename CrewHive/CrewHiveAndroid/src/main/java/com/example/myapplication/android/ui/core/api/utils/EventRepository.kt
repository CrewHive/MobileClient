package com.example.myapplication.android.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.android.ui.core.api.dto.UserIdAndUsernameAndHoursDTO
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.core.api.dto.CreateEventDTO
import com.example.myapplication.android.ui.core.api.dto.PatchEventDTO
import com.example.myapplication.android.ui.core.api.dto.PatchShiftProgrammedDTO
import com.example.myapplication.android.ui.core.api.service.ApiService
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.mappers.EventMapper
import com.example.myapplication.android.ui.core.mappers.toCreateShiftDTO
import com.example.myapplication.android.ui.core.security.JwtUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar

class EventRepository(private val api: ApiService) {

    companion object {
        private const val TAG = "EventRepoDebug"
        private const val DEBUG = true
    }

    // -------------------------  CACHE (MONTH)  ---------------------------
    private val userCache = mutableMapOf<Pair<Int, Int>, List<CalendarEvent>>()   // eventi utente
    private val companyCache = mutableMapOf<Pair<Int, Int>, List<CalendarEvent>>()// shift company
    private val userShiftCache = mutableMapOf<Pair<Int, Int>, List<CalendarEvent>>()// shift utente

    // -------------------------  CACHE (YEAR)   ---------------------------
    private val userYearCache = mutableMapOf<Long, List<CalendarEvent>>()
    private val userYearLoaded = mutableSetOf<Long>()

    private val companyYearCache = mutableMapOf<Long, List<CalendarEvent>>()
    private val companyYearLoaded = mutableSetOf<Long>()

    private val userShiftYearCache = mutableMapOf<Long, List<CalendarEvent>>()
    private val userShiftYearLoaded = mutableSetOf<Long>()

    // -------------------------  TOMBSTONES  ------------------------------
    private val deletedEventIds = mutableSetOf<Long>()
    private val deletedShiftIds = mutableSetOf<Long>()

    private fun List<CalendarEvent>.withoutTombstones(): List<CalendarEvent> =
        this.filterNot { ev -> deletedEventIds.contains(ev.id) || deletedShiftIds.contains(ev.id) }

    // ----------------------------- DEBUG ---------------------------------

    private fun CalendarEvent.mKey(): Pair<Int, Int> =
        date.get(Calendar.YEAR) to date.get(Calendar.MONTH)

    private fun List<CalendarEvent>.idsStr(): String = joinToString(",") { it.id.toString() }
    private fun Set<Long>.idsStr(): String = joinToString(",")

    private fun dumpMonthCache(name: String, cache: Map<Pair<Int, Int>, List<CalendarEvent>>) {
        if (!DEBUG) return
        val size = cache.values.sumOf { it.size }
        val keys = cache.keys.sortedWith(compareBy({ it.first }, { it.second }))
        Log.d(TAG, "[$name] total=$size months=${keys.size} keys=$keys")
        keys.take(6).forEach { k ->
            val list = cache[k].orEmpty()
            Log.d(TAG, "[$name]  $k -> count=${list.size} ids=[${list.idsStr()}]")
        }
    }

    private fun dumpYearCache(name: String, cache: Map<Long, List<CalendarEvent>>) {
        if (!DEBUG) return
        Log.d(TAG, "[$name] owners=${cache.keys}")
        cache.forEach { (owner, list) ->
            Log.d(TAG, "[$name]  owner=$owner count=${list.size} ids=[${list.idsStr()}]")
        }
    }

    fun debugDumpAllCaches() {
        if (!DEBUG) return
        Log.d(TAG, "===== DUMP CACHES =====")
        Log.d(TAG, "tombstones: events=[${deletedEventIds.idsStr()}], shifts=[${deletedShiftIds.idsStr()}]")

        dumpYearCache("userYearCache(EVENTI)", userYearCache)
        dumpMonthCache("userCache(EVENTI)", userCache)

        dumpYearCache("companyYearCache(SHIFT)", companyYearCache)
        dumpMonthCache("companyCache(SHIFT)", companyCache)

        dumpYearCache("userShiftYearCache(SHIFT)", userShiftYearCache)
        dumpMonthCache("userShiftCache(SHIFT)", userShiftCache)

        Log.d(TAG, "===== /DUMP CACHES =====")
    }

    // -------------------------  LOAD WINDOWS  ----------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadUser3Months(center: Calendar, userId: Long?): List<CalendarEvent> {
        val months = monthsAround(center)
        val all = months.flatMap { (_y, _m) ->
            val period = "MONTH"

            // 1) fetch DTO
            val dto = api.getEventsByPeriodAndUser(period, userId)

            // 2) filtra per company (fail-open se il DTO non espone companyId)
            val dtoFiltered = dto.filterByCompany { extractCompanyIdFromEventAny(it as Any) }

            // 3) mappa -> UI model
            val events = dtoFiltered.map(EventMapper::fromEvent)

            val filtered = events.withoutTombstones()
            indexByMonth(userCache, filtered)
            filtered
        }
        trimTo12(userCache)
        if (DEBUG) {
            Log.d(TAG, "loadUser3Months done for userId=$userId, total=${all.size}")
            debugDumpAllCaches()
        }
        return all
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadCompany3Months(center: Calendar, companyId: Long): List<CalendarEvent> {
        val months = monthsAround(center)
        val all = months.flatMap { (_y, _m) ->
            val period = "MONTH"
            val out = api.getShiftsByPeriodAndCompany(period, companyId)
            val namesByShiftId = out.usernames.associate { it.shiftProgrammedId to it.username }

            // (endpoint già per company) -> mappa
            val shifts = out.shifts.map(EventMapper::fromShift)
                .map { ev -> ev.copy(participants = namesByShiftId[ev.id] ?: emptyList()) }

            val filtered = shifts.withoutTombstones()
            indexByMonth(companyCache, filtered)
            filtered
        }
        trimTo12(companyCache)
        if (DEBUG) {
            Log.d(TAG, "loadCompany3Months done for companyId=$companyId, total=${all.size}")
            debugDumpAllCaches()
        }
        return all
    }

    // -------------------------  PREFETCH YEAR  ---------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun prefetchUserEventYear(userId: Long) {
        if (userYearLoaded.contains(userId)) return

        // fetch -> filtro company -> map
        val dto = api.getEventsByPeriodAndUser("YEAR", userId)
        val dtoFiltered = dto.filterByCompany { extractCompanyIdFromEventAny(it as Any) }
        val events = dtoFiltered.map(EventMapper::fromEvent)

        val filtered = events.withoutTombstones()
        userYearCache[userId] = filtered
        userYearLoaded.add(userId)
        indexByMonth(userCache, filtered)
        trimTo12(userCache)
        if (DEBUG) {
            Log.d(TAG, "prefetchUserEventYear userId=$userId count=${filtered.size}")
            debugDumpAllCaches()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadCompany1YearOnce(companyId: Long): List<CalendarEvent> {
        companyYearCache[companyId]?.let { return it.withoutTombstones() }
        if (companyYearLoaded.contains(companyId)) return emptyList()

        val out = api.getShiftsByPeriodAndCompany("YEAR", companyId)
        val namesByShiftId = out.usernames.associate { it.shiftProgrammedId to it.username }
        val shifts = out.shifts.map(EventMapper::fromShift)
            .map { ev -> ev.copy(participants = namesByShiftId[ev.id] ?: emptyList()) }

        val filtered = shifts.withoutTombstones()
        companyYearCache[companyId] = filtered
        companyYearLoaded.add(companyId)
        indexByMonth(companyCache, filtered)
        trimTo12(companyCache)
        if (DEBUG) {
            Log.d(TAG, "loadCompany1YearOnce companyId=$companyId count=${filtered.size}")
            debugDumpAllCaches()
        }
        return filtered
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun prefetchUserShiftsYear(userId: Long) {
        if (userShiftYearLoaded.contains(userId)) return
        val out = api.getShiftsByPeriodAndUser("YEAR", userId)
        val namesByShiftId = out.usernames.associate { it.shiftProgrammedId to it.username }

        // filtro company su DTO degli shift (fail-open)
        val shiftsDto = out.shifts.filterByCompany { extractCompanyIdFromShiftAny(it as Any) }

        val shifts = shiftsDto
            .map(EventMapper::fromShift)
            .map { ev -> ev.copy(participants = namesByShiftId[ev.id] ?: emptyList()) }

        val filtered = shifts.withoutTombstones()
        userShiftYearCache[userId] = filtered
        userShiftYearLoaded.add(userId)
        indexByMonth(userShiftCache, filtered)
        trimTo12(userShiftCache)
        if (DEBUG) {
            Log.d(TAG, "prefetchUserShiftsYear userId=$userId count=${filtered.size}")
            debugDumpAllCaches()
        }
    }

    // ----------------------------  SLICES  -------------------------------

    private val CalendarEvent.year: Int get() = date.get(Calendar.YEAR)
    private val CalendarEvent.month0: Int get() = date.get(Calendar.MONTH)

    fun monthSlice(userId: Long, year: Int, month0: Int): List<CalendarEvent> {
        val slice = (userYearCache[userId] ?: emptyList())
            .filter { it.year == year && it.month0 == month0 }
            .withoutTombstones()
        if (DEBUG) Log.d(TAG, "monthSlice user=$userId y=$year m0=$month0 -> ${slice.size} ids=[${slice.idsStr()}]")
        return slice
    }

    fun getCompanyMonth(year: Int, month: Int) =
        (companyCache[year to month] ?: emptyList()).withoutTombstones()
            .also { if (DEBUG) Log.d(TAG, "getCompanyMonth y=$year m0=$month -> ${it.size} ids=[${it.idsStr()}]") }

    fun getUserShiftMonth(year: Int, month0: Int) =
        (userShiftCache[year to month0] ?: emptyList()).withoutTombstones()
            .also { if (DEBUG) Log.d(TAG, "getUserShiftMonth y=$year m0=$month0 -> ${it.size} ids=[${it.idsStr()}]") }

    fun userShiftMonthSlice(userId: Long, year: Int, month0: Int): List<CalendarEvent> {
        val slice = (userShiftYearCache[userId] ?: emptyList())
            .filter { it.year == year && it.month0 == month0 }
            .withoutTombstones()
        if (DEBUG) Log.d(TAG, "userShiftMonthSlice user=$userId y=$year m0=$month0 -> ${slice.size} ids=[${slice.idsStr()}]")
        return slice
    }

    // -----------------------  HELPER DI CACHE  ---------------------------

    private fun monthsAround(center: Calendar): List<Pair<Int, Int>> {
        fun yM(cal: Calendar) = cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
        val prev = (center.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val curr = center
        val next = (center.clone() as Calendar).apply { add(Calendar.MONTH, +1) }
        return listOf(yM(prev), yM(curr), yM(next))
    }

    private fun indexByMonth(
        cache: MutableMap<Pair<Int, Int>, List<CalendarEvent>>,
        list: List<CalendarEvent>
    ) {
        val grouped = list.groupBy { ev -> ev.date.get(Calendar.YEAR) to ev.date.get(Calendar.MONTH) }
        cache.putAll(grouped)
        if (DEBUG) {
            val total = list.size
            Log.d(TAG, "indexByMonth -> grouped months=${grouped.keys.size}, listTotal=$total")
            dumpMonthCache("after indexByMonth", cache)
        }
    }

    private fun trimTo12(cache: MutableMap<Pair<Int, Int>, List<CalendarEvent>>) {
        if (cache.size <= 12) return
        val ordered = cache.keys.sortedWith(compareBy({ it.first }, { it.second }))
        val toRemove = ordered.take(cache.size - 12)
        toRemove.forEach { cache.remove(it) }
        if (DEBUG) Log.d(TAG, "trimTo12 removed=${toRemove.size} keep=${cache.size}")
    }

    private fun upsertEventInUserCaches(userId: Long, ev: CalendarEvent) {
        val all = (userYearCache[userId] ?: emptyList()).toMutableList()
        val i = all.indexOfFirst { it.id == ev.id }
        if (i >= 0) all[i] = ev else all.add(ev)
        userYearCache[userId] = all
        indexByMonth(userCache, all)
        trimTo12(userCache)
        deletedEventIds.remove(ev.id)
        if (DEBUG) Log.d(TAG, "upsertEventInUserCaches user=$userId id=${ev.id} -> sizeYear=${all.size}")
    }

    private fun upsertShiftInUserCaches(userId: Long, shift: CalendarEvent) {
        val all = (userShiftYearCache[userId] ?: emptyList()).toMutableList()
        val i = all.indexOfFirst { it.id == shift.id }
        if (i >= 0) all[i] = shift else all.add(shift)
        userShiftYearCache[userId] = all
        indexByMonth(userShiftCache, all)
        trimTo12(userShiftCache)
        deletedShiftIds.remove(shift.id)
        if (DEBUG) Log.d(TAG, "upsertShiftInUserCaches user=$userId id=${shift.id} -> sizeYear=${all.size}")
    }

    private fun upsertShiftInCompanyCaches(shift: CalendarEvent) {
        val all = companyCache.values.flatten().toMutableList()
        val i = all.indexOfFirst { it.id == shift.id }
        if (i >= 0) all[i] = shift else all.add(shift)
        indexByMonth(companyCache, all)
        trimTo12(companyCache)
        deletedShiftIds.remove(shift.id)
        if (DEBUG) Log.d(TAG, "upsertShiftInCompanyCaches id=${shift.id} total=${all.size}")
    }

    private fun evictFromMonthlyCache(
        cache: MutableMap<Pair<Int, Int>, List<CalendarEvent>>,
        predicate: (CalendarEvent) -> Boolean
    ) {
        val keys = cache.keys.toList()
        var removed = 0
        for (k in keys) {
            val old = cache[k] ?: continue
            val filtered = old.filterNot(predicate)
            if (filtered.size != old.size) {
                removed += old.size - filtered.size
                cache[k] = filtered
            }
        }
        if (DEBUG) Log.d(TAG, "evictFromMonthlyCache removed=$removed months=${cache.size}")
    }

    private fun evictFromYearCache(
        cache: MutableMap<Long, List<CalendarEvent>>,
        ownerId: Long,
        predicate: (CalendarEvent) -> Boolean
    ) {
        val old = cache[ownerId] ?: return
        val filtered = old.filterNot(predicate)
        cache[ownerId] = filtered
        if (DEBUG) Log.d(TAG, "evictFromYearCache owner=$ownerId before=${old.size} after=${filtered.size}")
    }

    fun evictUserEventFromCaches(userId: Long, eventId: Long) {
        if (DEBUG) Log.d(TAG, "evictUserEventFromCaches user=$userId id=$eventId")
        evictFromYearCache(userYearCache, userId) { it.id == eventId }
        evictFromMonthlyCache(userCache) { it.id == eventId }
        if (DEBUG) debugDumpAllCaches()
    }

    fun evictUserShiftFromCaches(userId: Long, shiftId: Long) {
        if (DEBUG) Log.d(TAG, "evictUserShiftFromCaches user=$userId id=$shiftId")
        evictFromYearCache(userShiftYearCache, userId) { it.id == shiftId }
        evictFromMonthlyCache(userShiftCache) { it.id == shiftId }
        if (DEBUG) debugDumpAllCaches()
    }

    fun evictCompanyShiftFromCaches(companyId: Long, shiftId: Long) {
        if (DEBUG) Log.d(TAG, "evictCompanyShiftFromCaches company=$companyId id=$shiftId")
        evictFromYearCache(companyYearCache, companyId) { it.id == shiftId }
        evictFromMonthlyCache(companyCache) { it.id == shiftId }
        if (DEBUG) debugDumpAllCaches()
    }

    // --------------------------   CRUD EVENTI   --------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createUserEvent(
        ownerUserId: Long,
        ev: CalendarEvent,
        eventType: String,
        alsoAssignOwner: Boolean = true
    ): CalendarEvent {
        if (DEBUG) Log.d(TAG, "createUserEvent req user=$ownerUserId title=${ev.title}")
        val dto = CreateEventDTO(
            name = ev.title,
            description = ev.description ?: "",
            start = hmToIsoUTC(ev.date, ev.startTime),
            end = hmToIsoUTC(ev.date, ev.endTime),
            color = ev.color.toHex6(),
            eventType = eventType,
            userId = if (alsoAssignOwner) listOf(ownerUserId) else emptyList()
        )
        val newId = api.createEvent(dto)
        val created = ev.copy(id = newId)
        upsertEventInUserCaches(ownerUserId, created)
        if (DEBUG) Log.d(TAG, "createUserEvent ok id=$newId mKey=${created.mKey()}")
        return created
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateUserEvent(
        ownerUserId: Long,
        ev: CalendarEvent,
        eventType: String
    ) {
        if (DEBUG) Log.d(TAG, "updateUserEvent req id=${ev.id}")
        val dto = PatchEventDTO(
            eventId = ev.id,
            name = ev.title,
            description = ev.description ?: "",
            start = hmToIsoUTC(ev.date, ev.startTime),
            end = hmToIsoUTC(ev.date, ev.endTime),
            color = ev.color.toHex6(),
            eventType = eventType,
            userId = null
        )
        api.patchEvent(dto)
        upsertEventInUserCaches(ownerUserId, ev)
        if (DEBUG) Log.d(TAG, "updateUserEvent ok id=${ev.id}")
    }

    suspend fun deleteUserEvent(userId: Long, eventId: Long) {
        if (DEBUG) {
            Log.d(TAG, "deleteUserEvent START user=$userId id=$eventId")
            debugDumpAllCaches()
        }
        api.deleteEvent(eventId)
        evictUserEventFromCaches(userId, eventId)
        deletedEventIds.add(eventId)
        if (DEBUG) {
            Log.d(TAG, "deleteUserEvent DONE user=$userId id=$eventId")
            debugDumpAllCaches()
        }
    }

    // ---------------------   CRUD SHIFT PROGRAMMED   ---------------------

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createShiftForUser(
        ownerUserId: Long,
        shift: CalendarEvent,
        alsoAssignOwner: Boolean
    ): CalendarEvent {
        val users = if (alsoAssignOwner) listOf(ownerUserId) else emptyList()
        val dto = shift.toCreateShiftDTO(users)
        try {
            val newId: Long = api.createShift(dto)

            val createdBase = shift.copy(id = newId)
            val created = withParticipantsFromIds(createdBase, users)

            upsertShiftInUserCaches(ownerUserId, created)
            return created
        } catch (e: retrofit2.HttpException) {
            val err = e.response()?.errorBody()?.string()
            android.util.Log.e("EventRepository", "createShiftForUser 400: $err")
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createCompanyShift(
        assignedUserIds: List<Long>,
        shift: CalendarEvent
    ): CalendarEvent {
        val dto = shift.toCreateShiftDTO(assignedUserIds)
        try {
            val newId: Long = api.createShift(dto)

            val createdBase = shift.copy(id = newId)
            val created = withParticipantsFromIds(createdBase, assignedUserIds)

            upsertShiftInCompanyCaches(created)
            assignedUserIds.forEach { uid -> upsertShiftInUserCaches(uid, created) }

            return created
        } catch (e: retrofit2.HttpException) {
            val err = e.response()?.errorBody()?.string()
            android.util.Log.e("EventRepository", "createCompanyShift 400: $err")
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateShiftForUser(
        userId: Long,
        shift: CalendarEvent,
        maybeAssignedUserIds: List<Long>? = null
    ) {
        if (DEBUG) Log.d(TAG, "updateShiftForUser req id=${shift.id}")
        val dto = PatchShiftProgrammedDTO(
            shiftProgrammedId = shift.id,
            name = shift.title,
            description = shift.description ?: "",
            start = hmToIsoUTC(shift.date, shift.startTime),
            end = hmToIsoUTC(shift.date, shift.endTime),
            color = shift.color.toHex6(),
            userId = maybeAssignedUserIds
        )
        api.patchShift(dto)
        upsertShiftInUserCaches(userId, shift)
        if (DEBUG) Log.d(TAG, "updateShiftForUser ok id=${shift.id}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateCompanyShift(
        shift: CalendarEvent,
        maybeAssignedUserIds: List<Long>? = null
    ) {
        if (DEBUG) Log.d(TAG, "updateCompanyShift req id=${shift.id}")
        val dto = PatchShiftProgrammedDTO(
            shiftProgrammedId = shift.id,
            name = shift.title,
            description = shift.description ?: "",
            start = hmToIsoUTC(shift.date, shift.startTime),
            end = hmToIsoUTC(shift.date, shift.endTime),
            color = shift.color.toHex6(),
            userId = maybeAssignedUserIds
        )
        api.patchShift(dto)
        upsertShiftInCompanyCaches(shift)
        maybeAssignedUserIds?.forEach { uid -> upsertShiftInUserCaches(uid, shift) }
        if (DEBUG) Log.d(TAG, "updateCompanyShift ok id=${shift.id}")
    }

    suspend fun deleteShiftForUser(userId: Long, shiftId: Long) {
        if (DEBUG) {
            Log.d(TAG, "deleteShiftForUser START user=$userId id=$shiftId")
            debugDumpAllCaches()
        }
        api.deleteShift(shiftId)
        evictUserShiftFromCaches(userId, shiftId)
        deletedShiftIds.add(shiftId)
        if (DEBUG) {
            Log.d(TAG, "deleteShiftForUser DONE user=$userId id=$shiftId")
            debugDumpAllCaches()
        }
    }

    suspend fun deleteCompanyShift(
        shiftId: Long,
        maybeAssignedUserIds: List<Long>? = null
    ) {
        if (DEBUG) {
            Log.d(TAG, "deleteCompanyShift START id=$shiftId assigned=$maybeAssignedUserIds")
            debugDumpAllCaches()
        }
        api.deleteShift(shiftId)

        evictFromMonthlyCache(companyCache) { it.id == shiftId }
        companyYearCache.keys.toList().forEach { cid ->
            evictFromYearCache(companyYearCache, cid) { it.id == shiftId }
        }
        maybeAssignedUserIds?.forEach { uid -> evictUserShiftFromCaches(uid, shiftId) }
        deletedShiftIds.add(shiftId)

        if (DEBUG) {
            Log.d(TAG, "deleteCompanyShift DONE id=$shiftId")
            debugDumpAllCaches()
        }
    }

    // -------------------------  TIME HELPERS  ----------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hmToIsoUTC(date: Calendar, hm: String): String {
        val (h, m) = hm.split(":").map { it.toInt() }
        val localDateTime = LocalDateTime.of(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH),
            h, m
        )
        val utc = localDateTime
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
            .toOffsetDateTime()
        return utc.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    private fun androidx.compose.ui.graphics.Color.toHex6(): String {
        val argb = this.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = (argb) and 0xFF
        return "%02X%02X%02X".format(r, g, b)
    }

    // --- Directory utenti per risolvere gli ID in nomi ---
    private var userDirectory: Map<Long, String> = emptyMap()

    fun updateUserDirectory(users: List<UserIdAndUsernameAndHoursDTO>) {
        userDirectory = users.associate { it.userId to it.username }
    }

    private fun toDisplayNames(raw: List<String>): List<String> =
        raw.map { s -> s.toLongOrNull()?.let { userDirectory[it] } ?: s }

    private fun withParticipantsFromIds(ev: CalendarEvent, ids: List<Long>?): CalendarEvent {
        val names = ids?.mapNotNull { userDirectory[it] }.orEmpty()
        return if (names.isEmpty()) ev else ev.copy(participants = names)
    }

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    suspend fun loadUserDayEvents(userId: Long): List<CalendarEvent> {
        val dto = api.getEventsByPeriodAndUser("DAY", userId)
        val dtoFiltered = dto.filterByCompany { extractCompanyIdFromEventAny(it as Any) }
        val events = dtoFiltered.map(EventMapper::fromEvent)
        return events.withoutTombstones()
    }

    @RequiresApi(android.os.Build.VERSION_CODES.O)
    suspend fun loadUserDayShifts(userId: Long): List<CalendarEvent> {
        val out = api.getShiftsByPeriodAndUser("DAY", userId)
        val namesByShiftId = out.usernames.associate { it.shiftProgrammedId to it.username }

        val shiftsDto = out.shifts.filterByCompany { extractCompanyIdFromShiftAny(it as Any) }

        val shifts = shiftsDto
            .map(EventMapper::fromShift)
            .map { ev -> ev.copy(participants = namesByShiftId[ev.id] ?: emptyList()) }
        return shifts.withoutTombstones()
    }

    fun clearAllCaches() {
        userCache.clear()
        companyCache.clear()
        userShiftCache.clear()

        userYearCache.clear()
        userYearLoaded.clear()

        companyYearCache.clear()
        companyYearLoaded.clear()

        userShiftYearCache.clear()
        userShiftYearLoaded.clear()

        deletedEventIds.clear()
        deletedShiftIds.clear()

        userDirectory = emptyMap()
    }

    // =====================  COMPANY FILTER UTILS  ========================

    // FIX: niente .toString() sul token; usa null-safety
    private fun currentCompanyId(): Long? =
        TokenManager.jwtToken?.let { JwtUtils.getCompanyId(it) }

    /**
     * Filtra la lista tenendo solo elementi della company corrente.
     * Se non riesco a estrarre la companyId dall'elemento, NON lo scarto (fail-open).
     * Se il token non ha company, non filtro (ritorno la lista originale).
     */
    private fun <T> List<T>.filterByCompany(extractCompanyId: (T) -> Long?): List<T> {
        val cid = currentCompanyId() ?: return this
        return this.filter { item ->
            val itemCid = runCatching { extractCompanyId(item) }.getOrNull()
            (itemCid == null) || (itemCid == cid)
        }
    }

    // Provo a leggere companyId con riflessione:
    //  - dto.companyId
    //  - dto.company?.companyId
    //  - dto.company?.id
    private fun extractCompanyIdFromEventAny(e: Any): Long? = extractCompanyIdGeneric(e)
    private fun extractCompanyIdFromShiftAny(s: Any): Long? = extractCompanyIdGeneric(s)

    private fun extractCompanyIdGeneric(obj: Any): Long? {
        getLongField(obj, "companyId")?.let { return it }
        val companyObj = getObjField(obj, "company") ?: return null
        return getLongField(companyObj, "companyId")
            ?: getLongField(companyObj, "id")
    }

    private fun getObjField(target: Any, name: String): Any? {
        var c: Class<*>? = target.javaClass
        while (c != null) {
            try {
                val f = c.getDeclaredField(name)
                f.isAccessible = true
                return f.get(target)
            } catch (_: NoSuchFieldException) {
                c = c.superclass
            }
        }
        return null
    }

    private fun getLongField(target: Any, name: String): Long? {
        var c: Class<*>? = target.javaClass
        while (c != null) {
            try {
                val f = c.getDeclaredField(name)
                f.isAccessible = true
                val v = f.get(target)
                return when (v) {
                    is Number -> v.toLong()
                    is String -> v.toLongOrNull()
                    else -> null
                }
            } catch (_: NoSuchFieldException) {
                c = c.superclass
            }
        }
        return null
    }
}
