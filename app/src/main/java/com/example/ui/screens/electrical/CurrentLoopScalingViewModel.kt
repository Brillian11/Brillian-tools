package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class ScalingTransferFunction(val label: String) {
    LINEAR("Linear (Pressure, Temp, Level)"),
    SQUARE_ROOT("Square Root (DP Flow: Q ∝ √ΔP)")
}

enum class PlcDcsProfile(val label: String, val minCount: Int, val maxCount: Int) {
    SIEMENS_S7("Siemens S7 (0 - 27648)", 0, 27648),
    ALLEN_BRADLEY("Allen-Bradley (4000 - 20000)", 4000, 20000),
    MODBUS_16BIT("Standard 16-bit Unsigned (0 - 65535)", 0, 65535),
    ADC_12BIT("12-bit ADC (0 - 4095)", 0, 4095),
    ADC_10BIT("10-bit ADC (0 - 1023)", 0, 1023),
    CUSTOM("Custom PLC Range", 0, 10000)
}

enum class NamurStatus(val label: String, val statusColorHex: Long) {
    BURNOUT_BREAK("NAMUR Fault: Open Circuit / Break (<3.6 mA)", 0xFFD32F2F),
    UNDER_RANGE("NAMUR Warning: Under-range (3.6 - 3.8 mA)", 0xFFF57C00),
    NORMAL_PROCESS("Normal Valid Measurement (3.8 - 20.5 mA)", 0xFF388E3C),
    OVER_RANGE("NAMUR Warning: Over-range (20.5 - 21.0 mA)", 0xFFF57C00),
    SATURATION_HIGH("NAMUR Fault: Saturation / Short (>21.0 mA)", 0xFFD32F2F)
}

data class CurrentLoopUiState(
    // Process Span
    val pvMin: Double = 0.0,
    val pvMax: Double = 100.0,
    val engineeringUnit: String = "PSI", // PSI, bar, °C, °F, GPM, m³/h, %, meters
    val transferFunction: ScalingTransferFunction = ScalingTransferFunction.LINEAR,
    val plcProfile: PlcDcsProfile = PlcDcsProfile.SIEMENS_S7,

    // Active Input Mode: "MA", "PV", "PERCENT", "COUNTS"
    val currentMa: Double = 12.0, // 4.0 to 20.0 mA
    val processVariable: Double = 50.0,
    val percentage: Double = 50.0,
    val plcRawCounts: Int = 13824,

    // Loop Burden & Compliance Inputs
    val powerSupplyVdc: Double = 24.0, // VDC
    val txMinOperatingVdc: Double = 10.5, // VDC minimum for transmitter
    val senseResistorOhms: Double = 250.0, // 250 ohms (1-5V drop)
    val cableLengthFt: Double = 500.0,
    val wireResistanceOhmsPer1000Ft: Double = 2.5, // 18 AWG copper ≈ 2.5 ohms per 1000ft single way (5 ohms loop)
    val safetyBarrierOhms: Double = 0.0, // Intrinsically safe barrier resistance

    // Calculated Diagnostics
    val namurStatus: NamurStatus = NamurStatus.NORMAL_PROCESS,
    val voltageDropSenseV: Double = 3.0, // at current Ma
    val totalLoopBurdenOhms: Double = 252.5,
    val maxAllowableBurdenOhms: Double = 675.0,
    val loopVoltageMarginV: Double = 8.45,
    val isLoopBurdenCompliant: Boolean = true,
    val voltageAtTransmitterV: Double = 18.95,
    val calculationSummary: String = ""
)

class CurrentLoopScalingViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrentLoopUiState())
    val uiState: StateFlow<CurrentLoopUiState> = _uiState.asStateFlow()

    init {
        recalculateFromMa(12.0)
    }

    fun setProcessSpan(min: Double, max: Double, unit: String) {
        val validMax = if (max <= min) min + 1.0 else max
        _uiState.value = _uiState.value.copy(
            pvMin = min,
            pvMax = validMax,
            engineeringUnit = unit
        )
        recalculateFromMa(_uiState.value.currentMa)
    }

    fun setTransferFunction(func: ScalingTransferFunction) {
        _uiState.value = _uiState.value.copy(transferFunction = func)
        recalculateFromMa(_uiState.value.currentMa)
    }

    fun setPlcProfile(profile: PlcDcsProfile) {
        _uiState.value = _uiState.value.copy(plcProfile = profile)
        recalculateFromMa(_uiState.value.currentMa)
    }

    fun updateCurrentMa(ma: Double) {
        recalculateFromMa(ma.coerceIn(0.0, 24.0))
    }

    fun updateProcessVariable(pv: Double) {
        val s = _uiState.value
        val span = s.pvMax - s.pvMin
        val frac = if (span != 0.0) (pv - s.pvMin) / span else 0.0
        val clampedFrac = frac.coerceIn(0.0, 1.0)

        val ma = when (s.transferFunction) {
            ScalingTransferFunction.LINEAR -> 4.0 + clampedFrac * 16.0
            ScalingTransferFunction.SQUARE_ROOT -> 4.0 + (clampedFrac * clampedFrac) * 16.0
        }
        recalculateFromMa(ma)
    }

    fun updatePercentage(pct: Double) {
        val s = _uiState.value
        val clampedPct = pct.coerceIn(0.0, 100.0)
        val frac = clampedPct / 100.0
        val ma = when (s.transferFunction) {
            ScalingTransferFunction.LINEAR -> 4.0 + frac * 16.0
            ScalingTransferFunction.SQUARE_ROOT -> 4.0 + (frac * frac) * 16.0
        }
        recalculateFromMa(ma)
    }

    fun updatePlcCounts(counts: Int) {
        val s = _uiState.value
        val countSpan = s.plcProfile.maxCount - s.plcProfile.minCount
        val frac = if (countSpan != 0) (counts.toDouble() - s.plcProfile.minCount) / countSpan else 0.0
        val ma = 4.0 + frac.coerceIn(0.0, 1.0) * 16.0
        recalculateFromMa(ma)
    }

    fun setLoopHardware(supplyVdc: Double, txMinVdc: Double, senseOhms: Double, cableFt: Double) {
        _uiState.value = _uiState.value.copy(
            powerSupplyVdc = supplyVdc,
            txMinOperatingVdc = txMinVdc,
            senseResistorOhms = senseOhms,
            cableLengthFt = cableFt
        )
        recalculateFromMa(_uiState.value.currentMa)
    }

    private fun recalculateFromMa(ma: Double) {
        val s = _uiState.value

        // NAMUR NE43 Status
        val namur = when {
            ma < 3.6 -> NamurStatus.BURNOUT_BREAK
            ma < 3.8 -> NamurStatus.UNDER_RANGE
            ma <= 20.5 -> NamurStatus.NORMAL_PROCESS
            ma <= 21.0 -> NamurStatus.OVER_RANGE
            else -> NamurStatus.SATURATION_HIGH
        }

        // Fraction calculation based on 4-20mA span
        val normFraction = ((ma - 4.0) / 16.0).coerceIn(0.0, 1.0)

        // Process Variable calculation
        val pvFraction = when (s.transferFunction) {
            ScalingTransferFunction.LINEAR -> normFraction
            ScalingTransferFunction.SQUARE_ROOT -> sqrt(normFraction)
        }
        val pct = pvFraction * 100.0
        val pv = s.pvMin + pvFraction * (s.pvMax - s.pvMin)

        // PLC integer counts
        val countSpan = s.plcProfile.maxCount - s.plcProfile.minCount
        val counts = (s.plcProfile.minCount + normFraction * countSpan).toInt()

        // Loop Burden & Compliance
        // Cable loop resistance = 2 conductors * (length / 1000) * resistance/1000ft
        val rCable = 2.0 * (s.cableLengthFt / 1000.0) * s.wireResistanceOhmsPer1000Ft
        val rTotal = s.senseResistorOhms + rCable + s.safetyBarrierOhms

        // Max allowable loop burden at 20mA (0.020A): R_max = (V_supply - V_tx_min) / 0.02
        val maxBurden = (s.powerSupplyVdc - s.txMinOperatingVdc) / 0.02
        val currentAmps = ma / 1000.0
        val loopVoltageDropTotal = currentAmps * rTotal
        val vAtTx = s.powerSupplyVdc - loopVoltageDropTotal
        val margin = vAtTx - s.txMinOperatingVdc
        val isCompliant = margin >= 0.0 && rTotal <= maxBurden

        val summary = "4-20mA Signal: ${String.format("%.3f", ma)} mA -> PV: ${String.format("%.2f", pv)} ${s.engineeringUnit} (${String.format("%.1f", pct)}%)\n" +
                "PLC Raw Count: $counts (${s.plcProfile.label})\n" +
                "Status: ${namur.label} | Total Burden: ${String.format("%.1f", rTotal)} Ω (Max ${String.format("%.1f", maxBurden)} Ω)"

        _uiState.value = s.copy(
            currentMa = ma,
            processVariable = pv,
            percentage = pct,
            plcRawCounts = counts,
            namurStatus = namur,
            voltageDropSenseV = (ma / 1000.0) * s.senseResistorOhms,
            totalLoopBurdenOhms = rTotal,
            maxAllowableBurdenOhms = maxBurden,
            loopVoltageMarginV = margin,
            isLoopBurdenCompliant = isCompliant,
            voltageAtTransmitterV = vAtTx,
            calculationSummary = summary
        )
    }

    fun saveToLogs() {
        viewModelScope.launch {
            val s = _uiState.value
            toolLogRepository.logToolActivity(
                toolType = "widget_current_loop_scaling",
                title = "4–20 mA Loop & Scaling Calculator",
                summary = "${String.format("%.2f", s.currentMa)} mA [${s.pvMin} to ${s.pvMax} ${s.engineeringUnit}] -> PV: ${String.format("%.2f", s.processVariable)} ${s.engineeringUnit} (${String.format("%.1f", s.percentage)}%) | PLC: ${s.plcRawCounts}",
                value = s.currentMa
            )
        }
    }
}
