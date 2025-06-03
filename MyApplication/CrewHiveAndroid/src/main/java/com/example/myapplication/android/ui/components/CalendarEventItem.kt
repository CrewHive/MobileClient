package com.example.myapplication.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex

@Composable
fun CalendarEventItem(
    event: CalendarEvent,
    modifier: Modifier = Modifier,
    showParticipants: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    onEdit: ((CalendarEvent) -> Unit)? = null

) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.clickable { showDialog = true }) {
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .border(6.dp, Color(0xFFFAF7C7), RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(20.dp, 8.dp, 6.dp, 6.dp)
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = event.title,
                        fontSize = 16.sp,
                        color = Color(0xFF5D4037)
                    )
                    if (event.participants.isEmpty()){
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Personal event",
                            tint = Color(0xFFF0D954),
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${event.startTime} - ${event.endTime}",
                    fontSize = 12.sp,
                    color = Color(0xFF7D4F16).copy(alpha = 0.84f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                if (showParticipants && event.participants.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.participants.joinToString(", "),
                        fontSize = 12.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }

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
                    Text(event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Orario: ${event.startTime} – ${event.endTime}", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Descrizione: ${event.description}", fontSize = 14.sp, color = Color.DarkGray)

                    if (showParticipants && event.participants.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Partecipanti:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(event.participants.joinToString(", "), fontSize = 13.sp, color = Color(0xFF5D4037))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (showParticipants || event.participants.isEmpty()) {
                            Button(
                                onClick = {
                                    onDelete?.invoke()
                                    showDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Elimina", color = Color.White)
                            }
                        }
                        if (showParticipants || event.participants.isEmpty()) {
                            Button(
                                onClick = {
                                    onEdit?.invoke(event)
                                    showDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                            ) {
                                Text("Modifica", color = Color.White)
                            }
                        }

                        if (!showParticipants && event.participants.isNotEmpty()) {
                            Button(
                                onClick = {
                                    onReport?.invoke()
                                    showDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("Report", color = Color.White)
                            }
                        }

//                        OutlinedButton(onClick = { showDialog = false }) {
//                            Text("Chiudi")
//                        }
                    }
                }
            }
        }
    }
}
