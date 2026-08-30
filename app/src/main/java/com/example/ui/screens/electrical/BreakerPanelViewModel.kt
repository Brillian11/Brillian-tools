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
    SINGLE_PHASE_220V_PLN("220V 1-Phase (PLN Indonesia Standard)", false),
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

data class PlnPowerTier(val label: String, val va: Double, val breakerAmps: Int)

data class BreakerPanelUiState(
    val panelType: PanelSystemType = PanelSystemType.SINGLE_PHASE_220V_PLN,
    val mainBreakerAmps: Int = 25,
    val busbarRatingAmps: Int = 40,
    val circuits: List<BreakerCircuit> = listOf(
        BreakerCircuit("1", "Lampu & Stop Kontak R1 (PLN 900W)", 4, CircuitPoleType.SINGLE_POLE, "A", 900.0),
        BreakerCircuit("2", "AC & Water Heater R2 (PLN 1300W)", 6, CircuitPoleType.SINGLE_POLE, "A", 1300.0),
        BreakerCircuit("3", "Dapur & Pompa Air (PLN 2200W)", 10, CircuitPoleType.SINGLE_POLE, "A", 2200.0)
    ),
    // Calculated Phase Loads
    val phaseALoadVa: Double = 4400.0,
    val phaseBLoadVa: Double = 0.0,
    val phaseCLoadVa: Double = 0.0,
    val totalConnectedVa: Double = 4400.0,
    val phaseACurrentAmps: Double = 20.0,
    val phaseBCurrentAmps: Double = 0.0,
    val phaseCCurrentAmps: Double = 0.0,
    val neutralUnbalanceAmps: Double = 20.0,
    val maxPhaseCurrentAmps: Double = 20.0,
    val busbarUtilizationPct: Double = 50.0,
    val phaseImbalancePct: Double = 0.0,
    val isOverloaded: Boolean = false,
    val plnStandards: List<PlnPowerTier> = listOf(
        PlnPowerTier("450 VA (2A)", 450.0, 2),
        PlnPowerTier("900 VA (4A)", 900.0, 4),
        PlnPowerTier("1300 VA (6A)", 1300.0, 6),
        PlnPowerTier("2200 VA (10A)", 2200.0, 10),
        PlnPowerTier("3500 VA (16A)", 3500.0, 16),
        PlnPowerTier("4400 VA (20A)", 4400.0, 20),
        PlnPowerTier("5500 VA (25A)", 5500.0, 25),
        PlnPowerTier("6600 VA (30A)", 6600.0, 30),
        PlnPowerTier("7700 VA (35A)", 7700.0, 35),
        PlnPowerTier("11000 VA (50A)", 11000.0, 50)
    )
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
        val defaultMainAmps = if (type == PanelSystemType.SINGLE_PHASE_220V_PLN) 25 else 200
        _uiState.value = _uiState.value.copy(panelType = type, mainBreakerAmps = defaultMainAmps, busbarRatingAmps = defaultMainAmps)
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
                    "A" -> if (isThreePhase) "B" else "A"
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
            PanelSystemType.SINGLE_PHASE_220V_PLN -> 220.0
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
                    when (c.assignedPhase) {
                        "AB" -> { vaA += c.loadVa / 2.0; vaB += c.loadVa / 2.0 }
                        "BC" -> { vaB += c.loadVa / 2.0; vaC += c.loadVa / 2.0 }
                        "CA" -> { vaC += c.loadVa / 2.0; vaA += c.loadVa / 2.0 }
                        else -> { vaA += c.loadVa / 2.0; vaB += c.loadVa / 2.0 }
                    }
                }
                CircuitPoleType.THREE_POLE -> {
                    vaA += c.loadVa / 3.0
                    vaB += c.loadVa / 3.0
                    vaC += c.loadVa / 3.0
                }
            }
        }

        val iA = vaA / lineToNeutralV
        val iB = if (s.panelType == PanelSystemType.SINGLE_PHASE_220V_PLN) 0.0 else vaB / lineToNeutralV
        val iC = if (s.panelType.isThreePhase) vaC / lineToNeutralV else 0.0

        val inAmps = if (s.panelType == PanelSystemType.SINGLE_PHASE_220V_PLN) {
            iA
        } else if (!s.panelType.isThreePhase) {
            abs(iA - iB)
        } else {
            val term = (iA * iA) + (iB * iB) + (iC * iC) - (iA * iB) - (iB * iC) - (iC * iA)
            sqrt(max(0.0, term))
        }

        val maxI = if (s.panelType.isThreePhase) max(iA, max(iB, iC)) else max(iA, iB)
        val minI = if (s.panelType.isThreePhase) min(iA, min(iB, iC)) else min(iA, iB)
        val avgI = if (s.panelType.isThreePhase) (iA + iB + iC) / 3.0 else (iA + iB) / 2.0
        val imbalancePct = if (avgI > 0.0) ((maxI - minI) / avgI) * 100.0 else 0.0
        val busbarUtil = if (s.busbarRatingAmps > 0) (maxI / s.busbarRatingAmps.toDouble()) * 100.0 else 0.0
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
        val summary = "${s.panelType.voltageLabel} Panel: Total ${String.format("%.1f", s.totalConnectedVa / 1000.0)} kVA. Phase A=${String.format("%.1f", s.phaseACurrentAmps)}A (Busbar: ${String.format("%.1f", s.busbarUtilizationPct)}%)"
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
