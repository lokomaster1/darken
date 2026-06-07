package cz.darken.app

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import cz.darken.app.data.PreferencesRepository
import cz.darken.app.locale.LocaleHelper
import cz.darken.app.widget.WidgetProviderRefresh
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DarkenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val preferences = PreferencesRepository(this)
        runBlocking {
            LocaleHelper.apply(preferences.appLanguage.first())
        }
        refreshWidgetProviderAfterUpdateIfNeeded()
    }

    /**
     * Fallback when [android.content.Intent.ACTION_MY_PACKAGE_REPLACED] is missed or the launcher
     * starts before the provider is re-registered. Opening the app after an update fixes the picker.
     */
    private fun refreshWidgetProviderAfterUpdateIfNeeded() {
        val prefs = getSharedPreferences(PREFS_INTERNAL, MODE_PRIVATE)
        val lastVersionCode = prefs.getInt(KEY_LAST_VERSION_CODE, -1)
        val currentVersionCode = PackageInfoCompat.getLongVersionCode(
            packageManager.getPackageInfo(packageName, 0),
        ).toInt()

        if (lastVersionCode >= 0 && lastVersionCode != currentVersionCode) {
            WidgetProviderRefresh.afterAppUpdate(this)
        }

        if (lastVersionCode != currentVersionCode) {
            prefs.edit().putInt(KEY_LAST_VERSION_CODE, currentVersionCode).apply()
        }
    }

    companion object {
        private const val PREFS_INTERNAL = "darken_internal"
        private const val KEY_LAST_VERSION_CODE = "last_version_code"
    }
}
