package cz.darken.app.tile

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import cz.darken.app.R

object QsTilePlacement {

    enum class Result {
        Added,
        AlreadyAdded,
        NotAdded,
        Unsupported,
        Error,
    }

    fun requestAddTile(context: Context, onResult: (Result) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onResult(Result.Unsupported)
            return
        }
        requestAddTileApi33(context, onResult)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestAddTileApi33(context: Context, onResult: (Result) -> Unit) {
        val statusBarManager = ContextCompat.getSystemService(context, StatusBarManager::class.java)
        if (statusBarManager == null) {
            onResult(Result.Error)
            return
        }

        val component = ComponentName(context, DarkenTileService::class.java)
        val label = context.getString(R.string.qs_tile_label)
        val icon = Icon.createWithResource(context, R.drawable.ic_notification)

        statusBarManager.requestAddTileService(
            component,
            label,
            icon,
            context.mainExecutor,
        ) { code ->
            onResult(mapResultCode(code))
        }
    }

    private fun mapResultCode(code: Int): Result = when (code) {
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> Result.Added
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> Result.AlreadyAdded
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> Result.NotAdded
        else -> Result.Error
    }
}
