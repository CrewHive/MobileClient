package com.example.myapplication.android.ui.components

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

object DrawerContentComponent {
    @Composable
    fun DrawerContent(onClose: () -> Unit) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Drawer",
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable { onClose() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Johanna Doe", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("johanna@company.com", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(24.dp))

            DrawerMenuItem(Icons.Default.Home, "Home") { /* TODO */ }
            DrawerMenuItem(Icons.Default.Info, "Reports") { /* TODO */ }
            DrawerMenuItem(Icons.Default.Person, "Employees") { /* TODO */ }
            DrawerMenuItem(Icons.Default.Settings, "Shift manager") { /* TODO */ }
            DrawerMenuItem(Icons.Default.MailOutline, "Communication") { /* TODO */ }

            Spacer(modifier = Modifier.weight(1f))

            DrawerMenuItem(Icons.Default.Settings, "Settings") { /* TODO */ }
        }
    }

    @Composable
    private fun DrawerMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontSize = 16.sp, color = Color(0xFF5D4037))
        }
    }
}

