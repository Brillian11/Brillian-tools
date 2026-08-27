package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class SegmentedRing(
    val ringIndex: Int,
    val outerDiameterInches: Double,
    val innerDiameterInches: Double,
    val thicknessInches: Double,
    val segmentCount: Int
)

data class SegmentCalculations(
    val miterAngleDeg: Double,
    val segmentOuterEdgeInches: Double,
    val segmentInnerEdgeInches: Double,
    val segmentDepthInches: Double,
    val boardStripWidthInches: Double,
    val totalBoardLengthInches: Double,
    val totalRingAreaSqIn: Double
)

class SegmentedTurningViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _segmentCount = MutableStateFlow(12)
    val segmentCount: StateFlow<Int> = _segmentCount.asStateFlow()

    private val _outerDiameter = MutableStateFlow(8.0)
    val outerDiameter: StateFlow<Double> = _outerDiameter.asStateFlow()

    private val _wallThickness = MutableStateFlow(0.75)
    val wallThickness: StateFlow<Double> = _wallThickness.asStateFlow()

    private val _ringThickness = MutableStateFlow(0.75)
    val ringThickness: StateFlow<Double> = _ringThickness.asStateFlow()

    private val _sawKerf = MutableStateFlow(0.125) // 1/8"
    val sawKerf: StateFlow<Double> = _sawKerf.asStateFlow()

    private val _ringStack = MutableStateFlow<List<SegmentedRing>>(
        listOf(
            SegmentedRing(1, 4.0, 3.0, 0.75, 12),
            SegmentedRing(2, 6.0, 4.8, 0.75, 12),
            SegmentedRing(3, 8.0, 6.5, 0.75, 12),
            SegmentedRing(4, 9.0, 7.5, 0.75, 12),
            SegmentedRing(5, 8.5, 7.0, 0.75, 12),
            SegmentedRing(6, 7.5, 6.2, 0.75, 12)
        )
    )
    val ringStack: StateFlow<List<SegmentedRing>> = _ringStack.asStateFlow()

    private val _selectedRingIndex = MutableStateFlow(0)
    val selectedRingIndex: StateFlow<Int> = _selectedRingIndex.asStateFlow()

    private val _calculations = MutableStateFlow(calculate(12, 8.0, 0.75, 0.75, 0.125))
    val calculations: StateFlow<SegmentCalculations> = _calculations.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    fun updateInputs(
        segments: Int = _segmentCount.value,
        od: Double = _outerDiameter.value,
        wall: Double = _wallThickness.value,
        thickness: Double = _ringThickness.value,
        kerf: Double = _sawKerf.value
    ) {
        _segmentCount.value = segments.coerceAtLeast(3)
        _outerDiameter.value = od.coerceAtLeast(1.0)
        _wallThickness.value = wall.coerceIn(0.1, (od / 2.0) - 0.1)
        _ringThickness.value = thickness.coerceAtLeast(0.1)
        _sawKerf.value = kerf.coerceAtLeast(0.0)

        _calculations.value = calculate(
            _segmentCount.value,
            _outerDiameter.value,
            _wallThickness.value,
            _ringThickness.value,
            _sawKerf.value
        )
        _lastLogSaved.value = false
    }

    private fun calculate(
        n: Int,
        od: Double,
        wall: Double,
        thickness: Double,
        kerf: Double
    ): SegmentCalculations {
        val miterAngle = 180.0 / n
        val angleRad = (miterAngle * PI) / 180.0
        val rOut = od / 2.0
        val id = (od - (wall * 2.0)).coerceAtLeast(0.1)
        val rIn = id / 2.0

        val outerEdge = 2.0 * rOut * tan(angleRad)
        val innerEdge = 2.0 * rIn * tan(angleRad)
        val depth = wall

        val stripWidth = wall + 0.125 // slight margin for safety & flattening
        val totalBoardLength = (outerEdge + kerf) * n + 2.0 // 2" clamping waste

        val outerArea = PI * rOut * rOut
        val innerArea = PI * rIn * rIn
        val ringArea = outerArea - innerArea

        return SegmentCalculations(
            miterAngleDeg = miterAngle,
            segmentOuterEdgeInches = outerEdge,
            segmentInnerEdgeInches = innerEdge,
            segmentDepthInches = depth,
            boardStripWidthInches = stripWidth,
            totalBoardLengthInches = totalBoardLength,
            totalRingAreaSqIn = ringArea
        )
    }

    fun addRingToStack() {
        val list = _ringStack.value.toMutableList()
        val nextIdx = list.size + 1
        val newRing = SegmentedRing(
            ringIndex = nextIdx,
            outerDiameterInches = _outerDiameter.value,
            innerDiameterInches = (_outerDiameter.value - _wallThickness.value * 2).coerceAtLeast(0.5),
            thicknessInches = _ringThickness.value,
            segmentCount = _segmentCount.value
        )
        list.add(newRing)
        _ringStack.value = list
    }

    fun removeRing(index: Int) {
        val list = _ringStack.value.toMutableList()
        if (index in list.indices && list.size > 1) {
            list.removeAt(index)
            _ringStack.value = list.mapIndexed { idx, r -> r.copy(ringIndex = idx + 1) }
        }
    }

    fun saveCalculationLog(projectName: String = "Segmented Walnut/Maple Bowl") {
        viewModelScope.launch {
            val calc = _calculations.value
            val n = _segmentCount.value
            val od = _outerDiameter.value

            toolLogRepository?.logToolActivity(
                toolType = "widget_segmented_turning",
                title = "Segmented Turning: $projectName ($n Segments, ${String.format("%.2f\"", od)} OD)",
                summary = "Miter: ${String.format("%.2f°", calc.miterAngleDeg)}, Outer Edge: ${String.format("%.3f\"", calc.segmentOuterEdgeInches)}, Inner Edge: ${String.format("%.3f\"", calc.segmentInnerEdgeInches)}, Strip Width: ${String.format("%.2f\"", calc.boardStripWidthInches)}, Total Board: ${String.format("%.1f\"", calc.totalBoardLengthInches)}",
                value = od
            )
            _lastLogSaved.value = true
        }
    }
}
