package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class CircuitType {
    DC,
    AC_SINGLE_PHASE,
    AC_THREE_PHASE
}

enum class CalculationInputMode {
    V_AND_I,  // Given Voltage & Current
    V_AND_R,  // Given Voltage & Resistance
    V_AND_P,  // Given Voltage & Power
    I_AND_R,  // Given Current & Resistance
    I_AND_P,  // Given Current & Power
    R_AND_P   // Given Resistance & Power
}

data class OhmsLawUiState(
    val circuitType: CircuitType = CircuitType.DC,
    val inputMode: CalculationInputMode = CalculationInputMode.V_AND_I,

    // Inputs
    val voltageV: Double = 120.0,
    val currentA: Double = 10.0,
    val resistanceOhm: Double = 12.0,
    val powerWatts: Double = 1200.0,
    val powerFactor: Double = 0.95, // For AC circuits (0.1 - 1.0)

    // Calculated Results
    val calculatedVoltage: Double = 120.0,
    val calculatedCurrent: Double = 10.0,
    val calculatedResistance: Double = 12.0,
    val calculatedRealPowerW: Double = 1200.0,
    val calculatedApparentPowerVA: Double = 1200.0,
    val calculatedReactivePowerVAR: Double = 0.0,
    val phaseAngleDegrees: Double = 0.0,

    // Energy estimates
    val dailyHours: Double = 8.0,
    val costPerKwh: Double = 0.15,
    val dailyKwh: Double = 9.6,
    val monthlyCostUsd: Double = 43.20
)

class OhmsLawViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OhmsLawUiState())
    val uiState: StateFlow<OhmsLawUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setCircuitType(type: CircuitType) {
        _uiState.value = _uiState.value.copy(circuitType = type)
        recalculate()
    }

    fun setInputMode(mode: CalculationInputMode) {
        _uiState.value = _uiState.value.copy(inputMode = mode)
        recalculate()
    }

    fun updateInputs(
        v: Double,
        i: Double,
        r: Double,
        p: Double,
        pf: Double,
        hours: Double = _uiState.value.dailyHours,
        rate: Double = _uiState.value.costPerKwh
    ) {
        _uiState.value = _uiState.value.copy(
            voltageV = v.coerceAtLeast(0.0001),
            currentA = i.coerceAtLeast(0.0001),
            resistanceOhm = r.coerceAtLeast(0.0001),
            powerWatts = p.coerceAtLeast(0.0001),
            powerFactor = pf.coerceIn(0.1, 1.0),
            dailyHours = hours.coerceIn(0.0, 24.0),
            costPerKwh = rate.coerceAtLeast(0.0)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val mode = s.inputMode
        val pf = if (s.circuitType == CircuitType.DC) 1.0 else s.powerFactor

        var v = s.voltageV
        var i = s.currentA
        var r = s.resistanceOhm
        var p = s.powerWatts

        val sqrt3 = sqrt(3.0)

        when (mode) {
            CalculationInputMode.V_AND_I -> {
                r = v / i
                p = when (s.circuitType) {
                    CircuitType.DC -> v * i
                    CircuitType.AC_SINGLE_PHASE -> v * i * pf
                    CircuitType.AC_THREE_PHASE -> sqrt3 * v * i * pf
                }
            }
            CalculationInputMode.V_AND_R -> {
                i = v / r
                p = when (s.circuitType) {
                    CircuitType.DC -> (v * v) / r
                    CircuitType.AC_SINGLE_PHASE -> (v * v) / r * pf
                    CircuitType.AC_THREE_PHASE -> sqrt3 * v * (v / (r * sqrt3)) * pf
                }
            }
            CalculationInputMode.V_AND_P -> {
                i = when (s.circuitType) {
                    CircuitType.DC -> p / v
                    CircuitType.AC_SINGLE_PHASE -> p / (v * pf)
                    CircuitType.AC_THREE_PHASE -> p / (sqrt3 * v * pf)
                }
                r = v / i
            }
            CalculationInputMode.I_AND_R -> {
                v = i * r
                p = when (s.circuitType) {
                    CircuitType.DC -> i * i * r
                    CircuitType.AC_SINGLE_PHASE -> i * i * r * pf
                    CircuitType.AC_THREE_PHASE -> sqrt3 * (i * r) * i * pf
                }
            }
            CalculationInputMode.I_AND_P -> {
                v = when (s.circuitType) {
                    CircuitType.DC -> p / i
                    CircuitType.AC_SINGLE_PHASE -> p / (i * pf)
                    CircuitType.AC_THREE_PHASE -> p / (sqrt3 * i * pf)
                }
                r = v / i
            }
            CalculationInputMode.R_AND_P -> {
                i = sqrt(p / r)
                v = i * r
            }
        }

        // Apparent & Reactive Power for Power Triangle
        val apparentPowerVA = when (s.circuitType) {
            CircuitType.DC -> p
            CircuitType.AC_SINGLE_PHASE -> v * i
            CircuitType.AC_THREE_PHASE -> sqrt3 * v * i
        }

        val reactivePowerVAR = if (s.circuitType == CircuitType.DC) {
            0.0
        } else {
            val sinPhi = sin(acos(pf.coerceIn(0.0, 1.0)))
            apparentPowerVA * sinPhi
        }

        val phaseAngleDeg = if (s.circuitType == CircuitType.DC) 0.0 else Math.toDegrees(acos(pf.coerceIn(0.0, 1.0)))

        // Energy & Cost
        val dailyKwh = (p * s.dailyHours) / 1000.0
        val monthlyCost = dailyKwh * 30.0 * s.costPerKwh

        _uiState.value = _uiState.value.copy(
            calculatedVoltage = v,
            calculatedCurrent = i,
            calculatedResistance = r,
            calculatedRealPowerW = p,
            calculatedApparentPowerVA = apparentPowerVA,
            calculatedReactivePowerVAR = reactivePowerVAR,
            phaseAngleDegrees = phaseAngleDeg,
            dailyKwh = dailyKwh,
            monthlyCostUsd = monthlyCost
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = String.format(
            "V=%.2f V, I=%.2f A, R=%.2f Ω, P=%.1f W (Apparent=%.1f VA, PF=%.2f, Angle=%.1f°)",
            s.calculatedVoltage, s.calculatedCurrent, s.calculatedResistance, s.calculatedRealPowerW,
            s.calculatedApparentPowerVA, s.powerFactor, s.phaseAngleDegrees
        )
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "ELECTRICAL",
                title = "Ohm's Law & Power Triangle",
                summary = "${s.circuitType.name}: $summary",
                value = s.calculatedRealPowerW
            )
        }
    }
}
