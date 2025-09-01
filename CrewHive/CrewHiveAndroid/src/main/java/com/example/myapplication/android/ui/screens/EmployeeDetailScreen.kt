// com/example/myapplication/android/ui/screens/EmployeeDetailScreen.kt
package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.components.pickers.EmployeeContractTypePickerDialog
import com.example.myapplication.android.ui.state.CompanyEmployee
import com.example.myapplication.android.ui.state.EmployeeContractType
import com.example.myapplication.android.ui.theme.CustomTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun EmployeeDetailScreen(
    employee: CompanyEmployee,
    initialOvertimeMinutes: Int? = null,
    onBack: () -> Unit,
    onSave: (CompanyEmployee, Double) -> Unit,
    onRemove: (CompanyEmployee) -> Unit,
    isLoading: Boolean = false,
    showRemoveButton: Boolean = true
) {
    val colors = CustomTheme.colors
    val brand = colors.shade700
    val brandDark = colors.shade900
    val borderColor = colors.shade100
    val rounded = RoundedCornerShape(14.dp)

    var showTypeDialog by remember { mutableStateOf(false) }
    var contractUi by rememberSaveable(employee.userId) { mutableStateOf(employee.contractType.toUi()) }

    var weeklyHours by rememberSaveable(employee.userId) {
        mutableStateOf(employee.weeklyHours.coerceIn(0, 80))
    }

    var overtimeMinutes by rememberSaveable(employee.userId) {
        mutableStateOf(initialOvertimeMinutes ?: (employee.overtimeHours.coerceAtLeast(0) * 60))
    }
    fun overtimeHoursDecimal(): Double = overtimeMinutes / 60.0

    var vacAcc by rememberSaveable(employee.userId) { mutableStateOf(employee.vacationDaysAccumulated.coerceAtLeast(0f)) }
    var vacUsed by rememberSaveable(employee.userId) { mutableStateOf(employee.vacationDaysUsed.coerceAtLeast(0f)) }
    var leaveAcc by rememberSaveable(employee.userId) { mutableStateOf(employee.leaveDaysAccumulated.coerceAtLeast(0f)) }
    var leaveUsed by rememberSaveable(employee.userId) { mutableStateOf(employee.leaveDaysUsed.coerceAtLeast(0f)) }

    LaunchedEffect(
        employee.userId,
        employee.contractType,
        employee.weeklyHours,
        employee.vacationDaysAccumulated,
        employee.vacationDaysUsed,
        employee.leaveDaysAccumulated,
        employee.leaveDaysUsed,
        initialOvertimeMinutes
    ) {
        contractUi = employee.contractType.toUi()
        weeklyHours = employee.weeklyHours.coerceIn(0, 80)
        overtimeMinutes = (initialOvertimeMinutes ?: (employee.overtimeHours.coerceAtLeast(0) * 60))
        vacAcc = employee.vacationDaysAccumulated.coerceAtLeast(0f)
        vacUsed = employee.vacationDaysUsed.coerceAtLeast(0f)
        leaveAcc = employee.leaveDaysAccumulated.coerceAtLeast(0f)
        leaveUsed = employee.leaveDaysUsed.coerceAtLeast(0f)
    }

    val isFullTime = contractUi?.name == "FULL_TIME"
    val maxWeekly = if (isFullTime) 40 else 39
    LaunchedEffect(isFullTime) {
        weeklyHours = if (isFullTime) 40 else weeklyHours.coerceAtMost(39)
    }

    val cream = Color(0xFFFFF8E1)
    val darkText = Color(0xFF5D4037)

    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Box(Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Top bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack, enabled = !isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = brandDark)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Dettagli dipendente",
                        color = brandDark,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                // Header contrastato
                Spacer(Modifier.height(10.dp))
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = cream),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(brand),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = employee.name.firstOrNull()?.uppercase() ?: "U",
                                color = cream,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = employee.name.ifBlank { "User ${employee.userId}" },
                                color = darkText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "ID utente: ${employee.userId}",
                                color = brandDark.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, brand),
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = "${weeklyHours}h/set",
                                color = brandDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // CARD: contratto
                TitledCard(title = "Tipo di contratto", brand = brand, borderColor = borderColor, rounded = rounded) {
                    Box(
                        modifier = Modifier
                            .clip(rounded)
                            .background(Color.White)
                            .border(BorderStroke(1.dp, brand), rounded)
                            .clickable(enabled = !isLoading) { showTypeDialog = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = contractUi?.displayName ?: "Seleziona…",
                            color = brandDark,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (isFullTime) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Full time: ore settimanali fissate a 40.",
                            color = brandDark.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // CARD: lavoro
                TitledCard(title = "Lavoro", brand = brand, borderColor = borderColor, rounded = rounded) {
                    SectionLabel("Ore settimanali", brandDark)
                    StepperNumber(
                        value = weeklyHours.toFloat(),
                        onChange = { v ->
                            val clamped = v.toInt().coerceIn(0, maxWeekly)
                            weeklyHours = if (isFullTime) 40 else clamped
                        },
                        step = 1f,
                        decimals = 0,
                        enabled = !isLoading && !isFullTime,
                        brand = brand,
                        rounded = rounded
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = if (isFullTime) (weeklyHours / 40f).coerceIn(0f, 1f) else (weeklyHours / 39f).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = brand,
                        trackColor = borderColor
                    )

                    Spacer(Modifier.height(16.dp))
                    SectionLabel("Straordinari (HH:MM, step 5')", brandDark)
                    StepperTime(
                        minutes = overtimeMinutes,
                        onChangeMinutes = { m -> overtimeMinutes = max(0, m) },
                        stepMinutes = 5,
                        enabled = !isLoading,
                        brand = brand,
                        rounded = rounded
                    )
                }

                Spacer(Modifier.height(14.dp))

                // CARD: time off
                TitledCard(title = "Tempo libero", brand = brand, borderColor = borderColor, rounded = rounded) {
                    SectionLabel("Ferie", brandDark)
                    StepperNumber("Maturate", vacAcc, { v -> vacAcc = max(0f, roundTo(v, 1)) }, 0.1f, 1, !isLoading, brand, rounded)
                    Spacer(Modifier.height(10.dp))
                    StepperNumber("Utilizzate", vacUsed, { v -> vacUsed = max(0f, roundTo(v, 1)) }, 0.1f, 1, !isLoading, brand, rounded)

                    Spacer(Modifier.height(16.dp))
                    SectionLabel("Congedi personali", brandDark)
                    StepperNumber("Maturati", leaveAcc, { v -> leaveAcc = max(0f, roundTo(v, 1)) }, 0.1f, 1, !isLoading, brand, rounded)
                    Spacer(Modifier.height(10.dp))
                    StepperNumber("Utilizzati", leaveUsed, { v -> leaveUsed = max(0f, roundTo(v, 1)) }, 0.1f, 1, !isLoading, brand, rounded)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = rounded,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = brand),
                        border = BorderStroke(1.5.dp, brand)
                    ) { Text("Indietro") }

                    Button(
                        onClick = {
                            val updated = employee.copy(
                                contractType = contractUi.toDomain(),
                                weeklyHours = weeklyHours,
                                overtimeHours = (overtimeHoursDecimal()).toInt(),
                                vacationDaysAccumulated = vacAcc,
                                vacationDaysUsed = vacUsed,
                                leaveDaysAccumulated = leaveAcc,
                                leaveDaysUsed = leaveUsed
                            )
                            onSave(updated, overtimeHoursDecimal())
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = rounded,
                        colors = ButtonDefaults.buttonColors(containerColor = brand, contentColor = Color(0xFFFFF8E1))
                    ) { Text(if (isLoading) "Salvataggio…" else "Salva") }
                }

                if (showRemoveButton) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onRemove(employee) },
                        enabled = !isLoading,
                        shape = rounded,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373), contentColor = Color.White)
                    ) { Text("Elimina dipendente") }
                }

                Spacer(Modifier.height(12.dp))
            }

            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFFFFF8E1)) }
            }
        }
    }

    if (showTypeDialog) {
        EmployeeContractTypePickerDialog(
            current = contractUi,
            onDismiss = { showTypeDialog = false },
            onSelect = { picked -> contractUi = picked; showTypeDialog = false }
        )
    }
}

