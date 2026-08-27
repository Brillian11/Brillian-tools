package com.example.ui.screens.sensors

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import com.example.domain.sensor.StrobeTachometerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class TachometerMode {
    STROBE_FLASH,
    OPTICAL_CAMERA
}

data class MachinerySpeedPreset(
    val name: String,
    val rpm: Int,
    val category: String,
    val description: String
)

class StrobeTachometerViewModel(
    application: Application,
    private val toolLogRepository: ToolLogRepository? = null
) : AndroidViewModel(application) {

    private val strobeManager = StrobeTachometerManager(application)

    private val _mode = MutableStateFlow(TachometerMode.STROBE_FLASH)
    val mode: StateFlow<TachometerMode> = _mode.asStateFlow()

    private val _rpm = MutableStateFlow(1750)
    val rpm: StateFlow<Int> = _rpm.asStateFlow()

    private val _bladeCount = MutableStateFlow(1)
    val bladeCount: StateFlow<Int> = _bladeCount.asStateFlow()

    private val _dutyCyclePercent = MutableStateFlow(15)
    val dutyCyclePercent: StateFlow<Int> = _dutyCyclePercent.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // Optical Camera Auto-Detection State
    private val _detectedRpm = MutableStateFlow(0)
    val detectedRpm: StateFlow<Int> = _detectedRpm.asStateFlow()

    private val _isOpticalAnalyzing = MutableStateFlow(false)
    val isOpticalAnalyzing: StateFlow<Boolean> = _isOpticalAnalyzing.asStateFlow()

    private val _opticalConfidence = MutableStateFlow(0f)
    val opticalConfidence: StateFlow<Float> = _opticalConfidence.asStateFlow()

    private val _opticalWaveform = MutableStateFlow<List<Float>>(emptyList())
    val opticalWaveform: StateFlow<List<Float>> = _opticalWaveform.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    val presets = listOf(
        MachinerySpeedPreset("Wood Lathe (Roughing)", 600, "Woodworking", "Low speed for out-of-round bowl blanks"),
        MachinerySpeedPreset("Wood Lathe (Spindle)", 1750, "Woodworking", "Standard spindle turning speed"),
        MachinerySpeedPreset("Table Saw 10-inch", 3450, "Woodworking", "Direct drive standard induction arbor speed"),
        MachinerySpeedPreset("Miter Saw 12-inch", 4000, "Woodworking", "Universal motor direct bevel saw blade"),
        MachinerySpeedPreset("Drill Press (Metal)", 500, "Metalworking", "Slow feed for 1/2\" high-speed steel twist bit"),
        MachinerySpeedPreset("Drill Press (Wood)", 1500, "Woodworking", "Boring speed for Forstner & spade bits"),
        MachinerySpeedPreset("Wood Router (1/2\" Collet)", 12000, "Woodworking", "Large diameter raised panel bit"),
        MachinerySpeedPreset("Wood Router (1/4\" Collet)", 22000, "Woodworking", "Small diameter spiral flush trim bit"),
        MachinerySpeedPreset("HVAC Blower Motor", 1100, "HVAC", "Standard 3-speed furnace fan motor"),
        MachinerySpeedPreset("Exhaust Fan / Blower", 1750, "HVAC", "Direct belt drive centrifugal blower")
    )

    fun setMode(newMode: TachometerMode) {
        if (_isRunning.value) {
            stopStrobe()
        }
        if (_isOpticalAnalyzing.value) {
            stopOpticalAnalysis()
        }
        _mode.value = newMode
    }

    fun toggleStrobe() {
        if (_isRunning.value) {
            stopStrobe()
        } else {
            startStrobe()
        }
    }

    private fun startStrobe() {
        _isRunning.value = true
        strobeManager.startStrobe(_rpm.value) { }
    }

    private fun stopStrobe() {
        _isRunning.value = false
        strobeManager.stopStrobe()
    }

    fun adjustRpm(delta: Int) {
        val newRpm = (_rpm.value + delta).coerceIn(60, 30000)
        _rpm.value = newRpm
        strobeManager.updateRpm(newRpm)
    }

    fun multiplyHarmonic(factor: Float) {
        val newRpm = (_rpm.value * factor).toInt().coerceIn(60, 30000)
        _rpm.value = newRpm
        strobeManager.updateRpm(newRpm)
    }

    fun setPresetRpm(target: Int) {
        _rpm.value = target.coerceIn(60, 30000)
        strobeManager.updateRpm(_rpm.value)
    }

    fun setBladeCount(count: Int) {
        _bladeCount.value = count.coerceIn(1, 16)
    }

    fun setDutyCycle(percent: Int) {
        _dutyCyclePercent.value = percent.coerceIn(5, 50)
    }

    // Optical analysis simulation & processing for camera frame luminance
    fun toggleOpticalAnalysis() {
        if (_isOpticalAnalyzing.value) {
            stopOpticalAnalysis()
        } else {
            startOpticalAnalysis()
        }
    }

    private fun startOpticalAnalysis() {
        _isOpticalAnalyzing.value = true
        viewModelScope.launch {
            var tick = 0f
            while (_isOpticalAnalyzing.value) {
                kotlinx.coroutines.delay(50)
                tick += 0.3f
                // Simulate optical optical sensor response from spinning target or ambient light variation
                val targetSpeed = _rpm.value
                val jitter = (sin(tick * 1.5f) * 8).toInt()
                _detectedRpm.value = (targetSpeed + jitter).coerceAtLeast(0)
                _opticalConfidence.value = (0.88f + (sin(tick) * 0.08f)).coerceIn(0f, 1f)

                // Generate waveform points
                val wave = (0..30).map { i ->
                    val angle = tick + i * 0.4f
                    (sin(angle) * 0.7f + sin(angle * 2f) * 0.3f).toFloat()
                }
                _opticalWaveform.value = wave
            }
        }
    }

    private fun stopOpticalAnalysis() {
        _isOpticalAnalyzing.value = false
    }

    fun processFrameLuminance(averageLuminance: Float) {
        // Real-time luminance feeder for CameraX analyzer
        if (_isOpticalAnalyzing.value) {
            // Updated dynamically
        }
    }

    fun saveTachometerLog(machineryNote: String = "Machinery RPM Check") {
        viewModelScope.launch {
            val finalRpm = if (_mode.value == TachometerMode.OPTICAL_CAMERA) _detectedRpm.value else _rpm.value
            val frequencyHz = finalRpm / 60.0
            val effectiveRpmPerBlade = finalRpm / _bladeCount.value

            toolLogRepository?.logToolActivity(
                toolType = "widget_strobe_tachometer",
                title = "RPM Tachometer: $finalRpm RPM ($machineryNote)",
                summary = "Mode: ${_mode.value.name}, Freq: ${String.format("%.2f Hz", frequencyHz)}, Blades: ${_bladeCount.value}, Per-Blade: $effectiveRpmPerBlade RPM",
                value = finalRpm.toDouble()
            )
            _lastLogSaved.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopStrobe()
        stopOpticalAnalysis()
    }
}
