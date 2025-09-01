package com.example.myapplication.android.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun CalendarEventItem(
    event: CalendarEvent,
    modifier: Modifier = Modifier,
    showParticipants: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    onEdit: ((CalendarEvent) -> Unit)? = null
) {
    val colors = CustomTheme.colors
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.clickable { showDialog = true }) {
        // Barra laterale colorata
        Box(
            modifier = Modifier
                .width(12.dp)
                .fillMaxHeight()
                .background(
                    color = event.color,
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                )
                .align(Alignment.CenterStart)
                .zIndex(1f)
        )

        // Card
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .border(6.dp, colors.shade100, RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(20.dp, 8.dp, 6.dp, 6.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(text = event.title, fontSize = 16.sp, color = colors.shade950)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${event.startTime} - ${event.endTime}",
                    fontSize = 12.sp,
                    color = colors.shade800.copy(alpha = 0.84f)
                )
                event.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(text = it, fontSize = 14.sp, color = Color.DarkGray)
                }
                if (showParticipants && event.participants.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = event.participants.joinToString(", "),
                        fontSize = 12.sp,
                        color = colors.shade950,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Faccina OVERLAY in alto a destra SOLO per EVENT
        if (event.kind == CalendarItemKind.EVENT) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Personal event",
                tint = colors.shade300,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .zIndex(2f)
            )
        }
    }

    // Popup custom coerente con il resto dell’app
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(event.title, fontSize = 20.sp, color = colors.shade950)
                    Spacer(Modifier.height(8.dp))
                    Text("Orario: ${event.startTime} – ${event.endTime}", fontSize = 14.sp, color = Color.DarkGray)
                    event.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text("Descrizione: $it", fontSize = 14.sp, color = Color.DarkGray)
                    }

                    if (showParticipants && event.participants.isNotEmpty() && event.kind == CalendarItemKind.SHIFT) {
                        Spacer(Modifier.height(8.dp))
                        Text("Partecipanti:", fontSize = 14.sp)
                        Text(event.participants.joinToString(", "), fontSize = 13.sp, color = colors.shade950)
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // In lista “utente”: elimina/modifica per EVENT; in lista con partecipanti: anche per SHIFT
                        if (showParticipants || (event.kind == CalendarItemKind.EVENT)) {
                            Button(
                                onClick = { onDelete?.invoke(); showDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) { Text("Elimina", color = Color.White) }
                        }
                        if (showParticipants || (event.kind == CalendarItemKind.EVENT)) {
                            Button(
                                onClick = { onEdit?.invoke(event); showDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                            ) { Text("Modifica", color = Color.White) }
                        }
//                        if (!showParticipants && event.kind == CalendarItemKind.SHIFT) {
//                            Button(
//                                onClick = { onReport?.invoke(); showDialog = false },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
//                            ) { Text("Report", color = Color.White) }
//                        }
                    }
                }
            }
        }
    }
}
