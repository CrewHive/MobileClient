package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.components.NotificationCard
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(
    onEditClick: () -> Unit,
    onNotificationClick: (NotificationData) -> Unit
) {
    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    val calendar = Calendar.getInstance()

    fun getDateOffset(daysOffset: Int): String {
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        return dateFormatter.format(calendar.time)
    }

    val sampleNotifications = listOf(
        NotificationData(
            title = "Notifica Scaduta",
            sender = "Sistema",
            createdAt = getDateOffset(-2) + " 08:00",
            deadline = getDateOffset(-2), // BLU
            body = "Questa notifica è scaduta ieri."
        ),
        NotificationData(
            title = "Deadline Imminente",
            sender = "Team Leader",
            createdAt = getDateOffset(-1) + " 10:00",
            deadline = getDateOffset(1), // ROSSO
            body = "Questa notifica scade domani."
        ),
        NotificationData(
            title = "Scadenza tra 3 Giorni",
            sender = "Ufficio",
            createdAt = getDateOffset(0) + " 12:00",
            deadline = getDateOffset(3), // GIALLO
            body = "La scadenza è tra qualche giorno."
        ),
        NotificationData(
            title = "Scadenza Lontana",
            sender = "Amministrazione",
            createdAt = getDateOffset(0) + " 09:30",
            deadline = getDateOffset(7), // VERDE
            body = "Hai ancora tempo per questa notifica."
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
