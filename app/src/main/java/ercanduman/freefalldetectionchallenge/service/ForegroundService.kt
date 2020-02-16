package ercanduman.freefalldetectionchallenge.service

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ercanduman.freefalldetectionchallenge.*
import ercanduman.freefalldetectionchallenge.ui.MainActivity
import ercanduman.freefalldetectionchallenge.utils.logd
import kotlin.math.pow
import kotlin.math.sqrt

class ForegroundService : Service(), SensorEventListener {
    private lateinit var notification: Notification
    private lateinit var sensorManager: SensorManager
    private var lastShakeTime = 0L

    // called first time service created, called only once
    override fun onCreate() {
        super.onCreate()
        logd("ForegroundService.onCreate() - called.")
        lastShakeTime = System.currentTimeMillis()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

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
        startSensorListening()
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    private fun startSensorListening() {
        logd("startSensorListening() - called.")
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        registerSensor(accelerometerSensor)

        val movementSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        registerSensor(movementSensor)
    }

    private fun registerSensor(sensor: Sensor) {
        logd("registerSensor() - called.")
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        // logd("onSensorChanged() - called.")
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerEvent(event)
            Sensor.TYPE_ROTATION_VECTOR -> handleRotationEvent(event)
            // can handle more sensor events...
            else -> logd("Other type of sensor event triggered...")
        }
    }

    private fun handleAccelerometerEvent(event: SensorEvent) {
        // logd("handleAccelerometerEvent() - called.")
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastShakeTime) > MIN_TIME_BETWEEN_SHAKES) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            /**
             * Free fall algorithms:
             * 1- https://www.hackster.io/RVLAD/free-fall-detection-using-3-axis-accelerometer-06383e
             * 2- https://stackoverflow.com/questions/36540058/can-anyone-tell-me-how-i-get-toast-when-mobile-falls-down
             */
            val acceleration = sqrt(x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2))
            // sqrt(x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2)) - SensorManager.GRAVITY_EARTH
            // logd("Acceleration is " + accelerationReader + "m/s^2")

            /**
             *  The total acceleration value during free fall were in the range of 25-45
             *  and the acceleration during free fall will drop below value 50 (FREE_FALL_RANGE_HIGHEST).
             */
            if (acceleration in FREE_FALL_RANGE_LOWEST..FREE_FALL_RANGE_HIGHEST) {
                logd("Fall Detected...")
                lastShakeTime = currentTime

                val duration = System.currentTimeMillis() - lastShakeTime
                logd("duration of fall: $duration ms")

            } // else logd("Not fall detected...")
        } else logd("Shake detected...")
    }

    private fun handleRotationEvent(event: SensorEvent) {
        // logd("handleRotationEvent() - called.")
        // logd("event: ${event.sensor.name}")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        logd("onDestroy() - called.")
        sensorManager.unregisterListener(this)
    }
}