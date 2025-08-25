package com.example.myapplication.android.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.components.pickers.EmployeeContractTypePickerDialog
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.state.EmployeeContractType

@Composable
fun EmployeeDetailScreen(
    employee: CompanyEmployee,
    onBack: () -> Unit,
    onSave: (CompanyEmployee) -> Unit,
    onRemove: (CompanyEmployee) -> Unit,
//    @DrawableRes backgroundRes: Int = R.drawable.onboarding_bg
) {
    var name by rememberSaveable(employee.userId) { mutableStateOf(employee.name) }
    var contract by rememberSaveable(employee.userId) { mutableStateOf(employee.contractType) }
    var weeklyHours by rememberSaveable(employee.userId) { mutableStateOf(employee.weeklyHours.toString()) }
    var overtime by rememberSaveable(employee.userId) { mutableStateOf(employee.overtimeHours) }
    var vacAcc by rememberSaveable(employee.userId) { mutableStateOf(employee.vacationDaysAccumulated.toString()) }
    var vacUsed by rememberSaveable(employee.userId) { mutableStateOf(employee.vacationDaysUsed.toString()) }
    var leaveAcc by rememberSaveable(employee.userId) { mutableStateOf(employee.leaveDaysAccumulated.toString()) }
    var leaveUsed by rememberSaveable(employee.userId) { mutableStateOf(employee.leaveDaysUsed.toString()) }
    var showTypeDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Box(Modifier.fillMaxSize()) {
//            Image(
//                painter = painterResource(backgroundRes),
//                contentDescription = null,
//                modifier = Modifier.matchParentSize(),
//                contentScale = ContentScale.Crop
//            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(text = "Employee details", color = Color(0xFF5D4037))
                Spacer(Modifier.height(8.dp))
                Text(text = "User ID: ${employee.userId}", color = Color(0xFF7D4F16))

                Spacer(Modifier.height(12.dp))

                // Nome visualizzato (facoltativo, utile per ParticipantRow)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (display)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16)
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Tipo di contratto (dialog personalizzato)
                OutlinedTextField(
                    value = contract?.displayName ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Contract type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoRipple { showTypeDialog = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16)
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Ore settimanali (numero)
                OutlinedTextField(
                    value = weeklyHours,
                    onValueChange = { weeklyHours = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Weekly hours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16)
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Straordinari (stepper + / −)
                Text(text = "Overtime hours", color = Color(0xFF5D4037))
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { if (overtime > 0) overtime-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7D4F16)),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) { Text("−") }
                    Spacer(Modifier.width(8.dp))
                    Text(text = overtime.toString(), color = Color(0xFF5D4037))
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { overtime++ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7D4F16)),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) { Text("+") }
                }

                Spacer(Modifier.height(12.dp))

                // Ferie e permessi (campi numerici)
                DoubleNumberRow(
                    leftLabel = "Vacation accumulated",
                    leftValue = vacAcc,
                    onLeftChange = { vacAcc = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                    rightLabel = "Vacation used",
                    rightValue = vacUsed,
                    onRightChange = { vacUsed = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } }
                )

                Spacer(Modifier.height(8.dp))

                DoubleNumberRow(
                    leftLabel = "Leave accumulated",
                    leftValue = leaveAcc,
                    onLeftChange = { leaveAcc = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                    rightLabel = "Leave used",
                    rightValue = leaveUsed,
                    onRightChange = { leaveUsed = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } }
                )

                Spacer(Modifier.height(16.dp))

                // Azioni
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7D4F16)),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) { Text("Back") }

                    Button(
                        onClick = {
                            val emp = employee.copy(
                                name = name.trim().ifBlank { employee.userId },
                                contractType = contract,
                                weeklyHours = weeklyHours.toIntOrNull() ?: employee.weeklyHours,
                                overtimeHours = overtime,
                                vacationDaysAccumulated = vacAcc.replace(',', '.').toFloatOrNull() ?: 0f,
                                vacationDaysUsed = vacUsed.replace(',', '.').toFloatOrNull() ?: 0f,
                                leaveDaysAccumulated = leaveAcc.replace(',', '.').toFloatOrNull() ?: 0f,
                                leaveDaysUsed = leaveUsed.replace(',', '.').toFloatOrNull() ?: 0f
                            )
                            onSave(emp)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16), contentColor = Color(0xFFFFF8E1))
                    ) { Text("Save") }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { onRemove(employee) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373), contentColor = Color.White)
                ) { Text("Remove employee") }
            }
        }
    }

    if (showTypeDialog) {
        EmployeeContractTypePickerDialog(
            current = contract,
            onDismiss = { showTypeDialog = false },
            onSelect = { picked -> contract = picked; showTypeDialog = false }
        )
    }
}

// piccolo helper per click "trasparente" sul TextField
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit) =
    this.then(Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() })

@Composable
private fun DoubleNumberRow(
    leftLabel: String,
    leftValue: String,
    onLeftChange: (String) -> Unit,
    rightLabel: String,
    rightValue: String,
    onRightChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = leftValue,
            onValueChange = onLeftChange,
            label = { Text(leftLabel) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7D4F16),
                unfocusedBorderColor = Color(0xFF7D4F16),
                focusedLabelColor = Color(0xFF7D4F16),
                unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                cursorColor = Color(0xFF7D4F16)
            )
        )
        OutlinedTextField(
            value = rightValue,
            onValueChange = onRightChange,
            label = { Text(rightLabel) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7D4F16),
                unfocusedBorderColor = Color(0xFF7D4F16),
                focusedLabelColor = Color(0xFF7D4F16),
                unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                cursorColor = Color(0xFF7D4F16)
            )
        )
    }
}
