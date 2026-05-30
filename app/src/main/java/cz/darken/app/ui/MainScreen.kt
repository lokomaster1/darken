package cz.darken.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cz.darken.app.R
import cz.darken.app.data.PreferencesRepository
import cz.darken.app.ui.components.DarkenCard

@Composable
fun MainScreen(
    dimLevel: Int,
    defaultDimLevel: Int,
    hasSavedDefault: Boolean,
    overlayActive: Boolean,
    canDrawOverlays: Boolean,
    notificationsGranted: Boolean,
    batteryUnrestricted: Boolean,
    onDimLevelChange: (Int) -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    onSaveDefault: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitApp: () -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkenPalette.NavyDeep,
                        DarkenPalette.NavyMid,
                        DarkenPalette.NavyDeep,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HeaderCard(
                    overlayActive = overlayActive,
                    canDrawOverlays = canDrawOverlays,
                )

                if (!canDrawOverlays) {
                    DarkenCard {
                        Text(
                            text = stringResource(R.string.permission_overlay_missing),
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkenPalette.Gold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onOpenPermissions) {
                            Text(
                                text = stringResource(R.string.permission_overlay_button),
                                color = DarkenPalette.Gold,
                            )
                        }
                    }
                }

                ToggleCard(
                    overlayActive = overlayActive,
                    enabled = canDrawOverlays && notificationsGranted,
                    onToggle = onToggleOverlay,
                )

                ControlCard(
                    dimLevel = dimLevel,
                    defaultDimLevel = defaultDimLevel,
                    hasSavedDefault = hasSavedDefault,
                    enabled = canDrawOverlays,
                    onDimLevelChange = onDimLevelChange,
                    onSaveDefault = onSaveDefault,
                )

                ExitSection(onExitApp = onExitApp)
            }

            BottomBar(
                onOpenPermissions = onOpenPermissions,
                onOpenSettings = onOpenSettings,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 72.dp),
        )
    }
}

@Composable
private fun BottomBar(
    onOpenPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.manage_permissions),
            style = MaterialTheme.typography.bodySmall,
            color = DarkenPalette.TextMuted,
            modifier = Modifier
                .clickable(onClick = onOpenPermissions)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        )
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .offset(x = (-6).dp, y = (-6).dp)
                .padding(end = 4.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings_title),
                tint = DarkenPalette.Gold,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun ExitSection(onExitApp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedButton(
            onClick = onExitApp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(R.string.exit_app),
                color = DarkenPalette.TextMuted,
            )
        }
    }
}

@Composable
private fun HeaderCard(
    overlayActive: Boolean,
    canDrawOverlays: Boolean,
) {
    DarkenCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_moon_header),
                contentDescription = null,
                tint = DarkenPalette.Gold,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = DarkenPalette.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusLine(active = overlayActive && canDrawOverlays)
        }
    }
}

@Composable
private fun StatusLine(active: Boolean) {
    val statusWord = if (active) {
        stringResource(R.string.status_active)
    } else {
        stringResource(R.string.status_inactive)
    }
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.status_prefix))
            append(" ")
            withStyle(SpanStyle(color = DarkenPalette.Gold, fontWeight = FontWeight.Bold)) {
                append(statusWord)
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = DarkenPalette.TextMuted,
    )
}

@Composable
private fun ControlCard(
    dimLevel: Int,
    defaultDimLevel: Int,
    hasSavedDefault: Boolean,
    enabled: Boolean,
    onDimLevelChange: (Int) -> Unit,
    onSaveDefault: () -> Unit,
) {
    DarkenCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.slider_hint),
            style = MaterialTheme.typography.bodySmall,
            color = DarkenPalette.TextMuted,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.intensity_value, dimLevel),
            style = MaterialTheme.typography.titleMedium,
            color = DarkenPalette.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            value = dimLevel.toFloat(),
            onValueChange = { onDimLevelChange(it.toInt()) },
            valueRange = PreferencesRepository.MIN_DIM.toFloat()..PreferencesRepository.MAX_DIM.toFloat(),
            steps = PreferencesRepository.MAX_DIM - 1,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = DarkenPalette.Gold,
                activeTrackColor = DarkenPalette.Gold,
                inactiveTrackColor = DarkenPalette.NavyTrack,
                disabledThumbColor = DarkenPalette.GoldDim,
                disabledActiveTrackColor = DarkenPalette.GoldDim,
                disabledInactiveTrackColor = DarkenPalette.NavyTrack,
            ),
        )
        if (hasSavedDefault) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.default_hint, defaultDimLevel),
                style = MaterialTheme.typography.bodySmall,
                color = DarkenPalette.TextMuted,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = onSaveDefault,
            enabled = enabled,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = stringResource(R.string.save_default),
                color = if (enabled) DarkenPalette.Gold else DarkenPalette.TextMuted,
            )
        }
    }
}

@Composable
private fun ToggleCard(
    overlayActive: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    DarkenCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (overlayActive) {
                    stringResource(R.string.toggle_on_label)
                } else {
                    stringResource(R.string.toggle_off_label)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (overlayActive) DarkenPalette.Gold else DarkenPalette.TextMuted,
            )
            Switch(
                checked = overlayActive,
                onCheckedChange = onToggle,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkenPalette.TextPrimary,
                    checkedTrackColor = DarkenPalette.Gold,
                    uncheckedThumbColor = DarkenPalette.TextMuted,
                    uncheckedTrackColor = DarkenPalette.NavyTrack,
                    uncheckedBorderColor = DarkenPalette.NavyTrack,
                ),
            )
        }
    }
}
