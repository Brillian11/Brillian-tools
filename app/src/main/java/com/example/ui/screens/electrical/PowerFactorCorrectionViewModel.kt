package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

enum class HarmonicReactorTuning(
    val label: String,
    val reactorPct: Double,
    val tuningFreq60Hz: Double,
    val application: String
) {
    NONE("Standard Capacitor (No Reactor)", 0.0, 0.0, "Clean linear loads (THD-V < 3%)"),
    PCT_5_67("5.67% Detuned Reactor (252 Hz)", 5.67, 252.0, "General industrial, 5th harmonic rejection"),
    PCT_7_00("7.00% Detuned Reactor (227 Hz)", 7.00, 227.0, "VFD drives, DC rectifiers, 5th & 7th harmonic protection"),
    PCT_14_00("14.00% Detuned Reactor (160 Hz)", 14.00, 160.0, "Heavy non-linear, 3rd harmonic & high distortion")
}

data class PowerFactorUiState(
    // System Inputs
    val realPowerKw: Double = 150.0, // kW
    val initialPowerFactor: Double = 0.72, // 0.50 to 0.95
    val targetPowerFactor: Double = 0.96, // 0.90 to 1.00
    val systemVoltageV: Int = 480, // 208, 240, 480, 600, 4160
    val systemFrequencyHz: Int = 60, // 50 or 60 Hz
    val demandCostPerKva: Double = 14.00, // $/kVA / month
    val transformerKva: Double = 500.0,
    val transformerZPct: Double = 5.75, // % impedance
    val harmonicReactor: HarmonicReactorTuning = HarmonicReactorTuning.PCT_7_00,

    // Calculated Power Outputs
    val initialKva: Double = 208.33,
    val correctedKva: Double = 156.25,
    val kvaReduction: Double = 52.08,
    val initialKvar: Double = 144.54,
    val correctedKvar: Double = 43.71,
    val requiredCapacitorKvar: Double = 100.83,
    val recommendedStandardKvarBank: Int = 100, // Standard steps: 25, 50, 75, 100, 150, 200, 300 kVAR

    // Currents & Losses
    val initialCurrentAmps: Double = 250.6,
    val correctedCurrentAmps: Double = 187.9,
    val currentReductionAmps: Double = 62.7,
    val lineLossReductionPct: Double = 43.7, // (1 - (I2/I1)^2)

    // Capacitance in microfarads
    val deltaCapacitanceUfPerPhase: Double = 383.9,
    val totalEffectiveKvarWithReactor: Double = 108.4,
    val parallelResonanceFrequencyHz: Double = 428.0,
    val isNearHarmonicHazard: Boolean = false,

    // Economic ROI
    val monthlyDemandSavings: Double = 729.12,
    val annualSavings: Double = 8749.44,
    val estimatedBankCost: Double = 4500.0,
    val simplePaybackMonths: Double = 6.2,
    val calculationSummary: String = ""
)

class PowerFactorCorrectionViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PowerFactorUiState())
    val uiState: StateFlow<PowerFactorUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setRealPowerKw(kw: Double) {
        _uiState.value = _uiState.value.copy(realPowerKw = kw.coerceAtLeast(1.0))
        recalculate()
    }

    fun setInitialPf(pf: Double) {
        _uiState.value = _uiState.value.copy(initialPowerFactor = pf.coerceIn(0.40, 0.98))
        recalculate()
    }

    fun setTargetPf(pf: Double) {
        _uiState.value = _uiState.value.copy(targetPowerFactor = pf.coerceIn(0.85, 1.00))
        recalculate()
    }

    fun setVoltage(v: Int) {
        _uiState.value = _uiState.value.copy(systemVoltageV = v)
        recalculate()
    }

    fun setFrequency(hz: Int) {
        _uiState.value = _uiState.value.copy(systemFrequencyHz = hz)
        recalculate()
    }

    fun setHarmonicReactor(reactor: HarmonicReactorTuning) {
        _uiState.value = _uiState.value.copy(harmonicReactor = reactor)
        recalculate()
    }

    fun setDemandCost(cost: Double) {
        _uiState.value = _uiState.value.copy(demandCostPerKva = cost.coerceAtLeast(0.0))
        recalculate()
    }

    fun setTransformerSpecs(kva: Double, zPct: Double) {
        _uiState.value = _uiState.value.copy(
            transformerKva = kva.coerceAtLeast(10.0),
            transformerZPct = zPct.coerceIn(1.0, 15.0)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val p = s.realPowerKw
        val pf1 = s.initialPowerFactor
        val pf2 = max(s.targetPowerFactor, pf1 + 0.01)

        // Phase angles
        val theta1 = acos(pf1)
        val theta2 = acos(pf2)

        // Apparent Power S (kVA)
        val s1 = p / pf1
        val s2 = p / pf2
        val deltaS = max(0.0, s1 - s2)

        // Reactive Power Q (kVAR)
        val q1 = p * tan(theta1)
        val q2 = p * tan(theta2)
        val reqKvar = max(0.0, q1 - q2)

        // Standard Commercial Capacitor Bank Steps: 25, 50, 75, 100, 125, 150, 200, 250, 300, 400, 500, 600, 800, 1000 kVAR
        val standardRatings = listOf(10, 15, 20, 25, 30, 40, 50, 60, 75, 100, 125, 150, 175, 200, 250, 300, 350, 400, 500, 600, 750, 1000)
        val recBank = standardRatings.firstOrNull { it.toDouble() >= reqKvar } ?: standardRatings.last()

        // Feeder Line Currents @ 3-Phase AC (I = S * 1000 / (sqrt(3) * V))
        val sqrt3 = 1.73205
        val i1 = (s1 * 1000.0) / (sqrt3 * s.systemVoltageV)
        val i2 = (s2 * 1000.0) / (sqrt3 * s.systemVoltageV)
        val deltaI = max(0.0, i1 - i2)

        // Line Heat I^2R loss reduction %
        val lineLossRedPct = (1.0 - (i2 / i1).pow(2)) * 100.0

        // Delta connected 3-phase capacitance per phase in microfarads (uF):
        // Q_phase = Q_total / 3
        // C_delta = (Q_total * 10^9) / (3 * 2 * pi * f * V^2)
        val v = s.systemVoltageV.toDouble()
        val f = s.systemFrequencyHz.toDouble()
        val cDeltaUf = (reqKvar * 1.0e9) / (3.0 * 2.0 * Math.PI * f * v * v)

        // Detuned reactor voltage rise factor: V_cap = V_net / (1 - p_reactor)
        val pReact = s.harmonicReactor.reactorPct / 100.0
        val effKvarWithReactor = if (pReact > 0.0) reqKvar / (1.0 - pReact) else reqKvar

        // Parallel Resonant Frequency Estimation: f_res = f_sys * sqrt(S_sc / Q_c)
        // Transformer short circuit capacity S_sc ≈ S_tx / (Z% / 100)
        val sSc = s.transformerKva / (s.transformerZPct / 100.0)
        val fRes = if (reqKvar > 0.0) f * sqrt(sSc / reqKvar) else 0.0

        // Check if resonant frequency is near 5th (300Hz @ 60Hz) or 7th (420Hz @ 60Hz) harmonic without reactor
        val isHarmonicHazard = s.harmonicReactor == HarmonicReactorTuning.NONE &&
                ((fRes in 270.0..330.0) || (fRes in 390.0..450.0))

        // Economic Calculations
        val monthlySav = deltaS * s.demandCostPerKva
        val annualSav = monthlySav * 12.0
        val estBankCost = recBank * 45.0 + (if (s.harmonicReactor != HarmonicReactorTuning.NONE) 2500.0 else 800.0)
        val paybackMonths = if (monthlySav > 0.0) estBankCost / monthlySav else 0.0

        val summary = "Power Factor Correction: ${String.format("%.2f", pf1)} -> ${String.format("%.2f", pf2)}\n" +
                "Capacitor Bank Required: ${String.format("%.1f", reqKvar)} kVAR (Install ${recBank} kVAR Bank)\n" +
                "Feeder Current: ${String.format("%.1f", i1)}A -> ${String.format("%.1f", i2)}A (-${String.format("%.1f", deltaI)}A, ${String.format("%.1f", lineLossRedPct)}% I²R loss reduction)\n" +
                "Demand Reduction: -${String.format("%.1f", deltaS)} kVA | Annual Savings: $${String.format("%.2f", annualSav)}/yr (Payback: ${String.format("%.1f", paybackMonths)} mo)"

        _uiState.value = s.copy(
            initialKva = s1,
            correctedKva = s2,
            kvaReduction = deltaS,
            initialKvar = q1,
            correctedKvar = q2,
            requiredCapacitorKvar = reqKvar,
            recommendedStandardKvarBank = recBank,
            initialCurrentAmps = i1,
            correctedCurrentAmps = i2,
            currentReductionAmps = deltaI,
            lineLossReductionPct = lineLossRedPct,
            deltaCapacitanceUfPerPhase = cDeltaUf,
            totalEffectiveKvarWithReactor = effKvarWithReactor,
            parallelResonanceFrequencyHz = fRes,
            isNearHarmonicHazard = isHarmonicHazard,
            monthlyDemandSavings = monthlySav,
            annualSavings = annualSav,
            estimatedBankCost = estBankCost,
            simplePaybackMonths = paybackMonths,
            calculationSummary = summary
        )
    }

    fun saveToLogs() {
        viewModelScope.launch {
            val s = _uiState.value
            toolLogRepository.logToolActivity(
                toolType = "widget_power_factor_correction",
                title = "Power Factor Correction & Harmonics",
                summary = "${s.realPowerKw} kW, PF ${String.format("%.2f", s.initialPowerFactor)} to ${String.format("%.2f", s.targetPowerFactor)} @ ${s.systemVoltageV}V -> Capacitor: ${String.format("%.1f", s.requiredCapacitorKvar)} kVAR (${s.recommendedStandardKvarBank} kVAR Bank) | -${String.format("%.1f", s.kvaReduction)} kVA",
                value = s.requiredCapacitorKvar
            )
        }
    }
}
