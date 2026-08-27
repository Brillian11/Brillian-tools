package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class ThermalPalette(val label: String, val description: String) {
    IRONBOW("Ironbow", "Industry standard for HVAC, plumbing & electrical inspection"),
    RAINBOW("Rainbow (Hi-Contrast)", "High dynamic range for building insulation & thermal leaks"),
    WHITE_HOT("White Hot", "Monochrome thermal grayscale with brightest highlights on hot areas"),
    BLACK_HOT("Black Hot", "Inverted grayscale with dark highlights for mechanical wear"),
    LAVA("Lava / Inferno", "High temperature hot-spot isolation for motors & bearings"),
    ARCTIC("Arctic Blue", "Optimized for refrigeration, drafts & cold envelope breaches")
}

data class EmissivityPreset(
    val name: String,
    val value: Float,
    val application: String
)

data class ThermalSpot(
    val x: Float, // 0.0 to 1.0
    val y: Float,
    val tempC: Float,
    val label: String
)

class ThermalCameraViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _palette = MutableStateFlow(ThermalPalette.IRONBOW)
    val palette: StateFlow<ThermalPalette> = _palette.asStateFlow()

    private val _isFahrenheit = MutableStateFlow(false)
    val isFahrenheit: StateFlow<Boolean> = _isFahrenheit.asStateFlow()

    private val _emissivity = MutableStateFlow(0.95f)
    val emissivity: StateFlow<Float> = _emissivity.asStateFlow()

    private val _centerTemp = MutableStateFlow(32.4f)
    val centerTemp: StateFlow<Float> = _centerTemp.asStateFlow()

    private val _maxTemp = MutableStateFlow(58.7f)
    val maxTemp: StateFlow<Float> = _maxTemp.asStateFlow()

    private val _minTemp = MutableStateFlow(18.2f)
    val minTemp: StateFlow<Float> = _minTemp.asStateFlow()

    private val _hotSpot = MutableStateFlow(ThermalSpot(0.65f, 0.35f, 58.7f, "HOT"))
    val hotSpot: StateFlow<ThermalSpot> = _hotSpot.asStateFlow()

    private val _coldSpot = MutableStateFlow(ThermalSpot(0.20f, 0.80f, 18.2f, "COLD"))
    val coldSpot: StateFlow<ThermalSpot> = _coldSpot.asStateFlow()

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive: StateFlow<Boolean> = _isAlarmActive.asStateFlow()

    private val _alarmThresholdC = MutableStateFlow(60.0f)
    val alarmThresholdC: StateFlow<Float> = _alarmThresholdC.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _selectedPreset = MutableStateFlow("Electrical Panel / Breakers")
    val selectedPreset: StateFlow<String> = _selectedPreset.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    val emissivityPresets = listOf(
        EmissivityPreset("PVC / Wire Insulation", 0.94f, "Electrical conduit, THHN jacket"),
        EmissivityPreset("Oxidized Copper Busbar", 0.65f, "Panelboard lugs, busbars"),
        EmissivityPreset("Polished Aluminum", 0.20f, "Bare metal, heat sinks"),
        EmissivityPreset("Drywall & Plaster", 0.90f, "Wall envelopes, ceiling insulation"),
        EmissivityPreset("Concrete & Masonry", 0.92f, "Slabs, foundation walls, brick"),
        EmissivityPreset("Wood (Pine/Oak)", 0.88f, "Framing studs, rafters, subfloors"),
        EmissivityPreset("Matte Paint (Any Color)", 0.96f, "Radiators, painted enclosures"),
        EmissivityPreset("Water & Ice", 0.96f, "Pipes, condenser coils, leaks")
    )

    init {
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            var step = 0f
            while (true) {
                kotlinx.coroutines.delay(100)
                step += 0.1f
                val base = 32f + sin(step) * 3f
                val max = 56f + sin(step * 0.8f) * 6f
                val min = 17f + sin(step * 0.5f) * 2f

                _centerTemp.value = base
                _maxTemp.value = max
                _minTemp.value = min

                _hotSpot.value = ThermalSpot(
                    x = (0.6f + sin(step * 0.3f) * 0.15f).coerceIn(0.1f, 0.9f),
                    y = (0.35f + sin(step * 0.4f) * 0.12f).coerceIn(0.1f, 0.9f),
                    tempC = max,
                    label = "HOT"
                )

                _coldSpot.value = ThermalSpot(
                    x = (0.25f + sin(step * 0.2f) * 0.10f).coerceIn(0.1f, 0.9f),
                    y = (0.75f + sin(step * 0.3f) * 0.10f).coerceIn(0.1f, 0.9f),
                    tempC = min,
                    label = "COLD"
                )

                _isAlarmActive.value = max >= _alarmThresholdC.value
            }
        }
    }

    fun setPalette(newPalette: ThermalPalette) {
        _palette.value = newPalette
    }

    fun toggleTemperatureUnit() {
        _isFahrenheit.value = !_isFahrenheit.value
    }

    fun setEmissivity(value: Float) {
        _emissivity.value = value.coerceIn(0.10f, 1.00f)
    }

    fun applyPreset(preset: EmissivityPreset) {
        _emissivity.value = preset.value
        _selectedPreset.value = preset.name
    }

    fun setAlarmThreshold(thresh: Float) {
        _alarmThresholdC.value = thresh.coerceIn(30f, 150f)
    }

    fun toDisplayTemp(celsius: Float): String {
        return if (_isFahrenheit.value) {
            val f = (celsius * 9f / 5f) + 32f
            String.format("%.1f°F", f)
        } else {
            String.format("%.1f°C", celsius)
        }
    }

    fun saveThermalAuditLog(targetName: String = "Electrical Panel Thermal Audit") {
        viewModelScope.launch {
            val cTemp = _centerTemp.value
            val hTemp = _maxTemp.value
            val lTemp = _minTemp.value
            val deltaT = hTemp - lTemp

            toolLogRepository?.logToolActivity(
                toolType = "widget_thermal_camera",
                title = "Thermal Inspection: $targetName",
                summary = "Center: ${toDisplayTemp(cTemp)}, Max: ${toDisplayTemp(hTemp)}, Min: ${toDisplayTemp(lTemp)}, ΔT: ${if (_isFahrenheit.value) String.format("%.1f°F", deltaT * 1.8f) else String.format("%.1f°C", deltaT)}, Emissivity: ${String.format("%.2f", _emissivity.value)}",
                value = cTemp.toDouble()
            )
            _lastLogSaved.value = true
        }
    }
}
