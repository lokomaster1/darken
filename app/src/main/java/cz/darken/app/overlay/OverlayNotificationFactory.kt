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

    fun build(
        context: Context,
        mode: String,
        currentDimLevel: Int,
        defaultDimLevel: Int,
    ): NotificationCompat.Builder {
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return if (mode == PreferencesRepository.NOTIF_INTERACTIVE) {
            buildInteractive(context, openApp, currentDimLevel, defaultDimLevel)
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
        defaultDimLevel: Int,
    ): NotificationCompat.Builder {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_interactive)
        remoteViews.setTextViewText(R.id.action_default, "$defaultDimLevel%")

        bindAction(remoteViews, context, R.id.action_minus5, OverlayService.ACTION_ADJUST, -5, 10)
        bindAction(remoteViews, context, R.id.action_minus1, OverlayService.ACTION_ADJUST, -1, 11)
        bindAction(remoteViews, context, R.id.action_default, OverlayService.ACTION_APPLY_DEFAULT, 0, 12)
        bindAction(remoteViews, context, R.id.action_stop, OverlayService.ACTION_STOP, 0, 13)
        bindAction(remoteViews, context, R.id.action_plus1, OverlayService.ACTION_ADJUST, 1, 14)
        bindAction(remoteViews, context, R.id.action_plus5, OverlayService.ACTION_ADJUST, 5, 15)

        return NotificationCompat.Builder(context, OverlayService.CHANNEL_INTERACTIVE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text_level, currentDimLevel))
            .setContentIntent(openApp)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
    }

    private fun bindAction(
        remoteViews: RemoteViews,
        context: Context,
        viewId: Int,
        action: String,
        delta: Int,
        requestCode: Int,
    ) {
        val intent = Intent(context, OverlayService::class.java).apply {
            this.action = action
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
        remoteViews.setOnClickPendingIntent(viewId, pending)
    }
}
