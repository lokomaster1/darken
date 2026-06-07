package cz.darken.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Runs on [Intent.ACTION_MY_PACKAGE_REPLACED] after an app update.
 */
class WidgetProviderRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        WidgetProviderRefresh.afterAppUpdate(context.applicationContext)
    }
}
