package com.example.myapplication.android.ui.components

import CustomDatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShiftSelectionHeader(
    shiftTitle: String,
    shiftColor: Color,
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateString = remember(selectedDate.timeInMillis) { dateFormat.format(selectedDate.time) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { dateStr ->
                val parts = dateStr.split("/")
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                    set(Calendar.MONTH, parts[1].toInt() - 1)
                    set(Calendar.YEAR, parts[2].toInt())
                }
                onDateChange(newCal)
                showDatePicker = false
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF7C7))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(shiftColor, shape = MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(shiftTitle, fontSize = 18.sp, color = Color(0xFF5D4037))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(dateString, color = Color(0xFF7D4F16), fontSize = 14.sp)
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleziona data",
                    tint = Color(0xFF5D4037)
                )
            }
        }
    }
}
