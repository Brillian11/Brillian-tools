package com.example.ui.screens.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max

enum class SurfacePorosity(
    val coverageSqFtPerGal: Double,
    val coverageSqMPerL: Double,
    val label: String,
    val desc: String
) {
    SMOOTH_PRIMED(400.0, 9.8, "Smooth Drywall (Pre-Primed)", "Standard interior walls, low paint absorption"),
    PREVIOUSLY_PAINTED(350.0, 8.6, "Previously Painted / Eggshell", "Repaint over sound existing paint film"),
    TEXTURED_DRYWALL(300.0, 7.4, "Textured / Orange Peel", "Orange peel, knockdown or light plaster"),
    ROUGH_MASONRY(220.0, 5.4, "Rough Brick, Stucco & Masonry", "Porous high-absorption masonry & concrete")
}

data class PaintCoverageUiState(
    val isMetric: Boolean = false,
    
    // Room Dimensions
    val roomLength: Double = 16.0, // ft or m
    val roomWidth: Double = 12.0,  // ft or m
    val ceilingHeight: Double = 9.0, // ft or m

    // Openings
    val doorCount: Int = 2,    // Standard 21 sq ft (2.0 m²) each
    val windowCount: Int = 2,  // Standard 15 sq ft (1.4 m²) each
    val customDeductionSqFtOrM2: Double = 0.0,

    // Toggles & Options
    val includeCeiling: Boolean = true,
    val includeTrimBaseboard: Boolean = true,
    val surfacePorosity: SurfacePorosity = SurfacePorosity.PREVIOUSLY_PAINTED,
    
    // Coat Counts
    val paintCoats: Int = 2,
    val primerCoats: Int = 1,
    val ceilingCoats: Int = 2,

    // Calculated Surface Areas
    val grossWallAreaSqFt: Double = 504.0,
    val totalOpeningsAreaSqFt: Double = 72.0,
    val netWallAreaSqFt: Double = 432.0,
    val netWallAreaSqM: Double = 40.13,
    val ceilingAreaSqFt: Double = 192.0,
    val ceilingAreaSqM: Double = 17.84,
    val trimLinearFt: Double = 50.0,
    val trimLinearM: Double = 15.24,

    // Paint Volumes - Wall
    val wallPaintGallonsExact: Double = 2.47,
    val wallPaintLitersExact: Double = 9.35,
    val wallPaintGallonCans: Int = 2,
    val wallPaintQuartCans: Int = 2,
    val wallPaintLitersBuy: Int = 10,

    // Primer Volumes
    val primerGallonsExact: Double = 1.08,
    val primerLitersExact: Double = 4.09,
    val primerGallonCans: Int = 2,
    val primerLitersBuy: Int = 5,

    // Ceiling Paint Volumes
    val ceilingPaintGallonsExact: Double = 0.96,
    val ceilingPaintLitersExact: Double = 3.63,
    val ceilingPaintGallonCans: Int = 1,
    val ceilingPaintLitersBuy: Int = 4,

    // Supplies Takeoff
    val painterTapeRolls: Int = 2, // 60 yd rolls
    val rollerCovers: Int = 2,
    val sashBrushes: Int = 1,
    val dropCloths: Int = 2
)

class PaintCoverageViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaintCoverageUiState())
    val uiState: StateFlow<PaintCoverageUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                roomLength = if (metric) 5.0 else 16.0,
                roomWidth = if (metric) 3.6 else 12.0,
                ceilingHeight = if (metric) 2.7 else 9.0,
                customDeductionSqFtOrM2 = if (metric) 0.0 else 0.0
            )
            recalculate()
        }
    }

    fun setSurfacePorosity(porosity: SurfacePorosity) {
        _uiState.value = _uiState.value.copy(surfacePorosity = porosity)
        recalculate()
    }

    fun toggleCeiling() {
        _uiState.value = _uiState.value.copy(includeCeiling = !_uiState.value.includeCeiling)
        recalculate()
    }

    fun toggleTrim() {
        _uiState.value = _uiState.value.copy(includeTrimBaseboard = !_uiState.value.includeTrimBaseboard)
        recalculate()
    }

    fun updateDimensions(
        length: Double? = null,
        width: Double? = null,
        height: Double? = null,
        doors: Int? = null,
        windows: Int? = null,
        customDeduct: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            roomLength = length ?: _uiState.value.roomLength,
            roomWidth = width ?: _uiState.value.roomWidth,
            ceilingHeight = height ?: _uiState.value.ceilingHeight,
            doorCount = (doors ?: _uiState.value.doorCount).coerceAtLeast(0),
            windowCount = (windows ?: _uiState.value.windowCount).coerceAtLeast(0),
            customDeductionSqFtOrM2 = customDeduct ?: _uiState.value.customDeductionSqFtOrM2
        )
        recalculate()
    }

    fun updateCoats(
        paint: Int? = null,
        primer: Int? = null,
        ceiling: Int? = null
    ) {
        _uiState.value = _uiState.value.copy(
            paintCoats = (paint ?: _uiState.value.paintCoats).coerceIn(1, 4),
            primerCoats = (primer ?: _uiState.value.primerCoats).coerceIn(0, 3),
            ceilingCoats = (ceiling ?: _uiState.value.ceilingCoats).coerceIn(1, 3)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        val lenFt = if (isM) s.roomLength * 3.28084 else s.roomLength.coerceAtLeast(1.0)
        val widFt = if (isM) s.roomWidth * 3.28084 else s.roomWidth.coerceAtLeast(1.0)
        val hgtFt = if (isM) s.ceilingHeight * 3.28084 else s.ceilingHeight.coerceAtLeast(1.0)

        val perimeterFt = 2.0 * (lenFt + widFt)
        val grossWallSqFt = perimeterFt * hgtFt

        val doorDeductionSqFt = s.doorCount * 21.0
        val windowDeductionSqFt = s.windowCount * 15.0
        val customDeductionSqFt = if (isM) s.customDeductionSqFtOrM2 * 10.7639 else s.customDeductionSqFtOrM2
        val totalDeductionsSqFt = doorDeductionSqFt + windowDeductionSqFt + customDeductionSqFt

        val netWallSqFt = max(10.0, grossWallSqFt - totalDeductionsSqFt)
        val netWallSqM = netWallSqFt / 10.7639

        val ceilingSqFt = lenFt * widFt
        val ceilingSqM = ceilingSqFt / 10.7639

        val trimFt = max(0.0, perimeterFt - (s.doorCount * 3.0))
        val trimM = trimFt * 0.3048

        // Coverage Rates
        val covSqFtPerGal = s.surfacePorosity.coverageSqFtPerGal
        val covSqMPerL = s.surfacePorosity.coverageSqMPerL

        // 1. Wall Paint
        val wallPaintGal = (netWallSqFt * s.paintCoats) / covSqFtPerGal
        val wallPaintL = (netWallSqM * s.paintCoats) / covSqMPerL
        val wallFullGal = wallPaintGal.toInt()
        val wallRemQuarts = ceil((wallPaintGal - wallFullGal) * 4.0).toInt()
        val (wallGalBuy, wallQtBuy) = if (wallRemQuarts >= 4) (wallFullGal + 1 to 0) else (wallFullGal to wallRemQuarts)
        val wallLitersBuy = ceil(wallPaintL).toInt()

        // 2. Primer
        val primerGal = if (s.primerCoats > 0) (netWallSqFt * s.primerCoats) / covSqFtPerGal else 0.0
        val primerL = if (s.primerCoats > 0) (netWallSqM * s.primerCoats) / covSqMPerL else 0.0
        val primerGalBuy = ceil(primerGal).toInt()
        val primerLitersBuy = ceil(primerL).toInt()

        // 3. Ceiling Paint
        val ceilingGal = if (s.includeCeiling) (ceilingSqFt * s.ceilingCoats) / covSqFtPerGal else 0.0
        val ceilingL = if (s.includeCeiling) (ceilingSqM * s.ceilingCoats) / covSqMPerL else 0.0
        val ceilingGalBuy = ceil(ceilingGal).toInt()
        val ceilingLitersBuy = ceil(ceilingL).toInt()

        // Supplies
        val tapeRolls = ceil((perimeterFt * 2.0 + s.windowCount * 14.0 + s.doorCount * 17.0) / 180.0).toInt() // 60yd = 180ft
        val dropCloths = ceil(ceilingSqFt / 144.0).toInt().coerceAtLeast(1) // 12x12 cloth ~ 144 sq ft

        _uiState.value = s.copy(
            grossWallAreaSqFt = grossWallSqFt,
            totalOpeningsAreaSqFt = totalDeductionsSqFt,
            netWallAreaSqFt = netWallSqFt,
            netWallAreaSqM = netWallSqM,
            ceilingAreaSqFt = ceilingSqFt,
            ceilingAreaSqM = ceilingSqM,
            trimLinearFt = trimFt,
            trimLinearM = trimM,
            wallPaintGallonsExact = wallPaintGal,
            wallPaintLitersExact = wallPaintL,
            wallPaintGallonCans = wallGalBuy,
            wallPaintQuartCans = wallQtBuy,
            wallPaintLitersBuy = wallLitersBuy,
            primerGallonsExact = primerGal,
            primerLitersExact = primerL,
            primerGallonCans = primerGalBuy,
            primerLitersBuy = primerLitersBuy,
            ceilingPaintGallonsExact = ceilingGal,
            ceilingPaintLitersExact = ceilingL,
            ceilingPaintGallonCans = ceilingGalBuy,
            ceilingPaintLitersBuy = ceilingLitersBuy,
            painterTapeRolls = tapeRolls.coerceAtLeast(1),
            dropCloths = dropCloths
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val areaText = if (s.isMetric) "${String.format("%.1f", s.netWallAreaSqM)} m²" else "${String.format("%.1f", s.netWallAreaSqFt)} sq.ft"
        val paintText = if (s.isMetric) "${s.wallPaintLitersBuy}L (${s.paintCoats} coats)" else "${s.wallPaintGallonCans} Gal + ${s.wallPaintQuartCans} Qt (${s.paintCoats} coats)"
        val summary = "Paint Takeoff (Net $areaText): $paintText, Primer ${if (s.isMetric) "${s.primerLitersBuy}L" else "${s.primerGallonCans} Gal"}, Ceiling ${if (s.isMetric) "${s.ceilingPaintLitersBuy}L" else "${s.ceilingPaintGallonCans} Gal"}"

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "FINISHING",
                title = "Wall Area & Paint Coverage",
                summary = summary,
                value = s.wallPaintGallonsExact
            )
        }
    }
}
