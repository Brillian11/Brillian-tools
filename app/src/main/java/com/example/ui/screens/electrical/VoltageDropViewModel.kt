package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ConductorMaterial(val kFactor: Double, val label: String) {
    COPPER(12.9, "Copper (Cu, K=12.9)"),
    ALUMINUM(21.2, "Aluminum (Al, K=21.2)")
}

enum class PhaseSystem(val multiplier: Double, val label: String) {
    SINGLE_PHASE_2W(2.0, "1-Phase (2-Wire AC/DC)"),
    THREE_PHASE_3W(1.7320508, "3-Phase (3-Wire / 4-Wire AC)")
}

data class WireGaugeSpec(
    val name: String,
    val circularMils: Double,
    val metricMm2: Double,
    val ampacity75C: Int
)

data class VoltageDropUiState(
    val isMetric: Boolean = false,
    val material: ConductorMaterial = ConductorMaterial.COPPER,
    val phaseSystem: PhaseSystem = PhaseSystem.SINGLE_PHASE_2W,
    val sourceVoltageV: Double = 120.0,
    val loadCurrentA: Double = 15.0,
    val oneWayDistanceFt: Double = 100.0,
    val selectedWireIndex: Int = 1,

    val voltageDropV: Double = 3.63,
    val percentageDrop: Double = 3.03,
    val voltageAtLoadV: Double = 116.37,
    val powerLossWatts: Double = 54.45,
    val distanceDisplay: String = "100.0 ft (30.5 m)",
    val isNecCompliant3Percent: Boolean = true,
    val isNecCompliant5Percent: Boolean = true,
    val recommendedWireIndex: Int = 1,
    val recommendedWireName: String = "12 AWG (3.3 mm²)"
) {
    companion object {
        val WIRE_SPECS = listOf(
            WireGaugeSpec("14 AWG (2.08 mm²)", 4110.0, 2.08, 15),
            WireGaugeSpec("12 AWG (3.31 mm²)", 6530.0, 3.31, 20),
            WireGaugeSpec("10 AWG (5.26 mm²)", 10380.0, 5.26, 30),
            WireGaugeSpec("8 AWG (8.37 mm²)", 16510.0, 8.37, 50),
            WireGaugeSpec("6 AWG (13.3 mm²)", 26240.0, 13.3, 65),
            WireGaugeSpec("4 AWG (21.2 mm²)", 41740.0, 21.2, 85),
            WireGaugeSpec("2 AWG (33.6 mm²)", 66360.0, 33.6, 115),
            WireGaugeSpec("1/0 AWG (53.5 mm²)", 105600.0, 53.5, 150),
            WireGaugeSpec("2/0 AWG (67.4 mm²)", 133100.0, 67.4, 175),
            WireGaugeSpec("4/0 AWG (107 mm²)", 211600.0, 107.0, 230)
        )
    }
}

class VoltageDropViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoltageDropUiState())
    val uiState: StateFlow<VoltageDropUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(isMetric = metric)
            recalculate()
        }
    }

    fun setMaterial(mat: ConductorMaterial) {
        _uiState.value = _uiState.value.copy(material = mat)
        recalculate()
    }

    fun setPhase(phase: PhaseSystem) {
        _uiState.value = _uiState.value.copy(phaseSystem = phase)
        recalculate()
    }

    fun setWireIndex(index: Int) {
        if (index in VoltageDropUiState.WIRE_SPECS.indices) {
            _uiState.value = _uiState.value.copy(selectedWireIndex = index)
            recalculate()
        }
    }

    fun updateInputs(sourceV: Double, loadA: Double, dist: Double) {
        val distFt = if (_uiState.value.isMetric) dist * 3.28084 else dist
        _uiState.value = _uiState.value.copy(
            sourceVoltageV = sourceV.coerceAtLeast(1.0),
            loadCurrentA = loadA.coerceAtLeast(0.1),
            oneWayDistanceFt = distFt.coerceAtLeast(0.1)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val k = s.material.kFactor
        val mult = s.phaseSystem.multiplier
        val wire = VoltageDropUiState.WIRE_SPECS[s.selectedWireIndex]
        val cm = wire.circularMils

        val vDrop = (mult * k * s.loadCurrentA * s.oneWayDistanceFt) / cm
        val pctDrop = (vDrop / s.sourceVoltageV) * 100.0
        val vLoad = (s.sourceVoltageV - vDrop).coerceAtLeast(0.0)
        val powerLoss = vDrop * s.loadCurrentA

        val distStr = if (s.isMetric) "%.1f m".format(s.oneWayDistanceFt / 3.28084) else "%.1f ft".format(s.oneWayDistanceFt)

        _uiState.value = s.copy(
            voltageDropV = vDrop,
            percentageDrop = pctDrop,
            voltageAtLoadV = vLoad,
            powerLossWatts = powerLoss,
            distanceDisplay = distStr,
            isNecCompliant3Percent = pctDrop <= 3.0,
            isNecCompliant5Percent = pctDrop <= 5.0
        )
    }
}
