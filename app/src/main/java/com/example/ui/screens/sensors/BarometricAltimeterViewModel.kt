package com.example.ui.screens.sensors

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sin

data class SurveyStation(
    val id: Int,
    val name: String,
    val pressureHpa: Float,
    val absoluteAltitudeM: Float,
    val deltaAltitudeM: Float
)

enum class PressureTrend(val label: String, val advisory: String, val colorHex: Long) {
    RISING_FAST("Rising Rapidly (> +2.0 hPa/3h)", "Strong high pressure arriving; clear, dry working conditions.", 0xFF16A34A),
    STEADY("Steady (±0.5 hPa/3h)", "Stable barometric pressure; persistent calm weather.", 0xFF0284C7),
    FALLING_SLOW("Falling Slowly (-1.0 hPa/3h)", "Mild pressure drop; expect increasing cloudiness / breeze.", 0xFFD97706),
    FALLING_RAPID("Falling Rapidly (> -2.5 hPa/3h)", "STORM WARNING: Sharp cold front or low pressure gale approaching.", 0xFFDC2626)
}

class BarometricAltimeterViewModel(
    application: Application,
    private val toolLogRepository: ToolLogRepository? = null
) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val _isSensorAvailable = MutableStateFlow(pressureSensor != null)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private val _seaLevelQnhHpa = MutableStateFlow(1013.25f) // Standard atmosphere QNH
    val seaLevelQnhHpa: StateFlow<Float> = _seaLevelQnhHpa.asStateFlow()

    private val _currentPressureHpa = MutableStateFlow(1008.45f)
    val currentPressureHpa: StateFlow<Float> = _currentPressureHpa.asStateFlow()

    private val _absoluteAltitudeM = MutableStateFlow(0f)
    val absoluteAltitudeM: StateFlow<Float> = _absoluteAltitudeM.asStateFlow()

    private val _tareAltitudeM = MutableStateFlow(0f)
    val tareAltitudeM: StateFlow<Float> = _tareAltitudeM.asStateFlow()

    private val _deltaAltitudeM = MutableStateFlow(0f)
    val deltaAltitudeM: StateFlow<Float> = _deltaAltitudeM.asStateFlow()

    private val _isFeet = MutableStateFlow(false)
    val isFeet: StateFlow<Boolean> = _isFeet.asStateFlow()

    private val _stations = MutableStateFlow<List<SurveyStation>>(emptyList())
    val stations: StateFlow<List<SurveyStation>> = _stations.asStateFlow()

    private val _pressureTrend = MutableStateFlow(PressureTrend.STEADY)
    val pressureTrend: StateFlow<PressureTrend> = _pressureTrend.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    init {
        if (pressureSensor != null && sensorManager != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI)
        }
        startSimulationFallback()
    }

    private fun startSimulationFallback() {
        viewModelScope.launch {
            var step = 0f
            while (true) {
                kotlinx.coroutines.delay(150)
                if (pressureSensor == null) {
                    step += 0.05f
                    val p = 1008.40f + (sin(step) * 0.15f).toFloat()
                    _currentPressureHpa.value = p
                    updateAltitudeCalculations(p)
                }
            }
        }
    }

    private fun updateAltitudeCalculations(pressureHpa: Float) {
        val qnh = _seaLevelQnhHpa.value
        // Hypsometric Formula: 44330 * (1 - (P/P0)^(1/5.255))
        val altM = 44330f * (1.0f - (pressureHpa / qnh).pow(1.0f / 5.255f))
        _absoluteAltitudeM.value = altM
        _deltaAltitudeM.value = altM - _tareAltitudeM.value
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val p = event.values[0]
            _currentPressureHpa.value = p
            updateAltitudeCalculations(p)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setSeaLevelQnh(qnh: Float) {
        _seaLevelQnhHpa.value = qnh.coerceIn(900f, 1100f)
        updateAltitudeCalculations(_currentPressureHpa.value)
    }

    fun tareBenchmarkZero() {
        _tareAltitudeM.value = _absoluteAltitudeM.value
        _deltaAltitudeM.value = 0f
    }

    fun toggleUnit() {
        _isFeet.value = !_isFeet.value
    }

    fun recordStation(name: String) {
        val currentList = _stations.value.toMutableList()
        val station = SurveyStation(
            id = currentList.size + 1,
            name = name.ifBlank { "Station #${currentList.size + 1}" },
            pressureHpa = _currentPressureHpa.value,
            absoluteAltitudeM = _absoluteAltitudeM.value,
            deltaAltitudeM = _deltaAltitudeM.value
        )
        currentList.add(station)
        _stations.value = currentList
    }

    fun clearStations() {
        _stations.value = emptyList()
    }

    fun saveElevationLog(surveyTitle: String = "Building Floor Elevation Survey") {
        viewModelScope.launch {
            val curP = _currentPressureHpa.value
            val absM = _absoluteAltitudeM.value
            val deltaM = _deltaAltitudeM.value

            toolLogRepository?.logToolActivity(
                toolType = "widget_barometric_altimeter",
                title = "Elevation Survey: $surveyTitle",
                summary = "Pressure: ${String.format("%.2f hPa", curP)}, MSL: ${String.format("%.2f m (%.2f ft)", absM, absM * 3.28084f)}, Δh: ${String.format("%.2f m", deltaM)}, Trend: ${_pressureTrend.value.label}, Stations: ${_stations.value.size}",
                value = absM.toDouble()
            )
            _lastLogSaved.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }
}
