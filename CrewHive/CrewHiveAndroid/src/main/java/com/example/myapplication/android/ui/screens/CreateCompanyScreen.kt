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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.components.pickers.CompanyTypePickerDialog
import com.example.myapplication.android.ui.core.api.dto.AddressDTO
import com.example.myapplication.android.ui.core.api.dto.singleLine
import com.example.myapplication.android.ui.theme.CustomTheme

enum class CompanyType(val displayName: String) {
    HOSPITAL("Hospital"),
    RESTAURANT("Restaurant"),
    BAR("Bar"),
    OTHER("Other");
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

    // controlli minimi
    val nameError = name.isBlank()
    val typeError = selectedType == null

    // logica indirizzo: o tutti vuoti, o tutti pieni
    val anyAddress = listOf(street, zipCode, city, province, country).any { it.isNotBlank() }
    val allAddressFilled = listOf(street, zipCode, city, province, country).all { it.isNotBlank() }
    val addressError = anyAddress && !allAddressFilled

    val formValid = !(nameError || typeError || addressError)

    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        Box(Modifier.fillMaxSize()) {
            // background immagine
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            // pulsante back come nelle altre schermate
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // card centrale con form (stile Home/Join)
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
                        "Create your company",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.shade900
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Fields marked * are required.",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.shade700)
                    )

                    Spacer(Modifier.height(18.dp))

                    // Company name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Company name *") },
                        singleLine = true,
                        isError = nameError,
                        supportingText = {
                            if (nameError) Text("Please enter a company name", color = MaterialTheme.colorScheme.error)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(colors)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Type (picker)
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedType?.displayName ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Type *") },
                            isError = typeError,
                            supportingText = {
                                if (typeError) Text("Please choose a type", color = MaterialTheme.colorScheme.error)
                            },
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
                        "Address (optional — all fields or none)",
                        style = MaterialTheme.typography.labelLarge.copy(color = colors.shade700),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street") },
                        singleLine = true,
                        isError = addressError && street.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(colors)
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = zipCode,
                            onValueChange = { zipCode = it },
                            label = { Text("ZIP Code") },
                            singleLine = true,
                            isError = addressError && zipCode.isBlank(),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            singleLine = true,
                            isError = addressError && city.isBlank(),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = province,
                            onValueChange = { province = it },
                            label = { Text("Province") },
                            singleLine = true,
                            isError = addressError && province.isBlank(),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Country") },
                            singleLine = true,
                            isError = addressError && country.isBlank(),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(colors)
                        )
                    }

                    if (addressError) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Please complete all address fields or leave them all blank.",
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
                        ) { Text("Back") }

                        Button(
                            onClick = {
                                val cleanedName = name.trim()
                                val type = selectedType ?: return@Button

                                if (allAddressFilled) {
                                    val dto = AddressDTO(
                                        street = street.trim(),
                                        city = city.trim(),
                                        zipCode = zipCode.trim(),
                                        province = province.trim(),
                                        country = country.trim()
                                    )
                                    // invio la DTO completa
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
                        ) { Text("Create") }
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
        focusedLabelColor = colors.shade600,
        unfocusedLabelColor = colors.shade600.copy(alpha = 0.7f),
        cursorColor = colors.shade600,
        focusedTextColor = Color(0xFF5D4037),
        unfocusedTextColor = Color(0xFF5D4037)
    )
