package cz.darken.app.overlay

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import cz.darken.app.MainActivity
import cz.darken.app.R
import cz.darken.app.data.PreferencesRepository

object OverlayNotificationFactory {

    enum class HighlightPhase {
        NONE,
        PRESSED,
        FADING,
    }

    fun build(
        context: Context,
        mode: String,
        currentDimLevel: Int,
        defaultDimLevel: Int,
        pressedCellId: Int? = null,
        highlightPhase: HighlightPhase = HighlightPhase.NONE,
    ): NotificationCompat.Builder {
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return if (mode == PreferencesRepository.NOTIF_INTERACTIVE) {
            buildInteractive(
                context,
                openApp,
                currentDimLevel,
                pressedCellId,
                highlightPhase,
            )
        } else {
            buildMinimal(context, openApp)
        }
    }

    private fun buildMinimal(
        context: Context,
        openApp: PendingIntent,
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(context, OverlayService.CHANNEL_MINIMAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

    private fun buildInteractive(
        context: Context,
        openApp: PendingIntent,
        currentDimLevel: Int,
        pressedCellId: Int?,
        highlightPhase: HighlightPhase,
    ): NotificationCompat.Builder {
        val collapsedViews = buildInteractiveRemoteViews(
            context,
            R.layout.notification_interactive_collapsed,
            currentDimLevel,
            pressedCellId,
            highlightPhase,
        )
        val expandedViews = buildInteractiveRemoteViews(
            context,
            R.layout.notification_interactive,
            currentDimLevel,
            pressedCellId,
            highlightPhase,
        )

        return NotificationCompat.Builder(context, OverlayService.CHANNEL_INTERACTIVE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text_level, currentDimLevel))
            .setContentIntent(openApp)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsedViews)
            .setCustomBigContentView(expandedViews)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
    }

    private fun buildInteractiveRemoteViews(
        context: Context,
        layoutId: Int,
        currentDimLevel: Int,
        pressedCellId: Int?,
        highlightPhase: HighlightPhase,
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, layoutId)
        remoteViews.setTextViewText(R.id.display_level, "$currentDimLevel%")

        val actionCells = listOf(
            R.id.cell_minus5,
            R.id.cell_minus1,
            R.id.cell_stop,
            R.id.cell_plus1,
            R.id.cell_plus5,
        )
        val pressedDrawable = when (highlightPhase) {
            HighlightPhase.PRESSED -> R.drawable.notification_action_pressed
            HighlightPhase.FADING -> R.drawable.notification_action_pressed_fade
            HighlightPhase.NONE -> null
        }
        for (cellId in actionCells) {
            val background = if (cellId == pressedCellId && pressedDrawable != null) {
                pressedDrawable
            } else {
                R.drawable.notification_action_bg
            }
            remoteViews.setInt(cellId, "setBackgroundResource", background)
        }

        bindAction(remoteViews, context, R.id.cell_minus5, OverlayService.ACTION_ADJUST, -10, 10)
        bindAction(remoteViews, context, R.id.cell_minus1, OverlayService.ACTION_ADJUST, -2, 11)
        bindAction(remoteViews, context, R.id.cell_stop, OverlayService.ACTION_STOP, 0, 13)
        bindAction(remoteViews, context, R.id.cell_plus1, OverlayService.ACTION_ADJUST, 2, 14)
        bindAction(remoteViews, context, R.id.cell_plus5, OverlayService.ACTION_ADJUST, 10, 15)

        return remoteViews
    }

    private fun bindAction(
        remoteViews: RemoteViews,
        context: Context,
        cellId: Int,
        action: String,
        delta: Int,
        requestCode: Int,
    ) {
        val intent = Intent(context, OverlayService::class.java).apply {
            this.action = action
            putExtra(OverlayService.EXTRA_PRESSED_CELL, cellId)
            if (action == OverlayService.ACTION_ADJUST) {
                putExtra(OverlayService.EXTRA_DELTA, delta)
            }
        }
        val pending = PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        remoteViews.setOnClickPendingIntent(cellId, pending)
    }
}
