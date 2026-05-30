package cz.darken.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.darken.app.R
import cz.darken.app.data.PreferencesRepository

@Composable
fun SettingsDialog(
    savedLanguage: String,
    savedAutoStartOnLaunch: Boolean,
    onConfirm: (language: String, autoStartOnLaunch: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var draftLanguage by remember(savedLanguage) { mutableStateOf(savedLanguage) }
    var draftAutoStart by remember(savedAutoStartOnLaunch) { mutableStateOf(savedAutoStartOnLaunch) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkenPalette.NavyCard,
        titleContentColor = DarkenPalette.TextPrimary,
        textContentColor = DarkenPalette.TextMuted,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle(stringResource(R.string.settings_language))
                LanguageOption(
                    label = stringResource(R.string.language_system),
                    selected = draftLanguage == PreferencesRepository.LANG_SYSTEM,
                    onClick = { draftLanguage = PreferencesRepository.LANG_SYSTEM },
                )
                LanguageOption(
                    label = stringResource(R.string.language_czech),
                    selected = draftLanguage == PreferencesRepository.LANG_CS,
                    onClick = { draftLanguage = PreferencesRepository.LANG_CS },
                )
                LanguageOption(
                    label = stringResource(R.string.language_english),
                    selected = draftLanguage == PreferencesRepository.LANG_EN,
                    onClick = { draftLanguage = PreferencesRepository.LANG_EN },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
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
                        checked = draftAutoStart,
                        onCheckedChange = { draftAutoStart = it },
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
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = DarkenPalette.NavyTrack,
                )

                SectionTitle(stringResource(R.string.settings_notifications))
                Text(
                    text = stringResource(R.string.notification_mode_minimal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkenPalette.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.notification_interactive_wip),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkenPalette.TextMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draftLanguage, draftAutoStart) }) {
                Text(stringResource(R.string.settings_ok), color = DarkenPalette.Gold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = DarkenPalette.TextPrimary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = DarkenPalette.Gold,
                unselectedColor = DarkenPalette.TextMuted,
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkenPalette.TextPrimary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
