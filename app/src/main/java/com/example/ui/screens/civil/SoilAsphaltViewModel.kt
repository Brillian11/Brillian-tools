package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

enum class PavingMaterial(
    val densityLbsCuFt: Double,
    val densityTonnesCuM: Double,
    val defaultRollDownPct: Double,
    val isAsphalt: Boolean,
    val label: String
) {
    HMA_SURFACE(145.0, 2.32, 22.0, true, "Hot Mix Asphalt (HMA Surface / Wearing Course)"),
    HMA_BINDER(148.0, 2.37, 20.0, true, "Asphalt Binder Course / Base Lift"),
    AGGREGATE_BASE(135.0, 2.16, 18.0, false, "Crushed Aggregate Base (Road Base / Class 2)"),
    CRUSHED_GRAVEL(105.0, 1.68, 15.0, false, "Crushed Stone / Clean 3/4\" Gravel"),
    CONCRETE_SAND(100.0, 1.60, 12.0, false, "Coarse Concrete Sand"),
    TOPSOIL_LOAM(80.0, 1.28, 15.0, false, "Topsoil / Screened Loam"),
    COMPACTED_CLAY(120.0, 1.92, 20.0, false, "Compacted Clay / Fill Dirt"),
    CUSTOM(140.0, 2.24, 20.0, false, "Custom Density Material")
}

data class SoilAsphaltUiState(
    val isMetric: Boolean = false,
    val selectedMaterial: PavingMaterial = PavingMaterial.HMA_SURFACE,

    // Dimensions
    val lengthFtOrM: Double = 60.0, // ft or m
    val widthFtOrM: Double = 20.0,  // ft or m
    val thicknessInOrCm: Double = 2.0, // 2 in (or 5 cm)

    val customDensityLbsCuFtOrTonnesM3: Double = 145.0,
    val rollDownFactorPct: Double = 22.0, // 22% loose-to-compacted roll down
    val wastePercent: Double = 10.0,
    val truckCapacityTons: Double = 15.0, // 15-ton tandem truck

    // Calculated Outputs
    val totalAreaSqFt: Double = 1200.0,
    val totalAreaSqYds: Double = 133.3,
    val totalAreaSqM: Double = 111.48,
    val compactedVolumeCuYds: Double = 7.41,
    val compactedVolumeCuM: Double = 5.66,
    val looseVolumeCuYds: Double = 9.50,
    val looseVolumeCuM: Double = 7.26,
    val totalWeightTons: Double = 16.03, // US Short Tons (2,000 lbs)
    val totalWeightTonnes: Double = 14.54, // Metric Tonnes (1,000 kg)
    val dumpTruckLoads: Int = 2,
    val tackCoatGallons: Double = 8.0, // approx 0.06 gal/sq.yd
    val tackCoatLiters: Double = 30.3,
    val applicationRateLbsSqYd: Double = 220.0 // 110 lbs/sq.yd per 1" thickness
)

class SoilAsphaltViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilAsphaltUiState())
    val uiState: StateFlow<SoilAsphaltUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                lengthFtOrM = if (metric) 20.0 else 60.0,
                widthFtOrM = if (metric) 6.0 else 20.0,
                thicknessInOrCm = if (metric) 5.0 else 2.0,
                customDensityLbsCuFtOrTonnesM3 = if (metric) 2.32 else 145.0,
                truckCapacityTons = if (metric) 15.0 else 15.0
            )
            recalculate()
        }
    }

    fun setMaterial(m: PavingMaterial) {
        _uiState.value = _uiState.value.copy(
            selectedMaterial = m,
            rollDownFactorPct = m.defaultRollDownPct
        )
        recalculate()
    }

    fun updateInputs(
        length: Double? = null,
        width: Double? = null,
        thickness: Double? = null,
        density: Double? = null,
        rollDown: Double? = null,
        waste: Double? = null,
        truckCap: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            lengthFtOrM = length ?: _uiState.value.lengthFtOrM,
            widthFtOrM = width ?: _uiState.value.widthFtOrM,
            thicknessInOrCm = thickness ?: _uiState.value.thicknessInOrCm,
            customDensityLbsCuFtOrTonnesM3 = density ?: _uiState.value.customDensityLbsCuFtOrTonnesM3,
            rollDownFactorPct = rollDown ?: _uiState.value.rollDownFactorPct,
            wastePercent = waste ?: _uiState.value.wastePercent,
            truckCapacityTons = truckCap ?: _uiState.value.truckCapacityTons
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        val areaSqM: Double
        val areaSqFt: Double
        val compVolM3: Double
        val compVolCuYd: Double

        if (isM) {
            areaSqM = s.lengthFtOrM * s.widthFtOrM
            areaSqFt = areaSqM * 10.7639
            val thickM = s.thicknessInOrCm / 100.0
            compVolM3 = areaSqM * thickM
            compVolCuYd = compVolM3 * 1.30795
        } else {
            areaSqFt = s.lengthFtOrM * s.widthFtOrM
            areaSqM = areaSqFt / 10.7639
            val thickFt = s.thicknessInOrCm / 12.0
            val compCuFt = areaSqFt * thickFt
            compVolCuYd = compCuFt / 27.0
            compVolM3 = compVolCuYd / 1.30795
        }
        val areaSqYds = areaSqFt / 9.0

        val rollDownMultiplier = 1.0 + (s.rollDownFactorPct / 100.0)
        val wasteMultiplier = 1.0 + (s.wastePercent / 100.0)

        val looseVolCuYd = compVolCuYd * rollDownMultiplier
        val looseVolCuM = compVolM3 * rollDownMultiplier

        val densityLbsCuFt = if (s.selectedMaterial == PavingMaterial.CUSTOM) {
            if (isM) s.customDensityLbsCuFtOrTonnesM3 * 62.428 else s.customDensityLbsCuFtOrTonnesM3
        } else s.selectedMaterial.densityLbsCuFt

        val densityTonnesM3 = densityLbsCuFt / 62.428

        // Weight = Compacted Volume * In-Place Density * Waste
        val grossCuFt = compVolCuYd * 27.0 * wasteMultiplier
        val totalLbs = grossCuFt * densityLbsCuFt
        val totalShortTons = totalLbs / 2000.0
        val totalTonnes = totalShortTons * 0.907185

        val truckLoads = if (s.truckCapacityTons > 0) ceil(totalShortTons / s.truckCapacityTons).toInt() else 0

        // Tack coat @ 0.06 gal / sq yd
        val tackGal = if (s.selectedMaterial.isAsphalt) areaSqYds * 0.06 else 0.0
        val tackL = tackGal * 3.78541

        // Application rate in lbs/sq.yd
        val thickInches = if (isM) s.thicknessInOrCm / 2.54 else s.thicknessInOrCm
        val appRateLbsSqYd = (densityLbsCuFt / 12.0) * thickInches * 9.0

        _uiState.value = s.copy(
            totalAreaSqFt = areaSqFt,
            totalAreaSqYds = areaSqYds,
            totalAreaSqM = areaSqM,
            compactedVolumeCuYds = compVolCuYd,
            compactedVolumeCuM = compVolM3,
            looseVolumeCuYds = looseVolCuYd,
            looseVolumeCuM = looseVolCuM,
            totalWeightTons = totalShortTons,
            totalWeightTonnes = totalTonnes,
            dumpTruckLoads = truckLoads,
            tackCoatGallons = tackGal,
            tackCoatLiters = tackL,
            applicationRateLbsSqYd = appRateLbsSqYd
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = "${s.selectedMaterial.label}: ${String.format("%.0f", s.totalAreaSqFt)} sq.ft (${String.format("%.1f", s.totalAreaSqM)} m²) @ ${s.thicknessInOrCm}${if (s.isMetric) "cm" else "\""} -> ${String.format("%.2f", s.totalWeightTons)} US Tons (${String.format("%.2f", s.totalWeightTonnes)} Tonnes, ${s.dumpTruckLoads} Trucks @ ${s.truckCapacityTons.toInt()} Tons)"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "CIVIL",
                title = "Soil Compaction & Asphalt Tonnage Estimator",
                summary = summary,
                value = s.totalWeightTons
            )
        }
    }
}
