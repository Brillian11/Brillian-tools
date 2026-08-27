package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class CogoCalcMode(val label: String) {
    STATION_OFFSET_TO_COORD("Station & Offset → (N, E, Z)"),
    COORD_TO_STATION_OFFSET("Survey Point (N, E) → Station & Offset (Inverse)"),
    CURVE_SOLVER("Horizontal Curve Parameters (PI, PC, PT, Delta, R)"),
    SUPERELEVATION_GRADE("Profile Elevation & Cross-Slope Grade")
}

enum class StationFormat {
    US_HUNDRED, // 10+00.00
    METRIC_THOUSAND // 1+000.000
}

data class StationingCogoUiState(
    val isMetric: Boolean = false,
    val mode: CogoCalcMode = CogoCalcMode.STATION_OFFSET_TO_COORD,

    // Alignment Tangent Origin (POB / Start Point)
    val startStation: Double = 1000.0, // 10+00.00 (ft or m)
    val startNorthing: Double = 5000.0, // N
    val startEasting: Double = 10000.0, // E
    val startElevation: Double = 120.0, // Z
    val azimuthDegrees: Double = 45.0, // 0 = North, 90 = East, 45 = NE
    val longitudinalGradePct: Double = 2.0, // +2.0% uphill

    // Target Station & Offset
    val targetStation: Double = 1250.0, // 12+50.00
    val targetOffset: Double = 15.0, // + is Right, - is Left

    // Inverse Survey Point Input (N, E)
    val pointNorthing: Double = 5180.0,
    val pointEasting: Double = 10165.0,
    val pointElevation: Double = 124.5,

    // Horizontal Curve Inputs
    val curveRadius: Double = 500.0, // R (ft or m)
    val deltaAngleDeg: Double = 35.0, // Deflection angle (deg)
    val piStation: Double = 1500.0, // P.I. station

    // Cross-Section & Superelevation
    val laneWidth: Double = 12.0, // ft or m
    val normalCrownPct: Double = -2.0, // -2.0% downward to ditch
    val superElevationRatePct: Double = 4.0, // +4.0% banked curve

    // Calculated Results
    val calculatedNorthing: Double = 0.0,
    val calculatedEasting: Double = 0.0,
    val calculatedElevation: Double = 0.0,
    val calculatedCenterlineZ: Double = 0.0,
    val calculatedEdgeZ: Double = 0.0,

    // Inverse Results
    val inverseStation: Double = 0.0,
    val inverseOffset: Double = 0.0, // + Right, - Left
    val inversePerpendicularDist: Double = 0.0,
    val inverseElevationDiff: Double = 0.0,

    // Horizontal Curve Outputs
    val curveTangentLength: Double = 0.0,
    val curveArcLength: Double = 0.0,
    val curveChordLength: Double = 0.0,
    val curveExternalDist: Double = 0.0,
    val curveMiddleOrd: Double = 0.0,
    val pcStation: Double = 0.0,
    val ptStation: Double = 0.0,
    val degreeOfCurve: Double = 0.0
)

class StationingCogoViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationingCogoUiState())
    val uiState: StateFlow<StationingCogoUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setMetric(metric: Boolean) {
        _uiState.value = _uiState.value.copy(isMetric = metric)
        recalculate()
    }

    fun setMode(mode: CogoCalcMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
        recalculate()
    }

    fun updateInputs(
        startStation: Double? = null,
        startNorthing: Double? = null,
        startEasting: Double? = null,
        startElevation: Double? = null,
        azimuthDegrees: Double? = null,
        longitudinalGradePct: Double? = null,
        targetStation: Double? = null,
        targetOffset: Double? = null,
        pointNorthing: Double? = null,
        pointEasting: Double? = null,
        pointElevation: Double? = null,
        curveRadius: Double? = null,
        deltaAngleDeg: Double? = null,
        piStation: Double? = null,
        laneWidth: Double? = null,
        normalCrownPct: Double? = null,
        superElevationRatePct: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            startStation = startStation ?: _uiState.value.startStation,
            startNorthing = startNorthing ?: _uiState.value.startNorthing,
            startEasting = startEasting ?: _uiState.value.startEasting,
            startElevation = startElevation ?: _uiState.value.startElevation,
            azimuthDegrees = azimuthDegrees ?: _uiState.value.azimuthDegrees,
            longitudinalGradePct = longitudinalGradePct ?: _uiState.value.longitudinalGradePct,
            targetStation = targetStation ?: _uiState.value.targetStation,
            targetOffset = targetOffset ?: _uiState.value.targetOffset,
            pointNorthing = pointNorthing ?: _uiState.value.pointNorthing,
            pointEasting = pointEasting ?: _uiState.value.pointEasting,
            pointElevation = pointElevation ?: _uiState.value.pointElevation,
            curveRadius = curveRadius ?: _uiState.value.curveRadius,
            deltaAngleDeg = deltaAngleDeg ?: _uiState.value.deltaAngleDeg,
            piStation = piStation ?: _uiState.value.piStation,
            laneWidth = laneWidth ?: _uiState.value.laneWidth,
            normalCrownPct = normalCrownPct ?: _uiState.value.normalCrownPct,
            superElevationRatePct = superElevationRatePct ?: _uiState.value.superElevationRatePct
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value

        // 1. Tangent Station & Offset → (N, E, Z)
        val distAlong = s.targetStation - s.startStation
        val azRad = Math.toRadians(s.azimuthDegrees)

        // Centerline point at target station
        val clN = s.startNorthing + distAlong * cos(azRad)
        val clE = s.startEasting + distAlong * sin(azRad)
        val clZ = s.startElevation + distAlong * (s.longitudinalGradePct / 100.0)

        // Perpendicular offset azimuth (Right is +90 deg, Left is -90 deg)
        val offsetAzRad = azRad + Math.toRadians(90.0)
        val targetN = clN + s.targetOffset * cos(offsetAzRad)
        val targetE = clE + s.targetOffset * sin(offsetAzRad)

        // Cross-slope adjustment to elevation
        val crossSlope = if (s.targetOffset >= 0) s.normalCrownPct else s.normalCrownPct
        val edgeZ = clZ + (abs(s.targetOffset) * (crossSlope / 100.0))

        // 2. Inverse Point (N, E) → Station & Offset
        val dN = s.pointNorthing - s.startNorthing
        val dE = s.pointEasting - s.startEasting
        val projectedDist = dN * cos(azRad) + dE * sin(azRad)
        val invStation = s.startStation + projectedDist

        // Cross-product for perpendicular offset (+ for Right, - for Left)
        val invOffset = dE * cos(azRad) - dN * sin(azRad)
        val perpDist = abs(invOffset)
        val invClZ = s.startElevation + projectedDist * (s.longitudinalGradePct / 100.0)
        val invElevDiff = s.pointElevation - invClZ

        // 3. Horizontal Curve Parameters
        val deltaRad = Math.toRadians(s.deltaAngleDeg)
        val r = if (s.curveRadius > 0) s.curveRadius else 500.0
        val t = r * tan(deltaRad / 2.0)
        val l = r * deltaRad
        val c = 2.0 * r * sin(deltaRad / 2.0)
        val e = r * (1.0 / cos(deltaRad / 2.0) - 1.0)
        val m = r * (1.0 - cos(deltaRad / 2.0))
        val pc = s.piStation - t
        val pt = pc + l
        val dc = if (s.isMetric) (2000.0 / (r * PI)) else (5729.578 / r) // Degree of curve

        _uiState.value = s.copy(
            calculatedNorthing = targetN,
            calculatedEasting = targetE,
            calculatedElevation = edgeZ,
            calculatedCenterlineZ = clZ,
            calculatedEdgeZ = edgeZ,
            inverseStation = invStation,
            inverseOffset = invOffset,
            inversePerpendicularDist = perpDist,
            inverseElevationDiff = invElevDiff,
            curveTangentLength = t,
            curveArcLength = l,
            curveChordLength = c,
            curveExternalDist = e,
            curveMiddleOrd = m,
            pcStation = pc,
            ptStation = pt,
            degreeOfCurve = dc
        )
    }

    fun formatStation(station: Double, isMetric: Boolean = false): String {
        return if (isMetric) {
            val km = (station / 1000.0).toInt()
            val m = abs(station % 1000.0)
            String.format("%d+%07.3f", km, m)
        } else {
            val sta = (station / 100.0).toInt()
            val rem = abs(station % 100.0)
            String.format("%d+%05.2f", sta, rem)
        }
    }

    fun parseStation(text: String): Double? {
        val cleaned = text.trim()
        if (cleaned.contains("+")) {
            val parts = cleaned.split("+")
            if (parts.size == 2) {
                val p1 = parts[0].toDoubleOrNull() ?: return null
                val p2 = parts[1].toDoubleOrNull() ?: return null
                return if (p1 >= 0) p1 * 100.0 + p2 else p1 * 100.0 - p2
            }
        }
        return cleaned.toDoubleOrNull()
    }

    fun saveToLog() {
        val s = _uiState.value
        toolLogRepository?.let { repo ->
            viewModelScope.launch {
                val summary = "COGO Sta: ${formatStation(s.targetStation, s.isMetric)} Off: ${s.targetOffset} -> N: ${String.format("%.3f", s.calculatedNorthing)}, E: ${String.format("%.3f", s.calculatedEasting)}, Z: ${String.format("%.3f", s.calculatedElevation)}"
                repo.logToolActivity(
                    toolType = "cogo_stationing",
                    title = "Stationing & Offset COGO",
                    summary = summary,
                    value = s.calculatedNorthing
                )
            }
        }
    }
}
