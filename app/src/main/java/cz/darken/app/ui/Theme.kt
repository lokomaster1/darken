package cz.darken.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object DarkenPalette {
    val NavyDeep = Color(0xFF0A0E1A)
    val NavyMid = Color(0xFF121A2E)
    val NavyCard = Color(0xFF1A2438)
    val NavyTrack = Color(0xFF243049)
    val Gold = Color(0xFFE8B84A)
    val GoldDim = Color(0xFFB8923A)
    val GoldGlow = Color(0x66E8B84A)
    val TextPrimary = Color(0xFFE8EAED)
    val TextMuted = Color(0xFF9AA3AF)
}

private val DarkenColors = darkColorScheme(
    primary = DarkenPalette.Gold,
    onPrimary = DarkenPalette.NavyDeep,
    background = DarkenPalette.NavyDeep,
    onBackground = DarkenPalette.TextPrimary,
    surface = DarkenPalette.NavyCard,
    onSurface = DarkenPalette.TextPrimary,
    surfaceVariant = DarkenPalette.NavyTrack,
    onSurfaceVariant = DarkenPalette.TextMuted,
)

private val DarkenTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    ),
)

@Composable
fun DarkenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkenColors,
        typography = DarkenTypography,
        content = content,
    )
}
