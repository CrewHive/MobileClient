package com.example.myapplication.android.ui.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.android.ui.theme.CustomTheme

object ShiftButtonsComponent {

    @Composable
    fun StartShiftButton(onClick: () -> Unit) {
        val colors = CustomTheme.colors

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(colors.shade500),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "Inizia il turno!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }

//    @Composable
//    fun ShiftActionButtons(onEndShift: () -> Unit) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 32.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            ShiftActionButton("Pause shift") { /* TODO */ }
//            ShiftActionButton("End shift", onClick = onEndShift)
//        }
//    }

    @Composable
    fun ShiftActionButton(text: String, onClick: () -> Unit) {
        val colors = CustomTheme.colors

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(colors.shade500),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

