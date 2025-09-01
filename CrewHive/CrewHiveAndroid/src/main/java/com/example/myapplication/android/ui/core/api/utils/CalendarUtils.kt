package com.example.myapplication.android.ui.core.api.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.core.security.JwtUtils
import com.example.myapplication.android.ui.core.security.SessionManager

/** Legge il companyId dal JWT corrente. In preview (niente token) torna null. */
@Composable
fun rememberCompanyId(context: Context = LocalContext.current): Long? {
    val token = SessionManager.getToken(context)
    return remember(token) { token?.let { JwtUtils.getCompanyId(it) } }
}

/** Filtra per companyId se l’evento espone un campo 'companyId'. In mancanza, non scarta nulla. */
fun List<CalendarEvent>.filterByCompanyId(cid: Long?): List<CalendarEvent> {
    if (cid == null) return this
    return filter { ev -> ev.extractCompanyIdIfAny()?.let { it == cid } ?: true }
}

/** Prova a leggere 'companyId' se esiste nel modello UI. Se non c’è, torna null (fail-open). */
private fun CalendarEvent.extractCompanyIdIfAny(): Long? = runCatching {
    val f = this.javaClass.getDeclaredField("companyId").apply { isAccessible = true }
    when (val v = f.get(this)) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull()
        else -> null
    }
}.getOrNull()
