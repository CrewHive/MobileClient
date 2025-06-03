package com.example.myapplication.android.ui.components.headers


import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.android.ui.components.calendar.sameDayAs
import com.example.myapplication.android.ui.theme.CustomTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeeklyHeader(
    weekStart: Calendar,
    selectedDate: Calendar,
    onDayClick: (Calendar) -> Unit,
    onSwipeWeek: (Int) -> Unit
) {
    val colors = CustomTheme.colors

    val sdfDayName = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val threshold = 50f
    var cumulativeDrag by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        cumulativeDrag += dragAmount
                    },
                    onDragEnd = {
                        when {
                            cumulativeDrag < -threshold -> onSwipeWeek(1)
                            cumulativeDrag > threshold -> onSwipeWeek(-1)
                        }
                        cumulativeDrag = 0f
                    }
                )
            }
    ) {
        // compute widths
        val totalWidthDp = with(density) { constraints.maxWidth.toDp() }
        val timeColWidth = 30.dp
        val gridWidth = totalWidthDp - timeColWidth
        val colWidth = gridWidth / 7f

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // spacer for time column alignment
            Spacer(modifier = Modifier.width(timeColWidth))
            // day columns
            for (i in 0 until 7) {
                val dayCal = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }
                val isSelected = dayCal.sameDayAs(selectedDate)
                Column(
                    modifier = Modifier
                        .width(colWidth)
                        .clickable { onDayClick(dayCal) }
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                        //.background(if (isSelected) Color(0xFFF0D954) else Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = sdfDayName.format(dayCal.time),
                        fontSize = 12.sp,
                        color = colors.shade950,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                        fontSize = 22.sp,
                        color = colors.shade950,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
