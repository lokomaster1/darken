package cz.darken.app.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.darken.app.data.PreferencesRepository

private val TrackHeight = 11.dp
private val ThumbSize = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimIntensitySlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SliderDefaults.colors(
        thumbColor = DarkenPalette.Gold,
        activeTrackColor = DarkenPalette.Gold,
        inactiveTrackColor = DarkenPalette.NavyTrack,
        disabledThumbColor = DarkenPalette.GoldDim,
        disabledActiveTrackColor = DarkenPalette.GoldDim,
        disabledInactiveTrackColor = DarkenPalette.NavyTrack,
    )
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = PreferencesRepository.MIN_DIM.toFloat()..PreferencesRepository.MAX_DIM.toFloat(),
        steps = PreferencesRepository.MAX_DIM - 1,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = colors,
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(TrackHeight),
                colors = colors,
            )
        },
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = remember { MutableInteractionSource() },
                modifier = Modifier.size(ThumbSize),
                colors = colors,
            )
        },
    )
}
