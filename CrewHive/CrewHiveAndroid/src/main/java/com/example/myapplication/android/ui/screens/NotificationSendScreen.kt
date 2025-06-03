package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationSendScreen(
    onBackClick: () -> Unit,
    onSendClick: (NotificationData) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFFFF9C4))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFFFC107)
                )
            }
            Text(
                text = "Nuovo messaggio",
                fontSize = 20.sp,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titolo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sender,
                onValueChange = { sender = it },
                label = { Text("Mittente") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = deadline,
                onValueChange = { deadline = it },
                label = { Text("Data di scadenza (es: 12/04/2025)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Messaggio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Button(
                onClick = {
                    val newNotification = NotificationData(
                        title = title,
                        sender = sender,
                        createdAt = getCurrentDateTimeString(),
                        deadline = deadline,
                        body = body
                    )
                    onSendClick(newNotification)
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Invia", color = Color.White)
            }
        }
    }
}

fun getCurrentDateTimeString(): String {
    return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())
}
