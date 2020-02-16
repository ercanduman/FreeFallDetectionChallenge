package ercanduman.freefalldetectionchallenge.service.internal

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import ercanduman.freefalldetectionchallenge.FREE_FALL_RANGE_HIGHEST
import ercanduman.freefalldetectionchallenge.FREE_FALL_RANGE_LOWEST
import ercanduman.freefalldetectionchallenge.MIN_TIME_BETWEEN_SHAKES
import ercanduman.freefalldetectionchallenge.data.entities.FreeFall
import ercanduman.freefalldetectionchallenge.utils.logd
import java.sql.Timestamp
import kotlin.math.pow
import kotlin.math.sqrt

class SensorEventHandler(
    private val sensorManager: SensorManager,
    private val contentWriter: ContentWriter?
) : SensorEventListener {
    private var lastShakeTime = 0L

    init {
        lastShakeTime = System.currentTimeMillis()
    }

    fun startSensorListening() {
        logd("startSensorListening() - called.")
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        registerSensor(accelerometerSensor)

        val movementSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        registerSensor(movementSensor)
    }

    private fun registerSensor(sensor: Sensor) {
        // logd("registerSensor() - called.")
        sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
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
                val timestamp = Timestamp(System.currentTimeMillis())
                // logd("Current timestamp: $timestamp")

                val duration = System.currentTimeMillis().minus(currentTime)
                // logd("Duration of fall: $duration ms")

                val freeFall = FreeFall(timestamp, duration)
                logd("freeFall: $freeFall")

                contentWriter?.content(freeFall)
                lastShakeTime = currentTime

            } // else logd("Not fall detected...")
        } else logd("Shake detected...")
    }

    private fun handleRotationEvent(event: SensorEvent) {
        // logd("handleRotationEvent() - called.")
        // logd("event: ${event.sensor.name}")
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun finish() {
        logd("finish() - called.")
        sensorManager.unregisterListener(this)
    }
}

