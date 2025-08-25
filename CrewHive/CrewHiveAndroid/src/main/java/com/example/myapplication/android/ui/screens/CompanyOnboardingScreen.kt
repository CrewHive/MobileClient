package com.example.myapplication.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.android.R

@Composable
fun CompanyOnboardingScreen(
    onCreateCompany: () -> Unit,
    onJoinCompany: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(R.drawable.sfondo_crea),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop               // Crop/FillWidth/Fit a tua scelta
            )

            // Contenuto centrale
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Piccolo “logo” testuale/emoji (puoi sostituirlo con un'Image se aggiungi un drawable)
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "CrewHive logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Create your first Hive!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 24.dp)
                )

                Button(
                    onClick = onCreateCompany,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Create a company", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onJoinCompany,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text("Join a company", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun HexagonBackground(
    primary: Color,
    secondary: Color
) {
    // Disegno alcune celle esagonali semi-trasparenti per richiamare il mockup
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        fun Path.addHexagon(center: Offset, radius: Float) {
            reset()
            for (i in 0..5) {
                val angle = Math.toRadians((60.0 * i) - 30.0) // punta verso l'alto
                val x = center.x + radius * kotlin.math.cos(angle).toFloat()
                val y = center.y + radius * kotlin.math.sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        val hex = Path()
        val alpha1 = 0.12f
        val alpha2 = 0.06f

        // in alto (varie tonalità)
        hex.addHexagon(Offset(w * 0.2f, h * 0.12f), radius = w * 0.18f)
        drawPath(hex, color = primary.copy(alpha = alpha1), style = Fill)

        hex.addHexagon(Offset(w * 0.55f, h * 0.08f), radius = w * 0.14f)
        drawPath(hex, color = secondary.copy(alpha = alpha2), style = Fill)

        hex.addHexagon(Offset(w * 0.88f, h * 0.18f), radius = w * 0.16f)
        drawPath(hex, color = primary.copy(alpha = alpha2), style = Fill)

        // in basso
        hex.addHexagon(Offset(w * 0.18f, h * 0.86f), radius = w * 0.16f)
        drawPath(hex, color = secondary.copy(alpha = alpha2), style = Fill)

        hex.addHexagon(Offset(w * 0.52f, h * 0.92f), radius = w * 0.18f)
        drawPath(hex, color = primary.copy(alpha = alpha1), style = Fill)

        hex.addHexagon(Offset(w * 0.86f, h * 0.80f), radius = w * 0.14f)
        drawPath(hex, color = secondary.copy(alpha = alpha2), style = Fill)
    }
}
