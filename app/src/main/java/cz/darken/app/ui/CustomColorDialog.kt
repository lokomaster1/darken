package cz.darken.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import cz.darken.app.R
import cz.darken.app.overlay.OverlayTint

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomColorDialog(
    currentArgb: Int,
    onColorApply: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var hexDigits by remember(currentArgb) {
        mutableStateOf(argbToHexDigits(currentArgb))
    }
    var selectedArgb by remember(currentArgb) { mutableStateOf(currentArgb) }
    var showHueSlider by remember { mutableStateOf(false) }
    var hueDegrees by remember(currentArgb) { mutableFloatStateOf(argbToHue(currentArgb)) }
    var pendingSliderPersist by remember { mutableStateOf<Int?>(null) }

    val isComplete = hexDigits.length == 6
    val parsedArgb = if (isComplete) OverlayTint.parseHexColor("#$hexDigits") else null
    val hexError = hexDigits.isNotEmpty() && !isComplete

    val previewBorderColor = parsedArgb?.let { OverlayTint.toComposeColor(it) } ?: DarkenPalette.Gold

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DarkenPalette.TextPrimary,
        unfocusedTextColor = DarkenPalette.TextPrimary,
        disabledTextColor = DarkenPalette.TextMuted,
        focusedLabelColor = DarkenPalette.TextMuted,
        unfocusedLabelColor = DarkenPalette.TextMuted,
        cursorColor = DarkenPalette.Gold,
        focusedBorderColor = previewBorderColor,
        unfocusedBorderColor = if (parsedArgb != null) previewBorderColor else DarkenPalette.NavyTrack,
        errorBorderColor = Color(OverlayTint.RedArgb),
        errorLabelColor = DarkenPalette.TextPrimary,
        errorTextColor = DarkenPalette.TextPrimary,
    )

    fun updateLocalColor(argb: Int, sliderHue: Float? = null) {
        selectedArgb = argb
        hexDigits = argbToHexDigits(argb)
        hueDegrees = sliderHue ?: argbToHue(argb).coerceIn(0f, SLIDER_HUE_MAX)
    }

    fun persistColor(argb: Int, sliderHue: Float? = null) {
        updateLocalColor(argb, sliderHue)
        onColorApply(argb)
    }

    fun previewHue(hue: Float) {
        val clamped = hue.coerceIn(0f, SLIDER_HUE_MAX)
        updateLocalColor(overlayTintArgbFromHue(clamped), sliderHue = clamped)
    }

    fun scheduleSliderPersist() {
        pendingSliderPersist = selectedArgb
    }

    LaunchedEffect(pendingSliderPersist) {
        val argb = pendingSliderPersist ?: return@LaunchedEffect
        pendingSliderPersist = null
        persistColor(argb, sliderHue = hueDegrees)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkenPalette.NavyCard,
        titleContentColor = DarkenPalette.TextPrimary,
        textContentColor = DarkenPalette.TextMuted,
        title = { Text(stringResource(R.string.custom_color_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OverlayTint.customPalette.forEach { argb ->
                        ColorSwatch(
                            argb = argb,
                            selected = !showHueSlider && argb == selectedArgb,
                            onClick = {
                                showHueSlider = false
                                persistColor(argb)
                            },
                        )
                    }
                    RainbowSwatch(
                        selected = showHueSlider,
                        onClick = {
                            showHueSlider = true
                            val hsv = argbToHsv(selectedArgb)
                            if (hsv[1] < 0.1f) {
                                persistColor(overlayTintArgbFromHue(hsv[0]))
                            }
                        },
                    )
                }
                if (showHueSlider) {
                    HueSlider(
                        hue = hueDegrees,
                        previewColor = OverlayTint.toComposeColor(selectedArgb),
                        onHueChange = { previewHue(it) },
                        onInteractionEnd = { scheduleSliderPersist() },
                    )
                }
                OutlinedTextField(
                    value = hexDigits,
                    onValueChange = { value ->
                        val normalized = normalizeHexDigits(value)
                        if (normalized == hexDigits) return@OutlinedTextField
                        hexDigits = normalized
                        val parsed = OverlayTint.parseHexColor("#$normalized")
                        if (parsed != null) {
                            persistColor(parsed)
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.custom_color_hex_label),
                            color = DarkenPalette.TextMuted,
                        )
                    },
                    placeholder = {
                        Text(
                            text = "RRGGBB",
                            color = DarkenPalette.TextMuted,
                        )
                    },
                    prefix = {
                        Text(
                            text = "#",
                            color = DarkenPalette.TextPrimary,
                        )
                    },
                    singleLine = true,
                    isError = hexError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    parsedArgb?.let { persistColor(it) }
                    onConfirm()
                },
                enabled = parsedArgb != null,
            ) {
                Text(stringResource(R.string.settings_ok), color = DarkenPalette.Gold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.settings_cancel),
                    color = DarkenPalette.TextPrimary,
                )
            }
        },
    )
}

@Composable
fun ColorSwatch(
    argb: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(36.dp),
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(OverlayTint.toComposeColor(argb))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) DarkenPalette.Gold else DarkenPalette.NavyTrack,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {}
}

@Composable
private fun RainbowSwatch(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    colors = listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red,
                    ),
                ),
            )
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) DarkenPalette.Gold else DarkenPalette.NavyTrack,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

private fun argbToHexDigits(argb: Int): String {
    val rgb = argb and 0xFFFFFF
    return String.format("%06X", rgb)
}

private fun argbToHue(argb: Int): Float = argbToHsv(argb)[0]

private fun argbToHsv(argb: Int): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    return hsv
}

private fun hsvToArgb(hue: Float, saturation: Float, value: Float): Int =
    android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))

/** Hex digits only (no #), uppercase, max 6 characters; extra input is ignored. */
private fun normalizeHexDigits(raw: String): String {
    return raw
        .replace("#", "")
        .filter { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }
        .uppercase()
        .take(6)
}
