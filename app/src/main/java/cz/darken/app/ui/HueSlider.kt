package cz.darken.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
/** Dim-friendly defaults when starting from an achromatic color. */
const val SLIDER_DEFAULT_SATURATION = 0.78f
const val SLIDER_DEFAULT_VALUE = 0.58f

/** Max hue on the slider; 360° wraps to 0° in HSV and would jump the thumb to the start. */
const val SLIDER_HUE_MAX = 359.99f

fun fractionFromHue(hue: Float): Float =
    (hue.coerceIn(0f, SLIDER_HUE_MAX) / SLIDER_HUE_MAX).coerceIn(0f, 1f)

fun hueFromPosition(x: Float, width: Float): Float {
    if (width <= 0f) return 0f
    return (x / width).coerceIn(0f, 1f) * SLIDER_HUE_MAX
}

/** Hue stops for the track gradient (degrees). */
private val TRACK_HUE_STOPS = listOf(
    0f, 18f, 32f, 45f, 58f, 75f, 95f, 120f, 155f, 185f, 210f, 235f, 260f, 285f, 310f, 335f, 360f,
)

/**
 * Maps hue to saturation/value suited for screen-dimming overlays:
 * warm/red/amber tones stay richer; greens and cyans are muted (less neon).
 */
fun overlayTintHsvForHue(hue: Float): FloatArray {
    val h = ((hue % 360f) + 360f) % 360f
    val saturation = when {
        h < 55f || h > 300f -> 0.82f
        h < 95f -> 0.68f
        h < 150f -> 0.42f
        h < 200f -> 0.52f
        else -> 0.72f
    }
    val value = when {
        h < 55f || h > 300f -> 0.60f
        h < 150f -> 0.50f
        else -> 0.55f
    }
    return floatArrayOf(h, saturation, value)
}

fun overlayTintArgbFromHue(hue: Float): Int {
    val hsv = overlayTintHsvForHue(hue)
    return android.graphics.Color.HSVToColor(hsv)
}

private fun hueTrackGradient(): List<Color> =
    TRACK_HUE_STOPS.map { hue ->
        val hsv = overlayTintHsvForHue(hue)
        Color.hsv(hsv[0], hsv[1], hsv[2])
    }

@Composable
fun HueSlider(
    hue: Float,
    previewColor: Color,
    onHueChange: (Float) -> Unit,
    onInteractionEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isInteracting by remember { mutableStateOf(false) }
    val onHueChangeState = rememberUpdatedState(onHueChange)
    val onInteractionEndState = rememberUpdatedState(onInteractionEnd)

    val trackHeight = 28.dp
    val thumbRadius = 10.dp
    val previewWidth = 52.dp
    val previewHeight = 36.dp
    val previewGap = 8.dp
    val previewShape = RoundedCornerShape(8.dp)
    val totalHeight = previewHeight + previewGap + trackHeight + thumbRadius * 2

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
    ) {
        val trackTop = previewHeight + previewGap
        val fraction = fractionFromHue(hue)
        val xOffset = (maxWidth - previewWidth) * fraction

        Box(
            modifier = Modifier
                .offset(x = xOffset)
                .size(width = previewWidth, height = previewHeight)
                .alpha(if (isInteracting) 1f else 0f)
                .clip(previewShape)
                .background(previewColor)
                .border(2.dp, DarkenPalette.TextPrimary, previewShape),
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight + thumbRadius * 2)
                .offset(y = trackTop)
                .pointerInput(Unit) {
                    fun updateHue(x: Float) {
                        onHueChangeState.value(hueFromPosition(x, size.width.toFloat()))
                    }
                    awaitEachGesture {
                        try {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isInteracting = true
                            updateHue(down.position.x)
                            val pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (!change.pressed) break
                                if (change.position != change.previousPosition) {
                                    updateHue(change.position.x)
                                }
                            }
                        } finally {
                            isInteracting = false
                            onInteractionEndState.value()
                        }
                    }
                },
        ) {
            val trackPxTop = thumbRadius.toPx()
            val trackPxHeight = trackHeight.toPx()
            val corner = CornerRadius(trackPxHeight / 2f, trackPxHeight / 2f)

            drawRoundRect(
                brush = Brush.horizontalGradient(hueTrackGradient()),
                topLeft = Offset(0f, trackPxTop),
                size = Size(size.width, trackPxHeight),
                cornerRadius = corner,
            )

            val thumbX = fractionFromHue(hue) * size.width
            val thumbY = trackPxTop + trackPxHeight / 2f
            val thumbPx = thumbRadius.toPx()
            val thumbHsv = overlayTintHsvForHue(hue)
            val thumbFill = Color.hsv(thumbHsv[0], thumbHsv[1], thumbHsv[2])

            drawCircle(
                color = thumbFill,
                radius = thumbPx,
                center = Offset(thumbX, thumbY),
            )
            drawCircle(
                color = DarkenPalette.TextPrimary,
                radius = thumbPx,
                center = Offset(thumbX, thumbY),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
