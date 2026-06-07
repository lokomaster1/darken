package cz.darken.app.ui

import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cz.darken.app.R
import cz.darken.app.data.PreferencesRepository

private val SettingsDividerPadding = 6.dp
private val CompactOptionHeight = 34.dp
private val ScrollFadeHeight = 28.dp

private val SettingsDialogHeightFraction = 0.80f

@Composable
fun SettingsDialog(
    language: String,
    autoStartOnLaunch: Boolean,
    notificationMode: String,
    onLanguageChange: (String) -> Unit,
    onAutoStartChange: (Boolean) -> Unit,
    onNotificationModeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenGithub: () -> Unit,
    onOpenContact: () -> Unit,
    onAddQsTile: () -> Unit,
) {
    val qsTilePlacementSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(SettingsDialogHeightFraction)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = DarkenPalette.NavyCard,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkenPalette.TextPrimary,
                    )
                    ScrollableSettingsSection(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                    SectionTitle(stringResource(R.string.settings_language))
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        SelectOption(
                            label = stringResource(R.string.language_system),
                            selected = language == PreferencesRepository.LANG_SYSTEM,
                            onClick = { onLanguageChange(PreferencesRepository.LANG_SYSTEM) },
                        )
                        SelectOption(
                            label = stringResource(R.string.language_czech),
                            selected = language == PreferencesRepository.LANG_CS,
                            onClick = { onLanguageChange(PreferencesRepository.LANG_CS) },
                        )
                        SelectOption(
                            label = stringResource(R.string.language_english),
                            selected = language == PreferencesRepository.LANG_EN,
                            onClick = { onLanguageChange(PreferencesRepository.LANG_EN) },
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = SettingsDividerPadding),
                        color = DarkenPalette.NavyTrack,
                    )

                    SectionTitle(stringResource(R.string.settings_behavior))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.settings_auto_start),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkenPalette.TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = autoStartOnLaunch,
                            onCheckedChange = onAutoStartChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkenPalette.TextPrimary,
                                checkedTrackColor = DarkenPalette.Gold,
                                uncheckedThumbColor = DarkenPalette.TextMuted,
                                uncheckedTrackColor = DarkenPalette.NavyTrack,
                            ),
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_auto_start_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkenPalette.TextMuted,
                        modifier = Modifier.padding(top = 2.dp),
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = SettingsDividerPadding),
                        color = DarkenPalette.NavyTrack,
                    )

                    SectionTitle(stringResource(R.string.settings_notification))
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        SelectOption(
                            label = stringResource(R.string.settings_notification_minimal),
                            selected = notificationMode == PreferencesRepository.NOTIF_MINIMAL,
                            onClick = { onNotificationModeChange(PreferencesRepository.NOTIF_MINIMAL) },
                        )
                        SelectOption(
                            label = stringResource(R.string.settings_notification_interactive),
                            selected = notificationMode == PreferencesRepository.NOTIF_INTERACTIVE,
                            onClick = { onNotificationModeChange(PreferencesRepository.NOTIF_INTERACTIVE) },
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_notification_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkenPalette.TextMuted,
                        modifier = Modifier.padding(top = 2.dp),
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = SettingsDividerPadding),
                        color = DarkenPalette.NavyTrack,
                    )

                    SectionTitle(stringResource(R.string.settings_qs_tile))
                    Text(
                        text = stringResource(
                            if (qsTilePlacementSupported) {
                                R.string.settings_qs_tile_hint
                            } else {
                                R.string.settings_qs_tile_hint_legacy
                            },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkenPalette.TextMuted,
                    )
                    if (qsTilePlacementSupported) {
                        TextButton(
                            onClick = onAddQsTile,
                            modifier = Modifier.padding(top = 0.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.settings_qs_tile_button),
                                color = DarkenPalette.Gold,
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = SettingsDividerPadding),
                        color = DarkenPalette.NavyTrack,
                    )

                    SectionTitle(stringResource(R.string.settings_about))
                    SettingsLink(
                        label = stringResource(R.string.settings_privacy),
                        subtitle = stringResource(R.string.settings_privacy_subtitle),
                        onClick = onOpenPrivacy,
                    )
                    SettingsLink(
                        label = stringResource(R.string.settings_github),
                        subtitle = stringResource(R.string.settings_github_subtitle),
                        onClick = onOpenGithub,
                    )
                    SettingsLink(
                        label = stringResource(R.string.settings_contact),
                        subtitle = stringResource(R.string.settings_contact_email),
                        onClick = onOpenContact,
                    )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.settings_close), color = DarkenPalette.Gold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollableSettingsSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val canScrollDown by remember {
        derivedStateOf {
            scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue - 8
        }
    }
    val canScrollUp by remember {
        derivedStateOf { scrollState.value > 8 }
    }
    val showScrollbar by remember {
        derivedStateOf { scrollState.maxValue > 0 }
    }
    val chevronAlpha by rememberInfiniteTransition(label = "settings_scroll_hint").animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "settings_scroll_chevron_alpha",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(
                    top = 10.dp,
                    bottom = if (canScrollDown) 22.dp else 8.dp,
                    end = if (showScrollbar) 8.dp else 0.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = content,
        )

        if (canScrollUp) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(ScrollFadeHeight)
                    .background(
                        Brush.verticalGradient(
                            0f to DarkenPalette.NavyCard,
                            1f to Color.Transparent,
                        ),
                    ),
            )
        }

        if (canScrollDown) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(ScrollFadeHeight + 6.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.5f to DarkenPalette.NavyCard.copy(alpha = 0.88f),
                            1f to DarkenPalette.NavyCard,
                        ),
                    ),
            )
            Text(
                text = "▾",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 1.dp),
                color = DarkenPalette.Gold.copy(alpha = if (canScrollDown) chevronAlpha else 0f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (showScrollbar) {
            SettingsScrollThumb(
                scrollState = scrollState,
                viewportHeightPx = constraints.maxHeight.toFloat(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 1.dp),
            )
        }
    }
}

@Composable
private fun SettingsScrollThumb(
    scrollState: ScrollState,
    viewportHeightPx: Float,
    modifier: Modifier = Modifier,
) {
    if (scrollState.maxValue <= 0 || viewportHeightPx <= 0f) return

    val density = LocalDensity.current
    val trackHeight = with(density) { viewportHeightPx.toDp() }
    val contentHeightPx = viewportHeightPx + scrollState.maxValue
    val thumbHeightFraction = (viewportHeightPx / contentHeightPx).coerceIn(0.14f, 1f)
    val thumbHeight = trackHeight * thumbHeightFraction
    val scrollFraction = scrollState.value.toFloat() / scrollState.maxValue
    val thumbOffset = (trackHeight - thumbHeight) * scrollFraction

    Box(
        modifier = modifier.width(3.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(thumbHeight)
                .offset(y = thumbOffset)
                .clip(RoundedCornerShape(2.dp))
                .background(DarkenPalette.Gold.copy(alpha = 0.42f)),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = DarkenPalette.TextPrimary,
        modifier = Modifier.padding(bottom = 2.dp),
    )
}

@Composable
private fun SelectOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CompactOptionHeight)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier
                .padding(end = 0.dp)
                .graphicsLayer {
                    scaleX = 0.82f
                    scaleY = 0.82f
                },
            colors = RadioButtonDefaults.colors(
                selectedColor = DarkenPalette.Gold,
                unselectedColor = DarkenPalette.TextMuted,
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkenPalette.TextPrimary,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

@Composable
private fun SettingsLink(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkenPalette.Gold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = DarkenPalette.TextMuted,
        )
    }
}
