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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.android.ui.screens.auth.LogInViewModel
import com.example.myapplication.android.ui.theme.CustomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInClick: (email: String, password: String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CustomTheme.colors
    // Stati locali della schermata
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


    // Manteniamo un Box con immagine di sfondo e contenuto sovrapposto
    Box(modifier = modifier
        .fillMaxSize()
        .background(colors.background)) {
        // === IMMAGINE DI SFONDO (SI SCALA E COPRE TUTTO) ===
        val bgPainter: Painter = painterResource(id = com.example.myapplication.android.R.drawable.signin_bg)
        Image(
            painter = bgPainter,
            contentDescription = "Login Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // === OVERLAY BIANCO SEMITRASPARENTE (OPZIONALE) ===
        // Se vuoi un leggero overlay bianco per far risaltare il form, decommenta:
        /*
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.7f))
        )
        */

        // === CONTENUTO CENTRALE (Form) ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // (Facoltativo) Se hai un logo da posizionare in cima, inseriscilo qui
            // Image(painter = painterResource(id = R.drawable.ic_logo), contentDescription = "Logo", modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().align(Alignment.Start)) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.shade900
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // === CAMPO EMAIL/USERNAME ===
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Username", color = colors.shade600) },
                placeholder = { Text(text = "example@email.com") },
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

            // === CAMPO PASSWORD CON VISIBILITY TOGGLE ===
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password", color = colors.shade600) },
                placeholder = { Text(text = "••••••••••") },
                singleLine = true,
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None
                else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    val img = if (passwordVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
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
            Spacer(modifier = Modifier.height(24.dp))

            // === PULSANTE SIGN IN ===
            Button(
                onClick = { onSignInClick(email.trim(), password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.shade600)
            ) {
                Text(text = "Sign in", color = colors.background, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === Riga "Forgot Password?   Sign Up" ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.clickable {
                        // Qui potresti navigare alla schermata di reset password
                    }
                )

                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.shade600,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}
