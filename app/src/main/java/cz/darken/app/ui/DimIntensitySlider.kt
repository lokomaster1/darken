package cz.darken.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cz.darken.app.data.PreferencesRepository

/**
 * Keeps the default Material3 slider drawing (solid active track + thumb).
 * Custom [SliderDefaults.Track] replaced the bar with thin step ticks — see issue obr1 vs obr2.
 * Thickness is increased only via vertical scale (~40 %), not by replacing track/thumb slots.
 */
private const val ThicknessScale = 1.4f

@Composable
fun DimIntensitySlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = PreferencesRepository.MIN_DIM.toFloat()..PreferencesRepository.MAX_DIM.toFloat(),
            steps = PreferencesRepository.MAX_DIM - 1,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleY = ThicknessScale
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                },
            colors = SliderDefaults.colors(
                thumbColor = DarkenPalette.Gold,
                activeTrackColor = DarkenPalette.Gold,
                inactiveTrackColor = DarkenPalette.NavyTrack,
                disabledThumbColor = DarkenPalette.GoldDim,
                disabledActiveTrackColor = DarkenPalette.GoldDim,
                disabledInactiveTrackColor = DarkenPalette.NavyTrack,
            ),
        )
    }
}
