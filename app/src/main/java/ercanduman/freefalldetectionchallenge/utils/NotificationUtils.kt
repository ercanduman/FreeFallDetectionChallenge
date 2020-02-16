package ercanduman.freefalldetectionchallenge.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ercanduman.freefalldetectionchallenge.NOTIFICATION_CHANNEL_ID
import ercanduman.freefalldetectionchallenge.R
import ercanduman.freefalldetectionchallenge.ui.MainActivity

fun createNotification(context: Context, contentText: String): Notification? {
    context.logd("createNotification() - called.")

    // If notification clicked then start MainActivity via pendingInten
    val notificationIntent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Challenge App")
        .setContentText(contentText)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentIntent(pendingIntent)
        .build()
}