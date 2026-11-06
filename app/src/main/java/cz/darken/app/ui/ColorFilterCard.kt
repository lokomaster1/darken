package cz.darken.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.darken.app.R
import cz.darken.app.overlay.OverlayTint
import cz.darken.app.ui.components.DarkenCard

@Composable
fun ColorFilterCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    tintPreset: String,
    resolvedTintArgb: Int,
    savedTintPresets: Map<String, Int>,
    enabled: Boolean,
    onPresetSelected: (String) -> Unit,
    onCustomClick: () -> Unit,
) {
    val expandInteraction = remember { MutableInteractionSource() }
    DarkenCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    interactionSource = expandInteraction,
                    indication = null,
                ) {
                    onExpandedChange(!expanded)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.filter_color_title),
                style = if (expanded) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodySmall
                },
                color = if (expanded) DarkenPalette.TextPrimary else DarkenPalette.TextMuted,
                modifier = Modifier.weight(1f),
            )
            if (!expanded) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(OverlayTint.toComposeColor(resolvedTintArgb)),
                )
                Spacer(modifier = Modifier.size(12.dp))
            }
            Switch(
                checked = expanded,
                onCheckedChange = null,
                enabled = enabled,
                interactionSource = expandInteraction,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkenPalette.TextPrimary,
                    checkedTrackColor = DarkenPalette.Gold,
                    uncheckedThumbColor = DarkenPalette.TextMuted,
                    uncheckedTrackColor = DarkenPalette.NavyTrack,
                    uncheckedBorderColor = DarkenPalette.NavyTrack,
                ),
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                val defaultLabels = listOf(
                    R.string.tint_gray,
                    R.string.palette_graphite,
                    R.string.palette_burgundy,
                )
                OverlayTint.presetSlots.forEachIndexed { index, preset ->
                    val savedArgb = savedTintPresets[preset]
                    val labelRes = if (savedArgb == null) {
                        defaultLabels[index]
                    } else {
                        filterColorPalette[savedArgb]
                    }
                    PresetColorOption(
                        argb = savedArgb ?: OverlayTint.resolveArgb(preset, resolvedTintArgb),
                        label = if (labelRes != null) stringResource(labelRes) else (index + 1).toString(),
                        selected = tintPreset == preset,
                        enabled = enabled,
                        onClick = { onPresetSelected(preset) },
                        modifier = Modifier.weight(1f),
                    )
                }
                PresetColorOption(
                    argb = resolvedTintArgb,
                    label = stringResource(R.string.tint_custom),
                    selected = tintPreset == OverlayTint.PRESET_CUSTOM,
                    enabled = enabled,
                    onClick = onCustomClick,
                    modifier = Modifier.weight(1f),
                    isCustom = true,
                )
            }
        }
    }
}

@Composable
private fun PresetColorOption(
    argb: Int,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    isCustom: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isCustom && !selected) {
            RainbowSwatch(
                selected = false,
                onClick = { if (enabled) onClick() },
                modifier = Modifier.size(44.dp),
            )
        } else {
            ColorSwatch(
                argb = argb,
                selected = selected,
                onClick = { if (enabled) onClick() },
                modifier = Modifier.size(44.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) DarkenPalette.Gold else DarkenPalette.TextMuted,
            textAlign = TextAlign.Center,
        )
    }
}
