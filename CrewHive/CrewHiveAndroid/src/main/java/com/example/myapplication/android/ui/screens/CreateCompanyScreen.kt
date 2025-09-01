package com.example.myapplication.android.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.components.pickers.CompanyTypePickerDialog
import com.example.myapplication.android.ui.core.api.dto.AddressDTO
import com.example.myapplication.android.ui.theme.CustomTheme
import androidx.compose.foundation.text.KeyboardOptions

enum class CompanyType(val displayName: String) {
    HOSPITAL("Ospedale"),
    RESTAURANT("Ristorante"),
    BAR("Bar"),
    OTHER("Altro");
}

@Composable
fun CreateCompanyScreen(
    onBack: () -> Unit,
    onCreateCompany: ((name: String, type: CompanyType, address: String?) -> Unit)? = null,
    onCreateCompanyDto: ((name: String, type: CompanyType, address: AddressDTO) -> Unit)? = null,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundRes: Int = R.drawable.sfondo_crea
) {
    val colors = CustomTheme.colors

    var name by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<CompanyType?>(null) }
    var showTypeDialog by remember { mutableStateOf(false) }

    // campi indirizzo
    var street by rememberSaveable { mutableStateOf("") }
    var zipCode by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var province by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }

    // VALIDAZIONI BASE
    val nameError = name.isBlank()
    val typeError = selectedType == null

    val anyAddress = listOf(street, zipCode, city, province, country).any { it.isNotBlank() }
    val allAddressFilled = listOf(street, zipCode, city, province, country).all { it.isNotBlank() }

    val provinceValid = province.length == 2 && province.all { it.isLetter() }
    val zipValid = zipCode.length >= 5 && zipCode.all { it.isDigit() }

    val addressValid = !anyAddress || (allAddressFilled && provinceValid && zipValid)
    val addressError = !addressValid

    val formValid = !nameError && !typeError && addressValid

    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            // back
            FilledTonalIconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(44.dp)
                    .clip(CircleShape),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = colors.shade600
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
            }

            // card form
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Crea la tua azienda",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.shade900
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "I campi contrassegnati con * sono obbligatori.",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.shade700)
                    )

                    Spacer(Modifier.height(18.dp))

                    // Nome azienda — OUTLINED
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome azienda *") },
                        placeholder = { Text("Es. CrewHive S.r.l.") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        ),
                        isError = nameError,
                        supportingText = {
                            if (nameError) Text("Inserisci il nome dell’azienda", color = MaterialTheme.colorScheme.error)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(colors)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Tipo — OUTLINED (readOnly) con overlay cliccabile
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedType?.displayName ?: "",
                            onValueChange = { /* readOnly */ },
                            readOnly = true,
                            label = { Text("Tipo *") },
                            placeholder = { Text("Scegli una categoria") },
                            isError = typeError,
                            supportingText = {
                                if (typeError) Text("Seleziona un tipo", color = MaterialTheme.colorScheme.error)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors(colors)
                        )
                        Spacer(
                            Modifier
                                .matchParentSize()
                                .clickable { showTypeDialog = true }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider(color = colors.shade600.copy(alpha = 0.2f))
                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Indirizzo (facoltativo — o tutti i campi o nessuno)",
                        style = MaterialTheme.typography.labelLarge.copy(color = colors.shade700),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Via e numero") },
                        singleLine = true,
                        isError = anyAddress && street.isBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(colors)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = zipCode,
                            onValueChange = { input -> zipCode = input.filter { it.isDigit() } },
                            label = { Text("CAP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = anyAddress && (!zipValid || zipCode.isBlank()),
                            supportingText = {
                                if (anyAddress && !zipValid) Text("Almeno 5 cifre", color = MaterialTheme.colorScheme.error)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Città") },
                            singleLine = true,
                            isError = anyAddress && city.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = province,
                            onValueChange = { input ->
                                province = input.uppercase().filter { it.isLetter() }.take(2)
                            },
                            label = { Text("Provincia (2 lettere)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            isError = anyAddress && (!provinceValid || province.isBlank()),
                            supportingText = {
                                if (anyAddress && !provinceValid) Text("Usa esattamente 2 lettere (es. TN)", color = MaterialTheme.colorScheme.error)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Paese") },
                            singleLine = true,
                            isError = anyAddress && country.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                    }

                    if (addressError && anyAddress) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Completa correttamente tutti i campi dell’indirizzo (Provincia = 2 lettere, CAP = almeno 5 cifre) oppure lasciali vuoti.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    Divider(color = colors.shade600.copy(alpha = 0.2f))
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.shade600),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(colors.shade600)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Indietro") }

                        Button(
                            onClick = {
                                val cleanedName = name.trim()
                                val type = selectedType ?: return@Button

                                if (allAddressFilled && provinceValid && zipValid) {
                                    val dto = AddressDTO(
                                        street = street.trim(),
                                        city = city.trim(),
                                        zipCode = zipCode.trim(),
                                        province = province.trim(),
                                        country = country.trim()
                                    )
                                    onCreateCompanyDto?.invoke(cleanedName, type, dto)
                                } else {
                                    onCreateCompany?.invoke(cleanedName, type, null)
                                }
                            },
                            enabled = formValid,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.shade600,
                                contentColor = colors.background,
                                disabledContainerColor = colors.shade600.copy(alpha = 0.4f),
                                disabledContentColor = colors.background.copy(alpha = 0.7f)
                            )
                        ) { Text("Crea") }
                    }
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

@Composable
private fun fieldColors(colors: com.example.myapplication.android.ui.theme.AppColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.shade600,
        unfocusedBorderColor = colors.shade600,
        errorBorderColor = MaterialTheme.colorScheme.error,
        focusedLabelColor = colors.shade600,
        unfocusedLabelColor = colors.shade600.copy(alpha = 0.7f),
        cursorColor = colors.shade600,
        focusedTextColor = Color(0xFF5D4037),
        unfocusedTextColor = Color(0xFF5D4037),
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error
    )
