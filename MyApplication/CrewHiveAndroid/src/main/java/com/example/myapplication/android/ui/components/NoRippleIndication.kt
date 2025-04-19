package com.example.myapplication.android.ui.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.ContentDrawScope

object NoRippleIndication : Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        return object : IndicationInstance {
            override fun ContentDrawScope.drawIndication() {
                drawContent() // Disegna solo il contenuto senza alcun effetto visivo
            }
        }
    }
}
