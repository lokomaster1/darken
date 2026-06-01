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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    var hexInput by remember(currentArgb) {
        mutableStateOf(argbToHex(currentArgb))
    }
    var selectedArgb by remember(currentArgb) { mutableStateOf(currentArgb) }
    var hexError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DarkenPalette.TextPrimary,
        unfocusedTextColor = DarkenPalette.TextPrimary,
        disabledTextColor = DarkenPalette.TextMuted,
        focusedLabelColor = DarkenPalette.TextMuted,
        unfocusedLabelColor = DarkenPalette.TextMuted,
        cursorColor = DarkenPalette.Gold,
        focusedBorderColor = DarkenPalette.Gold,
        unfocusedBorderColor = DarkenPalette.NavyTrack,
        errorBorderColor = Color(OverlayTint.RedArgb),
        errorLabelColor = DarkenPalette.TextPrimary,
        errorTextColor = DarkenPalette.TextPrimary,
    )

    fun applyColor(argb: Int) {
        selectedArgb = argb
        onColorApply(argb)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkenPalette.NavyCard,
        titleContentColor = DarkenPalette.TextPrimary,
        textContentColor = DarkenPalette.TextMuted,
        title = { Text(stringResource(R.string.custom_color_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OverlayTint.customPalette.forEach { argb ->
                        ColorSwatch(
                            argb = argb,
                            selected = argb == selectedArgb,
                            onClick = {
                                hexInput = argbToHex(argb)
                                hexError = false
                                applyColor(argb)
                            },
                        )
                    }
                }
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { value ->
                        val normalized = normalizeHexInput(value)
                        hexInput = normalized
                        val parsed = OverlayTint.parseHexColor(normalized)
                        if (parsed != null) {
                            hexError = false
                            applyColor(parsed)
                        } else {
                            hexError = normalized.removePrefix("#").isNotEmpty()
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
                            text = "#RRGGBB",
                            color = DarkenPalette.TextMuted,
                        )
                    },
                    singleLine = true,
                    isError = hexError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !hexError,
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
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

private fun argbToHex(argb: Int): String {
    val rgb = argb and 0xFFFFFF
    return String.format("#%06X", rgb)
}

/** Keeps a single leading #; strips invalid characters while typing. */
private fun normalizeHexInput(raw: String): String {
    val withoutHashes = raw.replace("#", "")
    val hexOnly = withoutHashes.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    return if (hexOnly.isEmpty()) {
        if (raw.isEmpty()) "" else "#"
    } else {
        "#$hexOnly"
    }
}
