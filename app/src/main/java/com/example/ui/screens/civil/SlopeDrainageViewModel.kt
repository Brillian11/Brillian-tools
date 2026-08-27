package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

enum class PipeMaterial(val n: Double, val label: String) {
    PVC_HDPE(0.010, "Smooth PVC / HDPE (n = 0.010)"),
    CONCRETE(0.013, "Reinforced Concrete Pipe (n = 0.013)"),
    CORRUGATED_METAL(0.024, "Corrugated Metal CMP / Dual-Wall (n = 0.024)"),
    CLAY(0.014, "Vitrified Clay (n = 0.014)")
}

enum class SurfaceType(val c: Double, val label: String) {
    PAVEMENT_ROOF(0.90, "Pavement, Asphalt & Roofs (C = 0.90)"),
    GRAVEL_DRIVEWAY(0.70, "Compacted Gravel & Hardpack (C = 0.70)"),
    BARE_EARTH(0.50, "Compacted Bare Soil (C = 0.50)"),
    LAWN_TURF(0.20, "Lawns & Vegetated Turf (C = 0.20)"),
    WOODLAND(0.15, "Woodland & Forest (C = 0.15)")
}

data class SlopeDrainageUiState(
    val isMetric: Boolean = false,
    val selectedMaterial: PipeMaterial = PipeMaterial.PVC_HDPE,
    val selectedSurface: SurfaceType = SurfaceType.PAVEMENT_ROOF,

    // Slope & Trench Inputs
    val upstreamElevation: Double = 105.0, // ft or m
    val downstreamElevation: Double = 103.0, // ft or m
    val horizontalRunLength: Double = 100.0, // ft or m

    // Storm Runoff Inputs (Rational Method Q = CIA)
    val catchmentAreaAcresOrHa: Double = 0.5, // 0.5 acres (or 0.2 ha)
    val rainfallIntensityInOrMmHr: Double = 3.0, // 3 in/hr design storm (or 75 mm/hr)

    // Culvert Pipe Inputs
    val pipeDiameterInchesOrMm: Double = 8.0, // 8 in (or 200 mm)

    // Calculated Slope Metrics
    val elevationDelta: Double = 2.0, // ft or m
    val percentGrade: Double = 2.0, // 2.0%
    val slopeRatio: String = "1:50.0", // 1 in 50
    val angleDegrees: Double = 1.15,
    val dropPerFootInches: Double = 0.24, // approx 1/4" per ft
    val dropPerMeterMm: Double = 20.0,

    // Calculated Runoff
    val peakRunoffCfs: Double = 1.35, // CFS
    val peakRunoffGpm: Double = 606.0,
    val peakRunoffLps: Double = 38.2, // Liters per sec

    // Calculated Pipe Flow Capacity (Manning's Equation)
    val pipeCapacityCfs: Double = 2.14,
    val pipeCapacityGpm: Double = 960.0,
    val pipeCapacityLps: Double = 60.6,
    val flowVelocityFtS: Double = 6.13, // ft/s
    val flowVelocityMS: Double = 1.87, // m/s
    val isAdequateForRunoff: Boolean = true,
    val velocitySelfCleaningOk: Boolean = true // >= 2 ft/s
)

class SlopeDrainageViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SlopeDrainageUiState())
    val uiState: StateFlow<SlopeDrainageUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                upstreamElevation = if (metric) 32.0 else 105.0,
                downstreamElevation = if (metric) 31.4 else 103.0,
                horizontalRunLength = if (metric) 30.0 else 100.0,
                catchmentAreaAcresOrHa = if (metric) 0.2 else 0.5,
                rainfallIntensityInOrMmHr = if (metric) 75.0 else 3.0,
                pipeDiameterInchesOrMm = if (metric) 200.0 else 8.0
            )
            recalculate()
        }
    }

    fun setMaterial(m: PipeMaterial) {
        _uiState.value = _uiState.value.copy(selectedMaterial = m)
        recalculate()
    }

    fun setSurface(s: SurfaceType) {
        _uiState.value = _uiState.value.copy(selectedSurface = s)
        recalculate()
    }

    fun updateInputs(
        upElev: Double? = null,
        downElev: Double? = null,
        runLen: Double? = null,
        area: Double? = null,
        intensity: Double? = null,
        pipeDia: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            upstreamElevation = upElev ?: _uiState.value.upstreamElevation,
            downstreamElevation = downElev ?: _uiState.value.downstreamElevation,
            horizontalRunLength = runLen ?: _uiState.value.horizontalRunLength,
            catchmentAreaAcresOrHa = area ?: _uiState.value.catchmentAreaAcresOrHa,
            rainfallIntensityInOrMmHr = intensity ?: _uiState.value.rainfallIntensityInOrMmHr,
            pipeDiameterInchesOrMm = pipeDia ?: _uiState.value.pipeDiameterInchesOrMm
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        val run = s.horizontalRunLength.coerceAtLeast(0.1)
        val deltaH = (s.upstreamElevation - s.downstreamElevation).coerceAtLeast(0.001)

        val gradePct = (deltaH / run) * 100.0
        val slopeFraction = deltaH / run
        val ratioVal = if (deltaH > 0) run / deltaH else 0.0
        val ratioStr = "1:${String.format("%.1f", ratioVal)}"
        val angleDeg = Math.toDegrees(atan(slopeFraction))

        val dropPerFtIn = slopeFraction * 12.0
        val dropPerMMm = slopeFraction * 1000.0

        // Runoff Q = C * I * A (Rational Method)
        val peakCfs: Double
        val peakLps: Double
        if (isM) {
            // SI: Q (m3/s) = 0.00278 * C * I (mm/hr) * A (ha)
            val qM3s = 0.00278 * s.selectedSurface.c * s.rainfallIntensityInOrMmHr * s.catchmentAreaAcresOrHa
            peakLps = qM3s * 1000.0
            peakCfs = qM3s * 35.3147
        } else {
            // US: Q (cfs) ≈ C * I (in/hr) * A (acres) (1.008 factor)
            peakCfs = s.selectedSurface.c * s.rainfallIntensityInOrMmHr * s.catchmentAreaAcresOrHa * 1.008
            peakLps = peakCfs * 28.3168
        }
        val peakGpm = peakCfs * 448.831

        // Manning's Equation for full pipe flow:
        // US: Q = (1.486 / n) * A * R^(2/3) * S^(1/2)
        // Pipe Diameter in ft
        val diaFt = if (isM) (s.pipeDiameterInchesOrMm / 1000.0) * 3.28084 else s.pipeDiameterInchesOrMm / 12.0
        val radiusFt = diaFt / 2.0
        val pipeAreaSqFt = PI * radiusFt * radiusFt
        val hydraulicRadiusFt = diaFt / 4.0 // For full circular pipe R = D/4
        val slopeS = (gradePct / 100.0).coerceAtLeast(0.0001)

        val qPipeCfs = (1.486 / s.selectedMaterial.n) * pipeAreaSqFt * (hydraulicRadiusFt.pow(2.0 / 3.0)) * sqrt(slopeS)
        val qPipeGpm = qPipeCfs * 448.831
        val qPipeLps = qPipeCfs * 28.3168

        val velocityFtS = if (pipeAreaSqFt > 0) qPipeCfs / pipeAreaSqFt else 0.0
        val velocityMS = velocityFtS * 0.3048

        val adequate = qPipeCfs >= peakCfs
        val selfCleaning = velocityFtS >= 2.0

        _uiState.value = s.copy(
            elevationDelta = deltaH,
            percentGrade = gradePct,
            slopeRatio = ratioStr,
            angleDegrees = angleDeg,
            dropPerFootInches = dropPerFtIn,
            dropPerMeterMm = dropPerMMm,
            peakRunoffCfs = peakCfs,
            peakRunoffGpm = peakGpm,
            peakRunoffLps = peakLps,
            pipeCapacityCfs = qPipeCfs,
            pipeCapacityGpm = qPipeGpm,
            pipeCapacityLps = qPipeLps,
            flowVelocityFtS = velocityFtS,
            flowVelocityMS = velocityMS,
            isAdequateForRunoff = adequate,
            velocitySelfCleaningOk = selfCleaning
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = "Drainage Slope ${String.format("%.2f", s.percentGrade)}% (${s.slopeRatio}): Pipe ${s.pipeDiameterInchesOrMm.toInt()}${if (s.isMetric) "mm" else "\""} Capacity ${String.format("%.2f", s.pipeCapacityCfs)} CFS vs Peak Runoff ${String.format("%.2f", s.peakRunoffCfs)} CFS (Adequate: ${if (s.isAdequateForRunoff) "YES" else "NO"})"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "CIVIL",
                title = "Slope, Drainage & Culvert Gradient Sizer",
                summary = summary,
                value = s.pipeCapacityCfs
            )
        }
    }
}
