package cz.darken.app.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import cz.darken.app.data.PreferencesRepository

object LocaleHelper {

    fun apply(languageTag: String) {
        val locales = when (languageTag) {
            PreferencesRepository.LANG_CS -> LocaleListCompat.forLanguageTags("cs")
            PreferencesRepository.LANG_EN -> LocaleListCompat.forLanguageTags("en")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
