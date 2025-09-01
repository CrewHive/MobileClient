// FILE: FullscreenPopupDialog.kt
package com.example.myapplication.android.ui.components.dialogs

import CustomDatePickerDialog
import CustomTimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.android.ui.components.pickers.AdvancedColorPickerDialog
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.theme.CustomTheme
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import java.text.SimpleDateFormat
import java.util.*

enum class CreateMode { PRIVATE_EVENT, PUBLIC_SHIFT }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FullscreenPopupDialog(
    mode: CreateMode,
    onDismiss: () -> Unit,
    onAddEvent: (CalendarEvent, List<Long>, Boolean) -> Unit,
    onAddEmployeesClick: (CalendarEvent) -> Unit, // legacy
    selectedDate: Calendar,
    allEmployees: List<CompanyEmployee>
) {
    val colors = CustomTheme.colors
    val isPublic = (mode == CreateMode.PUBLIC_SHIFT)
    val dialogTitle = if (isPublic) "Aggiungi turno" else "Aggiungi evento"

    // STATE
    var titolo by remember { mutableStateOf("") }
    var descrizione by remember { mutableStateOf("") }
    var selectedPickerDate by remember { mutableStateOf(selectedDate.clone() as Calendar) }
    var oraInizio by remember { mutableStateOf("") }
    var oraFine by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF81C784)) }
    var showColorDialog by remember { mutableStateOf(false) }

    val selectedEmployeeIds = remember { mutableStateListOf<Long>() }
    val chosenNames by remember(allEmployees, selectedEmployeeIds) {
        derivedStateOf {
            val ids = selectedEmployeeIds.toList()
            allEmployees.filter { it.userId.toLongOrNull() in ids }.map { it.name }
        }
    }
    var showParticipantPicker by remember { mutableStateOf(false) }

    val colorOptions = listOf(Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFFE57373))
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePickerStart by remember { mutableStateOf(false) }
    var showCustomTimePickerEnd by remember { mutableStateOf(false) }

    val hhmm = remember { Regex("""^\d{1,2}:\d{2}$""") }
    val timeError by remember(oraInizio, oraFine) {
        derivedStateOf {
            runCatching {
                val (h1, m1) = oraInizio.split(":").map { it.toInt() }
                val (h2, m2) = oraFine.split(":").map { it.toInt() }
                (h2 * 60 + m2) <= (h1 * 60 + m1)
            }.getOrElse { true }
        }
    }
    val canOpenParticipants = isPublic && titolo.isNotBlank() && hhmm.matches(oraInizio) && hhmm.matches(oraFine) && !timeError

    // pickers…
    if (showColorDialog) {
        AdvancedColorPickerDialog(
            onDismiss = { showColorDialog = false },
            onColorSelected = { selectedColor = it; showColorDialog = false }
        )
    }
    if (showCustomDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showCustomDatePicker = false },
            onDateSelected = {
                val parts = it.split("/")
                val cal = Calendar.getInstance()
                if (parts.size == 3) {
                    cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                    selectedPickerDate = cal
                }
                showCustomDatePicker = false
            }
        )
    }
    if (showCustomTimePickerStart) {
        CustomTimePickerDialog(onDismiss = { showCustomTimePickerStart = false }) {
            oraInizio = it; showCustomTimePickerStart = false
        }
    }
    if (showCustomTimePickerEnd) {
        CustomTimePickerDialog(onDismiss = { showCustomTimePickerEnd = false }) {
            oraFine = it; showCustomTimePickerEnd = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.background,
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Chiudi", color = colors.shade800) }
                }

                Text(dialogTitle, fontSize = 20.sp, color = colors.shade950)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = titolo, onValueChange = { titolo = it }, label = { Text("Titolo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.shade800, unfocusedTextColor = colors.shade800,
                        focusedBorderColor = colors.shade800, unfocusedBorderColor = colors.shade800,
                        focusedLabelColor = colors.shade800, unfocusedLabelColor = colors.shade800.copy(alpha = 0.7f),
                        cursorColor = colors.shade800
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showCustomDatePicker = true },
                    border = BorderStroke(2.dp, colors.shade800),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text("Data: ${fmt.format(selectedPickerDate.time)}", color = colors.shade800)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        onClick = { showCustomTimePickerStart = true },
                        border = BorderStroke(2.dp, colors.shade800),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text(if (oraInizio.isBlank()) "Ora Inizio" else oraInizio, color = colors.shade800) }

                    Button(
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        onClick = { showCustomTimePickerEnd = true },
                        border = BorderStroke(2.dp, colors.shade800),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text(if (oraFine.isBlank()) "Ora Fine" else oraFine, color = colors.shade800) }
                }

                if (timeError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⚠ L'orario di fine deve essere dopo quello di inizio (formato HH:mm)", color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descrizione, onValueChange = { descrizione = it }, label = { Text("Descrizione") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.shade800, unfocusedTextColor = colors.shade800,
                        focusedBorderColor = colors.shade800, unfocusedBorderColor = colors.shade800,
                        focusedLabelColor = colors.shade800, unfocusedLabelColor = colors.shade800.copy(alpha = 0.7f),
                        cursorColor = colors.shade800
                    )
                )

                // --- PARTECIPANTI (TURNI PUBBLICI) ---
                if (isPublic) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Partecipanti", color = colors.shade950)

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            chosenNames.forEach { name ->
                                AssistChip(
                                    onClick = {}, enabled = false, label = { Text(name) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = colors.shade100,
                                        disabledLabelColor = colors.shade800
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { if (canOpenParticipants) showParticipantPicker = true },
                            enabled = canOpenParticipants,                                  // ← BLOCCO QUI
                            border = BorderStroke(1.dp, colors.shade800),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.shade800)
                        ) {
                            Text(if (chosenNames.isEmpty()) "Gestisci partecipanti…" else "Modifica partecipanti…")
                        }
                        if (!canOpenParticipants) {
                            Spacer(Modifier.height(4.dp))
                            Text("Compila titolo e orari validi (HH:mm) prima di scegliere i partecipanti.", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Colore evento", color = colors.shade950)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { color ->
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(color)
                                    .border(width = if (isSelected) 2.dp else 1.dp, color = colors.shade800, shape = CircleShape)
                                    .clickable { selectedColor = color }
                            )
                        }
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.background)
                                .border(1.dp, colors.shade800, CircleShape).clickable { showColorDialog = true },
                            contentAlignment = Alignment.Center
                        ) { Icon(imageVector = Icons.Default.Edit, contentDescription = "Custom Color", tint = colors.shade800) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FOOTER ACTION
                if (!isPublic) {
                    Button(
                        onClick = {
                            if (!timeError && titolo.isNotBlank() && hhmm.matches(oraInizio) && hhmm.matches(oraFine)) {
                                val event = CalendarEvent(
                                    id = -System.currentTimeMillis(), startTime = oraInizio, endTime = oraFine,
                                    title = titolo, description = descrizione, color = selectedColor,
                                    date = selectedPickerDate.clone() as Calendar, participants = emptyList(),
                                    kind = CalendarItemKind.EVENT
                                )
                                onAddEvent(event, emptyList(), false)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.shade800)
                    ) { Text("Aggiungi", color = colors.shade100) }
                } else {
                    Button(
                        onClick = {
                            if (!timeError && titolo.isNotBlank() && hhmm.matches(oraInizio) && hhmm.matches(oraFine)) {
                                val shift = CalendarEvent(
                                    id = -System.currentTimeMillis(), startTime = oraInizio, endTime = oraFine,
                                    title = titolo, description = descrizione, color = selectedColor,
                                    date = selectedPickerDate.clone() as Calendar, participants = emptyList(),
                                    kind = CalendarItemKind.SHIFT
                                )
                                onAddEvent(shift, selectedEmployeeIds.toList(), true)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.shade800)
                    ) { Text("Crea turno", color = colors.shade100) }
                }
            }
        }
    }

    // --- FULLSCREEN: partecipanti (apre solo se canOpenParticipants = true) ---
    if (showParticipantPicker && isPublic) {
        val safeTemplate = remember(titolo, oraInizio, oraFine, selectedColor, descrizione) {
            val s = if (hhmm.matches(oraInizio)) oraInizio else "00:00"
            val e = if (hhmm.matches(oraFine))   oraFine   else "00:01"
            ShiftTemplate(
                title = titolo.ifBlank { "Turno" },
                startTime = s,
                endTime = e,
                description = descrizione,
                color = selectedColor
            )
        }
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showParticipantPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                com.example.myapplication.android.ui.screens.ShiftParticipantSelectionScreen(
                    selectedDate = selectedPickerDate,
                    onDateChange = { selectedPickerDate = it },
                    shiftTemplate = safeTemplate,
                    allEmployees = allEmployees,
                    onBack = { showParticipantPicker = false },
                    onConfirm = { _, ids ->
                        selectedEmployeeIds.clear()
                        selectedEmployeeIds.addAll(ids)
                        showParticipantPicker = false
                    },
                    events = emptyList(),
                    initialSelectedUserIds = selectedEmployeeIds.toList()
                )
            }
        }
    }
}
