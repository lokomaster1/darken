package cz.darken.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object WidgetProviderRefresh {

    /**
     * Re-registers the widget provider with the system. Needed after some app updates when
     * launchers keep a stale widget list until reinstall.
     *
     * Safe when no widgets are on the home screen (toggle trick). When widgets exist, only
     * refreshes their views — does not remove them.
     */
    fun afterAppUpdate(context: Context) {
        val appContext = context.applicationContext
        val component = ComponentName(appContext, DarkenAppWidgetProvider::class.java)
        val manager = AppWidgetManager.getInstance(appContext)
        val widgetIds = manager.getAppWidgetIds(component)

        if (widgetIds.isNotEmpty()) {
            DarkenAppWidgetProvider.refreshAll(appContext)
            return
        }

        val packageManager = appContext.packageManager
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}
