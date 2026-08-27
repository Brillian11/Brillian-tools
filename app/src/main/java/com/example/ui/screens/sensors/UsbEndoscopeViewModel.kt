package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DefectCategory(val label: String, val severity: String) {
    CRACK("Structural Crack / Fracture", "HIGH"),
    CORROSION("Rust / Pipe Corrosion", "MEDIUM"),
    BLOCKAGE("Drain / Duct Blockage", "HIGH"),
    MOISTURE("Water Leak / Moisture Intrusion", "HIGH"),
    WIRING("Exposed / Frayed Conductor", "CRITICAL"),
    PEST("Pest / Rodent Damage", "MEDIUM"),
    NORMAL("Clean / Passed Inspection", "LOW")
}

class UsbEndoscopeViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private val _brightness = MutableStateFlow(1.0f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _contrast = MutableStateFlow(1.0f)
    val contrast: StateFlow<Float> = _contrast.asStateFlow()

    private val _rotationDegrees = MutableStateFlow(0)
    val rotationDegrees: StateFlow<Int> = _rotationDegrees.asStateFlow()

    private val _isMirrored = MutableStateFlow(false)
    val isMirrored: StateFlow<Boolean> = _isMirrored.asStateFlow()

    private val _isGridVisible = MutableStateFlow(true)
    val isGridVisible: StateFlow<Boolean> = _isGridVisible.asStateFlow()

    private val _isTorchActive = MutableStateFlow(true)
    val isTorchActive: StateFlow<Boolean> = _isTorchActive.asStateFlow()

    private val _selectedDefect = MutableStateFlow(DefectCategory.CRACK)
    val selectedDefect: StateFlow<DefectCategory> = _selectedDefect.asStateFlow()

    private val _snapshotCount = MutableStateFlow(0)
    val snapshotCount: StateFlow<Int> = _snapshotCount.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    fun setZoom(zoom: Float) {
        _zoomLevel.value = zoom.coerceIn(1.0f, 5.0f)
    }

    fun setBrightness(b: Float) {
        _brightness.value = b.coerceIn(0.5f, 2.0f)
    }

    fun setContrast(c: Float) {
        _contrast.value = c.coerceIn(0.5f, 2.0f)
    }

    fun rotate90() {
        _rotationDegrees.value = (_rotationDegrees.value + 90) % 360
    }

    fun toggleMirror() {
        _isMirrored.value = !_isMirrored.value
    }

    fun toggleGrid() {
        _isGridVisible.value = !_isGridVisible.value
    }

    fun toggleTorch() {
        _isTorchActive.value = !_isTorchActive.value
    }

    fun setDefect(defect: DefectCategory) {
        _selectedDefect.value = defect
    }

    fun takeSnapshot() {
        _snapshotCount.value += 1
    }

    fun saveInspectionLog(location: String = "Wall Cavity Stud Bay 3", note: String = "Minor hairline pipe corrosion observed") {
        viewModelScope.launch {
            toolLogRepository?.logToolActivity(
                toolType = "widget_usb_endoscope",
                title = "Borescope Inspection: $location",
                summary = "Findings: $note, Classification: ${_selectedDefect.value.label} (${_selectedDefect.value.severity}), Snapshots: ${_snapshotCount.value}, Zoom: ${String.format("%.1fx", _zoomLevel.value)}",
                value = _snapshotCount.value.toDouble()
            )
            _lastLogSaved.value = true
        }
    }
}
