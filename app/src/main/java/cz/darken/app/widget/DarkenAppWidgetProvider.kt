package cz.darken.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import cz.darken.app.R
import cz.darken.app.overlay.OverlayLauncher

class DarkenAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildRemoteViews(context, appWidgetId),
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TOGGLE) {
            val appContext = context.applicationContext
            when (OverlayLauncher.toggle(appContext)) {
                OverlayLauncher.ToggleResult.NeedPermissions -> refreshAll(appContext)
                OverlayLauncher.ToggleResult.Started,
                OverlayLauncher.ToggleResult.Stopped,
                -> Unit
            }
            return
        }
        super.onReceive(context, intent)
    }

    companion object {
        const val ACTION_TOGGLE = "cz.darken.app.widget.TOGGLE"

        fun refreshAll(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val component = ComponentName(appContext, DarkenAppWidgetProvider::class.java)
            val widgetIds = manager.getAppWidgetIds(component)
            if (widgetIds.isEmpty()) return

            for (widgetId in widgetIds) {
                manager.updateAppWidget(widgetId, buildRemoteViews(appContext, widgetId))
            }
        }

        private fun buildRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
            val active = OverlayLauncher.isOverlayActive(context)
            val views = RemoteViews(context.packageName, R.layout.widget_darken)
            views.setInt(
                android.R.id.background,
                "setBackgroundResource",
                if (active) R.drawable.widget_background_active else R.drawable.widget_background_inactive,
            )
            views.setOnClickPendingIntent(R.id.widget_host, togglePendingIntent(context, appWidgetId))
            return views
        }

        private fun togglePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, DarkenAppWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE
            }
            return PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
