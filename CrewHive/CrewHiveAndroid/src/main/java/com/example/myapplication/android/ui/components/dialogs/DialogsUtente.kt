package com.example.myapplication.android.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.android.R

// Palette coerente con ErrorPopupDialog
private val Beige = Color(0xFFFFF8E1)
private val Brown = Color(0xFF7D4F16)
private val BrownText = Color(0xFF5D4037)
private val Danger = Color(0xFFE57373)

/* -------------------------------------------------------------------------- */
/* 1) DIALOG DI CONFERMA                                                       */
/* -------------------------------------------------------------------------- */
@Composable
fun ConfirmPopupDialog(
    title: String,
    message: String,
    confirmText: String = "Conferma",
    cancelText: String = "Annulla",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Beige,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Brown)

                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrownText,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = BrownText,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Brown)
                    ) { Text(cancelText) }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) Danger else Brown,
                            contentColor = Beige
                        )
                    ) { Text(confirmText) }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* 2) DIALOG INPUT TESTO SINGOLO (es. Cambia username)                         */
/* -------------------------------------------------------------------------- */
@Composable
fun TextInputPopupDialog(
    title: String,
    label: String,
    initialValue: String = "",
    confirmText: String = "Salva",
    cancelText: String = "Annulla",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Beige,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Brown)

                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrownText,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    label = { Text(label) },
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Brown.copy(alpha = 0.7f),
                        cursorColor = Brown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, color = Danger, fontSize = 13.sp, textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Brown)
                    ) { Text(cancelText) }

                    Button(
                        onClick = { onConfirm(value.trim()) },
                        enabled = !isLoading && value.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Brown, contentColor = Beige)
                    ) { Text(if (isLoading) "Salvataggio..." else confirmText) }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* 3) DIALOG CAMBIO PASSWORD (old/new + toggle visibilità)                     */
/* -------------------------------------------------------------------------- */
@Composable
fun ChangePasswordPopupDialog(
    title: String = "Cambia password",
    confirmText: String = "Aggiorna",
    cancelText: String = "Annulla",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onConfirm: (old: String, new: String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }

    val valid = oldPwd.isNotBlank() && newPwd.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Beige,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Brown)

                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrownText,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                // Password attuale
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text("Password attuale") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { oldVisible = !oldVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (oldVisible) R.drawable.hide else R.drawable.eye
                                ),
                                contentDescription = if (oldVisible) "Nascondi password" else "Mostra password",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Brown.copy(alpha = 0.7f),
                        cursorColor = Brown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // Nuova password
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("Nuova password") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (newVisible) R.drawable.hide else R.drawable.eye
                                ),
                                contentDescription = if (newVisible) "Nascondi password" else "Mostra password",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Brown.copy(alpha = 0.7f),
                        cursorColor = Brown
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, color = Danger, fontSize = 13.sp, textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Brown)
                    ) { Text(cancelText) }

                    Button(
                        onClick = { onConfirm(oldPwd, newPwd) },
                        enabled = !isLoading && valid,
                        colors = ButtonDefaults.buttonColors(containerColor = Brown, contentColor = Beige)
                    ) { Text(if (isLoading) "Aggiornamento..." else confirmText) }
                }
            }
        }
    }
}

