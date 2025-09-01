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
    fun TotalWeekHours(current: Int, max: Int) {
        val colors = CustomTheme.colors
        val clamped = current.coerceAtLeast(0)
        val progress = if (max > 0) {
            (clamped.toFloat() / max.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Ore settimanali totali:", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.shade950)
            Spacer(Modifier.height(25.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progress,
                    strokeWidth = 20.dp,
                    color = colors.shade500,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(160.dp)
                )
                // sempre "lavorate/totali h" → quando max=0 diventa "0/0 h"
                Text("${clamped}/${max.coerceAtLeast(0)} h",
                    color = colors.shade950, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}