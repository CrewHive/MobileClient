// FILE: TemplatePrivatePopup.kt
package com.example.myapplication.android.ui.components

import CustomTimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.*

@Composable
fun TemplatePrivatePopup(
    onDismiss: () -> Unit,
    onSave: (ShiftTemplate) -> Unit,
    selectedDate: Calendar
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF81C784)) }
    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    if (showTimePickerStart) {
        CustomTimePickerDialog(
            onDismiss = { showTimePickerStart = false },
            onTimeSelected = {
                startTime = it
                showTimePickerStart = false
            }
        )
    }
    if (showTimePickerEnd) {
        CustomTimePickerDialog(
            onDismiss = { showTimePickerEnd = false },
            onTimeSelected = {
                endTime = it
                showTimePickerEnd = false
            }
        )
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF8E1),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nuovo Template", fontSize = 20.sp, color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titolo", color = Color(0xFF5D4037)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF7D4F16),
                        unfocusedTextColor = Color(0xFF7D4F16),
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16)
                    ))
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showTimePickerStart = true },
                    border = BorderStroke(2.dp, Color(0xFF7D4F16)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text(if (startTime.isBlank()) "Ora Inizio" else startTime, color = Color(0xFF7D4F16))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showTimePickerEnd = true },
                    border = BorderStroke(2.dp, Color(0xFF7D4F16)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text(if (endTime.isBlank()) "Ora Fine" else endTime,
                        color = Color(0xFF7D4F16))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione", color = Color(0xFF5D4037)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF7D4F16),
                        unfocusedTextColor = Color(0xFF7D4F16),
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16)
                    ))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Colore", color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(4.dp))
                ColorPicker(selectedColor) { showColorDialog = true }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (title.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                        onSave(ShiftTemplate(title, startTime, endTime, selectedColor, description))
                    }
                },colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16))) {
                    Text("Salva",
                        color = Color(0xFFFAF7C7))
                }
            }
        }
    }
}

@Composable
fun TemplatePublicPopup(
    template: ShiftTemplate,
    onDismiss: () -> Unit,
    onSave: (ShiftTemplate) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(template.title) }
    var startTime by remember { mutableStateOf(template.startTime) }
    var endTime by remember { mutableStateOf(template.endTime) }
    var description by remember { mutableStateOf(template.description) }
    var selectedColor by remember { mutableStateOf(template.color) }
    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showTimePickerStart) {
        CustomTimePickerDialog(
            onDismiss = { showTimePickerStart = false },
            onTimeSelected = {
                startTime = it
                showTimePickerStart = false
            }
        )
    }
    if (showTimePickerEnd) {
        CustomTimePickerDialog(
            onDismiss = { showTimePickerEnd = false },
            onTimeSelected = {
                endTime = it
                showTimePickerEnd = false
            }
        )
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

    if (showDeleteConfirmation) {
        Dialog(onDismissRequest = { showDeleteConfirmation = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Confermi l'eliminazione di questo template?", color = Color.Black, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = {
                            showDeleteConfirmation = false
                            onDelete()
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Text("Elimina", color = Color.White)
                        }
                        OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Annulla")
                        }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF8E1),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Modifica Template", fontSize = 20.sp, color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titolo") },
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
                Button(onClick = { showTimePickerStart = true },
                    border = BorderStroke(2.dp, Color(0xFF7D4F16)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text(if (startTime.isBlank()) "Ora Inizio" else startTime,
                        color = Color(0xFF7D4F16))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showTimePickerEnd = true },
                    border = BorderStroke(2.dp, Color(0xFF7D4F16)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text(if (endTime.isBlank()) "Ora Fine" else endTime,
                        color = Color(0xFF7D4F16))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione") },
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
                Text("Colore", color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(4.dp))
                ColorPicker(selectedColor) { showColorDialog = true }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = {
                        if (title.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                            onSave(ShiftTemplate(title, startTime, endTime, selectedColor, description))
                        }
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16))) {
                        Text("Salva modifiche",
                            color = Color(0xFFFAF7C7))
                    }
                    OutlinedButton(onClick = { showDeleteConfirmation = true }) {
                        Text("Elimina", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPicker(currentColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(currentColor)
            .clickable(onClick = onClick)
    ) {}
}