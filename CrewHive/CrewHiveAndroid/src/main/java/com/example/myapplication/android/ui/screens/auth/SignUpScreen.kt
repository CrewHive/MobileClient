package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.android.ui.theme.CustomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpClick: (email: String, username: String, password: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CustomTheme.colors

    // Stati locali
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreedToTos by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .fillMaxSize()
        .background(colors.background)) {
        // === IMMAGINE DI SFONDO ===
        val bgPainter: Painter = painterResource(id = com.example.myapplication.android.R.drawable.signup_bg)
        Image(
            painter = bgPainter,
            contentDescription = "Sign Up Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // === OVERLAY BIANCO SEMITRASPARENTE (OPZIONALE) ===
        // Se vuoi rendere più leggibile il form, decommenta:

//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.White.copy(alpha = 0.3f))
//        )


        // === CONTENUTO CENTRALE ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // (Facoltativo) Logo in cima
            // Image(painter = painterResource(id = R.drawable.ic_logo), contentDescription = "Logo", modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Row (modifier = Modifier.fillMaxWidth().align(Alignment.Start)) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.shade900
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // === CAMPO EMAIL ===
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email", color = colors.shade600) },
                placeholder = { Text(text = "Your email address") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = colors.shade600,
                    focusedBorderColor = colors.shade600,
                    unfocusedBorderColor = colors.shade600,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // === CAMPO USERNAME ===
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(text = "Username", color = colors.shade600) },
                placeholder = { Text(text = "Choose a username") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = colors.shade600,
                        focusedBorderColor = colors.shade600,
                        unfocusedBorderColor = colors.shade600,
                    ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // === CAMPO PASSWORD ===
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password", color = colors.shade600) },
                placeholder = { Text(text = "Your password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None
                else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    val img = if (passwordVisible) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = img, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = colors.shade600,
                        focusedBorderColor = colors.shade600,
                        unfocusedBorderColor = colors.shade600,
                    ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // === CHECKBOX "I agree to the Terms of Services and Privacy Policy." ===
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
                        checkmarkColor = colors.shade600,
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Costruiamo il testo con parti in rosso cliccabili
                Text(
                    buildAnnotatedString {
                        append("I agree to the ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                            append("Terms of Services")
                        }
                        append(" and ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                            append("Privacy Policy")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.shade600,
                    modifier = Modifier.clickable {
                        // Qui puoi aprire una finestra di dialogo o WebView per TOS/Privacy
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // === PULSANTE CONTINUE ===
            Button(
                onClick = {
                    if (agreedToTos) {
                        onSignUpClick(email.trim(), username.trim(), password)
                    } else {
                        // Puoi mostrare un Toast o Snackbar: “Devi accettare i Terms…”
                    }
                },
                enabled = agreedToTos, // disabilita il button se non ha spuntato la checkbox
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.shade600,
                    disabledContainerColor = colors.shade600
                )
            ) {
                Text(text = "Continue", color = colors.background, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === Riga "Have an Account? Sign In" ===
            Row {
                Text(
                    text = "Have an Account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.shade600,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }
}
