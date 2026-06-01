package cz.darken.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cz.darken.app.overlay.OverlayTint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "darken_prefs")

class PreferencesRepository(private val context: Context) {

    val defaultDimLevel: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_DIM]?.coerceIn(MIN_DIM, MAX_DIM) ?: FALLBACK_DEFAULT
    }

    val hasSavedDefaultDim: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs.contains(KEY_DEFAULT_DIM)
    }

    suspend fun setDefaultDimLevel(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_DIM] = level.coerceIn(MIN_DIM, MAX_DIM)
        }
    }

    suspend fun resolveDefaultDimLevel(): Int = defaultDimLevel.first()

    fun defaultDimLevelBlocking(): Int = runBlocking { resolveDefaultDimLevel() }

    val overlayTintPreset: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_OVERLAY_TINT_PRESET] ?: OverlayTint.PRESET_GRAY
    }

    val customOverlayTintArgb: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_OVERLAY_TINT] ?: OverlayTint.AmberArgb
    }

    val resolvedOverlayTintArgb: Flow<Int> = context.dataStore.data.map { prefs ->
        resolveOverlayTintArgb(
            prefs[KEY_OVERLAY_TINT_PRESET] ?: OverlayTint.PRESET_GRAY,
            prefs[KEY_CUSTOM_OVERLAY_TINT] ?: OverlayTint.AmberArgb,
        )
    }

    suspend fun setOverlayTintPreset(preset: String) {
        val normalized = when (preset) {
            OverlayTint.PRESET_AMBER, OverlayTint.PRESET_RED, OverlayTint.PRESET_CUSTOM -> preset
            else -> OverlayTint.PRESET_GRAY
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_OVERLAY_TINT_PRESET] = normalized
        }
    }

    suspend fun setCustomOverlayTintArgb(argb: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_OVERLAY_TINT] = argb
            prefs[KEY_OVERLAY_TINT_PRESET] = OverlayTint.PRESET_CUSTOM
        }
    }

    suspend fun setOverlayTint(preset: String, customArgb: Int? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OVERLAY_TINT_PRESET] = when (preset) {
                OverlayTint.PRESET_AMBER, OverlayTint.PRESET_RED, OverlayTint.PRESET_CUSTOM -> preset
                else -> OverlayTint.PRESET_GRAY
            }
            if (customArgb != null) {
                prefs[KEY_CUSTOM_OVERLAY_TINT] = customArgb
            }
        }
    }

    fun resolveOverlayTintArgbBlocking(): Int = runBlocking {
        val prefs = context.dataStore.data.first()
        resolveOverlayTintArgb(
            prefs[KEY_OVERLAY_TINT_PRESET] ?: OverlayTint.PRESET_GRAY,
            prefs[KEY_CUSTOM_OVERLAY_TINT] ?: OverlayTint.AmberArgb,
        )
    }

    private fun resolveOverlayTintArgb(preset: String, customArgb: Int): Int =
        OverlayTint.resolveArgb(preset, customArgb)

    val appLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: LANG_SYSTEM
    }

    suspend fun setAppLanguage(language: String) {
        val normalized = when (language) {
            LANG_CS, LANG_EN -> language
            else -> LANG_SYSTEM
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = normalized
        }
    }

    val notificationMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATION_MODE] ?: NOTIF_MINIMAL
    }

    suspend fun setNotificationMode(mode: String) {
        val normalized = when (mode) {
            NOTIF_INTERACTIVE -> NOTIF_INTERACTIVE
            else -> NOTIF_MINIMAL
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATION_MODE] = normalized
        }
    }

    fun notificationModeBlocking(): String = runBlocking {
        notificationMode.first()
    }

    val autoStartOnLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_START] ?: false
    }

    suspend fun setAutoStartOnLaunch(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_START] = enabled
        }
    }

    suspend fun autoStartOnLaunchEnabled(): Boolean = autoStartOnLaunch.first()

    val privacyAcknowledged: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRIVACY_ACK] ?: false
    }

    suspend fun setPrivacyAcknowledged(acknowledged: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRIVACY_ACK] = acknowledged
        }
    }

    companion object {
        const val MIN_DIM = 0
        const val MAX_DIM = 99
        const val FALLBACK_DEFAULT = 50

        const val LANG_SYSTEM = "system"
        const val LANG_CS = "cs"
        const val LANG_EN = "en"

        const val NOTIF_MINIMAL = "minimal"
        const val NOTIF_INTERACTIVE = "interactive"

        private val KEY_DEFAULT_DIM = intPreferencesKey("default_dim_level")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_NOTIFICATION_MODE = stringPreferencesKey("notification_mode")
        private val KEY_AUTO_START = booleanPreferencesKey("auto_start_on_launch")
        private val KEY_OVERLAY_TINT_PRESET = stringPreferencesKey("overlay_tint_preset")
        private val KEY_CUSTOM_OVERLAY_TINT = intPreferencesKey("custom_overlay_tint_argb")
        private val KEY_PRIVACY_ACK = booleanPreferencesKey("privacy_acknowledged")
    }
}
