package cz.darken.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SizeF
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.os.BundleCompat
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

        // Launchers report available dimensions, not the widget's current grid span.
        private const val COMPACT_WIDTH_LIMIT_DP = 130f
        private const val COMPACT_HEIGHT_LIMIT_DP = 115f
        private const val MAX_REMOTE_VIEWS_SIZES = 16
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
            val sizes = BundleCompat.getParcelableArrayList(
                resolvedOptions,
                AppWidgetManager.OPTION_APPWIDGET_SIZES,
                SizeF::class.java,
            ).orEmpty().filter { it.width > 0f && it.height > 0f }.distinct()
            val active = OverlayLauncher.isOverlayActive(context)
            val toggle = togglePendingIntent(context, appWidgetId)
            val views = if (sizes.size in 1..MAX_REMOTE_VIEWS_SIZES) {
                RemoteViews(sizes.associateWith { buildRemoteViews(context, it, active, toggle) })
            } else {
                buildRemoteViews(context, fallbackSize(context, resolvedOptions), active, toggle)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun buildRemoteViews(
            context: Context,
            size: SizeF,
            active: Boolean,
            toggle: PendingIntent,
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_darken)
            views.setInt(
                android.R.id.background,
                "setBackgroundResource",
                if (active) R.drawable.widget_background_active else R.drawable.widget_background_inactive,
            )
            val density = context.resources.displayMetrics.density
            val frameInsetDp = context.resources.getDimension(R.dimen.widget_cell_inset) / density
            val innerPadDp = context.resources.getDimension(R.dimen.widget_inner_padding) / density
            val frameSide = (min(size.width, size.height) - 2f * frameInsetDp).coerceAtLeast(1f)
            if (size.width < COMPACT_WIDTH_LIMIT_DP && size.height < COMPACT_HEIGHT_LIMIT_DP) {
                views.setViewLayoutWidth(android.R.id.background, frameSide, TypedValue.COMPLEX_UNIT_DIP)
                views.setViewLayoutHeight(android.R.id.background, frameSide, TypedValue.COMPLEX_UNIT_DIP)
            }
            val iconSizeDp = ((frameSide - 2f * innerPadDp) * ICON_CELL_RATIO)
                .coerceIn(1f, ICON_MAX_DP.toFloat())
            views.setViewLayoutWidth(
                R.id.widget_icon,
                iconSizeDp,
                TypedValue.COMPLEX_UNIT_DIP,
            )
            views.setViewLayoutHeight(
                R.id.widget_icon,
                iconSizeDp,
                TypedValue.COMPLEX_UNIT_DIP,
            )
            views.setOnClickPendingIntent(R.id.widget_host, toggle)
            return views
        }

        private fun fallbackSize(context: Context, options: Bundle): SizeF {
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 }
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT).takeIf { it > 0 }
            val minCellDp = context.resources.getDimension(R.dimen.widget_min_cell) /
                context.resources.displayMetrics.density
            return SizeF(
                (minWidth ?: minHeight)?.toFloat() ?: minCellDp,
                (minHeight ?: minWidth)?.toFloat() ?: minCellDp,
            )
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
