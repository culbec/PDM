package ubb.pdm.gamestop.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ubb.pdm.gamestop.MainActivity

class MyNotifications {
    companion object {
        private const val CHANNEL_NAME = "GameStop"
        private const val CHANNEL_DESC = "GameStop notifications"
        private const val IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT

        fun createNotificationChannel(
            channelId: String,
            context: Context
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, CHANNEL_NAME, IMPORTANCE).apply {
                    description = CHANNEL_DESC
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun showSimpleNotification(
            context: Context,
            channelId: String,
            notificationId: Int,
            textTitle: String,
            textContent: String,
            priority: Int = NotificationCompat.PRIORITY_DEFAULT
        ) {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(priority)
            with(NotificationManagerCompat.from(context)) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notify(notificationId, builder.build())
                } else {
                    ActivityCompat.requestPermissions(context as MainActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                }
            }
        }
    }
}