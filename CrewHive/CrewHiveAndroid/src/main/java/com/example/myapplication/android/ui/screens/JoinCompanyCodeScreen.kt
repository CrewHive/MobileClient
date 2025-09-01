// FILE: JoinCompanyCodeScreen.kt
package com.example.myapplication.android.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.myapplication.android.R
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
@Suppress("UnusedBoxWithConstraintsScope") // usiamo maxHeight nei Modifier
fun JoinCompanyCodeScreen(
    userId: String,
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundRes: Int = R.drawable.sfondo_crea
) {
    val colors = CustomTheme.colors
    val swipeState = rememberSwipeRefreshState(isRefreshing)

    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        SwipeRefresh(state = swipeState, onRefresh = onRefresh) {
            Box(Modifier.fillMaxSize()) {
                // sfondo a schermo intero
                Image(
                    painter = painterResource(backgroundRes),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                // pulsante "indietro"
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .size(44.dp)
                        .zIndex(1f),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = colors.shade600
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }

                // contenuto scrollabile e centrato
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = maxHeight) // almeno quanto la viewport
                            .padding(top = 80.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Unisciti alla tua Hive",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.shade900
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Fornisci questo codice al tuo responsabile per essere aggiunto all’azienda.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.shade700),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(18.dp))

                        CodeCard(
                            userId = userId,
                            onCopy = { /* gestito nel child */ },
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Suggerimento: se sei già stato aggiunto, trascina verso il basso per aggiornare questa schermata.",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.shade600),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeCard(
    userId: String,
    onCopy: () -> Unit
) {
    val colors = CustomTheme.colors
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Il tuo codice",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5D4037) // marrone usato nella Home
                )
            )
            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = userId,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = colors.shade900,
                        lineHeight = 28.sp
                    )
                )

                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(userId))
                        copied = true
                        onCopy()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.shade600,
                        contentColor = colors.background
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Copia",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Copia")
                }
            }

            if (copied) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Copiato!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.shade600,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
