package cz.darken.app.overlay

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import cz.darken.app.MainActivity
import cz.darken.app.data.PreferencesRepository
import cz.darken.app.util.PermissionIntents

object OverlayLauncher {

    enum class ToggleResult {
        Started,
        Stopped,
        NeedPermissions,
    }

    fun isOverlayRunning(context: Context): Boolean {
        val manager = context.getSystemService(ActivityManager::class.java) ?: return false
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE).any { info ->
            info.service.className == OverlayService::class.java.name
        }
    }

    /** True when overlay service is running and dimming is active (not mid-shutdown). */
    fun isOverlayActive(context: Context): Boolean =
        isOverlayRunning(context) && OverlayService.lastKnownDimLevel != null

    fun canStart(context: Context): Boolean =
        PermissionIntents.canDrawOverlays(context) && notificationsGranted(context)

    fun start(context: Context, dimLevel: Int? = null) {
        val preferences = PreferencesRepository(context.applicationContext)
        val level = (dimLevel ?: preferences.defaultDimLevelBlocking())
            .coerceIn(PreferencesRepository.MIN_DIM, PreferencesRepository.MAX_DIM)
        OverlayService.start(context, level)
    }

    fun stop(context: Context) {
        OverlayService.stop(context)
    }

    fun toggle(context: Context): ToggleResult {
        if (isOverlayRunning(context)) {
            stop(context)
            return ToggleResult.Stopped
        }
        if (!canStart(context)) {
            openMainActivityForPermissions(context)
            return ToggleResult.NeedPermissions
        }
        start(context)
        return ToggleResult.Started
    }

    fun openMainActivityForPermissions(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_PERMISSIONS, true)
        }
        context.startActivity(intent)
    }

    fun notificationsGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
