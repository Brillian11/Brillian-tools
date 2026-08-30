package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class JoineryType {
    MORTISE_TENON,
    DOWELS,
    POCKET_HOLES,
    BOX_JOINTS
}

enum class SpacingDistributionMode {
    EQUAL_DIVISIONS,
    FIXED_EDGE_MARGIN
}

data class JoinerySpacingUiState(
    val isMetric: Boolean = false,
    val joineryType: JoineryType = JoineryType.MORTISE_TENON,
    val spacingMode: SpacingDistributionMode = SpacingDistributionMode.EQUAL_DIVISIONS,

    val workpieceWidthInches: Double = 18.0,
    val workpieceThicknessInches: Double = 0.75,
    val jointCount: Int = 3,
    val elementSizeInches: Double = 1.5,
    val fixedEdgeMarginInches: Double = 1.5,

    val centerToCenterSpacingInches: Double = 6.0,
    val edgeMarginInches: Double = 3.0,
    val gapBetweenElementsInches: Double = 4.5,
    val centerlineCoordinatesInches: List<Double> = listOf(3.0, 9.0, 15.0),
    val centerlineCoordinatesMm: List<Double> = listOf(76.2, 228.6, 381.0),

    val c2cDisplay: String = "6.00\" (152.4 mm)",
    val edgeDisplay: String = "3.00\" (76.2 mm)",
    val gapDisplay: String = "4.50\" (114.3 mm)"
)

class JoinerySpacingViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinerySpacingUiState())
    val uiState: StateFlow<JoinerySpacingUiState> = _uiState.asStateFlow()

    init {
        recalculateSpacing()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(isMetric = metric)
            recalculateSpacing()
        }
    }

    fun setJoineryType(type: JoineryType) {
        val defaultSize = when (type) {
            JoineryType.MORTISE_TENON -> 1.5
            JoineryType.DOWELS -> 0.375
            JoineryType.POCKET_HOLES -> 0.5
            JoineryType.BOX_JOINTS -> 0.5
        }
        _uiState.value = _uiState.value.copy(
            joineryType = type,
            elementSizeInches = defaultSize
        )
        recalculateSpacing()
    }

    fun setSpacingMode(mode: SpacingDistributionMode) {
        _uiState.value = _uiState.value.copy(spacingMode = mode)
        recalculateSpacing()
    }

    fun updateInputs(width: Double, thickness: Double, count: Int, elementSize: Double, fixedEdge: Double) {
        val isMetric = _uiState.value.isMetric
        val wIn = if (isMetric) width / 25.4 else width
        val tIn = if (isMetric) thickness / 25.4 else thickness
        val elemIn = if (isMetric) elementSize / 25.4 else elementSize
        val edgeIn = if (isMetric) fixedEdge / 25.4 else fixedEdge

        _uiState.value = _uiState.value.copy(
            workpieceWidthInches = wIn.coerceAtLeast(0.1),
            workpieceThicknessInches = tIn.coerceAtLeast(0.01),
            jointCount = count.coerceIn(1, 48),
            elementSizeInches = elemIn.coerceAtLeast(0.01),
            fixedEdgeMarginInches = edgeIn.coerceAtLeast(0.01)
        )
        recalculateSpacing()
    }

    private fun recalculateSpacing() {
        val s = _uiState.value
        val W = s.workpieceWidthInches
        val N = s.jointCount
        val elem = s.elementSizeInches
        val coords = mutableListOf<Double>()
        var c2c = 0.0
        var edge = 0.0
        var gap = 0.0

        if (N <= 1) {
            coords.add(W / 2.0)
            c2c = 0.0
            edge = W / 2.0
            gap = W - elem
        } else if (s.spacingMode == SpacingDistributionMode.EQUAL_DIVISIONS) {
            c2c = W / N.toDouble()
            edge = c2c / 2.0
            gap = c2c - elem
            for (i in 0 until N) {
                coords.add(edge + i * c2c)
            }
        } else {
            edge = s.fixedEdgeMarginInches
            val span = W - (2.0 * edge)
            c2c = if (N > 1) span / (N - 1).toDouble() else 0.0
            gap = c2c - elem
            for (i in 0 until N) {
                coords.add(edge + i * c2c)
            }
        }

        val isMetric = s.isMetric
        val c2cStr = if (isMetric) "%.1f mm".format(c2c * 25.4) else "%.2f\"".format(c2c)
        val edgeStr = if (isMetric) "%.1f mm".format(edge * 25.4) else "%.2f\"".format(edge)
        val gapStr = if (isMetric) "%.1f mm".format(gap * 25.4) else "%.2f\"".format(gap)

        _uiState.value = _uiState.value.copy(
            centerToCenterSpacingInches = c2c,
            edgeMarginInches = edge,
            gapBetweenElementsInches = gap,
            centerlineCoordinatesInches = coords,
            centerlineCoordinatesMm = coords.map { it * 25.4 },
            c2cDisplay = c2cStr,
            edgeDisplay = edgeStr,
            gapDisplay = gapStr
        )
    }
}
