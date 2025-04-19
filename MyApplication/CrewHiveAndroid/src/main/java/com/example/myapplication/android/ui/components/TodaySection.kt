package com.example.myapplication.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

object TodaySectionComponent {

    @Composable
    fun TodaySection() {
        Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Today",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(8.dp))

            EventCard("Morning shift", "7:30 AM", "10:00 AM", Color(0xFF9C27B0))
            EventCard("Meeting", "10:00 AM", "11:00 AM", Color(0xFF00BCD4))
            EventCard("Morning shift", "11:00 AM", "1:00 PM", Color(0xFF9C27B0))
        }
    }

    fun calculateDuration(start: String, end: String): Float {
        fun parseTime(time: String): Int {
            val parts = time.split(" ", ":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val isPM = parts[2] == "PM"
            return (if (hour == 12) 0 else hour * 60) + minute + if (isPM) 12 * 60 else 0
        }

        val startMinutes = parseTime(start)
        val endMinutes = parseTime(end)

        val duration = endMinutes - startMinutes
        return duration / 60f
    }

    @Composable
    private fun EventCard(title: String, startTime: String, endTime: String, indicatorColor: Color) {
        val duration = calculateDuration(startTime, endTime)
        val heightPerHour = 60.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .padding(vertical = 4.dp)
                .height(heightPerHour * duration)
                .background(Color(0xFFFFF9C4), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(indicatorColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Text(text = "$startTime - $endTime", fontSize = 12.sp, color = Color(0xFF5D4037))
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color(0xFFFFC107)
            )
        }
    }
}

