package ercanduman.freefalldetectionchallenge.ui

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import ercanduman.freefalldetectionchallenge.R
import ercanduman.freefalldetectionchallenge.service.ForegroundService
import ercanduman.freefalldetectionchallenge.service.internal.SensorEventHandler
import ercanduman.freefalldetectionchallenge.utils.logd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var isSensorListeningStarted = false
    private lateinit var sensorEventHandler: SensorEventHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorEventHandler = SensorEventHandler(sensorManager)

        initFab()
    }

    private fun initFab() {
        logd("initFab() - called.")
        fab.setOnClickListener { view ->
            Snackbar.make(
                view, getString(R.string.main_sensor_listening_question), Snackbar.LENGTH_LONG
            ).setAction(android.R.string.yes) {
                isSensorListeningStarted = true
                sensorEventHandler.startSensorListening()
            }.show()
        }
    }

    private fun stopSensorListening() {
        logd("stopSensorListening() - called.")
        isSensorListeningStarted = false
        stopService(Intent(this, ForegroundService::class.java))
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
}
