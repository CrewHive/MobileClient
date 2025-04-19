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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
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
            color = Color(0XFFC68F13).copy(alpha = 0.3f),
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp)
        )
        Spacer(modifier = Modifier.height(25.dp))

        if (!shiftStarted) {
            ShiftButtonsComponent.StartShiftButton { shiftStarted = true }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShiftButtonsComponent.ShiftActionButton("Pause shift") { /* TODO */ }
                ShiftButtonsComponent.ShiftActionButton("End shift") { shiftStarted = false }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))
        Divider(
            color = Color(0XFFC68F13).copy(alpha = 0.3f),
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
