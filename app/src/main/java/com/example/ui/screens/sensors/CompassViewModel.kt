package com.example.ui.screens.sensors

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CompassData(
    val azimuthDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val declinationOffset: Float = 0f,
    val lockedBearing: Float? = null,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isCalibrating: Boolean = false,
    val calibrationProgress: Float = 1.0f,
    val magneticStrengthuT: Float = 48.2f
) {
    val trueAzimuth: Float
        get() = (azimuthDegrees + declinationOffset + 360f) % 360f

    val cardinalDirection: String
        get() {
            val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
            val index = ((trueAzimuth + 11.25f) / 22.5f).toInt() % 16
            return directions[index]
        }

    val bearingOffLock: Float?
        get() {
            return lockedBearing?.let { lock ->
                var diff = trueAzimuth - lock
                while (diff > 180f) diff -= 360f
                while (diff < -180f) diff += 360f
                diff
            }
        }
}

class CompassViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val _compassState = MutableStateFlow(CompassData())
    val compassState: StateFlow<CompassData> = _compassState.asStateFlow()

    private var hasAccel = false
    private var hasMag = false

    fun startSensors() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accel != null) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
        if (mag != null) {
            sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            hasAccel = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            hasMag = true

            val bx = event.values[0]
            val by = event.values[1]
            val bz = event.values[2]
            val flux = Math.sqrt((bx * bx + by * by + bz * bz).toDouble()).toFloat()

            // If calibrating, increment progress as user moves device
            val currentState = _compassState.value
            if (currentState.isCalibrating) {
                val newProgress = (currentState.calibrationProgress + 0.05f).coerceAtMost(1.0f)
                val isDone = newProgress >= 1.0f
                _compassState.value = currentState.copy(
                    calibrationProgress = newProgress,
                    isCalibrating = !isDone,
                    magneticStrengthuT = flux,
                    accuracy = if (isDone) SensorManager.SENSOR_STATUS_ACCURACY_HIGH else currentState.accuracy
                )
            } else {
                _compassState.value = currentState.copy(magneticStrengthuT = flux)
            }
        }

        if (hasAccel && hasMag) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f

                val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                // Low-pass filter for smooth dial rotation
                val current = _compassState.value.azimuthDegrees
                var diff = azimuth - current
                if (diff > 180) diff -= 360
                if (diff < -180) diff += 360
                val smoothed = (current + diff * 0.15f + 360f) % 360f

                _compassState.value = _compassState.value.copy(
                    azimuthDegrees = smoothed,
                    pitchDegrees = pitch,
                    rollDegrees = roll,
                    accuracy = event.accuracy
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun toggleLockBearing() {
        val currentLock = _compassState.value.lockedBearing
        if (currentLock == null) {
            _compassState.value = _compassState.value.copy(lockedBearing = _compassState.value.trueAzimuth)
        } else {
            _compassState.value = _compassState.value.copy(lockedBearing = null)
        }
    }

    fun setManualAzimuthSim(degrees: Float) {
        _compassState.value = _compassState.value.copy(azimuthDegrees = (degrees + 360f) % 360f)
    }

    fun setDeclinationOffset(declination: Float) {
        _compassState.value = _compassState.value.copy(declinationOffset = declination)
    }

    fun startCalibration() {
        _compassState.value = _compassState.value.copy(
            isCalibrating = true,
            calibrationProgress = 0.0f,
            accuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}
