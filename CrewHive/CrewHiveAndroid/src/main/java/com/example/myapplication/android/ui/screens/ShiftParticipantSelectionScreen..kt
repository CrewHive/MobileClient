// FILE: ShiftParticipantSelectionScreen.kt
package com.example.myapplication.android.ui.screens

import CustomDatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.myapplication.android.ui.components.blocks.ParticipantRow
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import com.example.myapplication.android.ui.components.search.EmployeeSearchBar
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.state.getStartOfWeek
import com.example.myapplication.android.ui.theme.CustomTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShiftParticipantSelectionScreen(
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    shiftTemplate: ShiftTemplate,
    allEmployees: List<CompanyEmployee>,            // lista reale (può essere vuota)
    onBack: () -> Unit,
    onConfirm: (CalendarEvent, List<Long>) -> Unit, // ritorna anche gli ID
    events: List<CalendarEvent>,
    initialSelectedUserIds: List<Long> = emptyList()
) {
    val colors = CustomTheme.colors

    // ----- SANITIZZAZIONE INPUT -----
    val hhmm = remember { Regex("""^\d{1,2}:\d{2}$""") }
    val safeTitle = remember(shiftTemplate.title) { shiftTemplate.title.ifBlank { "Turno" } }
    val safeStart = remember(shiftTemplate.startTime) { if (hhmm.matches(shiftTemplate.startTime)) shiftTemplate.startTime else "00:00" }
    val safeEnd   = remember(shiftTemplate.endTime)   { if (hhmm.matches(shiftTemplate.endTime))   shiftTemplate.endTime   else "00:01" }
    val safeColor = shiftTemplate.color

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val selectedUserIds = remember(initialSelectedUserIds) {
        mutableStateListOf<Long>().apply { addAll(initialSelectedUserIds) }
    }

    val selectedWeekStart = remember(selectedDate.timeInMillis) { getStartOfWeek(selectedDate) }

    val currentPreviewEvent = remember(safeTitle, safeStart, safeEnd, selectedDate, safeColor) {
        mutableStateOf(
            CalendarEvent(
                title = safeTitle,
                startTime = safeStart,
                endTime = safeEnd,
                description = "Preview",
                color = safeColor,
                date = selectedDate,
                participants = emptyList(),
                id = -System.currentTimeMillis(),
                kind = CalendarItemKind.SHIFT
            )
        )
    }

    if (showDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                val parts = it.split("/")
                if (parts.size == 3) {
                    runCatching {
                        Calendar.getInstance().apply { set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt()) }
                    }.onSuccess { cal ->
                        onDateChange(cal)
                        currentPreviewEvent.value = currentPreviewEvent.value.copy(date = cal)
                    }
                }
                showDatePicker = false
            }
        )
    }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        // Top bar
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = colors.shade950, modifier = Modifier.width(32.dp))
            }
            IconButton(onClick = {
                val newEvent = CalendarEvent(
                    title = safeTitle,
                    startTime = safeStart,
                    endTime = safeEnd,
                    description = shiftTemplate.description,
                    color = safeColor,
                    date = currentPreviewEvent.value.date,
                    participants = emptyList(),
                    id = -System.currentTimeMillis(),
                    kind = CalendarItemKind.SHIFT
                )
                onConfirm(newEvent, selectedUserIds.toList())
            }) {
                Icon(Icons.Default.Check, null, tint = colors.shade950, modifier = Modifier.width(32.dp))
            }
        }

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp, start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(50.dp).background(safeColor, shape = CircleShape))
            Spacer(Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Text(safeTitle, color = colors.shade950, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        sdf.format(currentPreviewEvent.value.date.time),
                        color = colors.shade950.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$safeStart - $safeEnd",
                        color = colors.shade950.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.weight(1f))
        }

        // Date picker
        Button(
            onClick = { showDatePicker = true },
            colors = ButtonDefaults.buttonColors(containerColor = colors.shade800),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) { Text("Cambia data", color = Color.White) }

        // Search
        EmployeeSearchBar(searchText = searchQuery, onSearchChange = { searchQuery = it })

        // Lista dipendenti filtrati (se la lista è vuota, la LazyColumn sarà vuota senza crash)
        val filtered = remember(searchQuery, allEmployees) {
            allEmployees.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        val weekEnd = remember(selectedWeekStart.timeInMillis) {
            (selectedWeekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(filtered, key = { it.userId }) { emp ->
                val uid = emp.userId.toLongOrNull()
                val name = emp.name

                val userEvents = events.filter {
                    it.participants.contains(name) && !it.date.before(selectedWeekStart) && !it.date.after(weekEnd)
                }

                val isSel = uid != null && selectedUserIds.contains(uid)
                val preview = if (isSel) currentPreviewEvent.value.copy(participants = listOf(name)) else null

                ParticipantRow(
                    name = name,
                    weeklyHours = emp.weeklyHours,
                    userEvents = userEvents,
                    miniCalendarWeekStart = selectedWeekStart,
                    selectedDate = currentPreviewEvent.value.date,
                    isSelected = isSel,
                    onClick = {
                        uid?.let {
                            if (selectedUserIds.contains(it)) selectedUserIds.remove(it) else selectedUserIds.add(it)
                        }
                    },
                    currentTemplatePreview = preview
                )
            }
        }
    }
}
