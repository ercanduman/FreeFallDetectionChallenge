package ercanduman.freefalldetectionchallenge.ui

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import ercanduman.freefalldetectionchallenge.*
import ercanduman.freefalldetectionchallenge.service.ForegroundService
import ercanduman.freefalldetectionchallenge.utils.logd
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lastShakeTime = 0L

    private var isSensorListeningStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lastShakeTime = System.currentTimeMillis()

        initFab()

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

    override fun onDestroy() {
        super.onDestroy()
        if (isSensorListeningStarted) {
            /**
             * For Api level 26 (O) should call startForegroundService() and for Pre-O startService().
             * ContextCompat.startForegroundService() handles this check internally.
             */
            ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))
        }
    }

    private fun initFab() {
        logd("initFab() - called.")
        fab.setOnClickListener { view ->
            Snackbar.make(
                view, getString(R.string.main_sensor_listening_question), Snackbar.LENGTH_LONG
            ).setAction(android.R.string.yes) { startSensorListening() }.show()
        }
    }

    private fun startSensorListening() {
        logd("startSensorListening() - called.")
        isSensorListeningStarted = true
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor != null) registerSensorListener(accelerometerSensor)

        val movementSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (movementSensor != null) registerSensorListener(movementSensor)
    }

    private fun stopSensorListening() {
        logd("stopSensorListening() - called.")
        isSensorListeningStarted = false
        unRegisterSensor()
        stopService(Intent(this, ForegroundService::class.java))
    }


    private fun registerSensorListener(sensor: Sensor?) {
        logd("registerSensorListener() - called.")

        /**
         * the sensor reporting delay is small enough such that
         * the application receives an update before the system checks the sensor
         * readings again.
         */
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun unRegisterSensor() {
        logd("unRegisterSensor() - called.")
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        logd("onAccuracyChanged() - called.")
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
        logd("handleAccelerometerEvent() - called.")
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastShakeTime) > MIN_TIME_BETWEEN_SHAKES) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val accelerationReader =
                sqrt(x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0)) - SensorManager.GRAVITY_EARTH
            logd("Acceleration is " + accelerationReader + "m/s^2")

            if (accelerationReader > SHAKE_THRESHOLD_FOR_FREE_FALL) {
                logd("Fall Detected...")
                lastShakeTime = currentTime

                val duration = System.currentTimeMillis() - currentTime
                logd("duration of fall: $duration ms")

            } // else logd("Not fall detected...")
        } else logd("Shake detected...")
    }

    private fun handleRotationEvent(event: SensorEvent) {
        logd("handleRotationEvent() - called.")
        logd("event: ${event.sensor.name}")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                stopSensorListening()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
