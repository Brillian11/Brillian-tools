package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

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
    val material: ConductorMaterial = ConductorMaterial.COPPER,
    val phaseSystem: PhaseSystem = PhaseSystem.SINGLE_PHASE_2W,
    val sourceVoltageV: Double = 120.0,
    val loadCurrentA: Double = 15.0,
    val oneWayDistanceFt: Double = 100.0,
    val selectedWireIndex: Int = 3, // #12 AWG default

    // Calculated Outputs
    val voltageDropV: Double = 3.63,
    val percentageDrop: Double = 3.03,
    val voltageAtLoadV: Double = 116.37,
    val powerLossWatts: Double = 54.45,
    val isNecCompliant3Percent: Boolean = false,
    val isNecCompliant5Percent: Boolean = true,
    val recommendedWireIndex: Int = 4, // Next size up if non-compliant
    val recommendedWireName: String = "10 AWG"
) {
    companion object {
        val WIRE_SPECS = listOf(
            WireGaugeSpec("14 AWG", 4110.0, 2.08, 15),
            WireGaugeSpec("12 AWG", 6530.0, 3.31, 20),
            WireGaugeSpec("10 AWG", 10380.0, 5.26, 30),
            WireGaugeSpec("8 AWG", 16510.0, 8.37, 50),
            WireGaugeSpec("6 AWG", 26240.0, 13.3, 65),
            WireGaugeSpec("4 AWG", 41740.0, 21.2, 85),
            WireGaugeSpec("3 AWG", 52620.0, 26.7, 100),
            WireGaugeSpec("2 AWG", 66360.0, 33.6, 115),
            WireGaugeSpec("1 AWG", 83690.0, 42.4, 130),
            WireGaugeSpec("1/0 AWG", 105600.0, 53.5, 150),
            WireGaugeSpec("2/0 AWG", 133100.0, 67.4, 175),
            WireGaugeSpec("3/0 AWG", 167800.0, 85.0, 200),
            WireGaugeSpec("4/0 AWG", 211600.0, 107.0, 230),
            WireGaugeSpec("250 kcmil", 250000.0, 127.0, 255),
            WireGaugeSpec("300 kcmil", 300000.0, 152.0, 285),
            WireGaugeSpec("350 kcmil", 350000.0, 177.0, 310),
            WireGaugeSpec("500 kcmil", 500000.0, 253.0, 380)
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

    fun updateInputs(sourceV: Double, loadA: Double, distFt: Double) {
        _uiState.value = _uiState.value.copy(
            sourceVoltageV = sourceV.coerceAtLeast(1.0),
            loadCurrentA = loadA.coerceAtLeast(0.1),
            oneWayDistanceFt = distFt.coerceAtLeast(1.0)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val k = s.material.kFactor
        val mult = s.phaseSystem.multiplier
        val wire = VoltageDropUiState.WIRE_SPECS[s.selectedWireIndex]
        val cm = wire.circularMils

        // Vdrop = (mult * K * I * L) / CM
        val vDrop = (mult * k * s.loadCurrentA * s.oneWayDistanceFt) / cm
        val pctDrop = (vDrop / s.sourceVoltageV) * 100.0
        val vLoad = (s.sourceVoltageV - vDrop).coerceAtLeast(0.0)
        val powerLoss = vDrop * s.loadCurrentA

        val is3Pct = pctDrop <= 3.0
        val is5Pct = pctDrop <= 5.0

        // Find minimum wire gauge to satisfy <= 3% drop
        var recIndex = s.selectedWireIndex
        for (i in VoltageDropUiState.WIRE_SPECS.indices) {
            val testCm = VoltageDropUiState.WIRE_SPECS[i].circularMils
            val testDrop = (mult * k * s.loadCurrentA * s.oneWayDistanceFt) / testCm
            val testPct = (testDrop / s.sourceVoltageV) * 100.0
            if (testPct <= 3.0 && VoltageDropUiState.WIRE_SPECS[i].ampacity75C >= s.loadCurrentA) {
                recIndex = i
                break
            }
        }
        val recWireName = VoltageDropUiState.WIRE_SPECS[recIndex].name

        _uiState.value = _uiState.value.copy(
            voltageDropV = vDrop,
            percentageDrop = pctDrop,
            voltageAtLoadV = vLoad,
            powerLossWatts = powerLoss,
            isNecCompliant3Percent = is3Pct,
            isNecCompliant5Percent = is5Pct,
            recommendedWireIndex = recIndex,
            recommendedWireName = recWireName
        )
    }

    fun logVoltageDrop() {
        val s = _uiState.value
        val wire = VoltageDropUiState.WIRE_SPECS[s.selectedWireIndex]
        val summary = "${wire.name} ${s.material.name} over ${s.oneWayDistanceFt}ft @ ${s.loadCurrentA}A: Drop = ${String.format("%.2f", s.voltageDropV)}V (${String.format("%.2f", s.percentageDrop)}%), Load = ${String.format("%.1f", s.voltageAtLoadV)}V"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "ELECTRICAL",
                title = "Voltage Drop Calculation",
                summary = summary,
                value = s.percentageDrop
            )
        }
    }
}
