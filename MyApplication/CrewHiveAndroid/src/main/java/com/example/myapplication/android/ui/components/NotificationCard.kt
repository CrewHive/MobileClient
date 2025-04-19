package com.example.myapplication.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationCard(
    title: String,
    sender: String,
    createdAt: String, // formatted as dd/MM/yyyy HH:mm
    deadline: String, // formatted as dd/MM/yyyy
    onClick: () -> Unit
) {
    val indicatorColor = getColorForDeadline(deadline)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .background(Color(0xFFFFFFF0), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(60.dp)
                .background(indicatorColor, shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF5D4037)
            )
            Text(
                text = "$sender - $createdAt",
                fontSize = 14.sp,
                color = Color(0xFF5D4037)
            )
        }

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = Color(0xFFFFC107)
        )
    }
}

fun getColorForDeadline(deadline: String): Color {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC") // oppure TimeZone.getDefault()
        val deadlineDate = formatter.parse(deadline)

        if (deadlineDate != null) {
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diffInMillis = deadlineDate.time - currentDate.time
            val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

            when {
                diffInDays < 0 -> Color(0xFF2196F3) // Blu: scaduto
                diffInDays <= 2 -> Color(0xFFF44336) // Rosso
                diffInDays <= 4 -> Color(0xFFFFEB3B) // Giallo
                else -> Color(0xFF4CAF50) // Verde
            }
        } else {
            Color.Gray
        }
    } catch (e: Exception) {
        Color.Gray
    }
}