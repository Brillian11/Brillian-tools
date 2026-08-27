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
import kotlin.math.abs
import kotlin.math.tan

data class ArMeasurementData(
    val currentPitchDeg: Float = 0f,
    val eyeHeightMeters: Double = 1.5, // Standard holding height
    val lockedBasePitchDeg: Float? = null,
    val lockedTopPitchDeg: Float? = null,
    val manualDistanceMeters: Double? = null
) {
    // Calculated distance to base D = h_0 / tan(pitch_base)
    val calculatedDistanceMeters: Double
        get() {
            manualDistanceMeters?.let { return it }
            val basePitch = lockedBasePitchDeg ?: return 0.0
            val pitchRad = Math.toRadians(abs(basePitch).toDouble().coerceIn(1.0, 85.0))
            return eyeHeightMeters / tan(pitchRad)
        }

    // Calculated total object height H
    val calculatedHeightMeters: Double
        get() {
            val dist = calculatedDistanceMeters
            if (dist <= 0) return 0.0
            val topPitch = lockedTopPitchDeg ?: currentPitchDeg
            val topRad = Math.toRadians(topPitch.toDouble())

            val topOffset = dist * tan(topRad)
            val basePitch = lockedBasePitchDeg ?: 0f
            val baseOffset = if (basePitch < 0) {
                dist * tan(Math.toRadians(abs(basePitch).toDouble()))
            } else {
                eyeHeightMeters
            }

            return (topOffset + baseOffset).coerceAtLeast(0.0)
        }

    val heightFeet: Double get() = calculatedHeightMeters * 3.28084
    val distanceFeet: Double get() = calculatedDistanceMeters * 3.28084
}

class ArMeasurementViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val _arState = MutableStateFlow(ArMeasurementData())
    val arState: StateFlow<ArMeasurementData> = _arState.asStateFlow()

    private var hasAccel = false
    private var hasMag = false

    fun startSensors() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accel != null) sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        if (mag != null) sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME)
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
        }

        if (hasAccel && hasMag) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()

                // Low pass filter
                val curr = _arState.value.currentPitchDeg
                val smoothed = curr + (pitch - curr) * 0.15f
                _arState.value = _arState.value.copy(currentPitchDeg = smoothed)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setEyeHeight(heightMeters: Double) {
        _arState.value = _arState.value.copy(eyeHeightMeters = heightMeters.coerceIn(0.5, 3.0))
    }

    fun lockBaseAngle() {
        _arState.value = _arState.value.copy(lockedBasePitchDeg = _arState.value.currentPitchDeg)
    }

    fun lockTopAngle() {
        _arState.value = _arState.value.copy(lockedTopPitchDeg = _arState.value.currentPitchDeg)
    }

    fun resetMeasurement() {
        _arState.value = _arState.value.copy(
            lockedBasePitchDeg = null,
            lockedTopPitchDeg = null,
            manualDistanceMeters = null
        )
    }

    fun setManualPitchSim(pitchDeg: Float) {
        _arState.value = _arState.value.copy(currentPitchDeg = pitchDeg)
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}
