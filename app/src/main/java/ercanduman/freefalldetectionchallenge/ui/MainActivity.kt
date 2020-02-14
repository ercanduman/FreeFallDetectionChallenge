package ercanduman.freefalldetectionchallenge.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ercanduman.freefalldetectionchallenge.MIN_TIME_BETWEEN_SHAKES
import ercanduman.freefalldetectionchallenge.R
import ercanduman.freefalldetectionchallenge.SHAKE_THRESHOLD_FOR_FREE_FALL
import ercanduman.freefalldetectionchallenge.utils.logd
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lastShakeTime = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lastShakeTime = System.currentTimeMillis()

        initFab()
    }

    private fun initFab() {
        logd("initFab() - called.")
        fab.setOnClickListener { view ->
            Snackbar.make(
                view, getString(R.string.main_sensor_listening_question), Snackbar.LENGTH_LONG
            ).setAction(android.R.string.yes) { registerSensor() }.show()
        }
    }

    private fun registerSensor() {
        logd("registerSensor() - called.")

        /**
         * the sensor reporting delay is small enough such that
         * the application receives an update before the system checks the sensor
         * readings again.
         */
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor != null) {
            sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        val movementSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (movementSensor != null) {
            sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
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
            Sensor.TYPE_SIGNIFICANT_MOTION -> handleMotionEvent(event)
            // can handle proper sensor events below...
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
            } // else logd("Not fall detected...")
        } else logd("Shake detected...")
    }

    private fun handleMotionEvent(event: SensorEvent) {
        logd("handleMotionEvent() - called.")
    }

    private fun handleRotationEvent(event: SensorEvent) {
        logd("handleRotationEvent() - called.")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                unRegisterSensor()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
