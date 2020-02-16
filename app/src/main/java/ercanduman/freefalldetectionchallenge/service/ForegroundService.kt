package ercanduman.freefalldetectionchallenge.service

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ercanduman.freefalldetectionchallenge.NOTIFICATION_CHANNEL_ID
import ercanduman.freefalldetectionchallenge.NOTIFICATION_CHANNEL_NAME
import ercanduman.freefalldetectionchallenge.NOTIFICATION_ID
import ercanduman.freefalldetectionchallenge.R
import ercanduman.freefalldetectionchallenge.service.internal.SensorEventHandler
import ercanduman.freefalldetectionchallenge.ui.MainActivity
import ercanduman.freefalldetectionchallenge.utils.logd

class ForegroundService : Service() {
    private lateinit var notification: Notification
    private lateinit var sensorEventHandler: SensorEventHandler

    /**
     * Called first time service created, called only once
     */
    override fun onCreate() {
        super.onCreate()
        logd("ForegroundService.onCreate() - called.")
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorEventHandler = SensorEventHandler(sensorManager)

        /**
         * If api level is higher than Oreo (26) then a notification should be displayed
         * to user otherwise system will kill/stop service around 1 min
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        createNotification()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(notificationChannel)
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
        sensorEventHandler.startSensorListening()
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        logd("onDestroy() - called.")
        sensorEventHandler.finish()
    }
}