package com.example.myapplication.android.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun DrawerContent(
    onClose: () -> Unit,
    onDestinationSelected: (String) -> Unit
) {
    val colors = CustomTheme.colors

    Column(
        modifier = Modifier
            .width(280.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Drawer",
            tint = colors.shade500,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { onClose() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            tint = colors.shade500,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Giulia Verdi",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            "giuliaverdi@gmail.com",
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        DrawerMenuItem(Icons.Default.Home, "Home") {
            onDestinationSelected("Home")
            onClose()
        }
        DrawerMenuItem(Icons.Default.Info, "Notifiche") {
            onDestinationSelected("Notifications")
            onClose()
        }
        DrawerMenuItem(Icons.Default.Person, "Profilo") {
            onDestinationSelected("Profile")
            onClose()
        }
        DrawerMenuItem(Icons.Default.Settings, "Gestionale turni") {
            onDestinationSelected("Calendar")
            onClose()
        }
        DrawerMenuItem(Icons.Default.MailOutline, "Comunicazioni") {
            // Puoi aggiungere un'altra voce se implementi questa schermata
        }

        Spacer(modifier = Modifier.weight(1f))

        DrawerMenuItem(Icons.Default.Settings, "Impostazioni") {
            // opzionale
        }
    }
}


    @Composable
    private fun DrawerMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
        val colors = CustomTheme.colors

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = colors.shade500, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontSize = 16.sp, color = colors.shade950)
        }
    }


