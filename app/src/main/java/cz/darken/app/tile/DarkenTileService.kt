package cz.darken.app.tile

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import cz.darken.app.R
import cz.darken.app.overlay.OverlayLauncher
import java.lang.ref.WeakReference

class DarkenTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        listeningInstance = WeakReference(this)
        updateTileState()
    }

    override fun onStopListening() {
        if (listeningInstance?.get() === this) {
            listeningInstance = null
        }
        super.onStopListening()
    }

    override fun onClick() {
        when (OverlayLauncher.toggle(this)) {
            OverlayLauncher.ToggleResult.Started,
            OverlayLauncher.ToggleResult.Stopped,
            -> Unit
            OverlayLauncher.ToggleResult.NeedPermissions -> updateTileState()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val active = OverlayLauncher.isOverlayActive(this)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = getString(
            if (active) R.string.qs_tile_subtitle_on else R.string.qs_tile_subtitle_off,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tile.stateDescription = tile.subtitle
        }
        tile.updateTile()
    }

    companion object {
        private var listeningInstance: WeakReference<DarkenTileService>? = null

        /**
         * Updates the tile immediately when Quick Settings is open. [requestListeningState]
         * alone does not re-run [onStartListening] while the panel stays open.
         */
        fun refreshIfListening(context: Context) {
            val listening = listeningInstance?.get()
            if (listening != null) {
                listening.updateTileState()
            } else {
                requestTileUpdate(context)
            }
        }

        fun requestTileUpdate(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
            val component = ComponentName(context, DarkenTileService::class.java)
            requestListeningState(context, component)
        }
    }
}
