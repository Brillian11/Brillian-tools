package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

enum class ReturnPeriod(val years: Int, val frequencyFactorCf: Double, val label: String) {
    YEAR_2(2, 1.0, "2-Year Storm (Common)"),
    YEAR_5(5, 1.0, "5-Year Storm"),
    YEAR_10(10, 1.0, "10-Year Storm (Standard Municipal)"),
    YEAR_25(25, 1.1, "25-Year Storm (Commercial)"),
    YEAR_50(50, 1.2, "50-Year Storm"),
    YEAR_100(100, 1.25, "100-Year Major Flood (FEMA)")
}

enum class StormwaterPipeMaterial(val manningN: Double, val label: String) {
    HDPE_SMOOTH(0.012, "Smooth Interior HDPE (n = 0.012)"),
    RCP_CONCRETE(0.013, "Reinforced Concrete Pipe RCP (n = 0.013)"),
    CMP_CORRUGATED(0.024, "Corrugated Metal Pipe CMP (n = 0.024)"),
    PVC_PLASTIC(0.011, "PVC Gravity Sewer Pipe (n = 0.011)")
}

data class SurfaceSubArea(
    val id: String,
    val name: String,
    val runoffCoeffC: Double,
    val areaAcres: Double
)

data class StormwaterRationalUiState(
    val isMetric: Boolean = false,
    val returnPeriod: ReturnPeriod = ReturnPeriod.YEAR_10,
    val pipeMaterial: StormwaterPipeMaterial = StormwaterPipeMaterial.HDPE_SMOOTH,

    // Sub-Catchment Surface Areas (in acres or hectares)
    val subAreas: List<SurfaceSubArea> = listOf(
        SurfaceSubArea("roof", "Roofs & Building Footprint (C = 0.90)", 0.90, 1.2),
        SurfaceSubArea("pavement", "Asphalt / Concrete Parking (C = 0.90)", 0.90, 2.5),
        SurfaceSubArea("gravel", "Compacted Gravel Drives (C = 0.75)", 0.75, 0.8),
        SurfaceSubArea("lawn", "Lawn & Landscaped Turf (C = 0.25)", 0.25, 3.5),
        SurfaceSubArea("woods", "Vegetated Swales / Woods (C = 0.15)", 0.15, 1.0)
    ),

    // Storm Hydrology Inputs
    val timeOfConcentrationMin: Double = 15.0, // Tc in minutes
    val baseRainfallIntensityInHr: Double = 4.2, // I (in/hr for given storm)
    val preDevelopmentC: Double = 0.20, // Undeveloped raw field C

    // Culvert Sizing Inputs
    val culvertSlopePct: Double = 1.0, // 1.0% pipe slope
    val allowableHeadwaterHwRatio: Double = 1.2, // HW/D ratio

    // Calculated Hydrology Outputs
    val totalDrainageArea: Double = 0.0, // Acres or Hectares
    val compositeRunoffCoeffC: Double = 0.0,
    val adjustedPeakDischargeQ: Double = 0.0, // CFS or m3/s (Post-development)
    val preDevelopmentDischargeQ: Double = 0.0, // CFS

    // Culvert Hydraulics
    val requiredPipeDiameterInches: Double = 0.0,
    val recommendedStandardPipeSizeInches: Int = 0,
    val flowVelocityFps: Double = 0.0, // ft/s or m/s

    // Retention / Detention Basin Volume
    val detentionStorageVolumeCuFt: Double = 0.0, // cu ft
    val detentionStorageVolumeCuYd: Double = 0.0, // cu yd
    val detentionStorageAcreFt: Double = 0.0
)

class StormwaterRationalViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(StormwaterRationalUiState())
    val uiState: StateFlow<StormwaterRationalUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setMetric(metric: Boolean) {
        _uiState.value = _uiState.value.copy(isMetric = metric)
        recalculate()
    }

    fun setReturnPeriod(period: ReturnPeriod) {
        // Adjust intensity based on storm return period standard factor
        val baseI = when (period) {
            ReturnPeriod.YEAR_2 -> 2.8
            ReturnPeriod.YEAR_5 -> 3.6
            ReturnPeriod.YEAR_10 -> 4.2
            ReturnPeriod.YEAR_25 -> 5.1
            ReturnPeriod.YEAR_50 -> 5.8
            ReturnPeriod.YEAR_100 -> 6.6
        }
        _uiState.value = _uiState.value.copy(
            returnPeriod = period,
            baseRainfallIntensityInHr = baseI
        )
        recalculate()
    }

    fun setPipeMaterial(mat: StormwaterPipeMaterial) {
        _uiState.value = _uiState.value.copy(pipeMaterial = mat)
        recalculate()
    }

    fun updateSubArea(id: String, area: Double) {
        val updated = _uiState.value.subAreas.map { a ->
            if (a.id == id) a.copy(areaAcres = area.coerceAtLeast(0.0)) else a
        }
        _uiState.value = _uiState.value.copy(subAreas = updated)
        recalculate()
    }

    fun updateInputs(
        timeOfConcentrationMin: Double? = null,
        baseRainfallIntensityInHr: Double? = null,
        preDevelopmentC: Double? = null,
        culvertSlopePct: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            timeOfConcentrationMin = timeOfConcentrationMin ?: _uiState.value.timeOfConcentrationMin,
            baseRainfallIntensityInHr = baseRainfallIntensityInHr ?: _uiState.value.baseRainfallIntensityInHr,
            preDevelopmentC = preDevelopmentC ?: _uiState.value.preDevelopmentC,
            culvertSlopePct = culvertSlopePct ?: _uiState.value.culvertSlopePct
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value

        // 1. Total Area & Composite Runoff Coefficient (C)
        val totalArea = s.subAreas.sumOf { it.areaAcres }
        val sumCA = s.subAreas.sumOf { it.runoffCoeffC * it.areaAcres }
        val compC = if (totalArea > 0) (sumCA / totalArea).coerceIn(0.05, 0.99) else 0.50

        // 2. Rational Equation: Q = C * Cf * I * A (cfs in US customary)
        val cf = s.returnPeriod.frequencyFactorCf
        val adjustedC = (compC * cf).coerceAtMost(1.0)
        val areaAcres = if (s.isMetric) totalArea * 2.47105 else totalArea
        val intensityInHr = if (s.isMetric) (s.baseRainfallIntensityInHr / 25.4) else s.baseRainfallIntensityInHr

        val postPeakQcfs = adjustedC * intensityInHr * areaAcres
        val prePeakQcfs = (s.preDevelopmentC * cf).coerceAtMost(1.0) * intensityInHr * areaAcres

        // 3. Circular Pipe Sizing via Manning's Equation (Full Pipe Gravity Flow)
        // Q = (1.486 / n) * A * R^(2/3) * S^(1/2)
        // For circular pipe full: A = pi * D^2 / 4, R = D / 4
        // Q = (1.486 / n) * (pi/4 * D^2) * (D/4)^(2/3) * S^(1/2) = (0.463 / n) * D^(8/3) * S^(1/2)
        // D_ft = ( (Q * n) / (0.463 * S^(1/2)) )^(3/8)
        val slope = (s.culvertSlopePct / 100.0).coerceAtLeast(0.001)
        val n = s.pipeMaterial.manningN

        val reqDft = ((postPeakQcfs * n) / (0.463 * sqrt(slope))).pow(3.0 / 8.0)
        val reqDin = reqDft * 12.0

        // Standard commercial pipe diameters in inches
        val standardPipes = listOf(12, 15, 18, 24, 30, 36, 42, 48, 54, 60, 72)
        val selectedPipeIn = standardPipes.firstOrNull { it >= reqDin } ?: ceil(reqDin).toInt()

        // Velocity in selected pipe
        val pipeAreaSqFt = PI * (selectedPipeIn / 24.0).pow(2)
        val velocityFps = if (pipeAreaSqFt > 0) (postPeakQcfs / pipeAreaSqFt) else 0.0

        // 4. Detention Storage Volume (Modified Rational Method Triangular Hydrograph)
        // Storage = (Q_post - Q_pre) * Tc * 60 (seconds)
        val deltaQ = max(0.0, postPeakQcfs - prePeakQcfs)
        val tcSeconds = s.timeOfConcentrationMin * 60.0
        val storageCuFt = deltaQ * tcSeconds
        val storageCuYd = storageCuFt / 27.0
        val storageAcreFt = storageCuFt / 43560.0

        val finalPostQ = if (s.isMetric) postPeakQcfs * 0.0283168 else postPeakQcfs
        val finalPreQ = if (s.isMetric) prePeakQcfs * 0.0283168 else prePeakQcfs
        val finalVelocity = if (s.isMetric) velocityFps * 0.3048 else velocityFps

        _uiState.value = s.copy(
            totalDrainageArea = totalArea,
            compositeRunoffCoeffC = compC,
            adjustedPeakDischargeQ = finalPostQ,
            preDevelopmentDischargeQ = finalPreQ,
            requiredPipeDiameterInches = reqDin,
            recommendedStandardPipeSizeInches = selectedPipeIn,
            flowVelocityFps = finalVelocity,
            detentionStorageVolumeCuFt = if (s.isMetric) storageCuFt * 0.0283168 else storageCuFt,
            detentionStorageVolumeCuYd = storageCuYd,
            detentionStorageAcreFt = storageAcreFt
        )
    }

    fun saveToLog() {
        val s = _uiState.value
        val uQ = if (s.isMetric) "m³/s" else "cfs"
        toolLogRepository?.let { repo ->
            viewModelScope.launch {
                val summary = "Rational Stormwater: Q_post=${String.format("%.2f", s.adjustedPeakDischargeQ)} $uQ (Area=${String.format("%.1f", s.totalDrainageArea)}, C=${String.format("%.2f", s.compositeRunoffCoeffC)}, Storm=${s.returnPeriod.years}-yr)"
                repo.logToolActivity(
                    toolType = "stormwater_rational",
                    title = "Stormwater Runoff (Rational Method)",
                    summary = summary,
                    value = s.adjustedPeakDischargeQ
                )
            }
        }
    }
}
