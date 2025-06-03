// FILE: ShiftParticipantSelectionScreen.kt
package com.example.myapplication.android.ui.screens

import CustomDatePickerDialog
import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.state.GlobalParticipants
import com.example.myapplication.android.ui.components.blocks.ParticipantRow
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import com.example.myapplication.android.ui.components.search.EmployeeSearchBar
import com.example.myapplication.android.ui.state.getStartOfWeek
import com.example.myapplication.android.ui.theme.CustomTheme

import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShiftParticipantSelectionScreen(
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    shiftTemplate: ShiftTemplate,
    onBack: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit,
    events: List<CalendarEvent>
) {
    val colors = CustomTheme.colors

    val allEvents = events
    val allParticipants = remember { GlobalParticipants.list }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedNames = remember { mutableStateListOf<String>() }

    val selectedWeekStart = remember(selectedDate.timeInMillis) {
        getStartOfWeek(selectedDate)
    }

    val currentPreviewEvent = remember {
        mutableStateOf(
            CalendarEvent(
                title = shiftTemplate.title,
                startTime = shiftTemplate.startTime,
                endTime = shiftTemplate.endTime,
                description = "Preview",
                color = shiftTemplate.color,
                date = selectedDate,
                participants = emptyList()
            )
        )
    }

    if (showDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                val parts = it.split("/")
                val cal = Calendar.getInstance().apply {
                    set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                }
                onDateChange(cal)
                currentPreviewEvent.value = currentPreviewEvent.value.copy(date = cal)
                showDatePicker = false
            }
        )

    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(colors.background)) {
        // Top Bar
        Column {
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Absolute.SpaceBetween) {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.shade950,
                        modifier = Modifier.width(32.dp)
                    )
                }
                IconButton(onClick = {
                    val newEvent = CalendarEvent(
                        title = shiftTemplate.title,
                        startTime = shiftTemplate.startTime,
                        endTime = shiftTemplate.endTime,
                        description = shiftTemplate.description,
                        color = shiftTemplate.color,
                        date = selectedDate,
                        participants = selectedNames.toList()
                    )
                    onConfirm(newEvent)

                }) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = colors.shade950,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(shiftTemplate.color, shape = CircleShape)
                )

                Spacer(Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Spacer(Modifier.width(8.dp))
                        Text(shiftTemplate.title, color = colors.shade950, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(
                            "${sdf.format(selectedDate.time)}",
                            color = colors.shade950.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold)
                        Text(
                            "${shiftTemplate.startTime} - ${shiftTemplate.endTime}",
                            color = colors.shade950.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }

        // Date Picker
        Button(
            onClick = { showDatePicker = true },
            colors = ButtonDefaults.buttonColors(containerColor = colors.shade800),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Cambia data", color = Color.White)
        }

        // Search Bar
        EmployeeSearchBar(
            searchText = searchQuery,
            onSearchChange = { searchQuery = it }
        )

        // Lista partecipanti filtrata
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(allParticipants.filter { it.contains(searchQuery, ignoreCase = true) }) { name ->
                val weekEnd = (selectedWeekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }

                val userEvents = allEvents.filter {
                    it.participants.contains(name) &&
                            !it.date.before(selectedWeekStart) && !it.date.after(weekEnd)
                }

                val showPreview = name in selectedNames
                val previewEvent = if (showPreview) currentPreviewEvent.value.copy(participants = listOf(name)) else null

                ParticipantRow(
                    name = name,
                    weeklyHours = 40,
                    userEvents = userEvents,
                    miniCalendarWeekStart = selectedWeekStart,
                    selectedDate = selectedDate,
                    isSelected = name in selectedNames,
                    onClick = {
                        if (name in selectedNames) selectedNames.remove(name)
                        else selectedNames.add(name)
                    },
                    currentTemplatePreview = previewEvent
                )
            }
        }
    }
}
