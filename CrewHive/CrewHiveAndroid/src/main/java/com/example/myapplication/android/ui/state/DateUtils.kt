package com.example.myapplication.android.ui.state


import java.util.*

fun getStartOfWeek(date: Calendar): Calendar {
    val cal = date.clone() as Calendar
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal
}
