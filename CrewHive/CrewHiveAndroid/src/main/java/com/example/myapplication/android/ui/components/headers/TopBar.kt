package com.example.myapplication.android.ui.components.headers

import com.example.myapplication.android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.android.ui.theme.CustomTheme

object TopBarComponent {
    @Composable
    fun TopBar() {
        val colors = CustomTheme.colors

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.shade100),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "CrewHive",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.shade950,
            )
            Icon(
                modifier = Modifier
                    .padding(16.dp)
                    .size(32.dp),
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                tint = colors.shade500,
            )
        }
    }
}

