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
    const val SPEEDOMETER_CHANNEL_ID = "speedometer_channel"
    const val NOTIFICATION_ID = 1001
    const val SPEEDOMETER_NOTIFICATION_ID = 1002

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val trackingChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.tracking_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        manager?.createNotificationChannel(trackingChannel)

        val speedometerChannel = NotificationChannel(
            SPEEDOMETER_CHANNEL_ID,
            context.getString(R.string.speedometer_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        manager?.createNotificationChannel(speedometerChannel)
    }

    fun buildNotification(
        context: Context,
        destinationName: String,
        formattedDistance: String,
        sticky: Boolean
    ): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                putExtra("NAVIGATE_TO", "tracking")
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            context, 0,
            Intent(context, TrackingService::class.java).setAction(TrackingService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(destinationName)
            .setContentText(formattedDistance)
            .setContentIntent(contentIntent)
            .addAction(0, context.getString(R.string.stop_tracking), stopIntent)
            .setOngoing(sticky)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun buildSpeedometerNotification(
        context: Context,
        speedText: String,
        distanceText: String,
        elapsedText: String,
        isPaused: Boolean
    ): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            context, 1,
            Intent(context, MainActivity::class.java).apply {
                putExtra("NAVIGATE_TO", "speedometer")
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseResumeAction = if (isPaused) {
            sabuj.m.truedistance.service.SpeedometerService.ACTION_RESUME
        } else {
            sabuj.m.truedistance.service.SpeedometerService.ACTION_PAUSE
        }
        val pauseResumeTitle = if (isPaused) {
            context.getString(R.string.action_resume)
        } else {
            context.getString(R.string.action_pause)
        }

        val pauseResumeIntent = PendingIntent.getService(
            context, 2,
            Intent(context, sabuj.m.truedistance.service.SpeedometerService::class.java).setAction(pauseResumeAction),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            context, 3,
            Intent(context, sabuj.m.truedistance.service.SpeedometerService::class.java).setAction(sabuj.m.truedistance.service.SpeedometerService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isPaused) {
            context.getString(R.string.speedometer_notification_paused)
        } else {
            context.getString(R.string.speedometer_notification_title)
        }

        val contentText = "$speedText  •  $distanceText  •  $elapsedText"

        return NotificationCompat.Builder(context, SPEEDOMETER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .addAction(0, pauseResumeTitle, pauseResumeIntent)
            .addAction(0, context.getString(R.string.action_stop), stopIntent)
            .setOngoing(!isPaused)
            .setOnlyAlertOnce(true)
            .build()
    }
}
