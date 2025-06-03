package com.example.myapplication.android.ui.screens

import com.example.myapplication.android.ui.components.charts.CircularStatDial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.components.headers.TopBarComponent
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val colors = CustomTheme.colors

        TopBarComponent.TopBar()

        Spacer(modifier = Modifier.height(24.dp))

        // FOTO PROFILO + MATITA
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.logo), // metti la tua immagine profilo
                    contentDescription = "Foto profilo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(colors.background)
                )

                IconButton(
                    onClick = { /* modifica immagine profilo */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 12.dp)
                        .size(36.dp)
                        .background(colors.shade950, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Giulia Verdi",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colors.shade950
            )
            Text(
                text = "giuliaverdi@gmail.com",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = colors.shade100, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ore lavorate",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 18.sp,
            color = colors.shade950,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircularStatDial(current = 27, max = 40, label = "Settimanali", color = colors.shade400)
            CircularStatDial(current = 67, max = 160, label = "Mensili", color = colors.shade600)
            CircularStatDial(current = 7, label = "Straordinari", color = colors.shade900, showMax = false)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = colors.shade100, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabeledValue(label = "Ferie accumulate", value = "11")
            LabeledValue(label = "Ferie usate", value = "7")
            LabeledValue(label = "Permessi", value = "3")
        }
    }
}

@Composable
fun LabeledValue(label: String, value: String) {
    val colors = CustomTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.shade950)
        Text(text = value, color = colors.shade500, fontWeight = FontWeight.Bold)
    }
}