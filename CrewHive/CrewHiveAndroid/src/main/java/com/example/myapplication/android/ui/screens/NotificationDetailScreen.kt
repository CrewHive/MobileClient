package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun NotificationDetailScreen(
    notification: NotificationData,
    onBackClick: () -> Unit
) {
    val colors = CustomTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 👈 Fondo bianco visibile
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.shade950
                )
            }
            Text(
                text = "Dettagli",
                fontSize = 20.sp,
                color = colors.shade950,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LabelText("Titolo:", notification.title)
        Spacer(modifier = Modifier.height(8.dp))
        LabelText("Mittente:", notification.sender)
        Spacer(modifier = Modifier.height(8.dp))
        LabelText("Data di creazione:", notification.createdAt)
        Spacer(modifier = Modifier.height(8.dp))
        LabelText("Data di scadenza:", notification.deadline)
        Spacer(modifier = Modifier.height(8.dp))
        LabelText("Messaggio:", notification.body)
    }
}

@Composable
fun LabelText(label: String, content: String) {
    val colors = CustomTheme.colors

    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.shade950)
        Text(text = content, fontSize = 16.sp, color = colors.shade950)
    }
}
