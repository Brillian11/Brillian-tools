package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MotorPhase(val label: String) {
    SINGLE_PHASE("1-Phase AC"),
    THREE_PHASE("3-Phase AC")
}

data class IndustrialMotorUiState(
    val phase: MotorPhase = MotorPhase.THREE_PHASE,
    val voltage: Int = 460, // 115, 200, 208, 230, 460, 575
    val horsepower: Double = 15.0,
    val serviceFactor: Double = 1.15, // 1.0, 1.15, 1.25
    val motorEfficiency: Double = 91.0, // %
    val powerFactor: Double = 0.85,
    val codeLetter: String = "G", // NEMA Inrush Code G = 5.6 - 6.3 kVA/HP
    val ambientTempC: Int = 30,
    val wireInsulation: String = "75°C (THWN/RHW)", // 60C, 75C, 90C
    val conductorMaterial: String = "Copper", // Copper, Aluminum

    // Calculated Outputs
    val tableFla: Double = 21.0, // NEC Table Full Load Amps
    val nameplateKw: Double = 11.19,
    val minConductorAmpacity: Double = 26.25, // 125% of FLA
    val recommendedWireSize: String = "10 AWG THHN/THWN Cu",
    val overloadRatingAmps: Double = 26.25, // 125% or 115% based on SF
    val maxOverloadTripAmps: Double = 29.40, // 140% or 130%
    val timeDelayFuseStandardAmps: Int = 40, // 175% NEC 430.52
    val nonTimeDelayFuseAmps: Int = 70, // 300%
    val inverseTimeBreakerAmps: Int = 60, // 250%
    val instantaneousMcpAmps: Int = 175, // 800%
    val starterNemaSize: String = "NEMA Size 2",
    val starterIecCurrentAmps: Double = 25.0,
    val lockedRotorAmpsEstimate: Double = 126.0,
    val disconnectMinHpRating: Double = 15.0,
    val disconnectMinAmps: Double = 24.15, // 115% of FLA
    val calculationSummary: String = ""
)

class IndustrialMotorFlaViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndustrialMotorUiState())
    val uiState: StateFlow<IndustrialMotorUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setPhase(phase: MotorPhase) {
        val newVoltage = if (phase == MotorPhase.SINGLE_PHASE) 230 else 460
        _uiState.value = _uiState.value.copy(phase = phase, voltage = newVoltage)
        recalculate()
    }

    fun setVoltage(v: Int) {
        _uiState.value = _uiState.value.copy(voltage = v)
        recalculate()
    }

    fun setHorsepower(hp: Double) {
        _uiState.value = _uiState.value.copy(horsepower = hp.coerceAtLeast(0.1))
        recalculate()
    }

    fun setServiceFactor(sf: Double) {
        _uiState.value = _uiState.value.copy(serviceFactor = sf)
        recalculate()
    }

    fun setCodeLetter(letter: String) {
        _uiState.value = _uiState.value.copy(codeLetter = letter)
        recalculate()
    }

    fun setConductorMaterial(mat: String) {
        _uiState.value = _uiState.value.copy(conductorMaterial = mat)
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val hp = s.horsepower
        val v = s.voltage
        val is3Ph = s.phase == MotorPhase.THREE_PHASE

        // Lookup or calculate NEC FLA
        val fla = getNecTableFla(hp, v, is3Ph)
        val kw = hp * 0.7457

        // 1. Minimum Branch Circuit Conductor Ampacity (NEC 430.22: 125% of FLA)
        val minWireAmps = fla * 1.25
        val wireSize = determineWireSize(minWireAmps, s.conductorMaterial)

        // 2. Overload Protection (NEC 430.32)
        // SF >= 1.15: 125% standard, 140% max. SF < 1.15: 115% standard, 130% max.
        val olFactor = if (s.serviceFactor >= 1.15) 1.25 else 1.15
        val olMaxFactor = if (s.serviceFactor >= 1.15) 1.40 else 1.30
        val olAmps = fla * olFactor
        val olMaxAmps = fla * olMaxFactor

        // 3. Short-Circuit & Ground-Fault Protection (NEC Table 430.52)
        // Dual-element time-delay fuse: 175% (Next standard size up)
        val tdFuseAmps = getNextStandardFuseBreakerSize(fla * 1.75)
        // Non-time-delay fuse: 300%
        val ntdFuseAmps = getNextStandardFuseBreakerSize(fla * 3.00)
        // Inverse-time circuit breaker: 250%
        val cbAmps = getNextStandardFuseBreakerSize(fla * 2.50)
        // Instantaneous trip breaker (MCP): 800%
        val mcpAmps = (fla * 8.0).toInt()

        // 4. Starter NEMA & IEC Sizing
        val nemaSize = getNemaStarterSize(hp, v, is3Ph)
        val iecRating = fla * 1.15

        // 5. Inrush / Locked Rotor Current (LRA)
        val kvaPerHp = when (s.codeLetter) {
            "A" -> 1.5
            "B" -> 3.3
            "C" -> 3.7
            "D" -> 4.2
            "E" -> 4.7
            "F" -> 5.3
            "G" -> 6.0
            "H" -> 6.7
            "J" -> 7.5
            "K" -> 8.5
            "L" -> 9.5
            else -> 6.0
        }
        val lra = if (is3Ph) {
            (hp * kvaPerHp * 1000.0) / (v * 1.73205)
        } else {
            (hp * kvaPerHp * 1000.0) / v
        }

        // Disconnect Switch (NEC 430.110): Min 115% FLA
        val disconnectAmps = fla * 1.15

        val summary = "Motor: ${hp} HP (${String.format("%.2f", kw)} kW) @ ${v}V ${s.phase.label}\n" +
                "NEC FLA: ${String.format("%.1f", fla)} A | Wire: $wireSize (Min ${String.format("%.1f", minWireAmps)} A)\n" +
                "Overload Relay: ${String.format("%.1f", olAmps)} A (Max ${String.format("%.1f", olMaxAmps)} A)\n" +
                "Protection: Breaker ${cbAmps}A | TD Fuse ${tdFuseAmps}A | NEMA: $nemaSize"

        _uiState.value = s.copy(
            tableFla = fla,
            nameplateKw = kw,
            minConductorAmpacity = minWireAmps,
            recommendedWireSize = wireSize,
            overloadRatingAmps = olAmps,
            maxOverloadTripAmps = olMaxAmps,
            timeDelayFuseStandardAmps = tdFuseAmps,
            nonTimeDelayFuseAmps = ntdFuseAmps,
            inverseTimeBreakerAmps = cbAmps,
            instantaneousMcpAmps = mcpAmps,
            starterNemaSize = nemaSize,
            starterIecCurrentAmps = iecRating,
            lockedRotorAmpsEstimate = lra,
            disconnectMinHpRating = hp,
            disconnectMinAmps = disconnectAmps,
            calculationSummary = summary
        )
    }

    private fun getNecTableFla(hp: Double, voltage: Int, isThreePhase: Boolean): Double {
        if (isThreePhase) {
            // NEC Table 430.250 standard FLA lookup values for 460V & 230V
            val base460Fla = when {
                hp <= 0.5 -> 1.1
                hp <= 0.75 -> 1.6
                hp <= 1.0 -> 2.1
                hp <= 1.5 -> 3.0
                hp <= 2.0 -> 3.4
                hp <= 3.0 -> 4.8
                hp <= 5.0 -> 7.6
                hp <= 7.5 -> 11.0
                hp <= 10.0 -> 14.0
                hp <= 15.0 -> 21.0
                hp <= 20.0 -> 27.0
                hp <= 25.0 -> 34.0
                hp <= 30.0 -> 40.0
                hp <= 40.0 -> 52.0
                hp <= 50.0 -> 65.0
                hp <= 60.0 -> 77.0
                hp <= 75.0 -> 96.0
                hp <= 100.0 -> 124.0
                hp <= 125.0 -> 156.0
                hp <= 150.0 -> 180.0
                hp <= 200.0 -> 240.0
                else -> hp * 1.2
            }
            return when (voltage) {
                460 -> base460Fla
                230 -> base460Fla * 2.0
                208 -> base460Fla * (460.0 / 208.0)
                200 -> base460Fla * 2.3
                575 -> base460Fla * 0.8
                else -> (hp * 746.0) / (voltage * 1.73205 * 0.88 * 0.85)
            }
        } else {
            // NEC Table 430.248 (1-Phase AC)
            val base115Fla = when {
                hp <= 0.25 -> 5.8
                hp <= 0.33 -> 7.2
                hp <= 0.5 -> 9.8
                hp <= 0.75 -> 13.8
                hp <= 1.0 -> 16.0
                hp <= 1.5 -> 20.0
                hp <= 2.0 -> 24.0
                hp <= 3.0 -> 34.0
                hp <= 5.0 -> 56.0
                hp <= 7.5 -> 80.0
                hp <= 10.0 -> 100.0
                else -> hp * 10.0
            }
            return when (voltage) {
                115 -> base115Fla
                230 -> base115Fla / 2.0
                208 -> base115Fla * (115.0 / 208.0)
                200 -> base115Fla * (115.0 / 200.0)
                else -> (hp * 746.0) / (voltage * 0.85 * 0.82)
            }
        }
    }

    private fun determineWireSize(requiredAmps: Double, material: String): String {
        val isCopper = material.equals("Copper", ignoreCase = true)
        return if (isCopper) {
            when {
                requiredAmps <= 15.0 -> "14 AWG THHN Cu"
                requiredAmps <= 20.0 -> "12 AWG THHN Cu"
                requiredAmps <= 30.0 -> "10 AWG THHN Cu"
                requiredAmps <= 50.0 -> "8 AWG THHN Cu"
                requiredAmps <= 65.0 -> "6 AWG THHN Cu"
                requiredAmps <= 85.0 -> "4 AWG THHN Cu"
                requiredAmps <= 100.0 -> "3 AWG THHN Cu"
                requiredAmps <= 115.0 -> "2 AWG THHN Cu"
                requiredAmps <= 130.0 -> "1 AWG THHN Cu"
                requiredAmps <= 150.0 -> "1/0 AWG THHN Cu"
                requiredAmps <= 175.0 -> "2/0 AWG THHN Cu"
                requiredAmps <= 200.0 -> "3/0 AWG THHN Cu"
                requiredAmps <= 230.0 -> "4/0 AWG THHN Cu"
                requiredAmps <= 255.0 -> "250 kcmil THHN Cu"
                requiredAmps <= 285.0 -> "300 kcmil THHN Cu"
                requiredAmps <= 310.0 -> "350 kcmil THHN Cu"
                requiredAmps <= 380.0 -> "500 kcmil THHN Cu"
                else -> "2x 250 kcmil Cu (Parallel)"
            }
        } else {
            when {
                requiredAmps <= 15.0 -> "12 AWG Al"
                requiredAmps <= 25.0 -> "10 AWG Al"
                requiredAmps <= 40.0 -> "8 AWG Al"
                requiredAmps <= 50.0 -> "6 AWG Al"
                requiredAmps <= 65.0 -> "4 AWG Al"
                requiredAmps <= 75.0 -> "3 AWG Al"
                requiredAmps <= 90.0 -> "2 AWG Al"
                requiredAmps <= 100.0 -> "1 AWG Al"
                requiredAmps <= 120.0 -> "1/0 AWG Al"
                requiredAmps <= 135.0 -> "2/0 AWG Al"
                requiredAmps <= 155.0 -> "3/0 AWG Al"
                requiredAmps <= 180.0 -> "4/0 AWG Al"
                requiredAmps <= 205.0 -> "250 kcmil Al"
                requiredAmps <= 230.0 -> "300 kcmil Al"
                requiredAmps <= 250.0 -> "350 kcmil Al"
                requiredAmps <= 310.0 -> "500 kcmil Al"
                else -> "2x 300 kcmil Al (Parallel)"
            }
        }
    }

    private fun getNextStandardFuseBreakerSize(calculatedAmps: Double): Int {
        val standardRatings = listOf(15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 110, 125, 150, 175, 200, 225, 250, 300, 350, 400, 450, 500, 600, 800)
        return standardRatings.firstOrNull { it.toDouble() >= calculatedAmps } ?: standardRatings.last()
    }

    private fun getNemaStarterSize(hp: Double, voltage: Int, is3Ph: Boolean): String {
        return if (is3Ph) {
            if (voltage >= 460) {
                when {
                    hp <= 2.0 -> "NEMA Size 00 (Up to 2 HP @ 460V)"
                    hp <= 5.0 -> "NEMA Size 0 (Up to 5 HP @ 460V)"
                    hp <= 10.0 -> "NEMA Size 1 (Up to 10 HP @ 460V)"
                    hp <= 25.0 -> "NEMA Size 2 (Up to 25 HP @ 460V)"
                    hp <= 50.0 -> "NEMA Size 3 (Up to 50 HP @ 460V)"
                    hp <= 100.0 -> "NEMA Size 4 (Up to 100 HP @ 460V)"
                    hp <= 200.0 -> "NEMA Size 5 (Up to 200 HP @ 460V)"
                    else -> "NEMA Size 6 (>200 HP)"
                }
            } else {
                when {
                    hp <= 1.5 -> "NEMA Size 00 (Up to 1.5 HP @ 230V)"
                    hp <= 3.0 -> "NEMA Size 0 (Up to 3 HP @ 230V)"
                    hp <= 7.5 -> "NEMA Size 1 (Up to 7.5 HP @ 230V)"
                    hp <= 15.0 -> "NEMA Size 2 (Up to 15 HP @ 230V)"
                    hp <= 30.0 -> "NEMA Size 3 (Up to 30 HP @ 230V)"
                    hp <= 50.0 -> "NEMA Size 4 (Up to 50 HP @ 230V)"
                    hp <= 100.0 -> "NEMA Size 5 (Up to 100 HP @ 230V)"
                    else -> "NEMA Size 6 (>100 HP)"
                }
            }
        } else {
            when {
                hp <= 0.33 -> "NEMA Size 00 (1-Ph)"
                hp <= 1.0 -> "NEMA Size 0 (1-Ph)"
                hp <= 2.0 -> "NEMA Size 1 (1-Ph)"
                hp <= 3.0 -> "NEMA Size 2 (1-Ph)"
                else -> "NEMA Size 3 (1-Ph)"
            }
        }
    }

    fun saveToLogs() {
        viewModelScope.launch {
            val s = _uiState.value
            toolLogRepository.logToolActivity(
                toolType = "widget_industrial_motor_fla",
                title = "Industrial Motor FLA Sizer (NEC 430)",
                summary = "${s.horsepower} HP @ ${s.voltage}V ${s.phase.label} -> FLA: ${String.format("%.1f", s.tableFla)}A | Wire: ${s.recommendedWireSize} | Breaker: ${s.inverseTimeBreakerAmps}A",
                value = s.tableFla
            )
        }
    }
}
