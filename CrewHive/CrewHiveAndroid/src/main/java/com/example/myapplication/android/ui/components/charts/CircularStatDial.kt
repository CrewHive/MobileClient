package com.example.myapplication.android.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.android.ui.theme.CustomTheme


@Composable
fun CircularStatDial(
    current: Int,
    max: Int = 100,
    label: String,
    color: Color,
    size: Dp = 72.dp,
    showMax: Boolean = true   // puoi anche rimuoverlo se non lo usi più
) {
    val colors = CustomTheme.colors

    val clamped = current.coerceAtLeast(0)
    val progress = if (max > 0) {
        (clamped.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    } else 0f   // ← niente “mezzo cerchio” quando max è 0

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = size.toPx() * 0.12f
                drawArc(
                    color = Color.LightGray, startAngle = -90f, sweepAngle = 360f,
                    useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color, startAngle = -90f, sweepAngle = 360f * progress,
                    useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // sempre lavorate/totali, anche quando il totale è 0 → "0/0"
            val centerText = if (showMax) "${clamped}/${max.coerceAtLeast(0)}" else "$clamped"
            Text(
                text = centerText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.shade950
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 13.sp, color = colors.shade950, fontWeight = FontWeight.Medium)
    }
}

