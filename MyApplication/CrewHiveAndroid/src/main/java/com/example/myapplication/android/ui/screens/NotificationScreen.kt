package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.components.NotificationCard

@Composable
fun NotificationScreen(
    onEditClick: () -> Unit,
    onNotificationClick: (NotificationData) -> Unit
) {
    val sampleNotifications = listOf(
        NotificationData(
            title = "Cambio orario turno",
            sender = "Responsabile HR",
            createdAt = "08/04/2025 09:15",
            deadline = "13/04/2025",
            body = "Si informa che l'orario del turno mattutino è stato modificato."
        ),
        NotificationData(
            title = "Incontro settimanale",
            sender = "Team Manager",
            createdAt = "07/04/2025 15:00",
            deadline = "09/04/2025",
            body = "Reminder: meeting settimanale presso sala riunioni 2."
        ),
        NotificationData(
            title = "Aggiornamento ferie",
            sender = "Amministrazione",
            createdAt = "06/04/2025 11:30",
            deadline = "06/04/2025",
            body = "Le ferie sono state aggiornate nel sistema."
        ),
        NotificationData(
            title = "Consegna dispositivi",
            sender = "Ufficio tecnico",
            createdAt = "05/04/2025 10:00",
            deadline = "10/04/2025",
            body = "Ritiro dei nuovi badge e tablet presso il magazzino."
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFFFF9C4))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications",
                fontSize = 20.sp,
                color = Color(0xFF5D4037),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFFFFC107)
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sampleNotifications) { notification ->
                NotificationCard(
                    title = notification.title,
                    sender = notification.sender,
                    createdAt = notification.createdAt,
                    deadline = notification.deadline,
                    onClick = { onNotificationClick(notification) }
                )
            }
        }
    }
}

// Data model
data class NotificationData(
    val title: String,
    val sender: String,
    val createdAt: String,
    val deadline: String,
    val body: String
)
