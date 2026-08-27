package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

enum class BenderMode {
    OFFSET_BEND,
    THREE_BEND_SADDLE,
    FOUR_BEND_SADDLE,
    STUB_UP_90
}

enum class OffsetAngle(val degrees: Double, val multiplier: Double, val shrinkPerInch: Double, val label: String) {
    DEG_10(10.0, 5.759, 0.0625, "10° (x6, 1/16\" shrink)"),
    DEG_22_5(22.5, 2.613, 0.1875, "22.5° (x2.6, 3/16\" shrink)"),
    DEG_30(30.0, 2.000, 0.2500, "30° (x2.0, 1/4\" shrink)"),
    DEG_45(45.0, 1.414, 0.3750, "45° (x1.4, 3/8\" shrink)"),
    DEG_60(60.0, 1.155, 0.5000, "60° (x1.15, 1/2\" shrink)")
}

data class BenderSpec(
    val conduitSize: String,
    val takeUpInches: Double,
    val gain90Inches: Double
)

data class ConduitBenderUiState(
    val benderMode: BenderMode = BenderMode.OFFSET_BEND,
    val offsetAngle: OffsetAngle = OffsetAngle.DEG_30,
    val selectedConduitSizeIndex: Int = 1, // 3/4" EMT

    // Inputs
    val obstacleHeightInches: Double = 4.0,
    val obstacleWidthInches: Double = 6.0,
    val distanceToObstacleInches: Double = 36.0,
    val desiredStubHeightInches: Double = 18.0,

    // Offset Bend Outputs
    val distanceBetweenMarksInches: Double = 8.0,
    val totalShrinkInches: Double = 1.0,
    val mark1DistanceInches: Double = 37.0, // Obstacle dist + shrink
    val mark2DistanceInches: Double = 45.0,

    // 3-Bend Saddle Outputs
    val saddleCenterAngleDeg: Double = 45.0,
    val saddleOuterAngleDeg: Double = 22.5,
    val saddleCenterMarkInches: Double = 36.375,
    val saddleSideMarkDistanceInches: Double = 5.0,

    // Stub-Up Outputs
    val stubTakeUpInches: Double = 6.0,
    val stubMarkDistanceInches: Double = 12.0,
    val totalGainInches: Double = 3.25
) {
    companion object {
        val CONDUIT_SPECS = listOf(
            BenderSpec("1/2\" EMT", 5.0, 2.625),
            BenderSpec("3/4\" EMT", 6.0, 3.250),
            BenderSpec("1\" EMT", 8.0, 4.000),
            BenderSpec("1-1/4\" EMT", 11.0, 5.875)
        )
    }
}

class ConduitBenderViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConduitBenderUiState())
    val uiState: StateFlow<ConduitBenderUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setBenderMode(mode: BenderMode) {
        _uiState.value = _uiState.value.copy(benderMode = mode)
        recalculate()
    }

    fun setOffsetAngle(angle: OffsetAngle) {
        _uiState.value = _uiState.value.copy(offsetAngle = angle)
        recalculate()
    }

    fun setConduitSizeIndex(idx: Int) {
        if (idx in ConduitBenderUiState.CONDUIT_SPECS.indices) {
            _uiState.value = _uiState.value.copy(selectedConduitSizeIndex = idx)
            recalculate()
        }
    }

    fun updateInputs(height: Double, width: Double, dist: Double, stub: Double) {
        _uiState.value = _uiState.value.copy(
            obstacleHeightInches = height.coerceAtLeast(0.25),
            obstacleWidthInches = width.coerceAtLeast(0.5),
            distanceToObstacleInches = dist.coerceAtLeast(1.0),
            desiredStubHeightInches = stub.coerceAtLeast(1.0)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val bender = ConduitBenderUiState.CONDUIT_SPECS[s.selectedConduitSizeIndex]

        // 1. Offset Bend
        val distBetween = s.obstacleHeightInches * s.offsetAngle.multiplier
        val shrink = s.obstacleHeightInches * s.offsetAngle.shrinkPerInch
        val m1 = s.distanceToObstacleInches + shrink
        val m2 = m1 + distBetween

        // 2. 3-Bend Saddle (45° Center, 22.5° Ends)
        val saddleShrink = s.obstacleHeightInches * (3.0 / 16.0)
        val saddleCenterMark = s.distanceToObstacleInches + saddleShrink
        val saddleSideDist = s.obstacleHeightInches * 2.5 // Standard 45° center spacing (2.5x per inch of rise)

        // 3. Stub-up 90°
        val stubMark = (s.desiredStubHeightInches - bender.takeUpInches).coerceAtLeast(0.0)

        _uiState.value = _uiState.value.copy(
            distanceBetweenMarksInches = distBetween,
            totalShrinkInches = shrink,
            mark1DistanceInches = m1,
            mark2DistanceInches = m2,
            saddleCenterMarkInches = saddleCenterMark,
            saddleSideMarkDistanceInches = saddleSideDist,
            stubTakeUpInches = bender.takeUpInches,
            stubMarkDistanceInches = stubMark,
            totalGainInches = bender.gain90Inches
        )
    }

    fun logBenderCalculation() {
        val s = _uiState.value
        val pipe = ConduitBenderUiState.CONDUIT_SPECS[s.selectedConduitSizeIndex].conduitSize
        val summary = when (s.benderMode) {
            BenderMode.OFFSET_BEND -> "Offset Bend (${s.offsetAngle.degrees}°): ${s.obstacleHeightInches}\" rise, Spacing=${String.format("%.2f", s.distanceBetweenMarksInches)}\", Shrink=${String.format("%.2f", s.totalShrinkInches)}\""
            BenderMode.THREE_BEND_SADDLE -> "3-Bend Saddle: ${s.obstacleHeightInches}\" obstacle, Center Mark=${String.format("%.2f", s.saddleCenterMarkInches)}\", Spacing=${String.format("%.2f", s.saddleSideMarkDistanceInches)}\""
            BenderMode.FOUR_BEND_SADDLE -> "4-Bend Saddle: ${s.obstacleHeightInches}\" rise x ${s.obstacleWidthInches}\" width, ${s.offsetAngle.degrees}° offsets"
            BenderMode.STUB_UP_90 -> "90° Stub-Up: ${s.desiredStubHeightInches}\" desired on $pipe (Take-up -${s.stubTakeUpInches}\", Mark at ${String.format("%.2f", s.stubMarkDistanceInches)}\")"
        }
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "ELECTRICAL",
                title = "Conduit Bender Layout",
                summary = "$pipe $summary",
                value = s.distanceBetweenMarksInches
            )
        }
    }
}
