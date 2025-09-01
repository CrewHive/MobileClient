package com.example.myapplication.android.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.theme.CustomTheme

object BottomNavigationBarComponent {

    @Composable
    fun BottomNavigationBar(
        currentScreen: String,
        highlightCalendar: Boolean,              // ⬅️ NEW
        onMenuClick: () -> Unit,
        onTabSelected: (String) -> Unit
    ) {
        val colors = CustomTheme.colors

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = colors.shade100,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = (1.dp).toPx(),
                    )
                }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarIcon(
                icon = painterResource(id = R.drawable.menu),
                selected = false,
                onClick = onMenuClick
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (currentScreen == "Notifications") R.drawable.invoice_piena else R.drawable.invoice_vuota
                ),
                selected = currentScreen == "Notifications",
                onClick = { onTabSelected("Notifications") }
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (currentScreen == "Home") R.drawable.home_piena else R.drawable.home_vuota
                ),
                selected = currentScreen == "Home",
                onClick = { onTabSelected("Home") }
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (highlightCalendar) R.drawable.calendar_piena else R.drawable.calendar_vuota
                ),
                selected = highlightCalendar,        // ⬅️ usa il flag
                onClick = { onTabSelected("Calendar") }
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (currentScreen == "Profile") R.drawable.account_piena else R.drawable.account_vuota
                ),
                selected = currentScreen == "Profile",
                onClick = { onTabSelected("Profile") }
            )
        }
    }

    @Composable
    private fun BottomBarIcon(
        icon: Painter,
        selected: Boolean,
        onClick: () -> Unit,
        size: Dp = 32.dp
    ) {
        val colors = CustomTheme.colors
        val selectedIconTint = Color(0xFF7D4F16)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onClick() }
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (selected) selectedIconTint else colors.shade500,
                modifier = Modifier.size(size)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(selectedIconTint, CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

}
