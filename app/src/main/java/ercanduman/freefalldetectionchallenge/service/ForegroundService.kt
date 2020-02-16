package ercanduman.freefalldetectionchallenge.service

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import ercanduman.freefalldetectionchallenge.NOTIFICATION_CHANNEL_ID
import ercanduman.freefalldetectionchallenge.NOTIFICATION_CHANNEL_NAME
import ercanduman.freefalldetectionchallenge.NOTIFICATION_ID
import ercanduman.freefalldetectionchallenge.service.internal.SensorEventHandler
import ercanduman.freefalldetectionchallenge.utils.createNotification
import ercanduman.freefalldetectionchallenge.utils.logd

class ForegroundService : Service() {
    private lateinit var sensorEventHandler: SensorEventHandler

    /**
     * Called first time service created, called only once
     */
    override fun onCreate() {
        super.onCreate()
        logd("ForegroundService.onCreate() - called.")
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorEventHandler = SensorEventHandler(this, sensorManager, null)

        /**
         * If api level is higher than Oreo (26) then a notification should be displayed
         * to user otherwise system will kill/stop service around 1 min
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
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

    /**
     * Triggered every time service started.
     * All codes written here runs on main thread,
     * so if need any heavy operation do it on the background thread
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logd("onStartCommand() - called.")
        sensorEventHandler.startSensorListening()
        startForeground(NOTIFICATION_ID, createNotification(this, "Sensors listening..."))
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