// FILE: FullscreenPopupDialog.kt
package com.example.myapplication.android.ui.components.dialogs

import CustomDatePickerDialog
import CustomTimePickerDialog
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.myapplication.android.ui.theme.CustomTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenPopupDialog(
    onDismiss: () -> Unit,
    onAddEvent: (CalendarEvent) -> Unit,
    selectedDate: Calendar
) {
    val colors = CustomTheme.colors

    var selectedOption by remember { mutableStateOf(EventVisibility.PERSONALE) }
    val allEmployees = GlobalParticipants.list
    val selectedEmployees = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }

    val titolo = remember { mutableStateOf("") }
    val data = remember { mutableStateOf("") }
    val selectedPickerDate = remember { mutableStateOf(selectedDate.clone() as Calendar) }
    val oraInizio = remember { mutableStateOf("") }
    val oraFine = remember { mutableStateOf("") }
    val descrizione = remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF81C784)) }
    var showColorDialog by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFB74D),
        Color(0xFFBA68C8), Color(0xFFE57373)
    )

    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePickerStart by remember { mutableStateOf(false) }
    var showCustomTimePickerEnd by remember { mutableStateOf(false) }

    val timeError by remember {
        derivedStateOf {
            try {
                val inizio = oraInizio.value.split(":").map { it.toInt() }
                val fine = oraFine.value.split(":").map { it.toInt() }
                val startMinutes = inizio[0] * 60 + inizio[1]
                val endMinutes = fine[0] * 60 + fine[1]
                startMinutes >= endMinutes
            } catch (e: Exception) {
                false
            }
        }
    }

    if (showColorDialog) {
        AdvancedColorPickerDialog(
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                selectedColor = it
                showColorDialog = false
            }
        )
    }

    if (showCustomDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showCustomDatePicker = false },
            onDateSelected = {
                data.value = it
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
            color = colors.background,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Chiudi", color = colors.shade800)
                    }
                }

                Text("Aggiungi Evento", fontSize = 20.sp, color = colors.shade950)
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibilitySlider(
                    selected = selectedOption,
                    onSelectionChange = { selectedOption = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = titolo.value,
                    onValueChange = { titolo.value = it },
                    label = { Text("Titolo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.shade800,
                        unfocusedTextColor = colors.shade800,
                        focusedBorderColor = colors.shade800,
                        unfocusedBorderColor = colors.shade800,
                        focusedLabelColor = colors.shade800,
                        unfocusedLabelColor = colors.shade800.copy(alpha = 0.7f),
                        cursorColor = colors.shade800
                    )
                )

                Spacer(modifier = Modifier.height(8.dp).fillMaxWidth())
                Button(
                    onClick = { showCustomDatePicker = true },
                    border = BorderStroke(2.dp, colors.shade800),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text(
                        if (data.value.isBlank()) "Seleziona Data" else "Data: ${data.value}",
                        color = colors.shade800)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        onClick = { showCustomTimePickerStart = true },
                        border = BorderStroke(2.dp, colors.shade800),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                        Text(if (oraInizio.value.isBlank()) "Ora Inizio" else oraInizio.value,
                            color = colors.shade800)
                    }

                    Button(
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        onClick = { showCustomTimePickerEnd = true },
                        border = BorderStroke(2.dp, colors.shade800),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                        Text(if (oraFine.value.isBlank()) "Ora Fine" else oraFine.value,
                            color = colors.shade800)
                    }
                }

                if (timeError) {
                    Text("⚠ L'orario di fine deve essere dopo quello di inizio", color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descrizione.value,
                    onValueChange = { descrizione.value = it },
                    label = { Text("Descrizione") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.shade800,
                        unfocusedTextColor = colors.shade800,
                        focusedBorderColor = colors.shade800,
                        unfocusedBorderColor = colors.shade800,
                        focusedLabelColor = colors.shade800,
                        unfocusedLabelColor = colors.shade800.copy(alpha = 0.7f),
                        cursorColor = colors.shade800
                    )
                )

                if (selectedOption == EventVisibility.PUBBLICO) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Partecipanti", color = colors.shade950)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },

                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedEmployees.take(3).joinToString(", ") +
                                    if (selectedEmployees.size > 3) "…" else "",
                            onValueChange = {},
                            label = { Text("Seleziona partecipanti") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.shade800,
                                unfocusedTextColor = colors.shade800,
                                focusedBorderColor = colors.shade800,
                                unfocusedBorderColor = colors.shade800,
                                focusedLabelColor = colors.shade800,
                                unfocusedLabelColor = colors.shade800.copy(alpha = 0.7f),
                                cursorColor = colors.shade800
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(color = colors.background),
                        ) {
                            allEmployees.forEach { name ->
                                val isSelected = selectedEmployees.contains(name)

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = name,
                                            color = if (isSelected) Color.White else Color.Black
                                        )
                                    },
                                    onClick = {
                                        if (isSelected) selectedEmployees.remove(name)
                                        else selectedEmployees.add(name)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) colors.shade800 else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Colore evento", color = colors.shade950)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { color ->
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) colors.shade800 else colors.shade800,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color },
                                contentAlignment = Alignment.Center
                            ) {}
                        }

                        val isCustomSelected = selectedColor !in colorOptions
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isCustomSelected) selectedColor else colors.background)
                                .border(
                                    width = if (isCustomSelected) 2.dp else 1.dp,
                                    color = if (isCustomSelected) colors.shade800 else colors.shade800,
                                    shape = CircleShape
                                )
                                .clickable { showColorDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Custom Color",
                                tint = colors.shade800
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (!timeError && titolo.value.isNotBlank() && oraInizio.value.isNotBlank() && oraFine.value.isNotBlank()) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val dataString = sdf.format(selectedPickerDate.value.time)

                            val event = CalendarEvent(
                                startTime = oraInizio.value,
                                endTime = oraFine.value,
                                title = titolo.value,
                                description = descrizione.value,
                                color = selectedColor,
                                date = selectedPickerDate.value.clone() as Calendar,
                                participants = if (selectedOption == EventVisibility.PUBBLICO) selectedEmployees.toList() else listOf()
                            )
                            onAddEvent(event)
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.shade800)
                ) {
                    Text(
                        text = if (selectedOption == EventVisibility.PERSONALE) "Aggiungi" else "Richiedi",
                        color = colors.shade100
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedVisibilitySlider(
    selected: EventVisibility,
    onSelectionChange: (EventVisibility) -> Unit
) {
    val colors = CustomTheme.colors

    val totalWidth = 300.dp
    val sliderHeight = 44.dp
    val indicatorWidth = totalWidth / 2

    val indicatorOffset by animateDpAsState(
        targetValue = if (selected == EventVisibility.PERSONALE) 0.dp else indicatorWidth,
        label = "Segment Offset"
    )

    Box(
        modifier = Modifier
            .width(totalWidth)
            .height(sliderHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.background)
    ) {
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(indicatorWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.shade800)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelectionChange(EventVisibility.PERSONALE) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Personale",
                    color = if (selected == EventVisibility.PERSONALE) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelectionChange(EventVisibility.PUBBLICO) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pubblico",
                    color = if (selected == EventVisibility.PUBBLICO) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

enum class EventVisibility {
    PERSONALE,
    PUBBLICO
}