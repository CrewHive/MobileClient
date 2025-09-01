// FILE: ProfileScreen.kt
package com.example.myapplication.android.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.components.charts.CircularStatDial
import com.example.myapplication.android.ui.components.headers.TopBarComponent
import com.example.myapplication.android.ui.theme.CustomTheme
import com.example.myapplication.android.ui.core.api.utils.weeklyWorkedHoursSinceMonday // <-- usa la utility condivisa
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    username: String,
    email: String,
    weeklyTarget: Int,
    overtimeHours: Double,
    vacationAcc: Double,
    vacationUsed: Double,
    leaveAcc: Double,
    leaveUsed: Double,
    contractTypeLabel: String?,
    isManager: Boolean = false,
    onEditSelf: () -> Unit,
    onClickChangeUsername: () -> Unit,
    onClickChangePassword: () -> Unit,
    onClickLeaveCompany: () -> Unit,
    onClickDeleteAccount: () -> Unit,
    isBusy: Boolean = false
) {
    val colors = CustomTheme.colors
    val rounded = RoundedCornerShape(18.dp)

    // palette coerente con Home
    val brandCream = Color(0xFFFFF8E1)
    val brandBrown = Color(0xFF7D4F16)

    // vm per leggere gli shift (già in cache nella Home/Calendar)
    val calVm: CalendarViewModel = viewModel()

    // assicuriamoci di avere gli shift annuali (la funzione è no-op se già caricati)
    LaunchedEffect(Unit) {
        calVm.ensureUserShiftsYearLoaded()
    }

    // ore lavorate da lunedì 00:00 ad ora (solo SHIFT)
    val weeklyWorked by remember(calVm.userShifts.size) {
        mutableStateOf(weeklyWorkedHoursSinceMonday(calVm.userShifts, Calendar.getInstance()))
    }

    // ore lavorate da inizio mese ad ora (solo SHIFT) — funzione locale
    val monthlyWorked by remember(calVm.userShifts.size) {
        mutableStateOf(monthlyWorkedHoursSinceMonthStart(calVm.userShifts, Calendar.getInstance()))
    }
    val monthlyTarget = (weeklyTarget.coerceAtLeast(0) * 4) // semplice target mensile (4 settimane)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopBarComponent.TopBar()
            Spacer(modifier = Modifier.height(16.dp))

            // HERO card in stile Home
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = brandCream),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                shape = rounded,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(brandCream)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5D4037)
                            )
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFF5D4037).copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info contratto (in linea con card)
            SectionCard(title = "Informazioni lavorative", accent = brandBrown) {
                LabeledValue(
                    label = "Tipo di contratto",
                    value = contractTypeLabel ?: "Contratto non inserito"
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Divider(
                color = colors.shade600.copy(alpha = 0.3f),
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Ore lavorate – dials
            Text(
                text = "Ore lavorate",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 18.sp,
                color = colors.shade950,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularStatDial(
                    current = weeklyWorked,
                    max = weeklyTarget,
                    label = "Settimanali",
                    color = colors.shade400
                )
                CircularStatDial(
                    current = monthlyWorked,
                    max = monthlyTarget,
                    label = "Mensili",
                    color = colors.shade600
                )
                CircularStatDial(
                    current = overtimeHours.toInt(),
                    label = "Straordinari",
                    color = colors.shade900,
                    showMax = false
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Divider(
                color = colors.shade600.copy(alpha = 0.3f),
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Ferie/Permessi
            SectionCard(title = "Ferie e permessi", accent = brandBrown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LabeledValue(label = "Ferie accumulate", value = vacationAcc.toString())
                    LabeledValue(label = "Ferie utilizzate", value = vacationUsed.toString())
                    LabeledValue(label = "Congedi personali accumulati", value = leaveAcc.toString())
                    LabeledValue(label = "Congedi personali utilizzati", value = leaveUsed.toString())
                }
            }

            Spacer(Modifier.height(20.dp))

            // Azioni — stile bottoni coerente con Home (Outlined/bianco + bordo marrone)
            SectionCard(title = "Azioni", accent = brandBrown, bodyPadding = PaddingValues(12.dp)) {
                val safeBtnColors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = brandBrown
                )
                val safeBorder = BorderStroke(1.5.dp, brandBrown)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isManager) {
                        Button(
                            onClick = onEditSelf,
                            enabled = !isBusy,
                            colors = safeBtnColors,
                            shape = rounded,
                            border = safeBorder,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Modifica le mie informazioni") }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onClickChangeUsername,
                            enabled = !isBusy,
                            colors = safeBtnColors,
                            shape = rounded,
                            border = safeBorder,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Cambia username", textAlign = TextAlign.Center)
                            }
                        }

                        Button(
                            onClick = onClickChangePassword,
                            enabled = !isBusy,
                            colors = safeBtnColors,
                            shape = rounded,
                            border = safeBorder,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Cambia password", textAlign = TextAlign.Center)
                            }
                        }
                    }

                    // warning/critical
                    val warnColor = Color(0xFFE57373)
                    val warnBtnColors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = warnColor
                    )

                    Button(
                        onClick = onClickLeaveCompany,
                        enabled = !isBusy,
                        colors = warnBtnColors,
                        shape = rounded,
                        border = BorderStroke(1.5.dp, warnColor),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Lascia l'azienda") }

                    Button(
                        onClick = onClickDeleteAccount,
                        enabled = !isBusy,
                        shape = rounded,
                        border = BorderStroke(1.5.dp, warnColor),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = warnColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Elimina account") }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        if (isBusy) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = brandCream) }
        }
    }
}

/* ------- PARTIALS ------- */

@Composable
private fun SectionCard(
    title: String,
    accent: Color,
    bodyPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val rounded = RoundedCornerShape(18.dp)
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        shape = rounded,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = accent,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 0.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bodyPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}


@Composable
fun LabeledValue(label: String, value: String) {
    val colors = CustomTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.shade950,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
        Text(
            text = value,
            color = colors.shade700,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }
}



/* ------- Helpers: calcolo mensile da inizio mese (solo SHIFT) ------- */

private fun monthlyWorkedHoursSinceMonthStart(
    shifts: List<com.example.myapplication.android.ui.components.calendar.CalendarEvent>,
    now: Calendar = Calendar.getInstance()
): Int {
    val monthStart = (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startMs = monthStart.timeInMillis
    val nowMs = now.timeInMillis
    var totalMillis = 0L

    shifts.forEach { ev ->
        if (ev.kind != com.example.myapplication.android.ui.components.calendar.CalendarItemKind.SHIFT) return@forEach

        fun atTime(base: Calendar, hhmm: String): Calendar {
            val parts = hhmm.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return (base.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val s = atTime(ev.date, ev.startTime)
        var e = atTime(ev.date, ev.endTime)
        if (e.timeInMillis <= s.timeInMillis) {
            e = (e.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        }

        val sClamp = max(s.timeInMillis, startMs)
        val eClamp = min(e.timeInMillis, nowMs)
        if (eClamp > sClamp) totalMillis += (eClamp - sClamp)
    }

    return floor(totalMillis / 3_600_000.0).toInt()
}
