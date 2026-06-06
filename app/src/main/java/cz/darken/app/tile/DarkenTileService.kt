package cz.darken.app.tile

import android.content.ComponentName
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import cz.darken.app.overlay.OverlayLauncher

class DarkenTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        when (OverlayLauncher.toggle(this)) {
            OverlayLauncher.ToggleResult.Started,
            OverlayLauncher.ToggleResult.Stopped,
            -> updateTileState()
            OverlayLauncher.ToggleResult.NeedPermissions -> Unit
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val active = OverlayLauncher.isOverlayRunning(this)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    companion object {
        fun requestTileUpdate(context: android.content.Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
            val component = ComponentName(context, DarkenTileService::class.java)
            requestListeningState(context, component)
        }
    }
}
