// FILE: EditablePopupDialog.kt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.android.ui.components.pickers.AdvancedColorPickerDialog
import com.example.myapplication.android.ui.components.calendar.CalendarEvent
import com.example.myapplication.android.ui.components.calendar.CalendarItemKind
import com.example.myapplication.android.ui.components.navigation.ShiftTemplate
import com.example.myapplication.android.ui.state.CompanyEmployee
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditablePopupDialog(
    eventToEdit: CalendarEvent,
    onDismiss: () -> Unit,
    onUpdateEvent: (CalendarEvent, List<Long>?) -> Unit,
    allEmployees: List<CompanyEmployee> = emptyList(),
    canEditParticipants: Boolean = false,
    allEvents: List<CalendarEvent> = emptyList() // per la preview del mini-calendario nel selettore
) {
    val isShift = eventToEdit.kind == CalendarItemKind.SHIFT

    // ---------- STATE PRINCIPALI ----------
    var titolo by remember { mutableStateOf(eventToEdit.title) }
    var selectedPickerDate by remember { mutableStateOf(eventToEdit.date.clone() as Calendar) }
    var oraInizio by remember { mutableStateOf(eventToEdit.startTime) }
    var oraFine by remember { mutableStateOf(eventToEdit.endTime) }
    var descrizione by remember { mutableStateOf(eventToEdit.description ?: "") }
    var selectedColor by remember { mutableStateOf(eventToEdit.color) }

    // Pre-selezione partecipanti: se i "participants" sono numerici → sono userId,
    // altrimenti mappiamo i nomi sugli ID reali
    val preSelectedIds = remember {
        val parts = eventToEdit.participants
        val numeric = parts.mapNotNull { it.toLongOrNull() }
        if (numeric.isNotEmpty()) numeric
        else {
            val nameSet = parts.toSet()
            allEmployees.filter { it.name in nameSet }.mapNotNull { it.userId.toLongOrNull() }
        }
    }
    val selectedEmployeeIds = remember { mutableStateListOf<Long>().apply { addAll(preSelectedIds) } }

    // ---------- DIALOG SECONDARI ----------
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePickerStart by remember { mutableStateOf(false) }
    var showCustomTimePickerEnd by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showParticipantPicker by remember { mutableStateOf(false) }

    // ---------- STILI / HELPERS ----------
    val brown = Color(0xFF7D4F16)
    val brownText = Color(0xFF5D4037)
    val beige = Color(0xFFFFF8E1)
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val timeError by remember(oraInizio, oraFine) {
        derivedStateOf {
            try {
                val (h1, m1) = oraInizio.split(":").map { it.toInt() }
                val (h2, m2) = oraFine.split(":").map { it.toInt() }
                val s = h1 * 60 + m1
                val e = h2 * 60 + m2
                e <= s
            } catch (_: Exception) { false }
        }
    }

    // ---------- COLOR PICKER ----------
    if (showColorDialog) {
        AdvancedColorPickerDialog(
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                selectedColor = it
                showColorDialog = false
            }
        )
    }

    // ---------- DATE / TIME PICKER ----------
    if (showCustomDatePicker) {
        CustomDatePickerDialog(
            onDismiss = { showCustomDatePicker = false },
            onDateSelected = { ddmmyyyy ->
                runCatching {
                    val p = ddmmyyyy.split("/")
                    Calendar.getInstance().apply {
                        set(p[2].toInt(), p[1].toInt() - 1, p[0].toInt(), 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                }.onSuccess {
                    selectedPickerDate = it
                }
                showCustomDatePicker = false
            }
        )
    }
    if (showCustomTimePickerStart) {
        CustomTimePickerDialog(
            onDismiss = { showCustomTimePickerStart = false },
            onTimeSelected = { t -> oraInizio = t; showCustomTimePickerStart = false }
        )
    }
    if (showCustomTimePickerEnd) {
        CustomTimePickerDialog(
            onDismiss = { showCustomTimePickerEnd = false },
            onTimeSelected = { t -> oraFine = t; showCustomTimePickerEnd = false }
        )
    }

    // ---------- MAIN DIALOG ----------
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Chiudi", color = brown) }
                }

                Text(
                    if (isShift) "Modifica turno" else "Modifica evento",
                    fontSize = 20.sp,
                    color = brownText,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))

                // Titolo
                OutlinedTextField(
                    value = titolo,
                    onValueChange = { titolo = it },
                    label = { Text("Titolo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = brownText,
                        unfocusedTextColor = brownText,
                        focusedBorderColor = brown,
                        unfocusedBorderColor = brown,
                        focusedLabelColor = brown,
                        unfocusedLabelColor = brown.copy(alpha = 0.7f),
                        cursorColor = brown
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Data
                Button(
                    onClick = { showCustomDatePicker = true },
                    border = BorderStroke(2.dp, brown),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) { Text("Data: ${dateFmt.format(selectedPickerDate.time)}", color = brown) }

                Spacer(Modifier.height(8.dp))

                // Orari
                Row(Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        onClick = { showCustomTimePickerStart = true },
                        border = BorderStroke(2.dp, brown),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text(oraInizio.ifBlank { "Ora inizio" }, color = brown) }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        onClick = { showCustomTimePickerEnd = true },
                        border = BorderStroke(2.dp, brown),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text(oraFine.ifBlank { "Ora fine" }, color = brown) }
                }

                if (timeError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "⚠ L'orario di fine deve essere successivo a quello di inizio",
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Descrizione
                OutlinedTextField(
                    value = descrizione,
                    onValueChange = { descrizione = it },
                    label = { Text("Descrizione") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = brownText,
                        unfocusedTextColor = brownText,
                        focusedBorderColor = brown,
                        unfocusedBorderColor = brown,
                        focusedLabelColor = brown,
                        unfocusedLabelColor = brown.copy(alpha = 0.7f),
                        cursorColor = brown
                    )
                )

                // Partecipanti (solo per TURNI e se permesso)
                if (isShift && canEditParticipants) {
                    Spacer(Modifier.height(12.dp))
                    Text("Partecipanti", color = brownText, fontWeight = FontWeight.Medium)

                    val chosenNames by remember(allEmployees) {
                        derivedStateOf {
                            val ids = selectedEmployeeIds.toList() // <- lettura che invalida quando cambia
                            allEmployees
                                .filter { it.userId.toLongOrNull() in ids }
                                .map { it.name }
                        }
                    }


                    Spacer(Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            chosenNames.forEach { name ->
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(name) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = beige,
                                        disabledLabelColor = brownText
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showParticipantPicker = true },
                            border = BorderStroke(1.dp, brown),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = brown)
                        ) {
                            Text(if (chosenNames.isEmpty()) "Inserisci partecipanti" else "Modifica partecipanti")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Colore
                Text("Colore", color = brownText, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val colorOptions = listOf(
                        Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFB74D),
                        Color(0xFFBA68C8), Color(0xFFE57373)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { c ->
                            val isSel = selectedColor == c
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSel) 3.dp else 1.dp,
                                        color = if (isSel) Color.Black else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = c }
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
                            Icon(Icons.Default.Edit, contentDescription = "Colore personalizzato", tint = Color.DarkGray)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // SALVA
                Button(
                    onClick = {
                        if (timeError || titolo.isBlank() || oraInizio.isBlank() || oraFine.isBlank()) return@Button

                        val updatedEvent = eventToEdit.copy(
                            title = titolo,
                            startTime = oraInizio,
                            endTime = oraFine,
                            description = descrizione,
                            color = selectedColor,
                            date = (selectedPickerDate.clone() as Calendar),
                            participants = if (isShift) {
                                // in UI mostriamo i nomi corrispondenti agli ID
                                allEmployees
                                    .filter { it.userId.toLongOrNull() in selectedEmployeeIds }
                                    .map { it.name }
                            } else eventToEdit.participants
                        )

                        val idsOrNull: List<Long>? =
                            if (isShift && canEditParticipants) selectedEmployeeIds.toList() else null

                        onUpdateEvent(updatedEvent, idsOrNull)
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = brown)
                ) { Text("Salva", color = Color(0xFFFAF7C7)) }
            }
        }
    }

    // ---------- FULLSCREEN: selezione partecipanti (riuso della tua schermata) ----------
    if (showParticipantPicker && isShift && canEditParticipants) {
        val shiftTemplate = remember(titolo, oraInizio, oraFine, selectedColor, descrizione) {
            ShiftTemplate(
                title = titolo.ifBlank { "Turno" },
                startTime = oraInizio,
                endTime = oraFine,
                description = descrizione,
                color = selectedColor
            )
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showParticipantPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // full-screen
        ) {
            Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                // IMPORTANTE: la tua ShiftParticipantSelectionScreen deve accettare initialSelectedUserIds
                com.example.myapplication.android.ui.screens.ShiftParticipantSelectionScreen(
                    selectedDate = selectedPickerDate,
                    onDateChange = { selectedPickerDate = it },
                    shiftTemplate = shiftTemplate,
                    allEmployees = allEmployees,
                    onBack = { showParticipantPicker = false },
                    onConfirm = { _, ids ->
                        selectedEmployeeIds.clear()
                        selectedEmployeeIds.addAll(ids)
                        showParticipantPicker = false
                    },
                    events = allEvents,
                    initialSelectedUserIds = selectedEmployeeIds.toList()
                )
            }
        }
    }
}
