package com.example.myapplication.android.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.R
import com.example.myapplication.android.state.LocalCalendarState
import com.example.myapplication.android.ui.components.blocks.ParticipantRow
import com.example.myapplication.android.ui.state.CompanyEmployee
import java.util.*

@Composable
fun EmployeesScreen(
    employees: List<CompanyEmployee>,
    onAddByUserId: (String) -> Unit,
    onOpenDetails: (CompanyEmployee) -> Unit,
    modifier: Modifier = Modifier,
//    @DrawableRes backgroundRes: Int = R.drawable.onboarding_bg
) {
    val calendarState = LocalCalendarState.current
    val selectedDate = calendarState.selectedDate.value
    val weekStart = remember(selectedDate) { startOfWeek(selectedDate) }

    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        Box(Modifier.fillMaxSize()) {
//            Image(
//                painter = painterResource(backgroundRes),
//                contentDescription = null,
//                modifier = Modifier.matchParentSize(),
//                contentScale = ContentScale.Crop
//            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header + add by userId
                var userIdInput by remember { mutableStateOf("") }

                Text(text = "Employees", color = Color(0xFF5D4037))
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = userIdInput,
                        onValueChange = { userIdInput = it },
                        label = { Text("User ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7D4F16),
                            unfocusedBorderColor = Color(0xFF7D4F16),
                            focusedLabelColor = Color(0xFF7D4F16),
                            unfocusedLabelColor = Color(0xFF7D4F16).copy(alpha = 0.7f),
                            cursorColor = Color(0xFF7D4F16)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val id = userIdInput.trim()
                            if (id.isNotEmpty()) {
                                onAddByUserId(id)
                                userIdInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16), contentColor = Color(0xFFFFF8E1))
                    ) { Text("Add") }
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(employees, key = { it.userId }) { emp ->
                        ParticipantRow(
                            name = emp.name.ifBlank { emp.userId },
                            weeklyHours = emp.weeklyHours,
                            userEvents = calendarState.userEvents, // se hai eventi per utente, filtra altrove
                            selectedDate = selectedDate,
                            miniCalendarWeekStart = weekStart,
                            isSelected = false,
                            onClick = { onOpenDetails(emp) },
                            currentTemplatePreview = null
                        )
                    }
                }
            }
        }
    }
}

private fun startOfWeek(cal: Calendar): Calendar {
    val c = cal.clone() as Calendar
    c.firstDayOfWeek = Calendar.MONDAY
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
    return c
}
