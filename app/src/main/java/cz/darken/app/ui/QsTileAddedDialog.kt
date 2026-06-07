package cz.darken.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cz.darken.app.R

@Composable
fun QsTileAddedDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkenPalette.NavyCard,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.settings_qs_tile_added_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkenPalette.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.settings_qs_tile_added_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkenPalette.TextMuted,
                    modifier = Modifier.padding(top = 12.dp),
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.settings_ok), color = DarkenPalette.Gold)
                }
            }
        }
    }
}
