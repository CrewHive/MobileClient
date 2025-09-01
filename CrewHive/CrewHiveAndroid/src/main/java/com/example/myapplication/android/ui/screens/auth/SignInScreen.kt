// FILE: SignInScreen.kt
package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldColors
import com.example.myapplication.android.R as AR

@Composable
fun SignInScreen(
    onSignInClick: (emailOrUsername: String, password: String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    // ⬇️ nuovo: controllo del caricamento dallo strato superiore
    isLoading: Boolean = false
) {
    val colors = CustomTheme.colors

    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var triedSubmit by remember { mutableStateOf(false) }

    val userFieldEmpty = emailOrUsername.isBlank()
    val passFieldEmpty = password.isBlank()
    val canSignIn = !userFieldEmpty && !passFieldEmpty

    val fieldShape = RoundedCornerShape(12.dp)

    @Composable
    fun inputColors(error: Boolean): TextFieldColors {
        val borderFocused = if (error) MaterialTheme.colorScheme.error else colors.shade600
        val borderUnfocused = if (error) MaterialTheme.colorScheme.error else colors.shade600.copy(alpha = 0.6f)
        return OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = colors.background.copy(alpha = 0.96f),
            unfocusedContainerColor = colors.background.copy(alpha = 0.92f),
            errorContainerColor     = colors.background.copy(alpha = 0.96f),

            focusedBorderColor   = borderFocused,
            unfocusedBorderColor = borderUnfocused,

            focusedTextColor   = colors.shade900,
            unfocusedTextColor = colors.shade900,

            cursorColor = colors.shade900,

            focusedLabelColor   = colors.shade600,
            unfocusedLabelColor = colors.shade600,

            focusedPlaceholderColor   = colors.shade600.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = colors.shade600.copy(alpha = 0.6f),

            focusedSupportingTextColor   = MaterialTheme.colorScheme.error,
            unfocusedSupportingTextColor = MaterialTheme.colorScheme.error
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val bgPainter: Painter = painterResource(id = AR.drawable.signin_bg)
        Image(
            painter = bgPainter,
            contentDescription = "Sfondo accesso",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Accedi",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.shade900
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // USERNAME / EMAIL
            OutlinedTextField(
                value = emailOrUsername,
                onValueChange = { emailOrUsername = it },
                label = { Text("Username", color = colors.shade600) },
                placeholder = { Text("Il tuo username") },
                singleLine = true,
                supportingText = {
                    if (triedSubmit && userFieldEmpty) {
                        Text("Inserisci username", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                },
                colors = inputColors(error = triedSubmit && userFieldEmpty),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth(),
                // ⬇️ disabilita durante il loading
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = colors.shade600) },
                placeholder = { Text("La tua password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val iconRes = if (passwordVisible) AR.drawable.hide else AR.drawable.eye
                    IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                supportingText = {
                    if (triedSubmit && passFieldEmpty) {
                        Text("Inserisci la password", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                },
                colors = inputColors(error = triedSubmit && passFieldEmpty),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth(),
                // ⬇️ disabilita durante il loading
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ACCEDI
            Button(
                onClick = {
                    triedSubmit = true
                    if (canSignIn && !isLoading) onSignInClick(emailOrUsername.trim(), password)
                },
                enabled = canSignIn && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.shade600,
                    disabledContainerColor = colors.shade600.copy(alpha = 0.4f)
                )
            ) {
                if (isLoading) {
                    // spinner inline nel bottone
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = colors.background,
                        modifier = Modifier
                            .size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(text = "Accesso in corso…", color = colors.background, fontSize = 16.sp)
                } else {
                    Text(text = "Accedi", color = colors.background, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Non hai un account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Registrati",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.shade600,
                            modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToSignUp() }
                        )
                    }
                }
            }
        }

        // ⬇️ Overlay globale durante il loading (blocca interazioni e mostra spinner)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
