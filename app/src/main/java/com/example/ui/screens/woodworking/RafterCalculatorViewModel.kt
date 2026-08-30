package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

data class RafterCalculatorUiState(
    val isMetric: Boolean = false,
    val buildingSpanFeet: Double = 24.0,       // Span in feet (imperial) or meters (metric)
    val pitchOver12: Double = 6.0,             // Pitch ratio or degrees
    val ridgeThicknessInches: Double = 1.5,    // in or mm
    val overhangInches: Double = 16.0,         // in or mm
    val rafterLumberNominal: String = "2x6 (5.5\")",
    val rafterDepthInches: Double = 5.5,
    val birdsmouthSeatCutInches: Double = 3.5,

    val buildingRunFtIn: String = "11' 11-1/4\"",
    val pitchAngleDeg: Double = 26.57,
    val plumbCutAngleDeg: Double = 63.43,
    val seatCutAngleDeg: Double = 26.57,
    val totalRiseFtIn: String = "5' 11-5/8\"",

    val commonRafterLengthInches: Double = 160.1,
    val commonRafterFtIn: String = "13' 4-1/8\"",
    val rafterTailLengthInches: Double = 17.89,
    val totalCommonLengthWithTailFtIn: String = "14' 10\"",

    val hipRafterFtIn: String = "17' 11-3/4\"",
    val hipPitchAngleDeg: Double = 19.47,
    val hipPlumbCutAngleDeg: Double = 70.53,

    val jackStepDownDisplay: String = "17.9\" per 16\" O.C.",
    val heightAbovePlateDisplay: String = "3.94\"",
    val totalRoofAreaDisplay: String = "356 sq ft"
)

class RafterCalculatorViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RafterCalculatorUiState())
    val uiState: StateFlow<RafterCalculatorUiState> = _uiState.asStateFlow()

    init {
        recalculateRafters()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(isMetric = metric)
            recalculateRafters()
        }
    }

    fun updateInputs(
        span: Double,
        pitch: Double,
        ridgeThick: Double,
        overhang: Double,
        lumberNominal: String,
        seatCut: Double
    ) {
        val depth = when {
            lumberNominal.contains("2x8") || lumberNominal.contains("200") -> 7.25
            lumberNominal.contains("2x10") || lumberNominal.contains("250") -> 9.25
            lumberNominal.contains("2x12") || lumberNominal.contains("300") -> 11.25
            else -> 5.5
        }
        _uiState.value = _uiState.value.copy(
            buildingSpanFeet = span.coerceAtLeast(0.5),
            pitchOver12 = pitch.coerceIn(1.0, 75.0),
            ridgeThicknessInches = ridgeThick.coerceAtLeast(0.0),
            overhangInches = overhang.coerceAtLeast(0.0),
            rafterLumberNominal = lumberNominal,
            rafterDepthInches = depth,
            birdsmouthSeatCutInches = seatCut.coerceAtLeast(0.5)
        )
        recalculateRafters()
    }

    private fun recalculateRafters() {
        val s = _uiState.value
        val isMetric = s.isMetric

        val spanInches = if (isMetric) s.buildingSpanFeet * 39.3701 else s.buildingSpanFeet * 12.0
        val ridgeIn = if (isMetric) s.ridgeThicknessInches / 25.4 else s.ridgeThicknessInches
        val overhangIn = if (isMetric) s.overhangInches / 25.4 else s.overhangInches
        val seatIn = if (isMetric) s.birdsmouthSeatCutInches / 25.4 else s.birdsmouthSeatCutInches

        val runInches = (spanInches - ridgeIn) / 2.0
        val pitchRad = atan2(s.pitchOver12, 12.0)
        val pitchDeg = Math.toDegrees(pitchRad)
        val pitchFraction = s.pitchOver12 / 12.0

        val totalRiseIn = runInches * pitchFraction
        val unitMultiplier = sqrt(1.0 + pitchFraction * pitchFraction)
        val commonLenIn = runInches * unitMultiplier
        val tailLenIn = overhangIn * unitMultiplier
        val totalCommonIn = commonLenIn + tailLenIn

        val hipUnitMultiplier = sqrt(2.0 + pitchFraction * pitchFraction)
        val hipLenIn = runInches * hipUnitMultiplier
        val hipAngleRad = atan2(s.pitchOver12, 16.97056)
        val hipAngleDeg = Math.toDegrees(hipAngleRad)

        val jack16In = 16.0 * unitMultiplier
        val hapIn = s.rafterDepthInches - (seatIn * sin(pitchRad))
        val roofAreaSqFt = (totalCommonIn / 12.0) * (spanInches / 12.0) * 2.0

        val runDisp = if (isMetric) "%.2f m".format(runInches * 0.0254) else formatFtIn(runInches)
        val riseDisp = if (isMetric) "%.2f m".format(totalRiseIn * 0.0254) else formatFtIn(totalRiseIn)
        val commonDisp = if (isMetric) "%.2f m".format(commonLenIn * 0.0254) else formatFtIn(commonLenIn)
        val totalCommonDisp = if (isMetric) "%.2f m".format(totalCommonIn * 0.0254) else formatFtIn(totalCommonIn)
        val hipDisp = if (isMetric) "%.2f m".format(hipLenIn * 0.0254) else formatFtIn(hipLenIn)
        val jackDisp = if (isMetric) "%.1f cm per 40cm O.C.".format(jack16In * 2.54) else "%.1f\" per 16\" O.C.".format(jack16In)
        val hapDisp = if (isMetric) "%.1f mm".format(hapIn * 25.4) else "%.2f\"".format(hapIn)
        val areaDisp = if (isMetric) "%.1f m²".format(roofAreaSqFt * 0.092903) else "%.0f sq ft".format(roofAreaSqFt)

        _uiState.value = s.copy(
            buildingRunFtIn = runDisp,
            pitchAngleDeg = pitchDeg,
            plumbCutAngleDeg = 90.0 - pitchDeg,
            seatCutAngleDeg = pitchDeg,
            totalRiseFtIn = riseDisp,
            commonRafterLengthInches = commonLenIn,
            commonRafterFtIn = commonDisp,
            rafterTailLengthInches = tailLenIn,
            totalCommonLengthWithTailFtIn = totalCommonDisp,
            hipRafterFtIn = hipDisp,
            hipPitchAngleDeg = hipAngleDeg,
            hipPlumbCutAngleDeg = 90.0 - hipAngleDeg,
            jackStepDownDisplay = jackDisp,
            heightAbovePlateDisplay = hapDisp,
            totalRoofAreaDisplay = areaDisp
        )
    }

    private fun formatFtIn(totalInches: Double): String {        val ft = (totalInches / 12.0).toInt()
        val remainingIn = totalInches % 12.0
        return "${ft}' ${String.format("%.1f", remainingIn)}\""
    }
}
