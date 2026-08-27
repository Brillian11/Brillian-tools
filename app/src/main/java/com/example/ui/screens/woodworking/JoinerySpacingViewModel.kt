package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class JoineryType {
    MORTISE_TENON,  // Dominos / Floating Tenons
    DOWELS,          // Dowel Pins
    POCKET_HOLES,    // Pocket Hole Screws
    BOX_JOINTS       // Finger / Box Joints
}

enum class SpacingDistributionMode {
    EQUAL_DIVISIONS, // Center to center = W / N, Edge = Spacing / 2
    FIXED_EDGE_MARGIN // Fixed edge distance, remaining divided equally among (N-1) spans
}

data class JoinerySpacingUiState(
    val joineryType: JoineryType = JoineryType.MORTISE_TENON,
    val spacingMode: SpacingDistributionMode = SpacingDistributionMode.EQUAL_DIVISIONS,
    
    // Inputs
    val workpieceWidthInches: Double = 18.0,
    val workpieceThicknessInches: Double = 0.75,
    val jointCount: Int = 3,
    val elementSizeInches: Double = 1.5,      // Tenon width, Dowel diameter, etc.
    val fixedEdgeMarginInches: Double = 1.5,  // When in FIXED_EDGE_MARGIN mode
    
    // Outputs
    val centerToCenterSpacingInches: Double = 6.0,
    val edgeMarginInches: Double = 3.0,
    val gapBetweenElementsInches: Double = 4.5,
    val centerlineCoordinatesInches: List<Double> = listOf(3.0, 9.0, 15.0),
    val centerlineCoordinatesMm: List<Double> = listOf(76.2, 228.6, 381.0)
)

class JoinerySpacingViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinerySpacingUiState())
    val uiState: StateFlow<JoinerySpacingUiState> = _uiState.asStateFlow()

    init {
        recalculateSpacing()
    }

    fun setJoineryType(type: JoineryType) {
        val defaultSize = when (type) {
            JoineryType.MORTISE_TENON -> 1.5 // 1.5" (e.g. 8x40 or 10x50 domino width)
            JoineryType.DOWELS -> 0.375       // 3/8" dowel diameter
            JoineryType.POCKET_HOLES -> 0.5   // Pocket hole pocket width
            JoineryType.BOX_JOINTS -> 0.5     // 1/2" finger joint pin
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
        _uiState.value = _uiState.value.copy(
            workpieceWidthInches = width.coerceAtLeast(1.0),
            workpieceThicknessInches = thickness.coerceAtLeast(0.1),
            jointCount = count.coerceIn(1, 24),
            elementSizeInches = elementSize.coerceAtLeast(0.1),
            fixedEdgeMarginInches = fixedEdge.coerceAtLeast(0.1)
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

        _uiState.value = _uiState.value.copy(
            centerToCenterSpacingInches = c2c,
            edgeMarginInches = edge,
            gapBetweenElementsInches = gap,
            centerlineCoordinatesInches = coords,
            centerlineCoordinatesMm = coords.map { it * 25.4 }
        )
    }

    fun logJoineryPlan() {
        val s = _uiState.value
        val coordsStr = s.centerlineCoordinatesInches.joinToString(", ") { String.format("%.2f\"", it) }
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "WOODWORKING",
                title = "Joinery Layout Spacing",
                summary = "${s.jointCount}x ${s.joineryType.name} across ${s.workpieceWidthInches}\" board: Pitch ${String.format("%.2f", s.centerToCenterSpacingInches)}\", Centers at [$coordsStr]",
                value = s.centerToCenterSpacingInches
            )
        }
    }
}
