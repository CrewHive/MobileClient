package com.example.myapplication.android.ui.components.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.android.ui.theme.CustomTheme

@Composable
fun TermsDialog(
    onDismiss: () -> Unit
) {
    val colors = CustomTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Header con gradiente e icona
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colors.shade600, colors.shade900)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.background.copy(alpha = 0.15f))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = colors.background
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Termini e Condizioni",
                                color = colors.background,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Leggi con attenzione prima di continuare",
                                color = colors.background.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Chip "Ultimo aggiornamento"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.shade100)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Ultimo aggiornamento • 1 Settembre 2025",
                            color = colors.shade900,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Divider
                Divider(
                    modifier = Modifier
                        .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                    color = colors.shade100
                )

                // Testo scrollabile
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxHeight(0.6f)
                        .verticalScroll(rememberScrollState())
                ) {
                    SectionTitle("1. Scopo dell’applicazione", colors.shade900)
                    SectionBody(
                        "CrewHive è un’app destinata a facilitare la gestione dei turni di lavoro, la registrazione delle ore e la comunicazione interna tra dipendenti e manager.",
                        colors.shade600
                    )

                    SectionTitle("2. Creazione e utilizzo dell’account", colors.shade900)
                    SectionBody(
                        "L’utente è responsabile delle credenziali di accesso. È vietato condividere l’account con soggetti terzi senza autorizzazione.",
                        colors.shade600
                    )

                    SectionTitle("3. Dati sensibili", colors.shade900)
                    SectionBody(
                        "CrewHive tratta dati personali relativi a dipendenti, orari, ferie e permessi. L’utente si impegna a usarli solo per scopi aziendali legittimi e nel rispetto delle normative (GDPR e leggi nazionali).",
                        colors.shade600
                    )

                    SectionTitle("4. Responsabilità dell’utente", colors.shade900)
                    SectionBody(
                        "È vietato inserire dati falsi o usare l’app per scopi diversi dalla gestione delle attività lavorative. I dati dei colleghi devono essere trattati con riservatezza.",
                        colors.shade600
                    )

                    SectionTitle("5. Conservazione dei dati", colors.shade900)
                    SectionBody(
                        "I dati sono conservati su server sicuri e accessibili solo a soggetti autorizzati (dipendenti e manager delle aziende registrate).",
                        colors.shade600
                    )

                    SectionTitle("6. Limitazione di responsabilità", colors.shade900)
                    SectionBody(
                        "CrewHive non è responsabile per errori da inserimenti errati, interruzioni di servizio o uso improprio da parte dell’utente.",
                        colors.shade600
                    )

                    SectionTitle("7. Modifiche ai Termini", colors.shade900)
                    SectionBody(
                        "CrewHive può modificare i presenti Termini in qualsiasi momento. L’utente sarà informato di eventuali aggiornamenti.",
                        colors.shade600
                    )

                    Text(
                        text = "Accettando i presenti Termini, dichiari di aver letto e compreso quanto sopra.",
                        color = colors.shade900,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                // Footer con pulsante primario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ElevatedButton(
                        onClick = onDismiss
                    ) {
                        Text("Chiudi")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = title,
        color = color,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SectionBody(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
