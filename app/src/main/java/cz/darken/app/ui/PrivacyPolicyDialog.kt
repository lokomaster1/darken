package cz.darken.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cz.darken.app.R

@Composable
fun PrivacyPolicyDialog(
    firstRun: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!firstRun) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !firstRun,
            dismissOnClickOutside = !firstRun,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkenPalette.NavyCard,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.privacy_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkenPalette.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.privacy_updated),
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkenPalette.TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                HorizontalDivider(color = DarkenPalette.NavyTrack)
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PrivacySection(
                        title = stringResource(R.string.privacy_section_summary_title),
                        body = stringResource(R.string.privacy_section_summary_body),
                    )
                    PrivacySection(
                        title = stringResource(R.string.privacy_section_collect_title),
                        body = stringResource(R.string.privacy_section_collect_body),
                    )
                    PrivacySection(
                        title = stringResource(R.string.privacy_section_storage_title),
                        body = stringResource(R.string.privacy_section_storage_body),
                    )
                    PrivacySection(
                        title = stringResource(R.string.privacy_section_network_title),
                        body = stringResource(R.string.privacy_section_network_body),
                    )
                    PrivacySection(
                        title = stringResource(R.string.privacy_section_permissions_title),
                        body = stringResource(R.string.privacy_section_permissions_body),
                    )
                    PrivacySection(
                        title = stringResource(R.string.privacy_section_third_party_title),
                        body = stringResource(R.string.privacy_section_third_party_body),
                    )
                    PrivacySection(
                        title = stringResource(R.string.privacy_section_contact_title),
                        body = stringResource(R.string.privacy_section_contact_body),
                    )
                }
                HorizontalDivider(color = DarkenPalette.NavyTrack)
                TextButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(
                        text = if (firstRun) {
                            stringResource(R.string.privacy_accept)
                        } else {
                            stringResource(R.string.privacy_close)
                        },
                        color = DarkenPalette.Gold,
                    )
                }
                if (!firstRun) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_cancel),
                            color = DarkenPalette.TextMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = DarkenPalette.Gold,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = DarkenPalette.TextPrimary,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
        )
    }
}
