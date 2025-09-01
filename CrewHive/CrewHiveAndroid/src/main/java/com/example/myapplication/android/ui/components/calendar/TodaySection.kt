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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.myapplication.android.state.LocalCalendarState
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.security.JwtUtils.isManager
import com.example.myapplication.android.ui.theme.CustomTheme

object TodaySectionComponent {

    @Composable
    fun TodaySection(
        showParticipants: Boolean = false,
        onDelete: ((CalendarEvent) -> Unit)? = null,
        onReport: ((CalendarEvent) -> Unit)? = null,
        onEdit: ((CalendarEvent) -> Unit)? = null
    ) {
        val colors = CustomTheme.colors
        val calendarState = LocalCalendarState.current
        val todayEvents = calendarState.userEvents

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Oggi", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.shade950)
            Spacer(Modifier.height(18.dp))

            if (todayEvents.isEmpty()) {
                Text("Nessun evento oggi", color = colors.shade800)
            } else {
                todayEvents.sortedBy { it.startTime }.forEach { ev ->
                    CalendarEventItem1(
                        event = ev,
                        modifier = Modifier
                            .height(120.dp)
                            .padding(bottom = 8.dp),
                        showParticipants = showParticipants,
                        onDelete = onDelete?.let { cb -> { cb(ev) } },
                        onReport = onReport?.let { cb -> { cb(ev) } },
                        onEdit = onEdit?.let { cb -> { cb(ev) } }
                    )
                }
            }
        }
    }

    @Composable
    fun CalendarEventItem1(
        event: CalendarEvent,
        modifier: Modifier = Modifier,
        showParticipants: Boolean = false,
        onDelete: (() -> Unit)? = null,
        onReport: (() -> Unit)? = null,
        onEdit: (() -> Unit)? = null
    ) {
        val colors = CustomTheme.colors
        var showDialog by remember { mutableStateOf(false) }
        val token = TokenManager.jwtToken.orEmpty()

        Box(modifier = modifier.clickable { showDialog = true }) {
            // Barra colorata
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(color = event.color, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
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
                Column(Modifier.align(Alignment.CenterVertically)) {
                    Text(event.title, fontSize = 16.sp, color = colors.shade950)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${event.startTime} - ${event.endTime}",
                        fontSize = 12.sp,
                        color = colors.shade800.copy(alpha = 0.84f),
                        fontWeight = FontWeight.Bold
                    )
                    event.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(it, fontSize = 14.sp, color = Color.DarkGray)
                    }
                    if (showParticipants && event.participants.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            event.participants.joinToString(", "),
                            fontSize = 12.sp,
                            color = colors.shade950,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Faccina OVERLAY SOLO per EVENT
            if (event.kind == CalendarItemKind.EVENT) {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = "Personal event",
                    tint = colors.shade300,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .zIndex(2f)
                )
            }
        }

        // Popup custom
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
                        Text(event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.shade950)
                        Spacer(Modifier.height(8.dp))
                        Text("Orario: ${event.startTime} – ${event.endTime}", fontSize = 14.sp, color = Color.DarkGray)
                        Spacer(Modifier.height(4.dp))
                        Text("Descrizione: ${event.description.orEmpty()}", fontSize = 14.sp, color = Color.DarkGray)

                        if (showParticipants && event.participants.isNotEmpty() && event.kind == CalendarItemKind.SHIFT) {
                            Spacer(Modifier.height(8.dp))
                            Text("Partecipanti:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(event.participants.joinToString(", "), fontSize = 13.sp, color = colors.shade950)
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (event.kind == CalendarItemKind.EVENT) {
                                Button(
                                    onClick = { onDelete?.invoke(); showDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) { Text("Elimina", color = Color.White) }
                            }
                            if (event.kind == CalendarItemKind.EVENT) {
                                Button(
                                    onClick = { onEdit?.invoke(); showDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                                ) { Text("Modifica", color = Color.White) }
                            }
                        }
                    }
                }
            }
        }
    }
}
