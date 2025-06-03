package com.example.myapplication.android.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.compositionLocalOf

val LocalCurrentUser = compositionLocalOf { mutableStateOf("Giulia Verdi") }
