package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.ui.components.buttons.ShiftButtonsComponent
import com.example.myapplication.android.ui.components.calendar.TodaySectionComponent
import com.example.myapplication.android.ui.components.charts.TotalWeekHoursComponent
import com.example.myapplication.android.ui.components.headers.TopBarComponent
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun HomeScreen() {
    val colors = CustomTheme.colors

    var shiftStarted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarComponent.TopBar()
        Spacer(modifier = Modifier.height(16.dp))

        TodaySectionComponent.TodaySection()

        Spacer(modifier = Modifier.height(25.dp))
        Divider(
            color = colors.shade600.copy(alpha = 0.3f),
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
        )
        Spacer(modifier = Modifier.height(25.dp))

        if (!shiftStarted) {
            ShiftButtonsComponent.StartShiftButton { shiftStarted = true }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                ShiftButtonsComponent.ShiftActionButton("Vai in pausa") { /* TODO */ }
                Spacer(modifier = Modifier.height(8.dp))
                ShiftButtonsComponent.ShiftActionButton("Finisci il turno") { shiftStarted = false }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))
        Divider(
            color = colors.shade600.copy(alpha = 0.3f),
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
        )
        Spacer(modifier = Modifier.height(25.dp))
        TotalWeekHoursComponent.TotalWeekHours()
        Spacer(modifier = Modifier.height(16.dp))
    }
}
