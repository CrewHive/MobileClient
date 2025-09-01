package com.example.myapplication.android.ui.core.api.dto

enum class EventType { PUBLIC, PRIVATE }

// Per i parametri {temp} degli endpoint /event/... (DAY|WEEK|...):
enum class Period { DAY, WEEK, MONTH, TRIMESTER, SEMESTER, YEAR }