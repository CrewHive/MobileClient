// com/example/myapplication/android/ui/components/navigation/DrawerContent.kt
package com.example.myapplication.android.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun DrawerContent(
    userName: String? = null,
    userEmail: String? = null,
    isManager: Boolean,
    currentRoute: String? = null,
    highlightCalendar: Boolean = false,
    onCalendarOpenFromDrawer: () -> Unit = {},
    onClose: () -> Unit,
    onDestinationSelected: (String) -> Unit,
    onLogoutClicked: () -> Unit
) {
    val colors = CustomTheme.colors

    Column(
        modifier = Modifier
            .width(280.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close Drawer",
            tint = colors.shade500,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { onClose() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            tint = colors.shade500,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = userName?.takeIf { it.isNotBlank() } ?: "—",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = userEmail?.takeIf { it.isNotBlank() } ?: "—",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        DrawerMenuItem(
            icon = Icons.Filled.Home,
            label = "Home",
            selected = currentRoute == "Home"
        ) {
            onDestinationSelected("Home"); onClose()
        }

        DrawerMenuItem(
            icon = Icons.Filled.Info,
            label = "Notifiche",
            selected = currentRoute == "Notifications"
        ) {
            onDestinationSelected("Notifications"); onClose()
        }

        DrawerMenuItem(
            icon = Icons.Filled.Person,
            label = "Profilo",
            selected = currentRoute == "Profile"
        ) {
            onDestinationSelected("Profile"); onClose()
        }

        if (isManager) {
            // Calendar si evidenzia solo se: sei su Calendar **e** highlightCalendar==true
            DrawerMenuItem(
                icon = Icons.Filled.Settings,
                label = "Gestionale turni",
                selected = (currentRoute == "Calendar" && highlightCalendar)
            ) {
                onCalendarOpenFromDrawer()       // segna che è stato aperto dal drawer
                onDestinationSelected("Calendar")
                onClose()
            }

            DrawerMenuItem(
                icon = Icons.Filled.Face,
                label = "Dipendenti",
                selected = currentRoute == "Employees"
            ) {
                onDestinationSelected("Employees"); onClose()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DrawerMenuItem(
            icon = Icons.Filled.Settings,
            label = "Log Out",
            selected = false
        ) {
            onLogoutClicked()
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = CustomTheme.colors
    val selectedIconTint = Color(0xFF7D4F16)
    val iconTint = if (selected) selectedIconTint else colors.shade500
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    val bg = if (selected) colors.shade500.copy(alpha = 0.14f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = colors.shade950,
            fontWeight = fontWeight
        )
    }
}
