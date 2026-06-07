package cz.darken.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import cz.darken.app.R
import cz.darken.app.overlay.OverlayLauncher
import kotlin.math.min

class DarkenAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        updateWidget(context, appWidgetManager, appWidgetId, newOptions)
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

        private const val ICON_MIN_DP = 20
        private const val ICON_MAX_DP = 64
        /** Icon size as a fraction of the inner widget frame (scales when resized). */
        private const val ICON_CELL_RATIO = 0.96f

        fun refreshAll(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val component = ComponentName(appContext, DarkenAppWidgetProvider::class.java)
            val widgetIds = manager.getAppWidgetIds(component)
            if (widgetIds.isEmpty()) return

            for (widgetId in widgetIds) {
                updateWidget(appContext, manager, widgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            options: Bundle? = null,
        ) {
            val resolvedOptions = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildRemoteViews(context, appWidgetId, resolvedOptions),
            )
        }

        private fun buildRemoteViews(
            context: Context,
            appWidgetId: Int,
            options: Bundle,
        ): RemoteViews {
            val active = OverlayLauncher.isOverlayActive(context)
            val views = RemoteViews(context.packageName, R.layout.widget_darken)
            views.setInt(
                android.R.id.background,
                "setBackgroundResource",
                if (active) R.drawable.widget_background_active else R.drawable.widget_background_inactive,
            )
            val iconSizeDp = iconSizeDpForOptions(context, options)
            views.setViewLayoutWidth(
                R.id.widget_icon,
                iconSizeDp.toFloat(),
                TypedValue.COMPLEX_UNIT_DIP,
            )
            views.setViewLayoutHeight(
                R.id.widget_icon,
                iconSizeDp.toFloat(),
                TypedValue.COMPLEX_UNIT_DIP,
            )
            views.setOnClickPendingIntent(R.id.widget_host, togglePendingIntent(context, appWidgetId))
            return views
        }

        private fun iconSizeDpForOptions(context: Context, options: Bundle): Int {
            val cellDp = widgetCellDp(context, options)
            val frameInsetDp = context.resources.getDimension(R.dimen.widget_cell_inset) /
                context.resources.displayMetrics.density
            val innerPadDp = context.resources.getDimension(R.dimen.widget_inner_padding) /
                context.resources.displayMetrics.density
            val innerFrameDp = (cellDp - 2f * frameInsetDp - 2f * innerPadDp).coerceAtLeast(16f)
            return (innerFrameDp * ICON_CELL_RATIO).toInt().coerceIn(ICON_MIN_DP, ICON_MAX_DP)
        }

        private fun widgetCellDp(context: Context, options: Bundle): Float {
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            return when {
                minWidth > 0 && minHeight > 0 -> min(minWidth, minHeight).toFloat()
                minWidth > 0 -> minWidth.toFloat()
                minHeight > 0 -> minHeight.toFloat()
                else -> fallbackIconCellDp(context).toFloat()
            }
        }

        private fun fallbackIconCellDp(context: Context): Int {
            val minCellPx = context.resources.getDimensionPixelSize(R.dimen.widget_min_cell)
            return (minCellPx / context.resources.displayMetrics.density).toInt()
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
