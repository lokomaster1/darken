package cz.darken.app.overlay

import androidx.compose.ui.graphics.Color
import cz.darken.app.data.PreferencesRepository

object OverlayTint {

    const val PRESET_GRAY = "gray"
    const val PRESET_AMBER = "amber"
    const val PRESET_RED = "red"
    const val PRESET_CUSTOM = "custom"

    val GrayArgb: Int = 0xFF000000.toInt()
    /** Warm light brown (matches 4th swatch in custom palette). */
    val AmberArgb: Int = 0xFF4A3728.toInt()
    val RedArgb: Int = 0xFF8B3030.toInt()

    /** Curated palette for the custom picker (ARGB). */
    val customPalette: List<Int> = listOf(
        0xFF000000.toInt(),
        0xFF1A1A1A.toInt(),
        0xFF2D2416.toInt(),
        0xFF4A3728.toInt(),
        0xFF6B2020.toInt(),
        0xFF8B3030.toInt(),
        0xFFB85030.toInt(),
        0xFFD4A04A.toInt(),
        0xFFE8A838.toInt(),
        0xFFC9A227.toInt(),
        0xFF5C4033.toInt(),
        0xFF3D2B52.toInt(),
        0xFF1E3A2F.toInt(),
        0xFF2A3A4A.toInt(),
    )

    fun resolveArgb(preset: String, customArgb: Int): Int = when (preset) {
        PRESET_AMBER -> AmberArgb
        PRESET_RED -> RedArgb
        PRESET_CUSTOM -> customArgb
        else -> GrayArgb
    }

    fun toComposeColor(argb: Int): Color = Color(argb)

    fun parseHexColor(input: String): Int? {
        val cleaned = input.trim().removePrefix("#")
        if (cleaned.length != 6 && cleaned.length != 8) return null
        return try {
            val value = cleaned.toLong(16)
            if (cleaned.length == 6) {
                (0xFF000000 or value).toInt()
            } else {
                value.toInt()
            }
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun alphaForDimLevel(dimLevel: Int): Float =
        dimLevel.coerceIn(PreferencesRepository.MIN_DIM, PreferencesRepository.MAX_DIM) /
            PreferencesRepository.MAX_DIM.toFloat()
}
