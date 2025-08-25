package com.example.myapplication.android.ui.core.model

import java.util.Calendar

data class Event(
    val id: String,
    val date: Calendar,
    val startTime: String,  // "HH:mm"
    val endTime: String,    // "HH:mm"
    val title: String,
    val description: String,
    val color: Long,        // argb per serializzazione facile
    val participants: List<String> // userIds
)
