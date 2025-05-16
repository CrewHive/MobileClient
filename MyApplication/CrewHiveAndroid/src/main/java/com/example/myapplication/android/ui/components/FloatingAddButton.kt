package com.example.myapplication.android.ui.components

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

@Composable
fun FloatingAddButton(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = Color(0xFF7D4F16),
            contentColor = Color(0xFFFAF7C7),
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
