package com.example.ui.screens.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

enum class StandardTilePreset(
    val lengthIn: Double,
    val widthIn: Double,
    val thicknessIn: Double,
    val piecesPerBoxDefault: Int,
    val label: String
) {
    SIZE_12X12(12.0, 12.0, 0.375, 10, "12\" x 12\" Standard Floor (30x30 cm)"),
    SIZE_12X24(24.0, 12.0, 0.375, 8, "12\" x 24\" Large Format Plank (30x60 cm)"),
    SIZE_24X24(24.0, 24.0, 0.375, 4, "24\" x 24\" Large Porcelain (60x60 cm)"),
    SIZE_6X24_WOOD_PLANK(24.0, 6.0, 0.375, 12, "6\" x 24\" Wood Look Plank (15x60 cm)"),
    SIZE_3X6_SUBWAY(6.0, 3.0, 0.25, 80, "3\" x 6\" Subway Wall Tile (7.5x15 cm)"),
    SIZE_4X4_WALL(4.0, 4.0, 0.25, 50, "4\" x 4\" Classic Ceramic (10x10 cm)"),
    SIZE_18X18(18.0, 18.0, 0.375, 6, "18\" x 18\" Square Paver (45x45 cm)"),
    SIZE_60X120_SLAB(47.24, 23.62, 0.394, 2, "24\" x 48\" Large Slab (60x120 cm)"),
    CUSTOM(12.0, 12.0, 0.375, 10, "Custom Tile Dimensions")
}

enum class GroutJointWidth(val widthIn: Double, val widthMm: Double, val label: String) {
    JOINT_1_16(0.0625, 1.5, "1/16\" (1.5 mm) - Tight Rectified"),
    JOINT_1_8(0.125, 3.0, "1/8\" (3.0 mm) - Standard Floor"),
    JOINT_3_16(0.1875, 4.5, "3/16\" (4.5 mm) - Rustic/Irregular"),
    JOINT_1_4(0.25, 6.0, "1/4\" (6.0 mm) - Wide Paver"),
    JOINT_3_8(0.375, 9.5, "3/8\" (9.5 mm) - Heavy Quarry/Brick")
}

enum class GroutType(val label: String, val recommendation: String) {
    UNSANDED("Unsanded Grout", "Best for joints ≤ 1/8\" and polished/glass tile"),
    SANDED("Sanded Grout", "Best for joints > 1/8\" to prevent shrinkage cracks"),
    EPOXY("Epoxy Grout", "100% stain & waterproof for commercial & shower floors")
}

enum class MortarTrowelNotch(val coverageSqFtPer50lb: Double, val label: String) {
    NOTCH_1_4_SQUARE(85.0, "1/4\" x 1/4\" Square Notch (Tiles up to 8\")"),
    NOTCH_1_4_X_3_8(65.0, "1/4\" x 3/8\" Notch (Tiles 8\" to 15\")"),
    NOTCH_1_2_SQUARE(45.0, "1/2\" x 1/2\" Square Notch (Large Format ≥ 15\")")
}

data class TileGroutUiState(
    val isMetric: Boolean = false,
    val tilePreset: StandardTilePreset = StandardTilePreset.SIZE_12X24,
    val jointWidth: GroutJointWidth = GroutJointWidth.JOINT_1_8,
    val trowelNotch: MortarTrowelNotch = MortarTrowelNotch.NOTCH_1_4_X_3_8,

    // Dimensions
    val roomLength: Double = 16.0, // ft or m
    val roomWidth: Double = 12.0,  // ft or m
    val cutoutAreaSqFtOrM2: Double = 15.0, // e.g. vanity / tub cutout

    // Custom Tile Dimensions (if custom selected)
    val customTileLengthInOrCm: Double = 12.0,
    val customTileWidthInOrCm: Double = 12.0,
    val customTileThicknessInOrMm: Double = 0.375,

    // Packaging & Waste
    val piecesPerBox: Int = 8,
    val wastePercent: Double = 12.0, // 10% straight, 15% diagonal

    // Calculated Area
    val grossAreaSqFt: Double = 192.0,
    val netAreaSqFt: Double = 177.0,
    val netAreaSqM: Double = 16.44,
    val totalAreaWithWasteSqFt: Double = 198.24,
    val totalAreaWithWasteSqM: Double = 18.42,

    // Tile Counts
    val exactTilesNeeded: Int = 99,
    val totalTilesWithWaste: Int = 111,
    val totalBoxesNeeded: Int = 14,
    val tilesPerSqFt: Double = 0.5,

    // Grout Takeoff
    val groutWeightLbs: Double = 18.4,
    val groutWeightKg: Double = 8.35,
    val grout25lbBags: Int = 1,
    val grout10lbBags: Int = 2,
    val grout5kgBags: Int = 2,
    val recommendedGroutType: GroutType = GroutType.UNSANDED,

    // Thin-Set Mortar Takeoff
    val thinset50lbBags: Int = 3,
    val thinset25kgBags: Int = 3,

    // Edge Trim Linear Run
    val perimeterTrimFt: Double = 56.0,
    val perimeterTrimM: Double = 17.07
)

class TileGroutViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TileGroutUiState())
    val uiState: StateFlow<TileGroutUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                roomLength = if (metric) 5.0 else 16.0, // 5m vs 16ft
                roomWidth = if (metric) 3.6 else 12.0,  // 3.6m vs 12ft
                cutoutAreaSqFtOrM2 = if (metric) 1.5 else 15.0,
                customTileLengthInOrCm = if (metric) 30.0 else 12.0,
                customTileWidthInOrCm = if (metric) 30.0 else 12.0,
                customTileThicknessInOrMm = if (metric) 10.0 else 0.375
            )
            recalculate()
        }
    }

    fun setTilePreset(preset: StandardTilePreset) {
        _uiState.value = _uiState.value.copy(
            tilePreset = preset,
            piecesPerBox = preset.piecesPerBoxDefault
        )
        recalculate()
    }

    fun setJointWidth(joint: GroutJointWidth) {
        _uiState.value = _uiState.value.copy(jointWidth = joint)
        recalculate()
    }

    fun setTrowelNotch(notch: MortarTrowelNotch) {
        _uiState.value = _uiState.value.copy(trowelNotch = notch)
        recalculate()
    }

    fun updateDimensions(
        length: Double? = null,
        width: Double? = null,
        cutout: Double? = null,
        waste: Double? = null,
        boxCount: Int? = null,
        customL: Double? = null,
        customW: Double? = null,
        customT: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            roomLength = length ?: _uiState.value.roomLength,
            roomWidth = width ?: _uiState.value.roomWidth,
            cutoutAreaSqFtOrM2 = cutout ?: _uiState.value.cutoutAreaSqFtOrM2,
            wastePercent = waste ?: _uiState.value.wastePercent,
            piecesPerBox = (boxCount ?: _uiState.value.piecesPerBox).coerceAtLeast(1),
            customTileLengthInOrCm = customL ?: _uiState.value.customTileLengthInOrCm,
            customTileWidthInOrCm = customW ?: _uiState.value.customTileWidthInOrCm,
            customTileThicknessInOrMm = customT ?: _uiState.value.customTileThicknessInOrMm
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        val grossSqFt = if (isM) (s.roomLength * s.roomWidth) * 10.7639 else (s.roomLength * s.roomWidth).coerceAtLeast(1.0)
        val cutoutSqFt = if (isM) s.cutoutAreaSqFtOrM2 * 10.7639 else s.cutoutAreaSqFtOrM2
        val netSqFt = (grossSqFt - cutoutSqFt).coerceAtLeast(1.0)
        val netSqM = netSqFt / 10.7639

        val wasteMultiplier = 1.0 + (s.wastePercent / 100.0)
        val totalAreaWithWasteSqFt = netSqFt * wasteMultiplier
        val totalAreaWithWasteSqM = totalAreaWithWasteSqFt / 10.7639

        // Tile Dimensions in inches
        val tileL_in = if (s.tilePreset == StandardTilePreset.CUSTOM) {
            if (isM) s.customTileLengthInOrCm / 2.54 else s.customTileLengthInOrCm
        } else s.tilePreset.lengthIn

        val tileW_in = if (s.tilePreset == StandardTilePreset.CUSTOM) {
            if (isM) s.customTileWidthInOrCm / 2.54 else s.customTileWidthInOrCm
        } else s.tilePreset.widthIn

        val tileT_in = if (s.tilePreset == StandardTilePreset.CUSTOM) {
            if (isM) s.customTileThicknessInOrMm / 25.4 else s.customTileThicknessInOrMm
        } else s.tilePreset.thicknessIn

        val singleTileSqFt = (tileL_in * tileW_in) / 144.0
        val tilesPerSqFt = 1.0 / singleTileSqFt

        val exactTiles = ceil(netSqFt * tilesPerSqFt).toInt()
        val totalTiles = ceil(totalAreaWithWasteSqFt * tilesPerSqFt).toInt()
        val boxesNeeded = ceil(totalTiles.toDouble() / s.piecesPerBox.toDouble()).toInt()

        // Grout Weight Calculation (ANSI Industry Standard formula):
        // Grout Weight (lbs) = [(L + W) * T * J * NetArea * 1.75] / (L * W)
        val jointIn = s.jointWidth.widthIn
        val groutWeightLbs = ((tileL_in + tileW_in) * tileT_in * jointIn * netSqFt * 1.75) / (tileL_in * tileW_in)
        val groutWeightKg = groutWeightLbs * 0.453592

        val grout25lbBags = ceil(groutWeightLbs / 25.0).toInt()
        val grout10lbBags = ceil(groutWeightLbs / 10.0).toInt()
        val grout5kgBags = ceil(groutWeightKg / 5.0).toInt()

        val recommendedGrout = if (s.jointWidth.widthIn <= 0.125) GroutType.UNSANDED else GroutType.SANDED

        // Thin-set Mortar bags (50 lb bags based on trowel notch coverage + 10% waste)
        val thinset50lb = ceil((netSqFt / s.trowelNotch.coverageSqFtPer50lb) * 1.10).toInt()
        val thinset25kg = thinset50lb // roughly 50 lb ~ 22.7 kg ~ 25 kg bag

        // Perimeter Trim
        val perimFt = (s.roomLength + s.roomWidth) * 2.0 * (if (isM) 3.28084 else 1.0)
        val perimM = perimFt * 0.3048

        _uiState.value = s.copy(
            grossAreaSqFt = grossSqFt,
            netAreaSqFt = netSqFt,
            netAreaSqM = netSqM,
            totalAreaWithWasteSqFt = totalAreaWithWasteSqFt,
            totalAreaWithWasteSqM = totalAreaWithWasteSqM,
            exactTilesNeeded = exactTiles,
            totalTilesWithWaste = totalTiles,
            totalBoxesNeeded = boxesNeeded,
            tilesPerSqFt = tilesPerSqFt,
            groutWeightLbs = groutWeightLbs,
            groutWeightKg = groutWeightKg,
            grout25lbBags = grout25lbBags,
            grout10lbBags = grout10lbBags,
            grout5kgBags = grout5kgBags,
            recommendedGroutType = recommendedGrout,
            thinset50lbBags = thinset50lb,
            thinset25kgBags = thinset25kg,
            perimeterTrimFt = perimFt,
            perimeterTrimM = perimM
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val areaText = if (s.isMetric) "${String.format("%.1f", s.netAreaSqM)} m²" else "${String.format("%.1f", s.netAreaSqFt)} sq.ft"
        val groutText = if (s.isMetric) "${String.format("%.1f", s.groutWeightKg)} kg (${s.grout5kgBags} bags)" else "${String.format("%.1f", s.groutWeightLbs)} lbs (${s.grout25lbBags} bags)"
        val summary = "${s.tilePreset.label}: Area $areaText (${s.wastePercent.toInt()}% waste) -> ${s.totalTilesWithWaste} Tiles (${s.totalBoxesNeeded} boxes), Grout $groutText, ${s.thinset50lbBags} Mortar Bags"

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "FINISHING",
                title = "Tile, Grout & Flooring Estimator",
                summary = summary,
                value = s.totalTilesWithWaste.toDouble()
            )
        }
    }
}
