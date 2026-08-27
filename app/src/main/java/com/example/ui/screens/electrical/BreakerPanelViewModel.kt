package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

enum class PanelSystemType(val voltageLabel: String, val isThreePhase: Boolean) {
    SPLIT_PHASE_120_240("120/240V Split-Phase (1-Phase 3-Wire)", false),
    THREE_PHASE_120_208("120/208V 3-Phase 4-Wire (Wye)", true),
    THREE_PHASE_277_480("277/480V 3-Phase 4-Wire (Wye)", true)
}

enum class CircuitPoleType {
    SINGLE_POLE,
    DOUBLE_POLE,
    THREE_POLE
}

data class BreakerCircuit(
    val id: String,
    val name: String,
    val breakerRatingAmps: Int,
    val poleType: CircuitPoleType,
    val assignedPhase: String, // "A", "B", "C", "AB", "BC", "CA", "ABC"
    val loadVa: Double
)

data class BreakerPanelUiState(
    val panelType: PanelSystemType = PanelSystemType.SPLIT_PHASE_120_240,
    val mainBreakerAmps: Int = 200,
    val busbarRatingAmps: Int = 200,
    val circuits: List<BreakerCircuit> = listOf(
        BreakerCircuit("1", "Kitchen Small Appliances", 20, CircuitPoleType.SINGLE_POLE, "A", 1800.0),
        BreakerCircuit("2", "Living Room & Lighting", 15, CircuitPoleType.SINGLE_POLE, "B", 1200.0),
        BreakerCircuit("3", "HVAC Heat Pump", 40, CircuitPoleType.DOUBLE_POLE, "AB", 7200.0),
        BreakerCircuit("4", "EV Level 2 Charger", 50, CircuitPoleType.DOUBLE_POLE, "AB", 9600.0),
        BreakerCircuit("5", "Water Heater", 30, CircuitPoleType.DOUBLE_POLE, "AB", 4500.0),
        BreakerCircuit("6", "Master Bedroom & Bath", 20, CircuitPoleType.SINGLE_POLE, "A", 1500.0),
        BreakerCircuit("7", "Workshop & Garage Tools", 20, CircuitPoleType.SINGLE_POLE, "B", 2200.0)
    ),

    // Calculated Phase Loads
    val phaseALoadVa: Double = 13950.0,
    val phaseBLoadVa: Double = 14050.0,
    val phaseCLoadVa: Double = 0.0,
    val totalConnectedVa: Double = 28000.0,

    val phaseACurrentAmps: Double = 116.25,
    val phaseBCurrentAmps: Double = 117.08,
    val phaseCCurrentAmps: Double = 0.0,
    val neutralUnbalanceAmps: Double = 0.83,

    val maxPhaseCurrentAmps: Double = 117.08,
    val busbarUtilizationPct: Double = 58.54,
    val phaseImbalancePct: Double = 0.71,
    val isOverloaded: Boolean = false
)

class BreakerPanelViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreakerPanelUiState())
    val uiState: StateFlow<BreakerPanelUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setPanelType(type: PanelSystemType) {
        _uiState.value = _uiState.value.copy(panelType = type)
        recalculate()
    }

    fun setMainBreaker(amps: Int) {
        _uiState.value = _uiState.value.copy(mainBreakerAmps = amps, busbarRatingAmps = amps)
        recalculate()
    }

    fun addCircuit(name: String, breakerAmps: Int, pole: CircuitPoleType, phase: String, loadVa: Double) {
        val circuit = BreakerCircuit(
            id = System.currentTimeMillis().toString(),
            name = name,
            breakerRatingAmps = breakerAmps,
            poleType = pole,
            assignedPhase = phase,
            loadVa = loadVa.coerceAtLeast(0.0)
        )
        _uiState.value = _uiState.value.copy(circuits = _uiState.value.circuits + circuit)
        recalculate()
    }

    fun removeCircuit(id: String) {
        _uiState.value = _uiState.value.copy(circuits = _uiState.value.circuits.filter { it.id != id })
        recalculate()
    }

    fun toggleCircuitPhase(id: String) {
        val isThreePhase = _uiState.value.panelType.isThreePhase
        val updated = _uiState.value.circuits.map { c ->
            if (c.id == id && c.poleType == CircuitPoleType.SINGLE_POLE) {
                val nextPhase = when (c.assignedPhase) {
                    "A" -> "B"
                    "B" -> if (isThreePhase) "C" else "A"
                    "C" -> "A"
                    else -> "A"
                }
                c.copy(assignedPhase = nextPhase)
            } else c
        }
        _uiState.value = _uiState.value.copy(circuits = updated)
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        var vaA = 0.0
        var vaB = 0.0
        var vaC = 0.0

        val lineToNeutralV = when (s.panelType) {
            PanelSystemType.SPLIT_PHASE_120_240 -> 120.0
            PanelSystemType.THREE_PHASE_120_208 -> 120.0
            PanelSystemType.THREE_PHASE_277_480 -> 277.0
        }

        s.circuits.forEach { c ->
            when (c.poleType) {
                CircuitPoleType.SINGLE_POLE -> {
                    when (c.assignedPhase) {
                        "A" -> vaA += c.loadVa
                        "B" -> vaB += c.loadVa
                        "C" -> vaC += c.loadVa
                        else -> vaA += c.loadVa
                    }
                }
                CircuitPoleType.DOUBLE_POLE -> {
                    // 2-Pole splits equally between two legs
                    when (c.assignedPhase) {
                        "AB" -> { vaA += c.loadVa / 2.0; vaB += c.loadVa / 2.0 }
                        "BC" -> { vaB += c.loadVa / 2.0; vaC += c.loadVa / 2.0 }
                        "CA" -> { vaC += c.loadVa / 2.0; vaA += c.loadVa / 2.0 }
                        else -> { vaA += c.loadVa / 2.0; vaB += c.loadVa / 2.0 }
                    }
                }
                CircuitPoleType.THREE_POLE -> {
                    // 3-Pole splits equally across all 3 phases
                    vaA += c.loadVa / 3.0
                    vaB += c.loadVa / 3.0
                    vaC += c.loadVa / 3.0
                }
            }
        }

        val iA = vaA / lineToNeutralV
        val iB = vaB / lineToNeutralV
        val iC = if (s.panelType.isThreePhase) vaC / lineToNeutralV else 0.0

        // Neutral Current
        val inAmps = if (!s.panelType.isThreePhase) {
            abs(iA - iB)
        } else {
            // In = sqrt(Ia^2 + Ib^2 + Ic^2 - Ia*Ib - Ib*Ic - Ic*Ia)
            val term = (iA * iA) + (iB * iB) + (iC * iC) - (iA * iB) - (iB * iC) - (iC * iA)
            sqrt(max(0.0, term))
        }

        val maxI = if (s.panelType.isThreePhase) max(iA, max(iB, iC)) else max(iA, iB)
        val minI = if (s.panelType.isThreePhase) min(iA, min(iB, iC)) else min(iA, iB)
        val avgI = if (s.panelType.isThreePhase) (iA + iB + iC) / 3.0 else (iA + iB) / 2.0

        val imbalancePct = if (avgI > 0.0) ((maxI - minI) / avgI) * 100.0 else 0.0
        val busbarUtil = (maxI / s.busbarRatingAmps.toDouble()) * 100.0
        val isOver = maxI > s.mainBreakerAmps.toDouble()

        _uiState.value = _uiState.value.copy(
            phaseALoadVa = vaA,
            phaseBLoadVa = vaB,
            phaseCLoadVa = vaC,
            totalConnectedVa = vaA + vaB + vaC,
            phaseACurrentAmps = iA,
            phaseBCurrentAmps = iB,
            phaseCCurrentAmps = iC,
            neutralUnbalanceAmps = inAmps,
            maxPhaseCurrentAmps = maxI,
            busbarUtilizationPct = busbarUtil,
            phaseImbalancePct = imbalancePct,
            isOverloaded = isOver
        )
    }

    fun logPanelLoad() {
        val s = _uiState.value
        val summary = "${s.panelType.voltageLabel} Panel: Total ${String.format("%.1f", s.totalConnectedVa / 1000.0)} kVA. Phase A=${String.format("%.1f", s.phaseACurrentAmps)}A, Phase B=${String.format("%.1f", s.phaseBCurrentAmps)}A (Busbar: ${String.format("%.1f", s.busbarUtilizationPct)}%, Neutral=${String.format("%.1f", s.neutralUnbalanceAmps)}A)"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "ELECTRICAL",
                title = "Breaker Panel Load",
                summary = summary,
                value = s.maxPhaseCurrentAmps
            )
        }
    }
}
