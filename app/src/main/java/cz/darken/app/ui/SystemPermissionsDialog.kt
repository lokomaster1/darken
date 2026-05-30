package cz.darken.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.darken.app.R

@Composable
fun SystemPermissionsDialog(
    overlayGranted: Boolean,
    notificationsGranted: Boolean,
    batteryUnrestricted: Boolean,
    onOverlayClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onAppDetailsClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkenPalette.NavyCard,
        titleContentColor = DarkenPalette.TextPrimary,
        textContentColor = DarkenPalette.TextMuted,
        title = { Text(stringResource(R.string.permissions_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.permissions_dialog_hint),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                PermissionRow(
                    label = stringResource(R.string.permission_overlay_title),
                    granted = overlayGranted,
                    onClick = onOverlayClick,
                )
                Text(
                    text = stringResource(R.string.permission_overlay_fallback),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkenPalette.TextMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )
                PermissionRow(
                    label = stringResource(R.string.permission_notification_title),
                    granted = notificationsGranted,
                    onClick = onNotificationsClick,
                )
                PermissionRow(
                    label = stringResource(R.string.permission_battery_title),
                    granted = batteryUnrestricted,
                    onClick = onBatteryClick,
                )
                PermissionRow(
                    label = stringResource(R.string.permission_app_details),
                    granted = null,
                    onClick = onAppDetailsClick,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.permissions_dialog_close))
            }
        },
    )
}

@Composable
private fun PermissionRow(
    label: String,
    granted: Boolean?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkenPalette.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (granted != null) {
            Text(
                text = if (granted) {
                    stringResource(R.string.permission_status_ok)
                } else {
                    stringResource(R.string.permission_status_needed)
                },
                color = if (granted) DarkenPalette.Gold else DarkenPalette.TextMuted,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
