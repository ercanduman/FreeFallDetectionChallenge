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
import ercanduman.freefalldetectionchallenge.R
import ercanduman.freefalldetectionchallenge.utils.logd
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unRegisterSensor() {
        logd("unRegisterSensor() - called.")
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        logd("onAccuracyChanged() - called.")
    }

    override fun onSensorChanged(event: SensorEvent) {
//        logd("onSensorChanged() - called.")

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//            logd("Accelerometer sensors triggered.")

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val accelerationReader =
                sqrt(x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0))
            val precision = DecimalFormat("0.00")
            val round = precision.format(accelerationReader).toDouble()

            if (round > 0.3 && round < 0.5) {
                logd("Fall Detected...")
            } else {
                logd("Not fall...")
            }
        }
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
