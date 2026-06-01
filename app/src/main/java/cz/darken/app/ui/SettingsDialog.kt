package cz.darken.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cz.darken.app.R
import cz.darken.app.data.PreferencesRepository

@Composable
fun SettingsDialog(
    savedLanguage: String,
    savedAutoStartOnLaunch: Boolean,
    onConfirm: (language: String, autoStartOnLaunch: Boolean) -> Unit,
    onDismiss: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenGithub: () -> Unit,
    onOpenContact: () -> Unit,
) {
    var draftLanguage by remember(savedLanguage) { mutableStateOf(savedLanguage) }
    var draftAutoStart by remember(savedAutoStartOnLaunch) { mutableStateOf(savedAutoStartOnLaunch) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkenPalette.NavyCard,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkenPalette.TextPrimary,
                )
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
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
                        modifier = Modifier.padding(vertical = 10.dp),
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
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
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
                        Text(
                            text = stringResource(R.string.settings_cancel),
                            color = DarkenPalette.TextMuted,
                        )
                    }
                    TextButton(onClick = { onConfirm(draftLanguage, draftAutoStart) }) {
                        Text(stringResource(R.string.settings_ok), color = DarkenPalette.Gold)
                    }
                }
            }
        }
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
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.padding(end = 0.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = DarkenPalette.Gold,
                unselectedColor = DarkenPalette.TextMuted,
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkenPalette.TextPrimary,
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
            .padding(vertical = 6.dp),
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
