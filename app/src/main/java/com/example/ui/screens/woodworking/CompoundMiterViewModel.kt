package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class CompoundMiterUiState(
    val mode: Int = 0, // 0 = Crown Molding Flat on Bed, 1 = Polyhedral / Segmented Planter
    
    // Crown Molding Inputs
    val crownSpringAngle: Double = 38.0, // 38°, 45°, 52°
    val wallCornerAngle: Double = 90.0,  // Standard 90° inside/outside
    val isInsideCorner: Boolean = true,
    
    // Crown Molding Calculated Angles
    val crownMiterAngle: Double = 31.62, // Standard 31.62° for 38/52 crown on 90° corner
    val crownBevelAngle: Double = 33.86, // Standard 33.86° for 38/52 crown on 90° corner
    
    // Multi-Sided Polyhedral Hopper Inputs
    val numberOfSides: Int = 8,          // 4 = Square, 6 = Hexagon, 8 = Octagon, 12 = Segmented Ring
    val wallSlopeFlareAngle: Double = 15.0,// Flare angle from vertical (0° = vertical box)
    val outerRadiusInches: Double = 10.0,
    
    // Polyhedral Calculated Angles
    val polygonCornerAngle: Double = 135.0,
    val flatMiterAngle: Double = 22.5,
    val compoundMiterAngle: Double = 21.83,
    val compoundBevelAngle: Double = 5.98,
    val segmentLengthInches: Double = 8.28
)

class CompoundMiterViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompoundMiterUiState())
    val uiState: StateFlow<CompoundMiterUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setMode(m: Int) {
        _uiState.value = _uiState.value.copy(mode = m)
        recalculate()
    }

    fun updateCrownInputs(springAngle: Double, cornerAngle: Double, isInside: Boolean) {
        _uiState.value = _uiState.value.copy(
            crownSpringAngle = springAngle,
            wallCornerAngle = cornerAngle.coerceIn(30.0, 180.0),
            isInsideCorner = isInside
        )
        recalculate()
    }

    fun updatePolyhedralInputs(sides: Int, slope: Double, radius: Double) {
        _uiState.value = _uiState.value.copy(
            numberOfSides = sides.coerceIn(3, 36),
            wallSlopeFlareAngle = slope.coerceIn(0.0, 60.0),
            outerRadiusInches = radius.coerceAtLeast(1.0)
        )
        recalculate()
    }

    private fun recalculate() {
        // Crown Molding Math (flat on miter table)
        val springRad = Math.toRadians(_uiState.value.crownSpringAngle)
        val cornerHalfRad = Math.toRadians(_uiState.value.wallCornerAngle / 2.0)

        // Miter = atan(sin(spring) / tan(corner/2))
        val miterRad = atan(sin(springRad) / tan(cornerHalfRad))
        val miterDeg = Math.toDegrees(miterRad)

        // Bevel = asin(cos(spring) * cos(corner/2))
        val bevelRad = asin(cos(springRad) * cos(cornerHalfRad))
        val bevelDeg = Math.toDegrees(bevelRad)

        // Polyhedral Box Math
        val n = _uiState.value.numberOfSides
        val halfCornerDeg = 180.0 / n
        val halfCornerRad = Math.toRadians(halfCornerDeg)
        val slopeRad = Math.toRadians(_uiState.value.wallSlopeFlareAngle)

        val polyMiterRad = atan(cos(slopeRad) * tan(halfCornerRad))
        val polyMiterDeg = Math.toDegrees(polyMiterRad)

        val polyBevelRad = asin(sin(slopeRad) * sin(halfCornerRad))
        val polyBevelDeg = Math.toDegrees(polyBevelRad)

        val segLen = 2.0 * _uiState.value.outerRadiusInches * tan(halfCornerRad)

        _uiState.value = _uiState.value.copy(
            crownMiterAngle = miterDeg,
            crownBevelAngle = bevelDeg,
            polygonCornerAngle = 180.0 - (360.0 / n),
            flatMiterAngle = halfCornerDeg,
            compoundMiterAngle = polyMiterDeg,
            compoundBevelAngle = polyBevelDeg,
            segmentLengthInches = segLen
        )
    }

    fun logMiterPlan() {
        val s = _uiState.value
        val summary = if (s.mode == 0) {
            "Crown Molding (${s.crownSpringAngle}° Spring): Miter ${String.format("%.2f", s.crownMiterAngle)}°, Bevel ${String.format("%.2f", s.crownBevelAngle)}°"
        } else {
            "${s.numberOfSides}-Sided Polyhedral (${s.wallSlopeFlareAngle}° Slope): Miter ${String.format("%.2f", s.compoundMiterAngle)}°, Bevel ${String.format("%.2f", s.compoundBevelAngle)}°"
        }

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "WOODWORKING",
                title = "Compound Miter & Bevel",
                summary = summary,
                value = if (s.mode == 0) s.crownMiterAngle else s.compoundMiterAngle
            )
        }
    }
}
