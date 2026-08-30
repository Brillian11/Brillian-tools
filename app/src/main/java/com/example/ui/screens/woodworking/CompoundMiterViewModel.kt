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
    val isMetric: Boolean = false,
    val mode: Int = 0,
    val crownSpringAngle: Double = 38.0,
    val wallCornerAngle: Double = 90.0,
    val isInsideCorner: Boolean = true,
    val crownMiterAngle: Double = 31.62,
    val crownBevelAngle: Double = 33.86,

    val numberOfSides: Int = 8,
    val wallSlopeFlareAngle: Double = 15.0,
    val outerRadiusInches: Double = 10.0,

    val polygonCornerAngle: Double = 135.0,
    val flatMiterAngle: Double = 22.5,
    val compoundMiterAngle: Double = 21.83,
    val compoundBevelAngle: Double = 5.98,
    val segmentLengthInches: Double = 8.28,
    val segmentLengthDisplay: String = "8.28\" (210.3 mm)"
)

class CompoundMiterViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompoundMiterUiState())
    val uiState: StateFlow<CompoundMiterUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(isMetric = metric)
            recalculate()
        }
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
        val radIn = if (_uiState.value.isMetric) radius / 25.4 else radius
        _uiState.value = _uiState.value.copy(
            numberOfSides = sides.coerceIn(3, 36),
            wallSlopeFlareAngle = slope.coerceIn(0.0, 60.0),
            outerRadiusInches = radIn.coerceAtLeast(0.1)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val springRad = Math.toRadians(s.crownSpringAngle)
        val cornerHalfRad = Math.toRadians(s.wallCornerAngle / 2.0)

        val miterRad = atan(sin(springRad) / tan(cornerHalfRad))
        val miterDeg = Math.toDegrees(miterRad)

        val bevelRad = asin(cos(springRad) * cos(cornerHalfRad))
        val bevelDeg = Math.toDegrees(bevelRad)

        val n = s.numberOfSides
        val halfCornerDeg = 180.0 / n
        val halfCornerRad = Math.toRadians(halfCornerDeg)
        val slopeRad = Math.toRadians(s.wallSlopeFlareAngle)

        val polyMiterRad = atan(cos(slopeRad) * tan(halfCornerRad))
        val polyMiterDeg = Math.toDegrees(polyMiterRad)

        val polyBevelRad = asin(sin(slopeRad) * sin(halfCornerRad))
        val polyBevelDeg = Math.toDegrees(polyBevelRad)

        val segLenIn = 2.0 * s.outerRadiusInches * tan(halfCornerRad)
        val segDisp = if (s.isMetric) "%.1f mm".format(segLenIn * 25.4) else "%.2f\"".format(segLenIn)

        _uiState.value = s.copy(
            crownMiterAngle = miterDeg,
            crownBevelAngle = bevelDeg,
            polygonCornerAngle = 180.0 - (360.0 / n),
            flatMiterAngle = halfCornerDeg,
            compoundMiterAngle = polyMiterDeg,
            compoundBevelAngle = polyBevelDeg,
            segmentLengthInches = segLenIn,
            segmentLengthDisplay = segDisp
        )
    }
}
