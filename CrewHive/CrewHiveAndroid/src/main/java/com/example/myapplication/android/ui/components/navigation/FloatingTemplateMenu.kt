// FILE: FloatingTemplateMenu.kt
package com.example.myapplication.android.ui.components.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun FloatingTemplateMenu(
    visible: Boolean,
    templates: List<ShiftTemplate>,
    onTemplateClick: (ShiftTemplate) -> Unit,
    onTemplateLongClick: (ShiftTemplate) -> Unit,
    onNewClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CustomTheme.colors

    val scrollState = rememberScrollState()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, bottom = 80.dp)
                .wrapContentHeight(Alignment.Bottom)
                .wrapContentWidth(Alignment.End)
                .scrollable(scrollState, orientation = Orientation.Vertical),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            templates.forEach { template ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = template.title,
                        fontSize = 14.sp,
                        color = colors.shade950,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(template.color)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        Log.d("TEMPLATE_CLICK", "Tap su ${template.title}")
                                        onTemplateClick(template)
                                    },
                                    onLongPress = {
                                        Log.d("TEMPLATE_LONG_CLICK", "Long press su ${template.title}")
                                        onTemplateLongClick(template)
                                    }
                                )
                            }

                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NEW", fontSize = 14.sp, color = colors.shade950, modifier = Modifier.padding(end = 8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.background)
                        .clickable(onClick = onNewClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.DarkGray, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("EDIT", fontSize = 14.sp, color = colors.shade950, modifier = Modifier.padding(end = 8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.background)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✎", color = Color.DarkGray, fontSize = 16.sp)
                }
            }
        }
    }
}

// Data class temporanea per i template
data class ShiftTemplate(
    val title: String,
    val startTime: String,
    val endTime: String,
    val color: Color,
    val description: String
)