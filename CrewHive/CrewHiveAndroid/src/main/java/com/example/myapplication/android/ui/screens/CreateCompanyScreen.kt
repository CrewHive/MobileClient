package com.example.myapplication.android.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.components.pickers.CompanyTypePickerDialog

enum class CompanyType(val displayName: String) {
    HOSPITAL("Hospital"),
    RESTAURANT("Restaurant"),
    BAR("Bar"),
    OTHER("Other");
}

@Composable
fun CreateCompanyScreen(
    onBack: () -> Unit,
    onCreateCompany: (name: String, type: CompanyType, address: String?) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundRes: Int = R.drawable.sfondo_crea
) {
    var name by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<CompanyType?>(null) }
    var showTypeDialog by remember { mutableStateOf(false) }

    val nameError = name.isBlank()
    val typeError = selectedType == null
    val formValid = !nameError && !typeError

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White // bianco sotto al PNG
    ) {
        Box(Modifier.fillMaxSize()) {
            // Sfondo PNG
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            // Contenuto
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Create your company",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Nome (obbligatorio)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company name *") },
                    singleLine = true,
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16),
                        focusedTextColor = Color(0xFF5D4037),
                        unfocusedTextColor = Color(0xFF5D4037)
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Tipo (obbligatorio) — campo "finto" cliccabile che apre un Dialog
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType?.displayName ?: "",
                        onValueChange = { /* read-only */ },
                        readOnly = true,
                        label = { Text("Type *") },
                        isError = typeError,
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7D4F16),
                            unfocusedBorderColor = Color(0xFF7D4F16),
                            focusedLabelColor = Color(0xFF7D4F16),
                            unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                            cursorColor = Color(0xFF7D4F16),
                            focusedTextColor = Color(0xFF5D4037),
                            unfocusedTextColor = Color(0xFF5D4037)
                        )
                    )
                    // overlay cliccabile sopra al TextField
                    Spacer(
                        Modifier
                            .matchParentSize()
                            .clickable { showTypeDialog = true }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Indirizzo (opzionale)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7D4F16),
                        unfocusedBorderColor = Color(0xFF7D4F16),
                        focusedLabelColor = Color(0xFF7D4F16),
                        unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                        cursorColor = Color(0xFF7D4F16),
                        focusedTextColor = Color(0xFF5D4037),
                        unfocusedTextColor = Color(0xFF5D4037)
                    )
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7D4F16)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF7D4F16))
                        )
                    ) { Text("Back") }

                    Button(
                        onClick = {
                            val cleanedName = name.trim()
                            val cleanedAddress = address.trim().ifBlank { null }
                            val type = selectedType ?: return@Button
                            onCreateCompany(cleanedName, type, cleanedAddress)
                        },
                        enabled = formValid,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16), contentColor = Color(0xFFFFF8E1))
                    ) { Text("Create") }
                }
            }
        }
    }

    if (showTypeDialog) {
        CompanyTypePickerDialog(
            current = selectedType,
            onDismiss = { showTypeDialog = false },
            onSelect = { picked ->
                selectedType = picked
                showTypeDialog = false
            }
        )
    }
}
