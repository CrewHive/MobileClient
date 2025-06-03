package com.example.myapplication.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

@Composable
fun AdvancedColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var brightness by remember { mutableStateOf(1f) }
    val selectedColor by remember(hue, saturation, brightness) {
        derivedStateOf { Color.hsv(hue, saturation, brightness) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFAF7C7),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("HEX: #${selectedColor.toHex()}", color = Color(0xFF7D4F16), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Hue: ${hue.roundToInt()}", modifier = Modifier.align(Alignment.Start), color = Color(0xFF7D4F16))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            (0..360 step 15).map { Color.hsv(it.toFloat(), 1f, 1f) }
                        )
                    )
                ) {
                    Slider(
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                        modifier = Modifier.fillMaxSize(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Saturation: ${(saturation * 100).roundToInt()}", modifier = Modifier.align(Alignment.Start), color = Color(0xFF7D4F16))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.White, Color.hsv(hue, 1f, 1f))
                        )
                    )
                ) {
                    Slider(
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxSize(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Brightness: ${(brightness * 100).roundToInt()}", modifier = Modifier.align(Alignment.Start), color = Color(0xFF7D4F16))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Black, Color.hsv(hue, saturation, 1f))
                        )
                    )
                ) {
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxSize(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .border(1.dp, Color.Gray, CircleShape)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onColorSelected(selectedColor) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16))) {
                    Text("Conferma",
                        color = Color(0xFFFAF7C7))
                }
            }
        }
    }
}

fun Color.toHex(): String =
    String.format("%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())