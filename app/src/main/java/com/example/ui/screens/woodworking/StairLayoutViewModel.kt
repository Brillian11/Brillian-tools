package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class StairLayoutUiState(
    // Inputs
    val totalRiseInches: Double = 108.0,       // 9 ft floor to floor
    val targetRiserInches: Double = 7.5,       // Ideal 7.5"
    val treadRunInches: Double = 10.5,         // Ideal 10.5"
    val stringerLumberSize: String = "2x12 (11.25\")", // "2x10 (9.25\")" or "2x12 (11.25\")"
    val stringerWidthInches: Double = 11.25,
    val treadThicknessInches: Double = 1.0,
    val riserThicknessInches: Double = 0.75,
    val stairOpeningWellLengthInches: Double = 120.0,
    val upperFloorThicknessInches: Double = 10.5,
    
    // Outputs & Calculations
    val stepCountRisers: Int = 14,
    val treadCount: Int = 13,
    val exactRiserHeightInches: Double = 7.714,
    val exactTreadRunInches: Double = 10.5,
    val totalRunInches: Double = 136.5,
    val totalRunFeetInches: String = "11' 4-1/2\"",
    val stringerLengthInches: Double = 174.0,
    val stringerThroatDepthInches: Double = 5.2, // Minimum 3.5" required
    val stairAngleDeg: Double = 36.3,
    val blondelComfortIndex: Double = 25.9, // 2R + T (ideal 24" - 25.5")
    val calculatedHeadroomInches: Double = 84.0, // Minimum 80" (6'8")
    
    // Building Code Flags (IRC 2021)
    val isRiserCodeCompliant: Boolean = true,  // Max 7.75" (197mm)
    val isTreadCodeCompliant: Boolean = true,  // Min 10.0" (254mm)
    val isHeadroomCodeCompliant: Boolean = true,// Min 80.0" (2032mm)
    val isThroatCodeCompliant: Boolean = true  // Min 3.5" (89mm)
)

class StairLayoutViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StairLayoutUiState())
    val uiState: StateFlow<StairLayoutUiState> = _uiState.asStateFlow()

    init {
        recalculateStairs()
    }

    fun updateInputs(
        totalRise: Double,
        targetRiser: Double,
        treadRun: Double,
        lumberSize: String,
        wellLength: Double,
        upperFloorThick: Double
    ) {
        val stringerW = if (lumberSize.startsWith("2x10")) 9.25 else 11.25
        _uiState.value = _uiState.value.copy(
            totalRiseInches = totalRise.coerceAtLeast(12.0),
            targetRiserInches = targetRiser.coerceIn(4.0, 12.0),
            treadRunInches = treadRun.coerceIn(8.0, 16.0),
            stringerLumberSize = lumberSize,
            stringerWidthInches = stringerW,
            stairOpeningWellLengthInches = wellLength.coerceAtLeast(24.0),
            upperFloorThicknessInches = upperFloorThick.coerceAtLeast(4.0)
        )
        recalculateStairs()
    }

    private fun recalculateStairs() {
        val H = _uiState.value.totalRiseInches
        val targetR = _uiState.value.targetRiserInches
        val T = _uiState.value.treadRunInches
        val W_stringer = _uiState.value.stringerWidthInches
        val wellL = _uiState.value.stairOpeningWellLengthInches
        val floorThick = _uiState.value.upperFloorThicknessInches

        // Exact Step Count
        val numRisers = (H / targetR).roundToInt().coerceAtLeast(2)
        val exactRiser = H / numRisers.toDouble()
        val numTreads = numRisers - 1
        val totalRun = numTreads * T

        // Convert total run to feet & inches
        val runFt = (totalRun / 12.0).toInt()
        val runIn = totalRun % 12.0
        val runFtInStr = "${runFt}' ${String.format("%.1f", runIn)}\""

        // Stringer Hypotenuse Length
        val stringerLen = sqrt(H * H + totalRun * totalRun)

        // Incline Angle
        val angleRad = atan2(exactRiser, T)
        val angleDeg = Math.toDegrees(angleRad)

        // Stringer Throat Depth: Perpendicular wood left after cutting triangle notch
        // Notch depth perpendicular to stringer = R * cos(angle)
        // Throat = Lumber Width - (R * cos(angle))
        val throat = W_stringer - (exactRiser * cos(angleRad))

        // Blondel's Comfort Rule: 2 * Riser + Tread (Ideal 24 - 25.5")
        val blondel = 2.0 * exactRiser + T

        // Headroom calculation under opening well header
        // Header is at position wellL from bottom or top
        // Vertical distance from tread under ceiling edge to header bottom
        val headerDrop = H - floorThick
        val stepsUnderWell = (wellL / T).toInt().coerceIn(1, numTreads)
        val elevationAtHeader = stepsUnderWell * exactRiser
        val headroom = H - elevationAtHeader - floorThick + 24.0 // Standard well clearance

        // Code validations (IRC 2021)
        val riserOk = exactRiser <= 7.75
        val treadOk = T >= 10.0
        val headroomOk = headroom >= 80.0
        val throatOk = throat >= 3.5

        _uiState.value = _uiState.value.copy(
            stepCountRisers = numRisers,
            treadCount = numTreads,
            exactRiserHeightInches = exactRiser,
            exactTreadRunInches = T,
            totalRunInches = totalRun,
            totalRunFeetInches = runFtInStr,
            stringerLengthInches = stringerLen,
            stringerThroatDepthInches = throat,
            stairAngleDeg = angleDeg,
            blondelComfortIndex = blondel,
            calculatedHeadroomInches = headroom,
            isRiserCodeCompliant = riserOk,
            isTreadCodeCompliant = treadOk,
            isHeadroomCodeCompliant = headroomOk,
            isThroatCodeCompliant = throatOk
        )
    }

    fun logStairPlan() {
        val s = _uiState.value
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "WOODWORKING",
                title = "Stair Layout Stringer",
                summary = "${s.stepCountRisers} Risers @ ${String.format("%.2f", s.exactRiserHeightInches)}\", ${s.treadCount} Treads @ ${s.exactTreadRunInches}\" (Total Run: ${s.totalRunFeetInches})",
                value = s.stringerLengthInches
            )
        }
    }
}
