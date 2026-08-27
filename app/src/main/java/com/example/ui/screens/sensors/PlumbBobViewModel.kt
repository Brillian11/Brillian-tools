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
import kotlin.math.atan2

data class PlumbBobUiState(
    val pitchAngle: Float = 0f,         // Tilt angle from vertical (deg)
    val rollAngle: Float = 0f,          // Lateral tilt angle (deg)
    val plumbDeviationMmPerM: Float = 0f,// Deviation in mm/m
    val isPlumb: Boolean = false,       // Within ±0.2° vertical true plumb
    val isHold: Boolean = false,
    
    // Wall Corner 90° Squareness Mode
    val cornerLengthA: Double = 3.0,     // Side A (m or ft)
    val cornerLengthB: Double = 4.0,     // Side B (m or ft)
    val measuredDiagonalC: Double = 5.0, // Measured Hypotenuse
    val idealDiagonalC: Double = 5.0,    // Ideal sqrt(A²+B²)
    val cornerErrorMm: Double = 0.0,     // Off-square error
    val isSquare90: Boolean = true
)

class PlumbBobViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _uiState = MutableStateFlow(PlumbBobUiState())
    val uiState: StateFlow<PlumbBobUiState> = _uiState.asStateFlow()

    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        recalculateSquareness()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && !_uiState.value.isHold) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            // Calculate pitch (forward/back) and roll (left/right) tilt
            val pitch = Math.toDegrees(atan2(ay.toDouble(), az.toDouble())).toFloat()
            val roll = Math.toDegrees(atan2(ax.toDouble(), az.toDouble())).toFloat()

            // Calculate deviation from vertical 90° or 0°
            val plumbDev = abs(pitch)
            val devMm = (Math.tan(Math.toRadians(plumbDev.toDouble())) * 1000.0).toFloat()
            val isPlumbTrue = plumbDev < 0.25f && abs(roll) < 0.25f

            _uiState.value = _uiState.value.copy(
                pitchAngle = pitch,
                rollAngle = roll,
                plumbDeviationMmPerM = devMm,
                isPlumb = isPlumbTrue
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun toggleHold() {
        _uiState.value = _uiState.value.copy(isHold = !_uiState.value.isHold)
    }

    fun updateCornerDimensions(a: Double, b: Double, measuredC: Double) {
        _uiState.value = _uiState.value.copy(
            cornerLengthA = a.coerceAtLeast(0.1),
            cornerLengthB = b.coerceAtLeast(0.1),
            measuredDiagonalC = measuredC.coerceAtLeast(0.1)
        )
        recalculateSquareness()
    }

    private fun recalculateSquareness() {
        val a = _uiState.value.cornerLengthA
        val b = _uiState.value.cornerLengthB
        val mC = _uiState.value.measuredDiagonalC

        val idealC = Math.sqrt(a * a + b * b)
        val errorMm = abs(mC - idealC) * 1000.0
        val isSquare = errorMm < 2.0 // Less than 2mm difference

        _uiState.value = _uiState.value.copy(
            idealDiagonalC = idealC,
            cornerErrorMm = errorMm,
            isSquare90 = isSquare
        )
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
