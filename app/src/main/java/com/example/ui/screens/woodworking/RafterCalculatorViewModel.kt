package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RafterCalculatorUiState(
    // Inputs
    val buildingSpanFeet: Double = 24.0,       // Full building width (e.g. 24 ft)
    val pitchOver12: Double = 6.0,             // 6 in 12 pitch
    val ridgeThicknessInches: Double = 1.5,    // 2x ridge beam (1.5")
    val overhangInches: Double = 16.0,         // Eaves / soffit overhang
    val rafterLumberNominal: String = "2x6 (5.5\")", // "2x6 (5.5\")", "2x8 (7.25\")", "2x10 (9.25\")"
    val rafterDepthInches: Double = 5.5,
    val birdsmouthSeatCutInches: Double = 3.5, // 2x4 top plate width (3.5")
    
    // Outputs
    val buildingRunFeet: Double = 11.9375,     // Span/2 - ridge/2
    val buildingRunFtIn: String = "11' 11-1/4\"",
    val pitchAngleDeg: Double = 26.57,
    val plumbCutAngleDeg: Double = 63.43,
    val seatCutAngleDeg: Double = 26.57,
    val totalRiseInches: Double = 71.625,
    val totalRiseFtIn: String = "5' 11-5/8\"",
    
    // Common Rafter
    val commonRafterLengthInches: Double = 160.1,
    val commonRafterFtIn: String = "13' 4-1/8\"",
    val rafterTailLengthInches: Double = 17.89, // Overhang tail
    val totalCommonLengthWithTailInches: Double = 178.0,
    val totalCommonLengthWithTailFtIn: String = "14' 10\"",
    
    // Hip & Valley Rafters
    val hipRafterLengthInches: Double = 215.8,
    val hipRafterFtIn: String = "17' 11-3/4\"",
    val hipPitchAngleDeg: Double = 19.47,
    val hipPlumbCutAngleDeg: Double = 70.53,
    
    // Jack Rafter Step-Down Spacing
    val jackStepDown16InOC: Double = 17.89,    // Spacing deduction per 16" O.C.
    val jackStepDown24InOC: Double = 26.83,    // Spacing deduction per 24" O.C.
    
    // Birdsmouth & HAP (Height Above Plate)
    val heightAbovePlateInches: Double = 3.94  // Rafter depth remaining above plate
)

class RafterCalculatorViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RafterCalculatorUiState())
    val uiState: StateFlow<RafterCalculatorUiState> = _uiState.asStateFlow()

    init {
        recalculateRafters()
    }

    fun updateInputs(
        spanFeet: Double,
        pitch: Double,
        ridgeThick: Double,
        overhang: Double,
        lumberNominal: String,
        seatCut: Double
    ) {
        val depth = when {
            lumberNominal.startsWith("2x8") -> 7.25
            lumberNominal.startsWith("2x10") -> 9.25
            lumberNominal.startsWith("2x12") -> 11.25
            else -> 5.5
        }

        _uiState.value = _uiState.value.copy(
            buildingSpanFeet = spanFeet.coerceAtLeast(4.0),
            pitchOver12 = pitch.coerceIn(1.0, 24.0),
            ridgeThicknessInches = ridgeThick.coerceIn(0.0, 6.0),
            overhangInches = overhang.coerceIn(0.0, 48.0),
            rafterLumberNominal = lumberNominal,
            rafterDepthInches = depth,
            birdsmouthSeatCutInches = seatCut.coerceIn(1.5, 6.0)
        )
        recalculateRafters()
    }

    private fun recalculateRafters() {
        val s = _uiState.value
        val spanIn = s.buildingSpanFeet * 12.0
        val runIn = (spanIn - s.ridgeThicknessInches) / 2.0
        val runFt = runIn / 12.0

        // Pitch angle
        val pitchFraction = s.pitchOver12 / 12.0
        val angleRad = atan2(s.pitchOver12, 12.0)
        val angleDeg = Math.toDegrees(angleRad)
        val plumbAngle = 90.0 - angleDeg

        // Total Rise
        val totalRiseIn = runIn * pitchFraction

        // Common rafter unit length multiplier
        val unitMultiplier = sqrt(1.0 + pitchFraction * pitchFraction)
        val commonLenIn = runIn * unitMultiplier
        val tailLenIn = s.overhangInches * unitMultiplier
        val totalCommonIn = commonLenIn + tailLenIn

        // Hip & Valley
        // Unit run for hip = sqrt(12^2 + 12^2) = 16.97056"
        val hipUnitMultiplier = sqrt(2.0 + pitchFraction * pitchFraction)
        val hipLenIn = runIn * hipUnitMultiplier
        val hipAngleRad = atan2(s.pitchOver12, 16.97056)
        val hipAngleDeg = Math.toDegrees(hipAngleRad)
        val hipPlumbAngle = 90.0 - hipAngleDeg

        // Jack Rafter Step-down deductions
        val jack16 = 16.0 * unitMultiplier
        val jack24 = 24.0 * unitMultiplier

        // Birdsmouth HAP (Height Above Plate)
        // Vertical depth of seat cut = SeatCut * tan(angle)
        // HAP = LumberDepth - (SeatCut * sin(angle))
        val hap = s.rafterDepthInches - (s.birdsmouthSeatCutInches * sin(angleRad))

        _uiState.value = _uiState.value.copy(
            buildingRunFeet = runFt,
            buildingRunFtIn = formatFtIn(runIn),
            pitchAngleDeg = angleDeg,
            plumbCutAngleDeg = plumbAngle,
            seatCutAngleDeg = angleDeg,
            totalRiseInches = totalRiseIn,
            totalRiseFtIn = formatFtIn(totalRiseIn),
            commonRafterLengthInches = commonLenIn,
            commonRafterFtIn = formatFtIn(commonLenIn),
            rafterTailLengthInches = tailLenIn,
            totalCommonLengthWithTailInches = totalCommonIn,
            totalCommonLengthWithTailFtIn = formatFtIn(totalCommonIn),
            hipRafterLengthInches = hipLenIn,
            hipRafterFtIn = formatFtIn(hipLenIn),
            hipPitchAngleDeg = hipAngleDeg,
            hipPlumbCutAngleDeg = hipPlumbAngle,
            jackStepDown16InOC = jack16,
            jackStepDown24InOC = jack24,
            heightAbovePlateInches = hap
        )
    }

    private fun formatFtIn(totalInches: Double): String {
        val ft = (totalInches / 12.0).toInt()
        val remainingIn = totalInches % 12.0
        return "${ft}' ${String.format("%.1f", remainingIn)}\""
    }

    fun logRafterSpecs() {
        val s = _uiState.value
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "WOODWORKING",
                title = "Roof Rafter Specs",
                summary = "Pitch ${s.pitchOver12}/12 (${String.format("%.1f", s.pitchAngleDeg)}°): Common Rafter ${s.commonRafterFtIn} (Total with tail: ${s.totalCommonLengthWithTailFtIn}), Hip: ${s.hipRafterFtIn}",
                value = s.commonRafterLengthInches
            )
        }
    }
}
