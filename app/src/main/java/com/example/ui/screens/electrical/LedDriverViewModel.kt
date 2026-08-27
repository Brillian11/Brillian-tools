package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SystemVoltage(val volts: Double, val label: String) {
    V12_DC(12.0, "12V DC (Constant Voltage)"),
    V24_DC(24.0, "24V DC (Constant Voltage)"),
    V48_DC(48.0, "48V DC (Constant Voltage)"),
    V12_AC(12.0, "12V AC (Landscape Transformer)")
}

data class LightingFixture(
    val id: String,
    val name: String,
    val quantityOrLength: Double, // count or feet
    val wattsPerUnit: Double, // W/ea or W/ft
    val isLengthBased: Boolean = false
) {
    val totalWatts: Double get() = quantityOrLength * wattsPerUnit
}

data class LedDriverUiState(
    val voltage: SystemVoltage = SystemVoltage.V24_DC,
    val headroomPct: Double = 20.0, // 80% continuous load rule = 20% headroom / 1.25x
    val fixtures: List<LightingFixture> = listOf(
        LightingFixture("1", "Under-Cabinet LED Tape (COB)", 16.4, 3.0, true), // 16.4 ft @ 3W/ft = 49.2W
        LightingFixture("2", "Recessed Puck Lights", 6.0, 4.5, false), // 6x @ 4.5W = 27W
        LightingFixture("3", "Toe-Kick Accent Strip", 10.0, 1.5, true) // 10 ft @ 1.5W/ft = 15W
    ),

    // Calculated Specs
    val rawConnectedWatts: Double = 91.2,
    val minDriverRatingWatts: Double = 114.0, // With 20% headroom
    val outputCurrentAmps: Double = 4.75, // min Watts / Volts
    val recommendedDriverWattage: Int = 150, // Standard size
    val class2Compliant: Boolean = false, // Class 2 limit is max 96W (or 100VA) for 24V / 60W for 12V
    val driverEfficiencyPct: Double = 90.0,
    val primaryAcLoadWatts: Double = 126.7 // AC wall power draw
) {
    companion object {
        val STANDARD_DRIVER_SIZES = listOf(20, 30, 45, 60, 96, 100, 120, 150, 180, 200, 240, 300, 320, 480, 600)
    }
}

class LedDriverViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedDriverUiState())
    val uiState: StateFlow<LedDriverUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setVoltage(v: SystemVoltage) {
        _uiState.value = _uiState.value.copy(voltage = v)
        recalculate()
    }

    fun setHeadroomPct(pct: Double) {
        _uiState.value = _uiState.value.copy(headroomPct = pct.coerceIn(0.0, 50.0))
        recalculate()
    }

    fun addFixture(name: String, qty: Double, watts: Double, isLength: Boolean) {
        val fix = LightingFixture(
            id = System.currentTimeMillis().toString(),
            name = name,
            quantityOrLength = qty.coerceAtLeast(0.1),
            wattsPerUnit = watts.coerceAtLeast(0.1),
            isLengthBased = isLength
        )
        _uiState.value = _uiState.value.copy(fixtures = _uiState.value.fixtures + fix)
        recalculate()
    }

    fun removeFixture(id: String) {
        _uiState.value = _uiState.value.copy(fixtures = _uiState.value.fixtures.filter { it.id != id })
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val rawWatts = s.fixtures.sumOf { it.totalWatts }

        // Headroom multiplier (e.g. 20% headroom -> min Watts = raw / 0.8)
        val minDriverWatts = if (s.headroomPct < 100.0) {
            rawWatts / (1.0 - (s.headroomPct / 100.0))
        } else rawWatts * 1.25

        // Find next standard size
        val recSize = LedDriverUiState.STANDARD_DRIVER_SIZES.firstOrNull { it >= minDriverWatts }
            ?: ((((minDriverWatts / 100).toInt()) + 1) * 100)

        val outputAmps = if (s.voltage.volts > 0) minDriverWatts / s.voltage.volts else 0.0
        val isClass2 = when (s.voltage) {
            SystemVoltage.V12_DC -> minDriverWatts <= 60.0
            SystemVoltage.V24_DC -> minDriverWatts <= 96.0
            SystemVoltage.V48_DC -> minDriverWatts <= 96.0
            SystemVoltage.V12_AC -> minDriverWatts <= 60.0
        }
        val acWatts = minDriverWatts / (s.driverEfficiencyPct / 100.0)

        _uiState.value = _uiState.value.copy(
            rawConnectedWatts = rawWatts,
            minDriverRatingWatts = minDriverWatts,
            outputCurrentAmps = outputAmps,
            recommendedDriverWattage = recSize,
            class2Compliant = isClass2,
            primaryAcLoadWatts = acWatts
        )
    }

    fun logDriverSizing() {
        val s = _uiState.value
        val summary = "${s.voltage.label}: Connected ${String.format("%.1f", s.rawConnectedWatts)}W -> Required Driver: ${s.recommendedDriverWattage}W (Min ${String.format("%.1f", s.minDriverRatingWatts)}W @ ${String.format("%.2f", s.outputCurrentAmps)}A, Class 2: ${if (s.class2Compliant) "YES" else "NO"})"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "ELECTRICAL",
                title = "LED Driver & Transformer Sizing",
                summary = summary,
                value = s.recommendedDriverWattage.toDouble()
            )
        }
    }
}
