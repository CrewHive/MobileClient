package com.example.myapplication.android.ui.components.dialogs

import CustomDatePickerDialog
import CustomTimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.android.state.GlobalParticipants
import com.example.myapplication.android.ui.components.pickers.AdvancedColorPickerDialog
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditablePopupDialog(
    eventToEdit: CalendarEvent,
    onDismiss: () -> Unit,
    onUpdateEvent: (CalendarEvent) -> Unit
) {
    val allEmployees = GlobalParticipants.list
    val isPublic = eventToEdit.participants.isNotEmpty()

    val titolo = remember { mutableStateOf(eventToEdit.title) }
    val selectedPickerDate = remember { mutableStateOf(eventToEdit.date.clone() as Calendar) }
    val oraInizio = remember { mutableStateOf(eventToEdit.startTime) }
    val oraFine = remember { mutableStateOf(eventToEdit.endTime) }
    val descrizione = remember { mutableStateOf(eventToEdit.description) }
    val selectedColor = remember { mutableStateOf(eventToEdit.color) }
    val selectedEmployees = remember { mutableStateListOf<String>().apply { addAll(eventToEdit.participants) } }
    var expanded by remember { mutableStateOf(false) }

    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePickerStart by remember { mutableStateOf(false) }
    var showCustomTimePickerEnd by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        AdvancedColorPickerDialog(
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                selectedColor.value = it
                showColorDialog = false
            }
        )
    }

    if (showCustomDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showCustomDatePicker = false },
            onDateSelected = {
                val parts = it.split("/")
                val cal = Calendar.getInstance()
                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                selectedPickerDate.value = cal
                showCustomDatePicker = false
            }
        )
    }

    if (showCustomTimePickerStart) {
        CustomTimePickerDialog(
            onDismiss = { showCustomTimePickerStart = false },
            onTimeSelected = {
                oraInizio.value = it
                showCustomTimePickerStart = false
            }
        )
    }

    if (showCustomTimePickerEnd) {
        CustomTimePickerDialog(
            onDismiss = { showCustomTimePickerEnd = false },
            onTimeSelected = {
                oraFine.value = it
                showCustomTimePickerEnd = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF8E1),
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Chiudi", color = Color(0xFF7D4F16))
                    }
                }

                Text("Modifica Evento", fontSize = 20.sp, color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = titolo.value,
                    onValueChange = { titolo.value = it },
                    label = { Text("Titolo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF7D4F16),
                        unfocusedTextColor = Color(0xFF7D4F16),
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showCustomDatePicker = true },
                    border = BorderStroke(2.dp, Color(0xFF7D4F16)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent))  {
                    Text("Data: ${selectedPickerDate.value.time.toLocaleString().substring(0, 10)}",
                        color = Color(0xFF7D4F16))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(modifier = Modifier.weight(1f).padding(end = 4.dp), onClick = { showCustomTimePickerStart = true },
                        border = BorderStroke(2.dp, Color(0xFF7D4F16)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                        Text(oraInizio.value,
                            color = Color(0xFF7D4F16))
                    }

                    Button(modifier = Modifier.weight(1f).padding(start = 4.dp), onClick = { showCustomTimePickerEnd = true },
                        border = BorderStroke(2.dp, Color(0xFF7D4F16)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                        Text(oraFine.value,
                            color = Color(0xFF7D4F16))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descrizione.value,
                    onValueChange = { descrizione.value = it },
                    label = { Text("Descrizione") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF7D4F16),
                        unfocusedTextColor = Color(0xFF7D4F16),
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16)
                    )
                )

                if (isPublic) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Partecipanti", color = Color(0xFF5D4037))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedEmployees.take(3).joinToString(", ") + if (selectedEmployees.size > 3) "…" else "",
                            onValueChange = {},
                            label = { Text("Seleziona partecipanti") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF7D4F16),
                                unfocusedTextColor = Color(0xFF7D4F16),
                                focusedBorderColor = Color(0xFF7D4F16),
                                unfocusedBorderColor = Color(0xFF7D4F16),
                                focusedLabelColor = Color(0xFF7D4F16),
                                unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                                cursorColor = Color(0xFF7D4F16)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFFFFF8E1))
                        ) {
                            allEmployees.forEach { name ->
                                val isSelected = selectedEmployees.contains(name)
                                DropdownMenuItem(
                                    text = { Text(name, color = if (isSelected) Color.White else Color.Black) },
                                    onClick = {
                                        if (isSelected) selectedEmployees.remove(name)
                                        else selectedEmployees.add(name)
                                    },
                                    modifier = Modifier.fillMaxWidth().background(
                                        if (isSelected) Color(0xFF7D4F16) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Colore evento", color = Color(0xFF5D4037))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val colorOptions = listOf(
                        Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFB74D),
                        Color(0xFFBA68C8), Color(0xFFE57373)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { color ->
                            val isSelected = selectedColor.value == color
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.Black else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor.value = color }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { showColorDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Custom Color", tint = Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val updatedEvent = CalendarEvent(
                            title = titolo.value,
                            startTime = oraInizio.value,
                            endTime = oraFine.value,
                            description = descrizione.value,
                            color = selectedColor.value,
                            date = selectedPickerDate.value,
                            participants = if (isPublic) selectedEmployees.toList() else emptyList()
                        )
                        onUpdateEvent(updatedEvent)
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16))
                ) {
                    Text("Salva", color = Color(0xFFFAF7C7))
                }
            }
        }
    }
}