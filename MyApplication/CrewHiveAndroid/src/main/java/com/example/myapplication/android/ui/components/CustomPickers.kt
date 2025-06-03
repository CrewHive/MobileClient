// ✅ Import per compatibilità API 24 con Calendar
import java.util.Calendar
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.abs

@Composable
fun CustomDatePickerDialog(onDismiss: () -> Unit, onDateSelected: (String) -> Unit) {
    val now = Calendar.getInstance()
    Log.d("DATE_PICKER", "Data corrente: ${now.time}")

    val initialDay = (now.get(Calendar.DAY_OF_MONTH) - 2).coerceAtLeast(1)
    val initialMonth = (now.get(Calendar.MONTH) + 1 - 2).coerceAtLeast(1)
    val initialYear = now.get(Calendar.YEAR) - 2

    var day by remember { mutableStateOf(initialDay) }
    var month by remember { mutableStateOf(initialMonth) }
    var year by remember { mutableStateOf(initialYear) }

    Log.d("DATE_PICKER", "Valori iniziali -> Giorno: $initialDay, Mese: $initialMonth, Anno: $initialYear")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Seleziona Data", fontSize = 20.sp, color = Color(0xFF5D4037))
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PickerColumnScroll(1..31, selectedValue = day, onValueChanged = {
                        Log.d("DATE_PICKER", "[riga 40] Giorno selezionato: $it")
                        day = it
                    })
                    PickerColumnScroll(1..12, selectedValue = month, monthNames = true, onValueChanged = {
                        Log.d("DATE_PICKER", "[riga 43] Mese selezionato: $it")
                        month = it
                    })
                    PickerColumnScroll((initialYear - 10)..(initialYear + 10), selectedValue = year, onValueChanged = {
                        Log.d("DATE_PICKER", "[riga 46] Anno selezionato: $it")
                        year = it
                    })
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    val formatted = "%02d/%02d/%04d".format(day, month, year)
                    Log.d("DATE_PICKER", "[riga 51] Data confermata: $formatted")
                    onDateSelected(formatted)
                }, modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16))) {
                    Text("Conferma",color = Color(0xFFFAF7C7))
                }
            }
        }
    }
}

@Composable
fun CustomTimePickerDialog(onDismiss: () -> Unit, onTimeSelected: (String) -> Unit) {
    val now = Calendar.getInstance()
    Log.d("TIME_PICKER", "Ora corrente: ${now.time}")

    val initialHour = (6 - 2).coerceAtLeast(0) // ⏰ Inizio orario corretto compensato
    val initialMinute = 58 // ⏱ Imposta minuto iniziale a 00 per chiarezza

    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    Log.d("TIME_PICKER", "Valori iniziali -> Ora: $initialHour, Minuto: $initialMinute")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Seleziona Ora", fontSize = 20.sp, color = Color(0xFF5D4037))
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PickerColumnScroll(0..23, selectedValue = hour, onValueChanged = {
                        Log.d("TIME_PICKER", "[riga 81] Ora selezionata: $it")
                        hour = it
                    })
                    PickerColumnScroll(0..59, selectedValue = minute, onValueChanged = {
                        Log.d("TIME_PICKER", "[riga 84] Minuto selezionato: $it")
                        minute = it
                    })
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    val formatted = "%02d:%02d".format(hour, minute)
                    Log.d("TIME_PICKER", "[riga 89] Ora confermata: $formatted")
                    onTimeSelected(formatted)
                }, modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D4F16))) {
                    Text("Conferma",color = Color(0xFFFAF7C7))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PickerColumnScroll(
    baseRange: IntRange,
    selectedValue: Int,
    monthNames: Boolean = false,
    onValueChanged: (Int) -> Unit
) {
    val baseList = baseRange.toList()
    val repeatedItems = remember { List(100) { baseList }.flatten() }
    val center = repeatedItems.size / 2
    val baseListSize = baseList.size
    val baseIndex = baseList.indexOf(selectedValue).takeIf { it != -1 } ?: 0
    val initialIndex = center - center % baseListSize + baseIndex

    val listState = rememberLazyListState()
    var isInitialized by remember { mutableStateOf(false) }

    // Scroll iniziale
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
        isInitialized = true
        Log.d("PICKER_SCROLL", "[riga 112] Scroll iniziale a index=$initialIndex valore=${repeatedItems[initialIndex]}")
        repeatedItems.getOrNull(initialIndex)?.let { onValueChanged(it) }
    }

    LaunchedEffect(listState.isScrollInProgress, isInitialized) {
        if (isInitialized && !listState.isScrollInProgress) {
            val visibleCount = 5
            val centerIndex = listState.firstVisibleItemIndex + visibleCount / 2 // ✅ corretto: centro visivo
            val selected = repeatedItems.getOrNull(centerIndex)
            Log.d("PICKER_SCROLL", "[riga 117] Scroll fermo. centerIndex=$centerIndex, valore=$selected")
            selected?.let { onValueChanged(it) }
        }
    }

    val itemHeight = 40.dp
    val visibleCount = 5
    val pickerHeight = itemHeight * visibleCount

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(pickerHeight)
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(repeatedItems) { index, value ->
                val centerIndex = listState.firstVisibleItemIndex + visibleCount / 2
                val distance = abs(index - centerIndex)
                val baseColor = Color(0xFF5D4037)
                val alpha = (1f - distance * 0.2f).coerceIn(0.2f, 1f)
                val fontSize = (22 - distance * 3).coerceAtLeast(10).sp
                val horizontalOffset = (distance * 4).dp

                Text(
                    text = if (monthNames) monthNumberToName(value) else value.toString().padStart(2, '0'),
                    fontSize = fontSize,
                    color = baseColor.copy(alpha = alpha),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(horizontal = horizontalOffset)
                )
            }
        }

        Divider(Modifier.align(Alignment.Center).fillMaxWidth().offset(y = -itemHeight / 2 + 2.dp).height(1.dp), color = Color.Gray.copy(alpha = 0.5f))
        Divider(Modifier.align(Alignment.Center).fillMaxWidth().offset(y = itemHeight / 2 - 2.dp).height(1.dp), color = Color.Gray.copy(alpha = 0.5f))
    }
}

fun monthNumberToName(month: Int): String {
    return listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ).getOrElse(month - 1) { month.toString() }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomDatePickerDialog() {
    MaterialTheme {
        CustomDatePickerDialog(
            onDismiss = {},
            onDateSelected = { selected -> println("Selezionato: $selected") }
        )
    }
}
