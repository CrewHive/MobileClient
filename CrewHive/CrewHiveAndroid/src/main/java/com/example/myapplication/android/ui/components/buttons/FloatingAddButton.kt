package com.example.myapplication.android.ui.components.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.ui.theme.CustomTheme


@Composable
fun FloatingAddButton(onClick: () -> Unit = {}) {
    val colors = CustomTheme.colors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = colors.shade800,
            contentColor = colors.shade100,
            shape = CircleShape, // forza la forma circolare
            modifier = Modifier
                .padding(16.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Event"
            )
        }
    }
}
