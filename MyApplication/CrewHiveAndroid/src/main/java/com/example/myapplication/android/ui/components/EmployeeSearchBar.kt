package com.example.myapplication.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EmployeeSearchBar(
    searchText: String,
    onSearchChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchChange,
        label = { Text("Cerca dipendente...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7D4F16),
            unfocusedBorderColor = Color(0xFF7D4F16).copy(alpha = 0.5f),
            focusedLabelColor = Color(0xFF7D4F16),
            cursorColor = Color(0xFF7D4F16)
        )
    )
}
