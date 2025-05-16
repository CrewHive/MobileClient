package com.example.myapplication.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun CalendarEventItem(
    event: CalendarEvent,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .border(6.dp, Color(0xFFFAF7C7), RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(20.dp,8.dp,6.dp,6.dp)
        ) {
            Column {

                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "${event.startTime} - ${event.endTime}",
                    fontSize = 12.sp,
                    color = Color(0xFF7D4F16).copy(alpha = 0.84f),
                    fontWeight = FontWeight.Bold

                )
                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Box(
            modifier = Modifier
                .width(12.dp)
                .fillMaxHeight()
                .background(
                    color = event.color,
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                )
                .align(Alignment.CenterStart)
                .zIndex(1f)
        )
    }
}
