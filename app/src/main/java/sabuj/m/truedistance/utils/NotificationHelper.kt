package sabuj.m.truedistance.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import sabuj.m.truedistance.MainActivity
import sabuj.m.truedistance.R
import sabuj.m.truedistance.service.TrackingService

/** §6.1.4 / §14.1 — persistent tracking notification with live distance. */
object NotificationHelper {
    const val CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.tracking_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        destinationName: String,
        formattedDistance: String,
        sticky: Boolean
    ): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            context, 0,
            Intent(context, TrackingService::class.java).setAction(TrackingService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_map_pin)
            .setContentTitle(destinationName)
            .setContentText(formattedDistance)
            .setContentIntent(contentIntent)
            .addAction(0, context.getString(R.string.stop_tracking), stopIntent)
            .setOngoing(sticky) // §14.1 — sticky by default, configurable via Settings (V3)
            .setOnlyAlertOnce(true)
            .build()
    }
}