/* ------------------ UI helpers ------------------ */

@Composable
private fun TitledCard(
    title: String,
    brand: Color,
    borderColor: Color,
    rounded: RoundedCornerShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = rounded,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(brand)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = Color(0xFF3E2723),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(10.dp))
            Divider(color = borderColor, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(text = text, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))
}

/* ------------------ Stepper components ------------------ */

@Composable
private fun StepperNumber(
    label: String? = null,
    value: Float,
    onChange: (Float) -> Unit,
    step: Float,
    decimals: Int,
    enabled: Boolean,
    brand: Color,
    rounded: RoundedCornerShape
) {
    Column(Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, color = Color(0xFF5D4037), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RepeatButton("−", { onChange(value - step) }, enabled, true, brand, rounded)
            Spacer(Modifier.width(12.dp))
            EditableValue(
                text = value.format(decimals),
                onSubmit = { txt -> txt.replace(',', '.').toFloatOrNull()?.let { onChange(it) } },
                enabled = enabled,
                decimals = decimals
            )
            Spacer(Modifier.width(12.dp))
            RepeatButton("+", { onChange(value + step) }, enabled, true, brand, rounded)
        }
    }
}

@Composable
private fun StepperTime(
    minutes: Int,
    onChangeMinutes: (Int) -> Unit,
    stepMinutes: Int,
    enabled: Boolean,
    brand: Color,
    rounded: RoundedCornerShape
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RepeatButton("−", { onChangeMinutes(max(0, minutes - stepMinutes)) }, enabled, true, brand, rounded)
        Spacer(Modifier.width(12.dp))

        val display = "${minutes / 60}:${(minutes % 60).toString().padStart(2, '0')}"
        EditableValue(
            text = display,
            onSubmit = { txt ->
                val t = txt.trim()
                when {
                    ":" in t -> {
                        val p = t.split(":")
                        val h = p.getOrNull(0)?.toIntOrNull()
                        val m = p.getOrNull(1)?.toIntOrNull()
                        if (h != null && m != null && h >= 0 && m in 0..59) onChangeMinutes(h * 60 + m)
                    }
                    else -> t.replace(',', '.').toFloatOrNull()?.let { dec ->
                        if (dec >= 0f) onChangeMinutes((dec * 60f).toInt())
                    }
                }
            },
            enabled = enabled,
            decimals = null
        )

        Spacer(Modifier.width(12.dp))
        RepeatButton("+", { onChangeMinutes(minutes + stepMinutes) }, enabled, true, brand, rounded)
    }
}

