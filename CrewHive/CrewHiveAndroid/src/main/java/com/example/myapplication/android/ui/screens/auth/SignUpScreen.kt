// FILE: SignUpScreen.kt
package com.example.myapplication.android.ui.screens

import android.util.Patterns
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.components.messages.TermsDialog
import com.example.myapplication.android.ui.theme.CustomTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldColors
import com.example.myapplication.android.R as AR

@Composable
fun SignUpScreen(
    onSignUpClick: (email: String, username: String, password: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = CustomTheme.colors

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreedToTos by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }

    // ---- VALIDAZIONI ----
    val emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val passLenOK = password.length >= 8
    val passHasDigit = password.any { it.isDigit() }
    val passHasUpper = password.any { it.isUpperCase() }
    val passHasSpecial = password.any { !it.isLetterOrDigit() }
    val passwordValid = passLenOK && passHasDigit && passHasUpper && passHasSpecial

    val showEmailError = email.isNotBlank() && !emailValid
    val showPasswordError = password.isNotBlank() && !passwordValid
    val canContinue = agreedToTos && emailValid && passwordValid

    val fieldShape = RoundedCornerShape(12.dp)

    @Composable
    fun inputColors(error: Boolean): TextFieldColors {
        val borderFocused = if (error) MaterialTheme.colorScheme.error else colors.shade600
        val borderUnfocused = if (error) MaterialTheme.colorScheme.error else colors.shade600.copy(alpha = 0.6f)
        return OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = colors.background.copy(alpha = 0.96f),
            unfocusedContainerColor = colors.background.copy(alpha = 0.92f),

            focusedBorderColor   = borderFocused,
            unfocusedBorderColor = borderUnfocused,

            cursorColor = colors.shade900,

            focusedTextColor   = colors.shade900,
            unfocusedTextColor = colors.shade900,
            focusedLabelColor   = colors.shade600,
            unfocusedLabelColor = colors.shade600,
            focusedPlaceholderColor   = colors.shade600.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = colors.shade600.copy(alpha = 0.6f),

            errorContainerColor = colors.background.copy(alpha = 0.96f),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val bgPainter: Painter = painterResource(id = AR.drawable.signup_bg)
        Image(
            painter = bgPainter,
            contentDescription = "Sfondo registrazione",
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
                    .align(Alignment.Start)
            ) {
                Text(
                    text = "Registrati",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.shade900
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = colors.shade600) },
                placeholder = { Text("La tua email") },
                singleLine = true,
                supportingText = {
                    if (showEmailError) {
                        Text(
                            "Inserisci un'email valida (es. nome@dominio.com)",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = inputColors(error = showEmailError),
                shape = fieldShape,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // USERNAME
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = colors.shade600) },
                placeholder = { Text("Scegli uno username") },
                singleLine = true,
                enabled = !isLoading,
                colors = inputColors(error = false),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = colors.shade600) },
                placeholder = { Text("La tua password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val iconRes = if (passwordVisible) AR.drawable.hide else AR.drawable.eye
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                supportingText = {
                    if (showPasswordError) {
                        Column {
                            Text("La password deve contenere:", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            Text("- almeno 8 caratteri", color = if (passLenOK) colors.shade600 else MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            Text("- almeno 1 numero", color = if (passHasDigit) colors.shade600 else MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            Text("- almeno 1 maiuscola", color = if (passHasUpper) colors.shade600 else MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            Text("- almeno 1 carattere speciale", color = if (passHasSpecial) colors.shade600 else MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                colors = inputColors(error = showPasswordError),
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // TOS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreedToTos,
                    onCheckedChange = { agreedToTos = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.shade600,
                        uncheckedColor = colors.shade600,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    buildAnnotatedString {
                        append("Accetto i ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) { append("Termini di servizio") }
                        append(" e l' ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) { append("Informativa sulla privacy") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.shade600,
                    modifier = Modifier.clickable { showTerms = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CONTINUA
            Button(
                onClick = {
                    if (canContinue) {
                        onSignUpClick(email.trim(), username.trim(), password)
                    }
                },
                enabled = canContinue && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.shade600,
                    disabledContainerColor = colors.shade600.copy(alpha = 0.4f)
                )
            ) {
                Text("Continua", color = colors.background, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link: Accedi
            Row {
                Text(
                    text = "Hai già un account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Accedi",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.shade600,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }


    if (isLoading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x66000000)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showTerms) {
        TermsDialog(onDismiss = { showTerms = false })
    }
}
