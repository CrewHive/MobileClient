// FILE: EmployeesScreen.kt
package com.example.myapplication.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.core.api.utils.TokenManager
import com.example.myapplication.android.ui.core.security.JwtUtils

@Composable
fun EmployeesScreen(
    employees: List<CompanyEmployee>,
    onAddByUserId: (String) -> Unit,
    onOpenDetails: (CompanyEmployee) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    // Palette coerente col resto dell’app
    val brandCream = Color(0xFFFFF8E1)
    val brandBrown = Color(0xFF7D4F16)
    val textBrown  = Color(0xFF5D4037)
    val borderSoft = Color(0xFFEFE3CE)
    val cardTint   = Color(0xFFFDF6E9)

    var search by remember { mutableStateOf("") }
    var userIdInput by remember { mutableStateOf("") }

    // Utente autenticato (da JWT)
    val currentUserId: String? = remember {
        JwtUtils.getUserId(TokenManager.jwtToken ?: "")?.toString()
    }

    // Filtra per query
    val filtered = remember(employees, search) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) employees
        else employees.filter { e ->
            e.name.lowercase().contains(q) || e.userId.lowercase().contains(q)
        }
    }

    // Ordina alfabeticamente (nome → fallback a ID)
    fun sortAlpha(list: List<CompanyEmployee>): List<CompanyEmployee> =
        list.sortedWith(compareBy(
            { (it.name.ifBlank { it.userId }).trim().lowercase() },
            { it.userId }
        ))

    // Se NON stiamo cercando, metti il loggato in cima; altrimenti solo ordine alfabetico
    val finalList = remember(filtered, search, currentUserId) {
        val sorted = sortAlpha(filtered)
        if (search.isBlank() && currentUserId != null) {
            val me = sorted.firstOrNull { it.userId == currentUserId }
            if (me != null) listOf(me) + sorted.filter { it.userId != currentUserId } else sorted
        } else {
            sorted
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header
                Text(
                    text = "Dipendenti",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = textBrown,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(6.dp))

                // Sotto–header con badge conteggio + search
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${employees.size} totali") },
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Face,
                                contentDescription = null,
                                tint = brandBrown
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = brandCream,
                            disabledLabelColor = textBrown
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    // Search
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Cerca nome o ID") },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = brandBrown) },
                        trailingIcon = {
                            AnimatedVisibility(visible = search.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                                IconButton(onClick = { search = "" }) {
                                    Icon(Icons.Filled.Clear, null, tint = brandBrown)
                                }
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandBrown,
                            unfocusedBorderColor = brandBrown.copy(alpha = 0.6f),
                            cursorColor = brandBrown
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Card "Aggiungi per User ID" con lieve tinta per contrasto
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = cardTint),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Aggiungi dipendente",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = textBrown,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = userIdInput,
                                onValueChange = { new ->
                                    // consenti solo cifre
                                    if (new.isEmpty() || new.all { it.isDigit() }) userIdInput = new
                                },
                                label = { Text("User ID") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                leadingIcon = {
                                    Icon(Icons.Filled.Add, null, tint = brandBrown)
                                },
                                trailingIcon = {
                                    AnimatedVisibility(
                                        visible = userIdInput.isNotEmpty() && !isLoading,
                                        enter = fadeIn(), exit = fadeOut()
                                    ) {
                                        IconButton(onClick = { userIdInput = "" }) {
                                            Icon(Icons.Filled.Clear, null, tint = brandBrown)
                                        }
                                    }
                                },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandBrown,
                                    unfocusedBorderColor = brandBrown.copy(alpha = 0.6f),
                                    cursorColor = brandBrown,
                                    focusedLabelColor = brandBrown,
                                    unfocusedLabelColor = brandBrown.copy(alpha = 0.8f)
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            val canSubmit = userIdInput.isNotBlank() && userIdInput.all { it.isDigit() } && !isLoading
                            Button(
                                onClick = {
                                    val id = userIdInput.trim()
                                    if (id.isNotEmpty()) {
                                        onAddByUserId(id)
                                        userIdInput = ""
                                    }
                                },
                                enabled = canSubmit,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = brandBrown,
                                    contentColor = brandCream,
                                    disabledContainerColor = brandBrown.copy(alpha = 0.35f),
                                    disabledContentColor = brandCream.copy(alpha = 0.8f)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = brandCream
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Aggiungi")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Lista dipendenti
                if (finalList.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape),
                                color = brandCream
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Face, null, tint = brandBrown)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (search.isBlank()) "Nessun dipendente" else "Nessun risultato per “$search”",
                                style = MaterialTheme.typography.bodyMedium.copy(color = textBrown)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(finalList, key = { it.userId }) { emp ->
                            val isMe = currentUserId != null && emp.userId == currentUserId
                            EmployeeCard(
                                employee = emp,
                                isCurrentUser = isMe,
                                brandCream = brandCream,
                                brandBrown = brandBrown,
                                textBrown = textBrown,
                                borderSoft = borderSoft,
                                onClick = { onOpenDetails(emp) }
                            )
                        }
                    }
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x33000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = brandBrown)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeCard(
    employee: CompanyEmployee,
    isCurrentUser: Boolean,
    brandCream: Color,
    brandBrown: Color,
    textBrown: Color,
    borderSoft: Color,
    onClick: () -> Unit
) {
    val cardColor = if (isCurrentUser) brandCream.copy(alpha = 0.65f) else Color.White
    val borderColor = if (isCurrentUser) brandBrown else borderSoft

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        //elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initial = (employee.name.ifBlank { employee.userId })
                .trim()
                .firstOrNull()?.uppercaseChar() ?: '·'

            Box(modifier = Modifier.size(48.dp)) {
                Surface(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    color = if (isCurrentUser) brandBrown else brandCream
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = initial.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isCurrentUser) brandCream else brandBrown,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                if (isCurrentUser) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, brandBrown),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    ) {
                        Text(
                            "TU",
                            color = brandBrown,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = employee.name.ifBlank { "Utente ${employee.userId}" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textBrown,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("${employee.weeklyHours}h / sett.") },
                        modifier = Modifier,
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = brandCream,
                            disabledLabelColor = textBrown
                        )
                    )

                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ID #${employee.userId}",
                        style = MaterialTheme.typography.labelMedium.copy(color = textBrown.copy(alpha = 0.8f))
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = brandBrown,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onClick)
            )
        }
    }
}


