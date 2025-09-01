package com.example.myapplication.android.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme
import java.util.Calendar

/**
 * Grid view for weekly events: 7 columns (Mon-Sun), rows per hour, events positioned by time.
 * Draws both horizontal hour lines and vertical day separators, and renders colored event strips.
 */
@Composable
fun WeeklyEventGrid(
    weekStart: Calendar,
    events: List<CalendarEvent>,
    showParticipants: Boolean = false,
    onDelete: ((CalendarEvent) -> Unit)? = null,
    onReport: ((CalendarEvent) -> Unit)? = null,
    onEdit: ((CalendarEvent) -> Unit)? = null,
    hourRange: IntRange = 0..24,
    hourHeight: Dp = 40.dp,
    timeColumnWidth: Dp = 30.dp,
    modifier: Modifier = Modifier
) {
    val colors = CustomTheme.colors
    val scrollState = rememberScrollState()
    val totalHeight = hourHeight * hourRange.count()
    val density = LocalDensity.current

    // Misura runtime la larghezza disponibile per la griglia (tutto tranne la colonna oraria)
    var gridWidthDp by remember { mutableStateOf(0.dp) }
    val dayWidth: Dp = if (gridWidthDp > 0.dp) gridWidthDp / 7 else 0.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .height(totalHeight)
    ) {
        // Colonna orari fissa
        Column(
            modifier = Modifier
                .width(timeColumnWidth)
                .fillMaxHeight()
        ) {
            hourRange.forEach { hour ->
                Box(
                    modifier = Modifier
                        .height(hourHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "%2d".format(hour),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Griglia 7 giorni: occupa tutto lo spazio rimanente e si fa misurare
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .onGloballyPositioned { layoutCoordinates ->
                    gridWidthDp = with(density) { layoutCoordinates.size.width.toDp() }
                }
        ) {
            // Linee orizzontali ore
            hourRange.forEach { hour ->
                Box(
                    modifier = Modifier
                        .offset(y = hourHeight * (hour - hourRange.first))
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
            }

            // Separatori verticali giorni
            for (i in 1 until 7) {
                Box(
                    modifier = Modifier
                        .offset(x = dayWidth * i)
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(colors.shade950.copy(alpha = 0.3f))
                        .padding(horizontal = 1.dp)
                )
            }

            // Eventi
            for (dayIndex in 0 until 7) {
                val eventsForDay = events.filter {
                    val diffDays = ((normalizeDate(it.date).timeInMillis - normalizeDate(weekStart).timeInMillis) /
                            (1000L * 60 * 60 * 24)).toInt()
                    diffDays == dayIndex
                }

                val positionedForDay = remember(eventsForDay) {
                    resolveEventPositionsWithGrouping(eventsForDay)
                }

                positionedForDay.forEach { pos ->
                    val (startMin, durationMin) = calculateDuration(pos.event)
                    val top = ((startMin - hourRange.first * 60) / 60f) * hourHeight.value
                    val height = (durationMin / 60f) * hourHeight.value

                    val eventWidth = if (pos.totalColumns > 0) dayWidth / pos.totalColumns else 0.dp
                    val left = dayWidth * dayIndex + eventWidth * pos.columnIndex

                    val showDialogState = remember { mutableStateOf(false) }
                    val showDialog = showDialogState.value

                    if (showDialog) {
                        Dialog(onDismissRequest = { showDialogState.value = false }) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        pos.event.title,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.shade950
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Orario: ${pos.event.startTime} – ${pos.event.endTime}",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Descrizione: ${pos.event.description}",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )

                                    if (showParticipants && pos.event.participants.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Partecipanti:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text(
                                            pos.event.participants.joinToString(", "),
                                            fontSize = 13.sp,
                                            color = colors.shade950
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (showParticipants || pos.event.kind == CalendarItemKind.EVENT) {
                                            Button(
                                                onClick = {
                                                    onDelete?.invoke(pos.event)
                                                    showDialogState.value = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                            ) { Text("Elimina", color = Color.White) }
                                        }
                                        if (showParticipants || pos.event.kind == CalendarItemKind.EVENT) {
                                            Button(
                                                onClick = {
                                                    onEdit?.invoke(pos.event)
                                                    showDialogState.value = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                                            ) { Text("Modifica", color = Color.White) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = left, y = top.dp)
                            .width(eventWidth)
                            .height(height.dp.coerceAtLeast(24.dp))
                            .padding(start = 2.dp, top = 1.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(pos.event.color.copy(alpha = 1f))
                            .clickable { showDialogState.value = true }
                    )
                }
            }
        }
    }
}




fun normalizeDate(calendar: Calendar): Calendar {
    return (calendar.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
