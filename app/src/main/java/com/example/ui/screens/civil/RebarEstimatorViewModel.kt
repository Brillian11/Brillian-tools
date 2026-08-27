package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

enum class RebarStructure(val label: String) {
    SLAB_GRID("Slab / Mat Foundation Grid"),
    BEAM_FOOTING("Continuous Beam / Footing"),
    COLUMN("Column / Pier Caging")
}

data class RebarSize(
    val name: String,
    val diameterInches: Double,
    val diameterMm: Double,
    val weightLbsPerFt: Double,
    val weightKgPerM: Double
) {
    companion object {
        val US_SIZES = listOf(
            RebarSize("#3 (3/8\")", 0.375, 9.525, 0.376, 0.560),
            RebarSize("#4 (1/2\")", 0.500, 12.70, 0.668, 0.994),
            RebarSize("#5 (5/8\")", 0.625, 15.88, 1.043, 1.552),
            RebarSize("#6 (3/4\")", 0.750, 19.05, 1.502, 2.235),
            RebarSize("#7 (7/8\")", 0.875, 22.23, 2.044, 3.042),
            RebarSize("#8 (1\")", 1.000, 25.40, 2.670, 3.973),
            RebarSize("#9 (1-1/8\")", 1.128, 28.65, 3.400, 5.060),
            RebarSize("#10 (1-1/4\")", 1.270, 32.26, 4.303, 6.404),
            RebarSize("#11 (1-3/8\")", 1.410, 35.81, 5.313, 7.907)
        )

        val METRIC_SIZES = listOf(
            RebarSize("6 mm", 0.236, 6.0, 0.149, 0.222),
            RebarSize("8 mm", 0.315, 8.0, 0.265, 0.395),
            RebarSize("10 mm", 0.394, 10.0, 0.415, 0.617),
            RebarSize("12 mm", 0.472, 12.0, 0.597, 0.888),
            RebarSize("16 mm", 0.630, 16.0, 1.060, 1.578),
            RebarSize("20 mm", 0.787, 20.0, 1.657, 2.466),
            RebarSize("25 mm", 0.984, 25.0, 2.590, 3.853),
            RebarSize("32 mm", 1.260, 32.0, 4.242, 6.313)
        )
    }
}

data class RebarUiState(
    val isMetric: Boolean = false,
    val structure: RebarStructure = RebarStructure.SLAB_GRID,
    val selectedRebar: RebarSize = RebarSize.US_SIZES[1], // #4 default

    // Slab Grid inputs (ft or m, in or cm)
    val length: Double = 20.0, // ft or m
    val width: Double = 16.0, // ft or m
    val thickness: Double = 6.0, // in or cm
    val edgeClearCoverInOrCm: Double = 2.0, // 2 in (or 5 cm)
    val gridSpacingInOrCm: Double = 12.0, // 12 in (or 30 cm)
    val layersCount: Int = 1, // Single mat or Double mat

    // Beam / Footing inputs
    val beamLength: Double = 30.0,
    val beamWidthInOrCm: Double = 12.0,
    val beamDepthInOrCm: Double = 18.0,
    val longitudinalBarCount: Int = 4,
    val stirrupRebarSize: RebarSize = RebarSize.US_SIZES[0], // #3 for stirrups
    val stirrupSpacingInOrCm: Double = 8.0,

    // Column Inputs
    val columnHeight: Double = 10.0,
    val columnDiameterInOrCm: Double = 16.0,
    val mainVerticalBarsCount: Int = 6,
    val lateralTieSpacingInOrCm: Double = 8.0,

    val stockStickLengthFtOrM: Double = 20.0, // standard 20ft (or 6m / 12m)
    val lapSpliceMultiplier: Double = 40.0, // 40x bar diameter for lap splices
    val wastePercent: Double = 10.0,

    // Calculated Outputs
    val totalLinearFeet: Double = 700.0,
    val totalLinearMeters: Double = 213.3,
    val totalWeightLbs: Double = 467.6,
    val totalWeightKg: Double = 212.1,
    val totalWeightTonnes: Double = 0.21,
    val stockSticksNeeded: Int = 39,
    val gridIntersectionsCount: Int = 320,
    val tieWireLbsNeeded: Double = 2.1, // @ 1 lb per 150-200 ties
    val rebarChairsNeeded: Int = 32, // @ 1 chair per 2.5-3 ft
    val rebarRatioPct: Double = 0.35 // % steel area to concrete area
)

class RebarEstimatorViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RebarUiState())
    val uiState: StateFlow<RebarUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            val sizes = if (metric) RebarSize.METRIC_SIZES else RebarSize.US_SIZES
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                selectedRebar = if (metric) sizes[3] else sizes[1], // 12mm or #4
                stirrupRebarSize = if (metric) sizes[1] else sizes[0], // 8mm or #3
                length = if (metric) 6.0 else 20.0,
                width = if (metric) 5.0 else 16.0,
                thickness = if (metric) 15.0 else 6.0,
                edgeClearCoverInOrCm = if (metric) 5.0 else 2.0,
                gridSpacingInOrCm = if (metric) 30.0 else 12.0,
                beamLength = if (metric) 10.0 else 30.0,
                beamWidthInOrCm = if (metric) 30.0 else 12.0,
                beamDepthInOrCm = if (metric) 45.0 else 18.0,
                stirrupSpacingInOrCm = if (metric) 20.0 else 8.0,
                columnHeight = if (metric) 3.0 else 10.0,
                columnDiameterInOrCm = if (metric) 40.0 else 16.0,
                lateralTieSpacingInOrCm = if (metric) 20.0 else 8.0,
                stockStickLengthFtOrM = if (metric) 6.0 else 20.0
            )
            recalculate()
        }
    }

    fun setStructure(s: RebarStructure) {
        _uiState.value = _uiState.value.copy(structure = s)
        recalculate()
    }

    fun setSelectedRebar(r: RebarSize) {
        _uiState.value = _uiState.value.copy(selectedRebar = r)
        recalculate()
    }

    fun setStirrupRebar(r: RebarSize) {
        _uiState.value = _uiState.value.copy(stirrupRebarSize = r)
        recalculate()
    }

    fun updateInputs(
        length: Double? = null,
        width: Double? = null,
        thickness: Double? = null,
        edgeCover: Double? = null,
        spacing: Double? = null,
        layers: Int? = null,
        beamLength: Double? = null,
        beamWidth: Double? = null,
        beamDepth: Double? = null,
        longBars: Int? = null,
        stirrupSpacing: Double? = null,
        colHeight: Double? = null,
        colDiameter: Double? = null,
        colMainBars: Int? = null,
        colTieSpacing: Double? = null,
        stockLength: Double? = null,
        waste: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            length = length ?: _uiState.value.length,
            width = width ?: _uiState.value.width,
            thickness = thickness ?: _uiState.value.thickness,
            edgeClearCoverInOrCm = edgeCover ?: _uiState.value.edgeClearCoverInOrCm,
            gridSpacingInOrCm = spacing ?: _uiState.value.gridSpacingInOrCm,
            layersCount = layers ?: _uiState.value.layersCount,
            beamLength = beamLength ?: _uiState.value.beamLength,
            beamWidthInOrCm = beamWidth ?: _uiState.value.beamWidthInOrCm,
            beamDepthInOrCm = beamDepth ?: _uiState.value.beamDepthInOrCm,
            longitudinalBarCount = longBars ?: _uiState.value.longitudinalBarCount,
            stirrupSpacingInOrCm = stirrupSpacing ?: _uiState.value.stirrupSpacingInOrCm,
            columnHeight = colHeight ?: _uiState.value.columnHeight,
            columnDiameterInOrCm = colDiameter ?: _uiState.value.columnDiameterInOrCm,
            mainVerticalBarsCount = colMainBars ?: _uiState.value.mainVerticalBarsCount,
            lateralTieSpacingInOrCm = colTieSpacing ?: _uiState.value.lateralTieSpacingInOrCm,
            stockStickLengthFtOrM = stockLength ?: _uiState.value.stockStickLengthFtOrM,
            wastePercent = waste ?: _uiState.value.wastePercent
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        var totalLinFeet = 0.0
        var totalLinMeters = 0.0
        var totalWeightLbs = 0.0
        var totalWeightKg = 0.0
        var intersections = 0
        var chairs = 0
        var steelRatio = 0.0

        val lapLapLengthFactor = 1.0 + (s.lapSpliceMultiplier * s.selectedRebar.diameterInches / (12.0 * s.stockStickLengthFtOrM)).coerceIn(0.05, 0.20)
        val wasteFactor = 1.0 + (s.wastePercent / 100.0)

        when (s.structure) {
            RebarStructure.SLAB_GRID -> {
                val effectiveL_ft = if (isM) s.length * 3.28084 else s.length
                val effectiveW_ft = if (isM) s.width * 3.28084 else s.width
                val coverFt = if (isM) (s.edgeClearCoverInOrCm / 2.54) / 12.0 else s.edgeClearCoverInOrCm / 12.0
                val spacingFt = if (isM) (s.gridSpacingInOrCm / 2.54) / 12.0 else s.gridSpacingInOrCm / 12.0

                val netL_ft = (effectiveL_ft - (2 * coverFt)).coerceAtLeast(1.0)
                val netW_ft = (effectiveW_ft - (2 * coverFt)).coerceAtLeast(1.0)

                val numBarsAlongL = ceil(netW_ft / spacingFt).toInt() + 1
                val numBarsAlongW = ceil(netL_ft / spacingFt).toInt() + 1

                val linearFtLong = numBarsAlongL * netL_ft * s.layersCount * lapLapLengthFactor
                val linearFtCross = numBarsAlongW * netW_ft * s.layersCount * lapLapLengthFactor
                val grossLinFt = (linearFtLong + linearFtCross) * wasteFactor

                totalLinFeet = grossLinFt
                totalLinMeters = grossLinFt / 3.28084
                totalWeightLbs = grossLinFt * s.selectedRebar.weightLbsPerFt
                totalWeightKg = totalLinMeters * s.selectedRebar.weightKgPerM

                intersections = numBarsAlongL * numBarsAlongW * s.layersCount
                chairs = ceil((effectiveL_ft * effectiveW_ft) / 9.0).toInt() * s.layersCount

                // Steel Ratio: As / (b * d)
                val barAreaSqIn = Math.PI * (s.selectedRebar.diameterInches / 2.0) * (s.selectedRebar.diameterInches / 2.0)
                val spacingIn = if (isM) s.gridSpacingInOrCm / 2.54 else s.gridSpacingInOrCm
                val thickIn = if (isM) s.thickness / 2.54 else s.thickness
                val steelAreaPerFt = (12.0 / spacingIn) * barAreaSqIn * s.layersCount
                val concreteAreaPerFt = 12.0 * thickIn
                steelRatio = if (concreteAreaPerFt > 0) (steelAreaPerFt / concreteAreaPerFt) * 100.0 else 0.0
            }

            RebarStructure.BEAM_FOOTING -> {
                val bLenFt = if (isM) s.beamLength * 3.28084 else s.beamLength
                val bWidIn = if (isM) s.beamWidthInOrCm / 2.54 else s.beamWidthInOrCm
                val bDepIn = if (isM) s.beamDepthInOrCm / 2.54 else s.beamDepthInOrCm
                val coverIn = if (isM) s.edgeClearCoverInOrCm / 2.54 else s.edgeClearCoverInOrCm
                val stirrupSpacingIn = if (isM) s.stirrupSpacingInOrCm / 2.54 else s.stirrupSpacingInOrCm

                // Longitudinal main bars
                val mainLinFt = s.longitudinalBarCount * bLenFt * lapLapLengthFactor * wasteFactor
                val mainWeightLbs = mainLinFt * s.selectedRebar.weightLbsPerFt

                // Stirrup perimeters (2 * (W - 2*cover) + 2 * (D - 2*cover) + 2 * 6" hooks)
                val stirrupPerimeterFt = (2 * (bWidIn - 2 * coverIn) + 2 * (bDepIn - 2 * coverIn) + 12.0) / 12.0
                val numStirrups = ceil((bLenFt * 12.0) / stirrupSpacingIn).toInt() + 1
                val stirrupLinFt = numStirrups * stirrupPerimeterFt * wasteFactor
                val stirrupWeightLbs = stirrupLinFt * s.stirrupRebarSize.weightLbsPerFt

                totalLinFeet = mainLinFt + stirrupLinFt
                totalLinMeters = totalLinFeet / 3.28084
                totalWeightLbs = mainWeightLbs + stirrupWeightLbs
                totalWeightKg = totalWeightLbs * 0.453592
                intersections = numStirrups * s.longitudinalBarCount
                chairs = ceil(bLenFt / 3.0).toInt()
            }

            RebarStructure.COLUMN -> {
                val colHtFt = if (isM) s.columnHeight * 3.28084 else s.columnHeight
                val colDiaIn = if (isM) s.columnDiameterInOrCm / 2.54 else s.columnDiameterInOrCm
                val coverIn = if (isM) s.edgeClearCoverInOrCm / 2.54 else s.edgeClearCoverInOrCm
                val tieSpacingIn = if (isM) s.lateralTieSpacingInOrCm / 2.54 else s.lateralTieSpacingInOrCm

                // Main vertical bars (including dowel projection +2 ft)
                val mainLinFt = s.mainVerticalBarsCount * (colHtFt + 2.0) * wasteFactor
                val mainWeightLbs = mainLinFt * s.selectedRebar.weightLbsPerFt

                // Circular ties
                val tieDiaIn = colDiaIn - (2 * coverIn)
                val tiePerimeterFt = ((Math.PI * tieDiaIn) + 12.0) / 12.0
                val numTies = ceil((colHtFt * 12.0) / tieSpacingIn).toInt() + 1
                val tieLinFt = numTies * tiePerimeterFt * wasteFactor
                val tieWeightLbs = tieLinFt * s.stirrupRebarSize.weightLbsPerFt

                totalLinFeet = mainLinFt + tieLinFt
                totalLinMeters = totalLinFeet / 3.28084
                totalWeightLbs = mainWeightLbs + tieWeightLbs
                totalWeightKg = totalWeightLbs * 0.453592
                intersections = numTies * s.mainVerticalBarsCount
                chairs = 4
            }
        }

        val stockLen = if (isM) s.stockStickLengthFtOrM * 3.28084 else s.stockStickLengthFtOrM
        val sticks = if (stockLen > 0) ceil(totalLinFeet / stockLen).toInt() else 0
        val tieWireLbs = (intersections / 150.0).coerceAtLeast(0.5)

        _uiState.value = s.copy(
            totalLinearFeet = totalLinFeet,
            totalLinearMeters = totalLinMeters,
            totalWeightLbs = totalWeightLbs,
            totalWeightKg = totalWeightKg,
            totalWeightTonnes = totalWeightKg / 1000.0,
            stockSticksNeeded = sticks,
            gridIntersectionsCount = intersections,
            tieWireLbsNeeded = tieWireLbs,
            rebarChairsNeeded = chairs,
            rebarRatioPct = steelRatio
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = "${s.structure.label} (${s.selectedRebar.name}): ${String.format("%.1f", s.totalLinearFeet)} ft (${String.format("%.1f", s.totalLinearMeters)} m) -> ${String.format("%,.0f", s.totalWeightLbs)} lbs (${String.format("%.2f", s.totalWeightTonnes)} Tonnes, ${s.stockSticksNeeded}x ${s.stockStickLengthFtOrM.toInt()}${if (s.isMetric) "m" else "ft"} sticks)"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "CIVIL",
                title = "Rebar Spacing & Weight Estimator",
                summary = summary,
                value = s.totalWeightLbs
            )
        }
    }
}
