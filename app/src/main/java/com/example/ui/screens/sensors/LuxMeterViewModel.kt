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
import kotlin.math.sin

data class LightingStandard(
    val id: String,
    val taskName: String,
    val minLux: Float,
    val targetLux: Float,
    val maxLux: Float,
    val standardRef: String
)

enum class ComplianceResult(val status: String, val advisory: String, val colorHex: Long) {
    DEFICIENT("DEFICIENT (Below Code Minimum)", "Insufficient light poses safety & precision hazards; add task lighting.", 0xFFDC2626),
    COMPLIANT("COMPLIANT (Optimal Task Illumination)", "Meets IESNA / OSHA recommended lighting levels for this task.", 0xFF16A34A),
    EXCESSIVE("EXCESSIVE (High Glare Risk)", "Illumination exceeds standard; risk of direct glare and eye fatigue.", 0xFFD97706)
}

class LuxMeterViewModel(
    application: Application,
    private val toolLogRepository: ToolLogRepository? = null
) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _currentLux = MutableStateFlow(540f)
    val currentLux: StateFlow<Float> = _currentLux.asStateFlow()

    private val _isFootCandles = MutableStateFlow(false)
    val isFootCandles: StateFlow<Boolean> = _isFootCandles.asStateFlow()

    private val _minLux = MutableStateFlow(500f)
    val minLux: StateFlow<Float> = _minLux.asStateFlow()

    private val _maxLux = MutableStateFlow(580f)
    val maxLux: StateFlow<Float> = _maxLux.asStateFlow()

    private val _avgLux = MutableStateFlow(540f)
    val avgLux: StateFlow<Float> = _avgLux.asStateFlow()

    private val _history = MutableStateFlow<List<Float>>(emptyList())
    val history: StateFlow<List<Float>> = _history.asStateFlow()

    private val _selectedStandard = MutableStateFlow(
        LightingStandard(
            id = "woodworking_bench",
            taskName = "Woodworking & Joinery Bench",
            minLux = 500f,
            targetLux = 750f,
            maxLux = 1500f,
            standardRef = "IESNA RP-7 / OSHA 1926.56"
        )
    )
    val selectedStandard: StateFlow<LightingStandard> = _selectedStandard.asStateFlow()

    private val _compliance = MutableStateFlow(ComplianceResult.COMPLIANT)
    val compliance: StateFlow<ComplianceResult> = _compliance.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    val standardsList = listOf(
        LightingStandard("woodworking_bench", "Woodworking & Joinery Bench", 500f, 750f, 1500f, "IESNA RP-7 Industrial"),
        LightingStandard("fine_machining", "Precision Machining & Toolmaking", 1000f, 1500f, 3000f, "IESNA Precision Table"),
        LightingStandard("general_shop", "General Shop Assembly / Rough Work", 300f, 500f, 1000f, "OSHA 1926.56(a)"),
        LightingStandard("drafting_cad", "CAD / Blueprint Drafting Station", 500f, 1000f, 2000f, "IESNA Office Std"),
        LightingStandard("warehouse_aisle", "Warehouse Aisles & Material Storage", 100f, 200f, 500f, "OSHA Storage Rules"),
        LightingStandard("emergency_egress", "Emergency Egress & Corridors", 50f, 100f, 300f, "NFPA 101 Life Safety")
    )

    private var sum = 0.0
    private var count = 0

    init {
        if (lightSensor != null && sensorManager != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
        }
        startSimulationFallback()
    }

    private fun startSimulationFallback() {
        viewModelScope.launch {
            var step = 0f
            while (true) {
                kotlinx.coroutines.delay(120)
                if (lightSensor == null) {
                    step += 0.1f
                    val lx = (540f + sin(step) * 45f + sin(step * 3f) * 15f).coerceAtLeast(0f)
                    updateLux(lx)
                }
            }
        }
    }

    private fun updateLux(lux: Float) {
        _currentLux.value = lux
        if (lux < _minLux.value) _minLux.value = lux
        if (lux > _maxLux.value) _maxLux.value = lux

        sum += lux
        count++
        _avgLux.value = (sum / count).toFloat()

        val list = _history.value.toMutableList()
        list.add(lux)
        if (list.size > 40) list.removeAt(0)
        _history.value = list

        val std = _selectedStandard.value
        _compliance.value = when {
            lux < std.minLux -> ComplianceResult.DEFICIENT
            lux > std.maxLux -> ComplianceResult.EXCESSIVE
            else -> ComplianceResult.COMPLIANT
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lx = event.values[0]
            updateLux(lx)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setStandard(std: LightingStandard) {
        _selectedStandard.value = std
        updateLux(_currentLux.value)
    }

    fun toggleUnit() {
        _isFootCandles.value = !_isFootCandles.value
    }

    fun formatLux(lux: Float): String {
        return if (_isFootCandles.value) {
            String.format("%.1f fc", lux / 10.764f)
        } else {
            String.format("%.0f lx", lux)
        }
    }

    fun saveLuxLog(workspaceNote: String = "Main Assembly Workbench #2") {
        viewModelScope.launch {
            val lx = _currentLux.value
            val std = _selectedStandard.value
            val comp = _compliance.value

            toolLogRepository?.logToolActivity(
                toolType = "widget_surface_lux_meter",
                title = "Lighting Audit: $workspaceNote",
                summary = "Illumination: ${String.format("%.0f lx", lx)} (${String.format("%.1f fc", lx / 10.764f)}), Standard: ${std.taskName}, Required: ${formatLux(std.minLux)} - ${formatLux(std.maxLux)}, Status: ${comp.status}",
                value = lx.toDouble()
            )
            _lastLogSaved.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }
}
