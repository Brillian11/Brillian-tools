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

enum class ConcreteShape(val label: String) {
    SLAB("Slab / Flatwork (L x W x H)"),
    FOOTING("Footing / Trench (L x W x D)"),
    COLUMN("Round Column / Sonotube"),
    POST_HOLE("Post Hole (Cylinder - Post)"),
    STAIRS("Steps & Stairs"),
    CURB("Curb & Gutter")
}

enum class BagSize(val lbs: Double, val kg: Double, val cuFtYield: Double, val cuMYield: Double, val label: String) {
    BAG_80LB(80.0, 36.3, 0.60, 0.0170, "80 lb (0.60 cu.ft)"),
    BAG_60LB(60.0, 27.2, 0.45, 0.0127, "60 lb (0.45 cu.ft)"),
    BAG_50LB(50.0, 22.7, 0.375, 0.0106, "50 lb (0.375 cu.ft)"),
    BAG_40LB(40.0, 18.1, 0.30, 0.0085, "40 lb (0.30 cu.ft)"),
    BAG_25KG(55.1, 25.0, 0.424, 0.0120, "25 kg (0.012 m³)"),
    BAG_40KG(88.2, 40.0, 0.671, 0.0190, "40 kg (0.019 m³)"),
    BAG_50KG(110.2, 50.0, 0.848, 0.0240, "50 kg (0.024 m³)")
}

data class ConcreteVolumeUiState(
    val isMetric: Boolean = false,
    val shape: ConcreteShape = ConcreteShape.SLAB,
    val selectedBag: BagSize = BagSize.BAG_80LB,

    // Slab / Footing Inputs (ft or m, inches or cm for thickness)
    val length: Double = 12.0, // ft or m
    val width: Double = 10.0,  // ft or m
    val thicknessInchesOrCm: Double = 4.0, // 4 inches (or 10 cm in metric)

    // Circular / Column Inputs
    val diameterInchesOrCm: Double = 12.0, // 12 in or 30 cm
    val heightFtOrM: Double = 8.0, // 8 ft or 2.5 m
    val quantity: Int = 4,

    // Post Hole Inputs
    val holeDiameterInOrCm: Double = 10.0,
    val holeDepthInOrCm: Double = 36.0,
    val postSquareInOrCm: Double = 4.0, // 4x4 post (3.5 actual or 10cm)
    val postHoleCount: Int = 6,

    // Stairs Inputs
    val stepCount: Int = 4,
    val stepRiseInOrCm: Double = 7.0, // 7 inches
    val stepRunInOrCm: Double = 11.0, // 11 inches
    val stairWidthFtOrM: Double = 4.0, // 4 ft

    // Curb Inputs
    val curbLengthFtOrM: Double = 20.0,
    val curbHeightInOrCm: Double = 12.0,
    val curbWidthInOrCm: Double = 6.0,
    val gutterWidthInOrCm: Double = 12.0,
    val gutterThicknessInOrCm: Double = 6.0,

    val wastePercent: Double = 10.0,

    // Calculated Outputs
    val netVolumeCuFt: Double = 40.0,
    val netVolumeCuYards: Double = 1.48,
    val netVolumeCuMeters: Double = 1.13,
    val totalVolumeCuYardsWithWaste: Double = 1.63,
    val totalVolumeCuMetersWithWaste: Double = 1.25,
    val totalWeightLbs: Double = 6600.0,
    val totalWeightTonnes: Double = 3.0,
    val bagsNeeded: Int = 74,
    val readyMixTruckLoads: Double = 0.20, // @ 8 yd3 truck
    val waterGallonsNeeded: Double = 55.5,
    val waterLitersNeeded: Double = 210.0
)

class ConcreteVolumeViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConcreteVolumeUiState())
    val uiState: StateFlow<ConcreteVolumeUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            // Adjust defaults for unit switch
            if (metric) {
                _uiState.value = _uiState.value.copy(
                    isMetric = true,
                    length = 4.0,
                    width = 3.0,
                    thicknessInchesOrCm = 10.0,
                    diameterInchesOrCm = 30.0,
                    heightFtOrM = 2.5,
                    holeDiameterInOrCm = 25.0,
                    holeDepthInOrCm = 90.0,
                    postSquareInOrCm = 10.0,
                    stepRiseInOrCm = 18.0,
                    stepRunInOrCm = 28.0,
                    stairWidthFtOrM = 1.2,
                    curbLengthFtOrM = 6.0,
                    curbHeightInOrCm = 30.0,
                    curbWidthInOrCm = 15.0,
                    gutterWidthInOrCm = 30.0,
                    gutterThicknessInOrCm = 15.0,
                    selectedBag = BagSize.BAG_40KG
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isMetric = false,
                    length = 12.0,
                    width = 10.0,
                    thicknessInchesOrCm = 4.0,
                    diameterInchesOrCm = 12.0,
                    heightFtOrM = 8.0,
                    holeDiameterInOrCm = 10.0,
                    holeDepthInOrCm = 36.0,
                    postSquareInOrCm = 4.0,
                    stepRiseInOrCm = 7.0,
                    stepRunInOrCm = 11.0,
                    stairWidthFtOrM = 4.0,
                    curbLengthFtOrM = 20.0,
                    curbHeightInOrCm = 12.0,
                    curbWidthInOrCm = 6.0,
                    gutterWidthInOrCm = 12.0,
                    gutterThicknessInOrCm = 6.0,
                    selectedBag = BagSize.BAG_80LB
                )
            }
            recalculate()
        }
    }

    fun setShape(s: ConcreteShape) {
        _uiState.value = _uiState.value.copy(shape = s)
        recalculate()
    }

    fun setSelectedBag(b: BagSize) {
        _uiState.value = _uiState.value.copy(selectedBag = b)
        recalculate()
    }

    fun updateInputs(
        length: Double? = null,
        width: Double? = null,
        thickness: Double? = null,
        diameter: Double? = null,
        height: Double? = null,
        quantity: Int? = null,
        holeDiameter: Double? = null,
        holeDepth: Double? = null,
        postSquare: Double? = null,
        postHoles: Int? = null,
        stepCount: Int? = null,
        stepRise: Double? = null,
        stepRun: Double? = null,
        stairWidth: Double? = null,
        curbLength: Double? = null,
        curbHeight: Double? = null,
        curbWidth: Double? = null,
        gutterWidth: Double? = null,
        gutterThickness: Double? = null,
        waste: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            length = length ?: _uiState.value.length,
            width = width ?: _uiState.value.width,
            thicknessInchesOrCm = thickness ?: _uiState.value.thicknessInchesOrCm,
            diameterInchesOrCm = diameter ?: _uiState.value.diameterInchesOrCm,
            heightFtOrM = height ?: _uiState.value.heightFtOrM,
            quantity = quantity ?: _uiState.value.quantity,
            holeDiameterInOrCm = holeDiameter ?: _uiState.value.holeDiameterInOrCm,
            holeDepthInOrCm = holeDepth ?: _uiState.value.holeDepthInOrCm,
            postSquareInOrCm = postSquare ?: _uiState.value.postSquareInOrCm,
            postHoleCount = postHoles ?: _uiState.value.postHoleCount,
            stepCount = stepCount ?: _uiState.value.stepCount,
            stepRiseInOrCm = stepRise ?: _uiState.value.stepRiseInOrCm,
            stepRunInOrCm = stepRun ?: _uiState.value.stepRunInOrCm,
            stairWidthFtOrM = stairWidth ?: _uiState.value.stairWidthFtOrM,
            curbLengthFtOrM = curbLength ?: _uiState.value.curbLengthFtOrM,
            curbHeightInOrCm = curbHeight ?: _uiState.value.curbHeightInOrCm,
            curbWidthInOrCm = curbWidth ?: _uiState.value.curbWidthInOrCm,
            gutterWidthInOrCm = gutterWidth ?: _uiState.value.gutterWidthInOrCm,
            gutterThicknessInOrCm = gutterThickness ?: _uiState.value.gutterThicknessInOrCm,
            wastePercent = waste ?: _uiState.value.wastePercent
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        // Calculate Net Volume in Cubic Meters
        val netCuMeters: Double = if (isM) {
            when (s.shape) {
                ConcreteShape.SLAB, ConcreteShape.FOOTING -> {
                    val thickM = s.thicknessInchesOrCm / 100.0
                    s.length * s.width * thickM * s.quantity
                }
                ConcreteShape.COLUMN -> {
                    val radiusM = (s.diameterInchesOrCm / 100.0) / 2.0
                    PI * radiusM * radiusM * s.heightFtOrM * s.quantity
                }
                ConcreteShape.POST_HOLE -> {
                    val holeRadiusM = (s.holeDiameterInOrCm / 100.0) / 2.0
                    val holeDepthM = s.holeDepthInOrCm / 100.0
                    val holeVol = PI * holeRadiusM * holeRadiusM * holeDepthM
                    val postSideM = s.postSquareInOrCm / 100.0
                    val postVol = postSideM * postSideM * holeDepthM
                    (holeVol - postVol).coerceAtLeast(0.0) * s.postHoleCount
                }
                ConcreteShape.STAIRS -> {
                    // Stair volume = Width * Sum(step * rise * run)
                    val riseM = s.stepRiseInOrCm / 100.0
                    val runM = s.stepRunInOrCm / 100.0
                    var totalProfileArea = 0.0
                    for (i in 1..s.stepCount) {
                        totalProfileArea += (i * riseM) * runM
                    }
                    totalProfileArea * s.stairWidthFtOrM
                }
                ConcreteShape.CURB -> {
                    val cHeightM = s.curbHeightInOrCm / 100.0
                    val cWidthM = s.curbWidthInOrCm / 100.0
                    val gWidthM = s.gutterWidthInOrCm / 100.0
                    val gThickM = s.gutterThicknessInOrCm / 100.0
                    val sectionArea = (cHeightM * cWidthM) + (gWidthM * gThickM)
                    sectionArea * s.curbLengthFtOrM
                }
            }
        } else {
            // Imperial calculation -> convert ft3 to m3 (1 m3 = 35.3147 ft3)
            val netCuFt: Double = when (s.shape) {
                ConcreteShape.SLAB, ConcreteShape.FOOTING -> {
                    val thickFt = s.thicknessInchesOrCm / 12.0
                    s.length * s.width * thickFt * s.quantity
                }
                ConcreteShape.COLUMN -> {
                    val radiusFt = (s.diameterInchesOrCm / 12.0) / 2.0
                    PI * radiusFt * radiusFt * s.heightFtOrM * s.quantity
                }
                ConcreteShape.POST_HOLE -> {
                    val holeRadiusFt = (s.holeDiameterInOrCm / 12.0) / 2.0
                    val holeDepthFt = s.holeDepthInOrCm / 12.0
                    val holeVol = PI * holeRadiusFt * holeRadiusFt * holeDepthFt
                    val postSideFt = s.postSquareInOrCm / 12.0
                    val postVol = postSideFt * postSideFt * holeDepthFt
                    (holeVol - postVol).coerceAtLeast(0.0) * s.postHoleCount
                }
                ConcreteShape.STAIRS -> {
                    val riseFt = s.stepRiseInOrCm / 12.0
                    val runFt = s.stepRunInOrCm / 12.0
                    var totalProfileArea = 0.0
                    for (i in 1..s.stepCount) {
                        totalProfileArea += (i * riseFt) * runFt
                    }
                    totalProfileArea * s.stairWidthFtOrM
                }
                ConcreteShape.CURB -> {
                    val cHeightFt = s.curbHeightInOrCm / 12.0
                    val cWidthFt = s.curbWidthInOrCm / 12.0
                    val gWidthFt = s.gutterWidthInOrCm / 12.0
                    val gThickFt = s.gutterThicknessInOrCm / 12.0
                    val sectionArea = (cHeightFt * cWidthFt) + (gWidthFt * gThickFt)
                    sectionArea * s.curbLengthFtOrM
                }
            }
            netCuFt / 35.3146667
        }

        val netCuFt = netCuMeters * 35.3146667
        val netCuYards = netCuFt / 27.0

        val wasteMultiplier = 1.0 + (s.wastePercent / 100.0)
        val grossCuMeters = netCuMeters * wasteMultiplier
        val grossCuYards = netCuYards * wasteMultiplier
        val grossCuFt = netCuFt * wasteMultiplier

        // Weight: Concrete density approx 145 lb/cu.ft or 2322 kg/m3
        val totalLbs = grossCuFt * 145.0
        val totalTonnes = grossCuMeters * 2.35

        // Bags needed
        val bagYield = if (isM) s.selectedBag.cuMYield else s.selectedBag.cuFtYield
        val totalTarget = if (isM) grossCuMeters else grossCuFt
        val bags = if (bagYield > 0) ceil(totalTarget / bagYield).toInt() else 0

        // Ready mix trucks (standard 8 yd3 or 6 m3 truck)
        val truckLoads = grossCuYards / 8.0

        // Water requirement: approx 0.75 gal per 80lb bag or 2.8 L per 40kg bag
        val waterGal = bags * (s.selectedBag.lbs / 80.0) * 0.75
        val waterL = waterGal * 3.78541

        _uiState.value = s.copy(
            netVolumeCuFt = netCuFt,
            netVolumeCuYards = netCuYards,
            netVolumeCuMeters = netCuMeters,
            totalVolumeCuYardsWithWaste = grossCuYards,
            totalVolumeCuMetersWithWaste = grossCuMeters,
            totalWeightLbs = totalLbs,
            totalWeightTonnes = totalTonnes,
            bagsNeeded = bags,
            readyMixTruckLoads = truckLoads,
            waterGallonsNeeded = waterGal,
            waterLitersNeeded = waterL
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = "${s.shape.label}: ${String.format("%.2f", s.totalVolumeCuYardsWithWaste)} yd³ (${String.format("%.2f", s.totalVolumeCuMetersWithWaste)} m³) -> ${s.bagsNeeded}x ${s.selectedBag.label} bags / ${String.format("%.2f", s.readyMixTruckLoads)} Ready-Mix Trucks"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "CIVIL",
                title = "Concrete Volume & Bag Mix Sizer",
                summary = summary,
                value = s.totalVolumeCuYardsWithWaste
            )
        }
    }
}
