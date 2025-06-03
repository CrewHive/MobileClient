// FILE: TopBarWithDate.kt
package com.example.myapplication.android.ui.components.headers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TopBarWithDate(
    selectedDate: Calendar,
    viewMode: String,
    onModeChange: (String) -> Unit
) {
    val colors = CustomTheme.colors

    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.time)
    val fullDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.time)

    val options = listOf("D", "W", "M")
    val selectedMode = viewMode

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.shade100)
            .padding(8.dp, 40.dp, 8.dp, 8.dp)
    ) {
        Column {
            when (viewMode) {
                "M" -> {
                    val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                    val month = SimpleDateFormat("MMMM", Locale.getDefault())
                        .format(selectedDate.time)
                        .replaceFirstChar { it.uppercase() }

                    Text(
                        text = "$day $month",
                        fontSize = 30.sp,
                        color = colors.shade950
                    )
                    Text(
                        text = selectedDate.get(Calendar.YEAR).toString(),
                        fontSize = 14.sp,
                        color = colors.shade950
                    )
                }

                "W" -> {
                    val weekStart = (selectedDate.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    }
                    val weekEnd = (weekStart.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_MONTH, 6)
                    }
                    val formatterDay = SimpleDateFormat("d", Locale.getDefault())
                    val formatterMonth = SimpleDateFormat("MMMM", Locale.getDefault())
                    val formatterYear = SimpleDateFormat("yyyy", Locale.getDefault())

                    Text(
                        text = "${formatterDay.format(weekStart.time)} - ${formatterDay.format(weekEnd.time)}",
                        fontSize = 30.sp,
                        color = colors.shade950
                    )
                    Text(
                        text = "${formatterMonth.format(selectedDate.time)} ${formatterYear.format(selectedDate.time)}".uppercase(Locale.ROOT),
                        fontSize = 14.sp,
                        color = colors.shade950
                    )
                }

                "D" -> {
                    Text(
                        text = dayOfWeek.replaceFirstChar { it.uppercase() },
                        fontSize = 30.sp,
                        color = colors.shade950
                    )
                    Text(
                        text = fullDate.uppercase(Locale.ROOT),
                        fontSize = 14.sp,
                        color = colors.shade950
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.shade950)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { mode ->
                val isSelected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color.White else colors.shade950)
                        .clickable {
                            onModeChange(mode)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = mode,
                        color = if (isSelected) colors.shade950 else Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
