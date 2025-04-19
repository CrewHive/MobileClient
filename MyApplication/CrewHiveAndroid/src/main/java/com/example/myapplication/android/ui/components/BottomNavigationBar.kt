package com.example.myapplication.android.ui.components

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
import com.example.myapplication.android.R

object BottomNavigationBarComponent {

    @Composable
    fun BottomNavigationBar(
        currentScreen: String,
        onMenuClick: () -> Unit,
        onTabSelected: (String) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = Color(0XFFFAF7C7),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = (1.dp).toPx(),
                    )
                }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarIcon(
                icon = painterResource(id = R.drawable.menu),
                onClick = onMenuClick
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (currentScreen == "Notifications") R.drawable.invoice_piena else R.drawable.invoice_vuota
                ),
                onClick = { onTabSelected("Notifications") }
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (currentScreen == "Home") R.drawable.home_piena else R.drawable.home_vuota
                ),
                onClick = { onTabSelected("Home") }
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (currentScreen == "Calendar") R.drawable.calendar_piena else R.drawable.calendar_vuota
                ),
                onClick = { onTabSelected("Calendar") }
            )
            BottomBarIcon(
                icon = painterResource(
                    id = if (currentScreen == "Profile") R.drawable.account_piena else R.drawable.account_vuota
                ),
                onClick = { onTabSelected("Profile") }
            )
        }
    }

    @Composable
    private fun BottomBarIcon(
        icon: Painter,
        onClick: () -> Unit,
        size: Dp = 34.dp
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier
                .size(size)
                .clickable { onClick() }
        )
    }
}
