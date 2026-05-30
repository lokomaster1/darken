package cz.darken.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import cz.darken.app.MainActivity
import cz.darken.app.MainActivityHolder
import cz.darken.app.R
import cz.darken.app.data.PreferencesRepository

class OverlayService : Service() {

    private lateinit var preferences: PreferencesRepository
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentDimLevel: Int = PreferencesRepository.FALLBACK_DEFAULT

    override fun onCreate() {
        super.onCreate()
        preferences = PreferencesRepository(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                tearDown()
                return START_NOT_STICKY
            }
            ACTION_EXIT_APP -> {
                tearDown()
                MainActivityHolder.requestExitApp()
                return START_NOT_STICKY
            }
            ACTION_REFRESH_NOTIFICATION -> {
                refreshNotification()
                return START_STICKY
            }
            ACTION_UPDATE -> {
                val level = intent.getIntExtra(EXTRA_DIM_LEVEL, preferences.defaultDimLevelBlocking())
                if (overlayView == null) {
                    showOverlay(level)
                    startForeground(NOTIFICATION_ID, buildNotification())
                } else {
                    setDimLevel(level)
                }
                return START_STICKY
            }
            ACTION_ADJUST -> {
                if (overlayView == null) return START_STICKY
                val delta = intent.getIntExtra(EXTRA_DELTA, 0)
                setDimLevel(currentDimLevel + delta)
                return START_STICKY
            }
            ACTION_APPLY_DEFAULT -> {
                if (overlayView == null) return START_STICKY
                setDimLevel(preferences.defaultDimLevelBlocking())
                return START_STICKY
            }
            else -> {
                val level = intent?.getIntExtra(EXTRA_DIM_LEVEL, preferences.defaultDimLevelBlocking())
                    ?: preferences.defaultDimLevelBlocking()
                ensureOverlay(level)
                startForeground(NOTIFICATION_ID, buildNotification())
                return START_STICKY
            }
        }
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private fun tearDown() {
        removeOverlay()
        lastKnownDimLevel = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun ensureOverlay(dimLevel: Int) {
        if (overlayView == null) {
            showOverlay(dimLevel)
        } else {
            setDimLevel(dimLevel)
        }
    }

    private fun setDimLevel(dimLevel: Int) {
        currentDimLevel = dimLevel.coerceIn(PreferencesRepository.MIN_DIM, PreferencesRepository.MAX_DIM)
        lastKnownDimLevel = currentDimLevel
        applyDimLevel(currentDimLevel)
        refreshNotification()
    }

    private fun showOverlay(dimLevel: Int) {
        setDimLevel(dimLevel)

        val wm = getSystemService<WindowManager>() ?: return
        windowManager = wm

        val view = View(this).apply {
            setBackgroundColor(0xFF000000.toInt())
            alpha = currentDimLevel / PreferencesRepository.MAX_DIM.toFloat()
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        wm.addView(view, params)
        overlayView = view
    }

    private fun applyDimLevel(dimLevel: Int) {
        overlayView?.alpha = dimLevel / PreferencesRepository.MAX_DIM.toFloat()
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            windowManager?.removeView(view)
        }
        overlayView = null
        windowManager = null
    }

    private fun refreshNotification() {
        if (overlayView == null) return
        val manager = getSystemService<NotificationManager>() ?: return
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        createChannelsIfNeeded()
        val mode = preferences.notificationModeBlocking()
        val defaultLevel = preferences.defaultDimLevelBlocking()
        return OverlayNotificationFactory
            .build(this, mode, currentDimLevel, defaultLevel)
            .build()
    }

    private fun createChannelsIfNeeded() {
        val manager = getSystemService<NotificationManager>() ?: return

        val minimal = NotificationChannel(
            CHANNEL_MINIMAL,
            getString(R.string.notification_channel_minimal_name),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = getString(R.string.notification_channel_minimal_description)
            setShowBadge(false)
        }

        val interactive = NotificationChannel(
            CHANNEL_INTERACTIVE,
            getString(R.string.notification_channel_interactive_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_interactive_description)
            setShowBadge(false)
        }

        manager.createNotificationChannel(minimal)
        manager.createNotificationChannel(interactive)
    }

    companion object {
        @Volatile
        var lastKnownDimLevel: Int? = null

        const val CHANNEL_MINIMAL = "darken_overlay_minimal"
        const val CHANNEL_INTERACTIVE = "darken_overlay_interactive"
        private const val NOTIFICATION_ID = 1

        const val ACTION_STOP = "cz.darken.app.overlay.STOP"
        const val ACTION_UPDATE = "cz.darken.app.overlay.UPDATE"
        const val ACTION_ADJUST = "cz.darken.app.overlay.ADJUST"
        const val ACTION_APPLY_DEFAULT = "cz.darken.app.overlay.APPLY_DEFAULT"
        const val ACTION_EXIT_APP = "cz.darken.app.overlay.EXIT_APP"
        const val ACTION_REFRESH_NOTIFICATION = "cz.darken.app.overlay.REFRESH_NOTIFICATION"
        const val EXTRA_DIM_LEVEL = "dim_level"
        const val EXTRA_DELTA = "delta"

        fun start(context: Context, dimLevel: Int) {
            val intent = Intent(context, OverlayService::class.java).apply {
                putExtra(EXTRA_DIM_LEVEL, dimLevel)
            }
            context.startForegroundService(intent)
        }

        fun update(context: Context, dimLevel: Int) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_DIM_LEVEL, dimLevel)
            }
            context.startService(intent)
        }

        fun refreshNotification(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_REFRESH_NOTIFICATION
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
