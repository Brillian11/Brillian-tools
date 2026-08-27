package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

enum class MasonryType(
    val lengthIn: Double,
    val widthIn: Double,
    val heightIn: Double,
    val isCmuBlock: Boolean,
    val label: String
) {
    MODULAR_BRICK(7.625, 3.625, 2.25, false, "Standard Modular Brick (7-5/8 x 3-5/8 x 2-1/4 in)"),
    QUEEN_BRICK(7.625, 2.75, 2.75, false, "Queen Brick (7-5/8 x 2-3/4 x 2-3/4 in)"),
    KING_BRICK(9.625, 2.625, 2.625, false, "King Brick (9-5/8 x 2-5/8 x 2-5/8 in)"),
    INDONESIAN_BATA_MERAH(7.87, 3.94, 1.97, false, "Indonesian Red Brick / Bata Merah (200x100x50 mm)"),
    CMU_4_INCH(15.625, 3.625, 7.625, true, "CMU Concrete Block 4\" (16x4x8 in nominal)"),
    CMU_6_INCH(15.625, 5.625, 7.625, true, "CMU Concrete Block 6\" (16x6x8 in nominal)"),
    CMU_8_INCH(15.625, 7.625, 7.625, true, "CMU Concrete Block 8\" (16x8x8 in nominal)"),
    CMU_12_INCH(15.625, 11.625, 7.625, true, "CMU Concrete Block 12\" (16x12x8 in nominal)"),
    CUSTOM(8.0, 4.0, 2.5, false, "Custom Brick / Block")
}

enum class GroutFillOption(val fractionCoreFilled: Double, val label: String) {
    NONE(0.0, "No Core Grouting (Hollow)"),
    EVERY_48_IN(0.25, "Grout Every 48\" O.C."),
    EVERY_24_IN(0.50, "Grout Every 24\" O.C."),
    EVERY_16_IN(0.75, "Grout Every 16\" O.C."),
    FULL(1.0, "Fully Grouted (100% Solid)")
}

data class MasonryUiState(
    val isMetric: Boolean = false,
    val masonryType: MasonryType = MasonryType.CMU_8_INCH,
    val groutOption: GroutFillOption = GroutFillOption.EVERY_24_IN,

    // Custom dimensions if selected
    val customLengthInOrMm: Double = 8.0,
    val customWidthInOrMm: Double = 4.0,
    val customHeightInOrMm: Double = 2.5,

    // Wall dimensions
    val wallLength: Double = 30.0, // ft or m
    val wallHeight: Double = 8.0,  // ft or m
    val isDoubleWythe: Boolean = false,
    val mortarJointInOrMm: Double = 0.375, // 3/8 in (or 10 mm)

    // Openings (doors/windows)
    val openingsAreaSqFtOrM2: Double = 40.0, // e.g. 40 sq ft (or 4 m2)
    val wastePercent: Double = 10.0,

    // Calculated Results
    val netWallAreaSqFt: Double = 200.0,
    val netWallAreaSqM: Double = 18.58,
    val totalUnitsNeeded: Int = 248, // With waste
    val rawUnitsCount: Int = 225,
    val mortarCuFtNeeded: Double = 8.5,
    val mortarCuMNeeded: Double = 0.24,
    val preMixMortar80lbBags: Int = 13,
    val masonrySandTons: Double = 0.55,
    val cementBagsForSiteMix: Int = 3,
    val groutVolumeCuYds: Double = 0.42,
    val groutPreMixBags: Int = 19
)

class MasonryMortarViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MasonryUiState())
    val uiState: StateFlow<MasonryUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                wallLength = if (metric) 10.0 else 30.0,
                wallHeight = if (metric) 2.5 else 8.0,
                mortarJointInOrMm = if (metric) 10.0 else 0.375,
                openingsAreaSqFtOrM2 = if (metric) 4.0 else 40.0,
                customLengthInOrMm = if (metric) 200.0 else 8.0,
                customWidthInOrMm = if (metric) 100.0 else 4.0,
                customHeightInOrMm = if (metric) 50.0 else 2.5
            )
            recalculate()
        }
    }

    fun setMasonryType(type: MasonryType) {
        _uiState.value = _uiState.value.copy(masonryType = type)
        recalculate()
    }

    fun setGroutOption(opt: GroutFillOption) {
        _uiState.value = _uiState.value.copy(groutOption = opt)
        recalculate()
    }

    fun toggleDoubleWythe() {
        _uiState.value = _uiState.value.copy(isDoubleWythe = !_uiState.value.isDoubleWythe)
        recalculate()
    }

    fun updateInputs(
        wLength: Double? = null,
        wHeight: Double? = null,
        joint: Double? = null,
        openingsArea: Double? = null,
        waste: Double? = null,
        cL: Double? = null,
        cW: Double? = null,
        cH: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            wallLength = wLength ?: _uiState.value.wallLength,
            wallHeight = wHeight ?: _uiState.value.wallHeight,
            mortarJointInOrMm = joint ?: _uiState.value.mortarJointInOrMm,
            openingsAreaSqFtOrM2 = openingsArea ?: _uiState.value.openingsAreaSqFtOrM2,
            wastePercent = waste ?: _uiState.value.wastePercent,
            customLengthInOrMm = cL ?: _uiState.value.customLengthInOrMm,
            customWidthInOrMm = cW ?: _uiState.value.customWidthInOrMm,
            customHeightInOrMm = cH ?: _uiState.value.customHeightInOrMm
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        val grossAreaSqFt = if (isM) (s.wallLength * s.wallHeight) * 10.7639 else s.wallLength * s.wallHeight
        val openingsSqFt = if (isM) s.openingsAreaSqFtOrM2 * 10.7639 else s.openingsAreaSqFtOrM2
        val netAreaSqFt = (grossAreaSqFt - openingsSqFt).coerceAtLeast(1.0)
        val netAreaSqM = netAreaSqFt / 10.7639

        // Unit Dimensions (in inches)
        val unitL_in = if (s.masonryType == MasonryType.CUSTOM) {
            if (isM) s.customLengthInOrMm / 25.4 else s.customLengthInOrMm
        } else s.masonryType.lengthIn

        val unitW_in = if (s.masonryType == MasonryType.CUSTOM) {
            if (isM) s.customWidthInOrMm / 25.4 else s.customWidthInOrMm
        } else s.masonryType.widthIn

        val unitH_in = if (s.masonryType == MasonryType.CUSTOM) {
            if (isM) s.customHeightInOrMm / 25.4 else s.customHeightInOrMm
        } else s.masonryType.heightIn

        val jointIn = if (isM) s.mortarJointInOrMm / 25.4 else s.mortarJointInOrMm

        // Nominal Face Area in sq inches
        val nominalFaceSqIn = (unitL_in + jointIn) * (unitH_in + jointIn)
        val unitsPerSqFt = 144.0 / nominalFaceSqIn
        val wytheMultiplier = if (s.isDoubleWythe) 2.0 else 1.0

        val rawUnits = netAreaSqFt * unitsPerSqFt * wytheMultiplier
        val wasteFactor = 1.0 + (s.wastePercent / 100.0)
        val totalUnits = ceil(rawUnits * wasteFactor).toInt()

        // Mortar volume calculation
        // Mortar volume per unit = (unitL * unitW * joint) + (unitH * unitW * joint) in cu inches
        // For CMU blocks, face-shell bedding is typical (~3.5 cu.ft per 100 blocks)
        val mortarCuFtPer100Units = if (s.masonryType.isCmuBlock) {
            when (s.masonryType) {
                MasonryType.CMU_4_INCH -> 2.6
                MasonryType.CMU_6_INCH -> 3.2
                MasonryType.CMU_8_INCH -> 3.8
                MasonryType.CMU_12_INCH -> 4.8
                else -> 3.8
            }
        } else {
            // Bricks: approx 5.5 - 7.5 cu.ft per 1000 bricks
            6.5 / 10.0 // 0.65 cu.ft per 100 bricks
        }

        val totalMortarCuFt = (totalUnits / 100.0) * mortarCuFtPer100Units * wasteFactor
        val totalMortarCuM = totalMortarCuFt / 35.3147

        // Pre-mix Mortar 80lb / Type N/S bag yields ~0.67 cu.ft
        val mortar80lbBags = ceil(totalMortarCuFt / 0.67).toInt()
        val sandTons = (totalMortarCuFt * 80.0) / 2000.0 // approx 80 lbs sand per cu ft mortar
        val cementBags = ceil(totalMortarCuFt / 3.0).toInt() // 1 bag portland cement per ~3 cu ft mortar

        // Grout Core Filling (for CMU only)
        var groutYards = 0.0
        var groutBags = 0
        if (s.masonryType.isCmuBlock && s.groutOption.fractionCoreFilled > 0) {
            // 8" CMU block cores hold approx 0.009 yd3 per block if 100% full
            val cuYdsPerBlockFull = when (s.masonryType) {
                MasonryType.CMU_6_INCH -> 0.006
                MasonryType.CMU_8_INCH -> 0.009
                MasonryType.CMU_12_INCH -> 0.015
                else -> 0.004
            }
            groutYards = totalUnits * cuYdsPerBlockFull * s.groutOption.fractionCoreFilled * wasteFactor
            groutBags = ceil((groutYards * 27.0) / 0.60).toInt() // 80lb core fill grout bags
        }

        _uiState.value = s.copy(
            netWallAreaSqFt = netAreaSqFt,
            netWallAreaSqM = netAreaSqM,
            rawUnitsCount = ceil(rawUnits).toInt(),
            totalUnitsNeeded = totalUnits,
            mortarCuFtNeeded = totalMortarCuFt,
            mortarCuMNeeded = totalMortarCuM,
            preMixMortar80lbBags = mortar80lbBags,
            masonrySandTons = sandTons,
            cementBagsForSiteMix = cementBags,
            groutVolumeCuYds = groutYards,
            groutPreMixBags = groutBags
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = "${s.masonryType.label}: Wall ${String.format("%.1f", s.netWallAreaSqFt)} sq.ft (${String.format("%.1f", s.netWallAreaSqM)} m²) -> ${s.totalUnitsNeeded} Units, ${s.preMixMortar80lbBags} Mortar Bags, ${String.format("%.2f", s.groutVolumeCuYds)} yd³ Grout"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "CIVIL",
                title = "Brick, Block & Mortar Calculator",
                summary = summary,
                value = s.totalUnitsNeeded.toDouble()
            )
        }
    }
}
