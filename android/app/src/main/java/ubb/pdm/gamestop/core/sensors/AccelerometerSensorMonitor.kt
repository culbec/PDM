package ubb.pdm.gamestop.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ubb.pdm.gamestop.core.util.TAG

class AccelerometerSensorMonitor(val context: Context) {
    val data: Flow<List<Float>> = callbackFlow {
        val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorListener = object: SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "onAccuracyChanged: $accuracy")
            }

            override fun onSensorChanged(event: SensorEvent?) {
                val x = event?.values[0] ?: 0f
                val y = event?.values[1] ?: 0f
                val z = event?.values[2] ?: 0f
                channel.trySend(listOf(x, y, z))
            }
        }

        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        awaitClose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}