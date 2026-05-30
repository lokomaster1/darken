package cz.darken.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService

object PermissionIntents {

    fun canDrawOverlays(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService<PowerManager>() ?: return true
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Opens overlay permission for this app, or app details as fallback
     * (some OEMs hide the app until [SYSTEM_ALERT_WINDOW] is declared in the manifest).
     */
    fun overlaySettings(context: Context): Intent {
        val packageUri = Uri.parse("package:${context.packageName}")
        val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri)
        if (overlayIntent.resolveActivity(context.packageManager) != null) {
            return overlayIntent
        }
        return appDetailsSettings(context)
    }

    fun appDetailsSettings(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }

    fun requestIgnoreBatteryOptimizations(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }

    fun batteryOptimizationSettings(context: Context): Intent {
        val request = requestIgnoreBatteryOptimizations(context)
        if (request.resolveActivity(context.packageManager) != null) {
            return request
        }
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    }
}
