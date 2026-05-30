package cz.darken.app

import android.app.Application
import cz.darken.app.data.PreferencesRepository
import cz.darken.app.locale.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DarkenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val preferences = PreferencesRepository(this)
        runBlocking {
            LocaleHelper.apply(preferences.appLanguage.first())
        }
    }
}
