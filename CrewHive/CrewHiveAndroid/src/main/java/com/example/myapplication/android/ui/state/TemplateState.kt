package com.example.myapplication.android.state

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate

val LocalTemplateState = compositionLocalOf {
    mutableStateListOf<ShiftTemplate>()
}