package com.example.myapplication.android.ui.core.repo

import com.example.myapplication.android.ui.screens.NotificationData
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<NotificationData>> // puoi riusare il tuo model per ora
    suspend fun send(notification: NotificationData): Result<Unit>
}