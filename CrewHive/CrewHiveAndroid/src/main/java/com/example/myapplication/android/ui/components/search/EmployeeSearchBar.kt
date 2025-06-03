package com.example.myapplication.android.ui.components.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun EmployeeSearchBar(
    searchText: String,
    onSearchChange: (String) -> Unit
) {
    val colors = CustomTheme.colors

    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchChange,
        label = { Text("Cerca dipendente...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.shade800,
            unfocusedBorderColor = colors.shade800.copy(alpha = 0.5f),
            focusedLabelColor = colors.shade800,
            cursorColor = colors.shade800
        )
    )
}
