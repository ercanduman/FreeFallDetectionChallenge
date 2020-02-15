package ercanduman.freefalldetectionchallenge.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ercanduman.freefalldetectionchallenge.NOTIFICATION_CHANNEL_ID
import ercanduman.freefalldetectionchallenge.NOTIFICATION_ID
import ercanduman.freefalldetectionchallenge.R
import ercanduman.freefalldetectionchallenge.ui.MainActivity
import ercanduman.freefalldetectionchallenge.utils.logd

class ForegroundService : Service(), SensorEventListener {
    private lateinit var notification: Notification

    // called first time service created, called only once
    override fun onCreate() {
        logd("onCreate() - called.")
        super.onCreate()
        createNotification()
    }

    private fun createNotification() {
        logd("createNotification() - called.")

        // If notification clicked then start MainActivity via pendingInten
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Challenge App")
            .setContentText("Sensors listening...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * Triggered every time service started.
     * All codes written here runs on main thread,
     * so if need any heavy operation do it on the background thread
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logd("onStartCommand() - called.")
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        logd("onSensorChanged() - called.")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}