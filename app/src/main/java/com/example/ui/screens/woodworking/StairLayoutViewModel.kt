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
    val isMetric: Boolean = false,
    // Inputs (in inches when imperial, in cm when metric)
    val totalRiseInches: Double = 108.0,       // 9 ft or 270 cm
    val targetRiserInches: Double = 7.5,       // Ideal 7.5" or 19.0 cm
    val treadRunInches: Double = 10.5,         // Ideal 10.5" or 26.5 cm
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
    val totalRunSubText: String = "136.5\"",
    val stringerLengthInches: Double = 174.0,
    val stringerLengthDisplay: String = "174.0\"",
    val exactRiserDisplay: String = "7.714\" each",
    val exactTreadRunDisplay: String = "10.5\" run each",
    val stringerThroatDepthInches: Double = 5.2,
    val stringerThroatDisplay: String = "5.20\"",
    val stairAngleDeg: Double = 36.3,
    val blondelComfortIndex: Double = 25.9,
    val blondelDisplay: String = "25.9\"",
    val calculatedHeadroomInches: Double = 84.0,
    
    // Building Code Flags (IRC 2021)
    val isRiserCodeCompliant: Boolean = true,
    val isTreadCodeCompliant: Boolean = true,
    val isHeadroomCodeCompliant: Boolean = true,
    val isThroatCodeCompliant: Boolean = true
)

class StairLayoutViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StairLayoutUiState())
    val uiState: StateFlow<StairLayoutUiState> = _uiState.asStateFlow()

    init {
        recalculateStairs()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            if (metric) {
                _uiState.value = _uiState.value.copy(
                    isMetric = true,
                    totalRiseInches = 270.0, // cm
                    targetRiserInches = 19.0, // cm
                    treadRunInches = 26.5, // cm
                    stairOpeningWellLengthInches = 300.0, // cm
                    upperFloorThicknessInches = 26.5 // cm
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isMetric = false,
                    totalRiseInches = 108.0, // inches
                    targetRiserInches = 7.5, // inches
                    treadRunInches = 10.5, // inches
                    stairOpeningWellLengthInches = 120.0, // inches
                    upperFloorThicknessInches = 10.5 // inches
                )
            }
            recalculateStairs()
        }
    }

    fun updateInputs(
        totalRise: Double,
        targetRiser: Double,
        treadRun: Double,
        lumberSize: String,
        wellLength: Double,
        upperFloorThick: Double
    ) {
        val isMetric = _uiState.value.isMetric
        val minRise = if (isMetric) 30.0 else 12.0
        val minTargetR = if (isMetric) 10.0 else 4.0
        val maxTargetR = if (isMetric) 30.0 else 12.0
        val minTread = if (isMetric) 15.0 else 8.0
        val maxTread = if (isMetric) 45.0 else 16.0
        val minWell = if (isMetric) 60.0 else 24.0
        val minFloor = if (isMetric) 10.0 else 4.0

        val stringerW = if (lumberSize.startsWith("2x10")) {
            if (isMetric) 23.5 else 9.25
        } else {
            if (isMetric) 28.58 else 11.25
        }

        _uiState.value = _uiState.value.copy(
            totalRiseInches = totalRise.coerceAtLeast(minRise),
            targetRiserInches = targetRiser.coerceIn(minTargetR, maxTargetR),
            treadRunInches = treadRun.coerceIn(minTread, maxTread),
            stringerLumberSize = lumberSize,
            stringerWidthInches = stringerW,
            stairOpeningWellLengthInches = wellLength.coerceAtLeast(minWell),
            upperFloorThicknessInches = upperFloorThick.coerceAtLeast(minFloor)
        )
        recalculateStairs()
    }

    private fun recalculateStairs() {
        val s = _uiState.value
        val H = s.totalRiseInches
        val targetR = s.targetRiserInches
        val T = s.treadRunInches
        val isMetric = s.isMetric

        val W_stringer = if (s.stringerLumberSize.startsWith("2x10")) {
            if (isMetric) 23.5 else 9.25
        } else {
            if (isMetric) 28.58 else 11.25
        }

        val wellL = s.stairOpeningWellLengthInches
        val floorThick = s.upperFloorThicknessInches

        // Exact Step Count
        val numRisers = (H / targetR).roundToInt().coerceAtLeast(2)
        val exactRiser = H / numRisers.toDouble()
        val numTreads = numRisers - 1
        val totalRun = numTreads * T

        // Hypotenuse Length
        val stringerLen = sqrt(H * H + totalRun * totalRun)

        // Incline Angle
        val angleRad = atan2(exactRiser, T)
        val angleDeg = Math.toDegrees(angleRad)

        // Throat depth
        val throat = W_stringer - (exactRiser * cos(angleRad))

        // Blondel rule (2R + T)
        val blondel = 2.0 * exactRiser + T

        // Headroom calculation
        val maxTreads = numTreads.coerceAtLeast(1)
        val stepsUnderWell = if (T > 0.0) (wellL / T).toInt().coerceIn(1, maxTreads) else 1
        val elevationAtHeader = stepsUnderWell * exactRiser
        val headroomOffset = if (isMetric) 61.0 else 24.0
        val headroom = H - elevationAtHeader - floorThick + headroomOffset

        // Code validations (IRC 2021)
        val riserOk = if (isMetric) exactRiser <= 19.7 else exactRiser <= 7.75
        val treadOk = if (isMetric) T >= 25.4 else T >= 10.0
        val headroomOk = if (isMetric) headroom >= 203.2 else headroom >= 80.0
        val throatOk = if (isMetric) throat >= 8.9 else throat >= 3.5

        val riserDisp = if (isMetric) "%.1f cm each".format(exactRiser) else "%.3f\" each".format(exactRiser)
        val treadDisp = if (isMetric) "%.1f cm run each".format(T) else "%.1f\" run each".format(T)

        val totalRunFtInStr = if (isMetric) {
            "%.2f m".format(totalRun / 100.0)
        } else {
            val runFt = (totalRun / 12.0).toInt()
            val runIn = totalRun % 12.0
            "${runFt}' ${String.format("%.1f", runIn)}\""
        }

        val totalRunSubStr = if (isMetric) "%.1f cm".format(totalRun) else "%.1f\"".format(totalRun)
        val stringerLenStr = if (isMetric) "%.2f m".format(stringerLen / 100.0) else "%.1f\"".format(stringerLen)
        val throatStr = if (isMetric) "%.1f cm".format(throat) else "%.2f\"".format(throat)
        val blondelStr = if (isMetric) "%.1f cm".format(blondel) else "%.1f\"".format(blondel)

        _uiState.value = s.copy(
            stepCountRisers = numRisers,
            treadCount = numTreads,
            exactRiserHeightInches = exactRiser,
            exactTreadRunInches = T,
            totalRunInches = totalRun,
            totalRunFeetInches = totalRunFtInStr,
            totalRunSubText = totalRunSubStr,
            stringerLengthInches = stringerLen,
            stringerLengthDisplay = stringerLenStr,
            exactRiserDisplay = riserDisp,
            exactTreadRunDisplay = treadDisp,
            stringerThroatDepthInches = throat,
            stringerThroatDisplay = throatStr,
            stairAngleDeg = angleDeg,
            blondelComfortIndex = blondel,
            blondelDisplay = blondelStr,
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
                summary = "${s.stepCountRisers} Risers @ ${s.exactRiserDisplay}, ${s.treadCount} Treads @ ${s.exactTreadRunDisplay} (Total Run: ${s.totalRunFeetInches})",
                value = s.stringerLengthInches
            )
        }
    }
}
