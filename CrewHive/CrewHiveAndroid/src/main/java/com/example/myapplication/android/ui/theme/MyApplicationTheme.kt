package com.example.myapplication.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// 1. Data class che contiene tutte le sfumature da 50 a 950
data class AppColors(
    val shade50: Color,
    val shade100: Color,
    val shade200: Color,
    val shade300: Color,
    val shade400: Color,
    val shade500: Color,
    val shade550: Color,
    val shade600: Color,
    val shade700: Color,
    val shade800: Color,
    val shade900: Color,
    val shade950: Color,

    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color
)


private val LightAppColors = AppColors(
    shade50      = Color(0xFFFCFBEA),
    shade100     = Color(0xFFFAF7C7),
    shade200     = Color(0xFFF6EC92),
    shade300     = Color(0xFFF0D954),
    shade400     = Color(0xFFEAC525),
    shade500     = Color(0xFFEAC525),
    shade550     = Color(0xFFDAAE18),
    shade600     = Color(0xFFC68F13),
    shade700     = Color(0xFF966212),
    shade800     = Color(0xFF7D4F16),
    shade900     = Color(0xFF6A4019),
    shade950     = Color(0xFF3E220A),

    primary      = Color(0xFFDAAE18),   // shade500
    onPrimary    = Color(0xFF000000),   // nero su primary chiaro
    background   = Color(0xFFFFFFFF),   // bianco puro
    onBackground = Color(0xFF000000),   // testo scuro su background chiaro
    surface      = Color(0xFFFFFFFF),
    onSurface    = Color(0xFF000000),
    error        = Color(0xFFB00020),
    onError      = Color(0xFFFFFFFF)
)

private val DarkAppColors = AppColors(
    shade50      = Color(0xFFFCFBEA),
    shade100     = Color(0xFFFAF7C7),
    shade200     = Color(0xFFF6EC92),
    shade300     = Color(0xFFF0D954),
    shade400     = Color(0xFFEAC525),
    shade500     = Color(0xFFEAC525),
    shade550     = Color(0xFFDAAE18),
    shade600     = Color(0xFFC68F13),
    shade700     = Color(0xFF966212),
    shade800     = Color(0xFF7D4F16),
    shade900     = Color(0xFF6A4019),
    shade950     = Color(0xFF3E220A),

    primary      = Color(0xFFDAAE18),   // shade500
    onPrimary    = Color(0xFF000000),   // nero su primary chiaro
    background   = Color(0xFFFFFFFF),   // bianco puro
    onBackground = Color(0xFF000000),   // testo scuro su background chiaro
    surface      = Color(0xFFFFFFFF),
    onSurface    = Color(0xFF000000),
    error        = Color(0xFFB00020),
    onError      = Color(0xFFFFFFFF)
)


private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

object CustomTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors

    // Typography di base (puoi estendere con headline, title, ecc.)
    val typography = Typography(
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    )

    // Shapes di base
    val shapes = Shapes(
        small  = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large  = RoundedCornerShape(0.dp)
    )

    CompositionLocalProvider(
        LocalAppColors provides colors
    ) {
        MaterialTheme(
            // Poiché usiamo i nostri AppColors, non è necessario popolare tutti i campi di colorScheme:
            colorScheme = androidx.compose.material3.ColorScheme(
                primary             = Color.Unspecified,
                onPrimary           = Color.Unspecified,
                primaryContainer    = Color.Unspecified,
                onPrimaryContainer  = Color.Unspecified,
                secondary           = Color.Unspecified,
                onSecondary         = Color.Unspecified,
                secondaryContainer  = Color.Unspecified,
                onSecondaryContainer= Color.Unspecified,
                tertiary            = Color.Unspecified,
                onTertiary          = Color.Unspecified,
                tertiaryContainer   = Color.Unspecified,
                onTertiaryContainer = Color.Unspecified,
                error               = Color.Unspecified,
                onError             = Color.Unspecified,
                errorContainer      = Color.Unspecified,
                onErrorContainer    = Color.Unspecified,
                background          = Color.Unspecified,
                onBackground        = Color.Unspecified,
                surface             = Color.Unspecified,
                onSurface           = Color.Unspecified,
                surfaceVariant      = Color.Unspecified,
                onSurfaceVariant    = Color.Unspecified,
                outline             = Color.Unspecified,
                inverseOnSurface    = Color.Unspecified,
                inverseSurface      = Color.Unspecified,
                inversePrimary      = Color.Unspecified,
                surfaceTint         = Color.Unspecified,
                outlineVariant      = Color.Unspecified,
                scrim               = Color.Unspecified
            ),
            typography = typography,
            shapes     = shapes,
            content    = content
        )
    }
}
