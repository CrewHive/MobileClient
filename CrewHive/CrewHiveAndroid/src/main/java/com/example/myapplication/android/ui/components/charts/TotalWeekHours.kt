package com.example.myapplication.android.ui.components.charts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.android.ui.theme.CustomTheme

object TotalWeekHoursComponent {

    @Composable
    fun TotalWeekHours() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val colors = CustomTheme.colors

            Text(
                text = "Ore settimanali totali:",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = colors.shade950
            )

            Spacer(modifier = Modifier.height(25.dp))

            Box (contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = 27f / 40f,
                    strokeWidth = 20.dp,
                    color = colors.shade500,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(160.dp)
                )

                Text("27/40 h", color = colors.shade950,fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

