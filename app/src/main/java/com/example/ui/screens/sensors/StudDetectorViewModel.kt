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
import kotlin.math.sqrt

data class StudDetectorUiState(
    val magneticFieldUT: Float = 45f,         // Magnetic flux density in microtesla (uT)
    val baselineUT: Float = 45f,              // Calibrated ambient baseline
    val signalDeltaUT: Float = 0f,            // Delta magnetic signal strength above baseline
    val signalPercent: Float = 0f,            // 0 - 100% proximity gauge
    val isStudDetected: Boolean = false,      // Signal above metal detection threshold
    val detectedMaterialType: String = "Clear Wall", // "Clear Wall", "Drywall Metal Screw", "Steel Rebar / Iron Pipe", "Electrical Conduit"
    val sensitivityThresholdUT: Float = 15f    // Sensitivity slider threshold
)

class StudDetectorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _uiState = MutableStateFlow(StudDetectorUiState())
    val uiState: StateFlow<StudDetectorUiState> = _uiState.asStateFlow()

    init {
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val totalUT = sqrt(x * x + y * y + z * z)
            val delta = abs(totalUT - _uiState.value.baselineUT)
            val thresh = _uiState.value.sensitivityThresholdUT

            val signalPct = (delta / (thresh * 2.5f) * 100f).coerceIn(0f, 100f)
            val isDetected = delta >= thresh

            val matType = when {
                delta >= thresh * 3.0f -> "Heavy Steel Rebar / Iron Pipe"
                delta >= thresh * 1.8f -> "Metal Framing Stud / Bracket"
                delta >= thresh -> "Drywall Screw / Small Fastener"
                else -> "Clear Wall Surface"
            }

            _uiState.value = _uiState.value.copy(
                magneticFieldUT = totalUT,
                signalDeltaUT = delta,
                signalPercent = signalPct,
                isStudDetected = isDetected,
                detectedMaterialType = matType
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun calibrateBaseline() {
        _uiState.value = _uiState.value.copy(baselineUT = _uiState.value.magneticFieldUT)
    }

    fun setSensitivity(thresh: Float) {
        _uiState.value = _uiState.value.copy(sensitivityThresholdUT = thresh.coerceIn(5f, 50f))
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
