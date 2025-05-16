package com.example.myapplication.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.*
import androidx.compose.material3.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TopBarWithDate(selectedDate: Calendar) {
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.time)
    val fullDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.time)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF7C7))
            .padding(8.dp,40.dp,8.dp,8.dp)
    ) {
        Column {
            Text(
                text = dayOfWeek.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color(0xFF5D4037)
            )
            Text(
                text = fullDate.toUpperCase(Locale.ROOT),
                fontSize = 14.sp,
                color = Color(0xFF5D4037)
            )
        }

        IconButton(
            onClick = { /* TODO: Apri calendario */ },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar",
                tint = Color(0xFF5D4037)
            )
        }
    }
}