/**
 * Versione "precedente": press-and-hold basato su pointerInput + detectTapGestures.
 * Primo scatto immediato, poi ripete finché tieni premuto.
 */
@Composable
private fun RepeatButton(
    text: String,
    onRepeatedClick: () -> Unit,
    enabled: Boolean,
    outlined: Boolean,
    brand: Color,
    rounded: RoundedCornerShape
) {
    val pressAndHoldModifier = Modifier.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        detectTapGestures(
            onPress = {
                if (!enabled) return@detectTapGestures
                onRepeatedClick() // primo subito
                coroutineScope {
                    val job = launch {
                        delay(350)
                        while (isActive) {
                            onRepeatedClick()
                            delay(60)
                        }
                    }
                    try {
                        awaitRelease()
                    } finally {
                        job.cancel()
                    }
                }
            }
        )
    }

    val content: @Composable RowScope.() -> Unit = {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }

    if (outlined) {
        OutlinedButton(
            onClick = { onRepeatedClick() },
            enabled = enabled,
            shape = rounded,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = brand),
            border = BorderStroke(1.5.dp, brand),
            modifier = pressAndHoldModifier,
            content = content
        )
    } else {
        Button(
            onClick = { onRepeatedClick() },
            enabled = enabled,
            shape = rounded,
            colors = ButtonDefaults.buttonColors(containerColor = brand, contentColor = Color.White),
            modifier = pressAndHoldModifier,
            content = content
        )
    }
}

/* ------------------ Editable value ------------------ */

@Composable
private fun EditableValue(
    text: String,
    onSubmit: (String) -> Unit,
    enabled: Boolean,
    decimals: Int?
) {
    var editing by remember { mutableStateOf(false) }
    var local by remember { mutableStateOf(text) }

    if (!editing) {
        Text(
            text = text,
            color = Color(0xFF5D4037),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = enabled) { local = text; editing = true }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    } else {
        OutlinedTextField(
            value = local,
            onValueChange = { s ->
                local = if (decimals == null) {
                    s
                } else {
                    val n = s.replace(',', '.')
                    var dot = 0
                    n.filterIndexed { idx, ch ->
                        when {
                            ch.isDigit() -> true
                            ch == '.' -> { dot += 1; dot <= 1 }
                            ch == '-' -> idx == 0
                            else -> false
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = if (decimals == null) KeyboardType.Text else KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 90.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7D4F16),
                unfocusedBorderColor = Color(0xFF7D4F16),
                cursorColor = Color(0xFF7D4F16)
            ),
            trailingIcon = {
                TextButton(onClick = { editing = false; onSubmit(local) }) { Text("OK") }
            }
        )
    }
}

/* ---------- Mapping enum UI <-> Dominio ---------- */

private fun CompanyEmployee.ContractType?.toUi(): EmployeeContractType? =
    this?.name?.let { runCatching { EmployeeContractType.valueOf(it) }.getOrNull() }

private fun EmployeeContractType?.toDomain(): CompanyEmployee.ContractType? =
    this?.name?.let { runCatching { CompanyEmployee.ContractType.valueOf(it) }.getOrNull() }

/* ---------- Helpers num ---------- */
private fun Float.format(decimals: Int) = "%.${decimals}f".format(this).replace(',', '.')
private fun roundTo(v: Float, decimals: Int): Float {
    val p = 10f.pow(decimals); return kotlin.math.round(v * p) / p
}
private fun Float.pow(p: Int) = (0 until p).fold(1f) { acc, _ -> acc * 10f }
