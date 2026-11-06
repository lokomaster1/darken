package cz.darken.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cz.darken.app.R
import cz.darken.app.overlay.OverlayTint
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomColorDialog(
    initialArgb: Int,
    savedTintPresets: Map<String, Int>,
    onColorApply: (Int) -> Unit,
    onColorClear: (Int?) -> Unit,
    onSavePreset: (Int, Int) -> Unit,
    onSelectPreset: (Int) -> Unit,
    onResetPresets: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var hexDigits by remember(initialArgb) {
        mutableStateOf(argbToHexDigits(initialArgb))
    }
    var selectedArgb by remember(initialArgb) { mutableStateOf(initialArgb) }
    var showHueSlider by remember { mutableStateOf(false) }
    var hueDegrees by remember(initialArgb) { mutableFloatStateOf(argbToHue(initialArgb)) }
    var pendingSliderPersist by remember { mutableStateOf<Int?>(null) }
    var colorEdited by remember { mutableStateOf(false) }
    var selectionSaved by remember { mutableStateOf(false) }
    var lastPresetIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val glowTransition = rememberInfiniteTransition(label = "presetGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "presetGlowAlpha",
    )
    var hideKeyboardIfVisible by remember { mutableStateOf<(() -> Boolean)?>(null) }

    fun parseSelection(): Int? =
        if (hexDigits.length == 6) OverlayTint.parseHexColor("#$hexDigits") else null

    val parsedArgb = parseSelection()
    val canSavePreset = colorEdited && parsedArgb != null
    val hexError = hexDigits.isNotEmpty() && parsedArgb == null

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
        selectionSaved = false
        selectedArgb = argb
        hexDigits = argbToHexDigits(argb)
        hueDegrees = sliderHue ?: argbToHue(argb).coerceIn(0f, SLIDER_HUE_MAX)
    }

    fun persistColor(argb: Int, sliderHue: Float? = null) {
        updateLocalColor(argb, sliderHue)
        colorEdited = true
        onColorApply(argb)
    }

    fun previewHue(hue: Float) {
        val clamped = hue.coerceIn(0f, SLIDER_HUE_MAX)
        updateLocalColor(overlayTintArgbFromHue(clamped), sliderHue = clamped)
    }

    fun scheduleSliderPersist() {
        if (showHueSlider && !selectionSaved) {
            pendingSliderPersist = selectedArgb
        }
    }

    LaunchedEffect(pendingSliderPersist) {
        val argb = pendingSliderPersist ?: return@LaunchedEffect
        pendingSliderPersist = null
        persistColor(argb, sliderHue = hueDegrees)
    }

    BasicAlertDialog(
        onDismissRequest = {
            if (hideKeyboardIfVisible?.invoke() != true) onDismiss()
        },
        properties = DialogProperties(decorFitsSystemWindows = false),
        modifier = Modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    if (waitForUpOrCancellation() != null) hideKeyboardIfVisible?.invoke()
                }
            }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val density = LocalDensity.current
        val ime = WindowInsets.ime
        val keyboardVisible = ime.getBottom(density) > 0
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val scrollState = rememberScrollState()

        fun hideKeyboard() {
            focusManager.clearFocus()
            keyboardController?.hide()
        }

        SideEffect {
            hideKeyboardIfVisible = {
                val visible = ime.getBottom(density) > 0
                if (visible) hideKeyboard()
                visible
            }
        }

        LaunchedEffect(keyboardVisible, scrollState.maxValue) {
            if (keyboardVisible) scrollState.scrollTo(scrollState.maxValue)
        }

        Surface(
            shape = AlertDialogDefaults.shape,
            color = DarkenPalette.NavyCard,
            contentColor = DarkenPalette.TextMuted,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = if (keyboardVisible) 8.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.custom_color_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = DarkenPalette.TextPrimary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        filterColorPalette.forEach { (argb, labelRes) ->
                            val label = stringResource(labelRes)
                            ColorSwatch(
                                argb = argb,
                                selected = !showHueSlider && argb == parsedArgb,
                                onClick = {
                                    val deselect = colorEdited && !showHueSlider && parseSelection() == argb
                                    showHueSlider = false
                                    pendingSliderPersist = null
                                    if (deselect) {
                                        hexDigits = ""
                                        colorEdited = false
                                        selectionSaved = lastPresetIndex != null
                                        onColorClear(lastPresetIndex)
                                    } else {
                                        persistColor(argb)
                                    }
                                },
                                modifier = Modifier.size(36.dp).semantics { contentDescription = label },
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
                            selectionSaved = false
                            colorEdited = true
                            hexDigits = normalized
                            val parsed = parseSelection()
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
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { hideKeyboard() }),
                    )
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            if (canSavePreset) {
                                R.string.custom_color_save_to_slot
                            } else {
                                R.string.custom_color_presets
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OverlayTint.presetSlots.forEachIndexed { index, preset ->
                            val label = stringResource(
                                if (canSavePreset) R.string.custom_color_save_slot else R.string.custom_color_select_slot,
                                index + 1,
                            )
                            val presetArgb = savedTintPresets[preset]
                                ?: OverlayTint.resolveArgb(preset, initialArgb)
                            val indicatorColor = OverlayTint.toComposeColor(presetArgb)
                            val highlight = remember { Animatable(0f) }
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .drawBehind {
                                        if (canSavePreset) {
                                            drawCircle(
                                                color = DarkenPalette.Gold.copy(alpha = glowAlpha * 0.35f),
                                                radius = 20.dp.toPx(),
                                                style = Stroke(width = 12.dp.toPx()),
                                            )
                                            drawCircle(
                                                color = DarkenPalette.Gold.copy(alpha = glowAlpha),
                                                radius = 20.dp.toPx(),
                                                style = Stroke(width = 5.dp.toPx()),
                                            )
                                        }
                                    }
                                    .clip(CircleShape)
                                    .semantics { contentDescription = label }
                                    .clickable(role = Role.Button) {
                                        val newArgb = parseSelection()
                                        if (colorEdited && newArgb != null) {
                                            hexDigits = ""
                                            onSavePreset(index, newArgb)
                                        } else {
                                            updateLocalColor(presetArgb)
                                            onSelectPreset(index)
                                        }
                                        colorEdited = false
                                        lastPresetIndex = index
                                        pendingSliderPersist = null
                                        showHueSlider = false
                                        selectionSaved = true
                                        hideKeyboard()
                                        scope.launch {
                                            highlight.snapTo(1f)
                                            highlight.animateTo(0f, tween(durationMillis = 200))
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .drawBehind {
                                            val ringWidth = 4.8.dp.toPx()
                                            val radius = size.minDimension / 2f
                                            drawCircle(
                                                color = indicatorColor,
                                                radius = radius - ringWidth / 2f,
                                                style = Stroke(width = ringWidth),
                                            )
                                            if (indicatorColor.luminance() < 0.12f) {
                                                val outlineWidth = 1.dp.toPx()
                                                drawCircle(
                                                    color = DarkenPalette.TextMuted.copy(alpha = 0.65f),
                                                    radius = radius - outlineWidth / 2f,
                                                    style = Stroke(width = outlineWidth),
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = (index + 1).toString(),
                                        color = lerp(DarkenPalette.Gold, Color.White, highlight.value * 0.35f),
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onResetPresets) {
                            Text(stringResource(R.string.custom_color_reset_slots), color = DarkenPalette.Gold)
                        }
                    }
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.settings_cancel), color = DarkenPalette.TextPrimary)
                    }
                    TextButton(
                        onClick = {
                            val argb = parseSelection()
                            if (argb != null && !selectionSaved) persistColor(argb)
                            if (argb != null || selectionSaved) onConfirm()
                        },
                        enabled = parsedArgb != null || selectionSaved,
                    ) {
                        Text(stringResource(R.string.settings_ok), color = DarkenPalette.Gold)
                    }
                }
            }
        }
    }
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
internal fun RainbowSwatch(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(36.dp),
) {
    Box(
        modifier = modifier
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
