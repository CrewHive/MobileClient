package com.example.myapplication.android.ui.components.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.android.ui.state.EmployeeContractType

@Composable
fun EmployeeContractTypePickerDialog(
    current: EmployeeContractType?,
    onDismiss: () -> Unit,
    onSelect: (EmployeeContractType) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF8E1),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Contract type", fontSize = 20.sp, color = Color(0xFF5D4037))
                Spacer(Modifier.height(12.dp))

                EmployeeContractType.values().forEach { t ->
                    val selected = (t == current)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Color(0xFF7D4F16) else Color.Transparent)
                            .clickable { onSelect(t) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t.displayName,
                            fontSize = 16.sp,
                            color = if (selected) Color(0xFFFFF8E1) else Color(0xFF5D4037)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF7D4F16)) }
                }
            }
        }
    }
}
