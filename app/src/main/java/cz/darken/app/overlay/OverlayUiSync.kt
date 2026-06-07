package cz.darken.app.overlay

import android.content.Context
import cz.darken.app.tile.DarkenTileService
import cz.darken.app.widget.DarkenAppWidgetProvider

internal object OverlayUiSync {

    fun notifyOverlayStateChanged(context: Context) {
        val appContext = context.applicationContext
        DarkenTileService.refreshIfListening(appContext)
        DarkenAppWidgetProvider.refreshAll(appContext)
    }
}
