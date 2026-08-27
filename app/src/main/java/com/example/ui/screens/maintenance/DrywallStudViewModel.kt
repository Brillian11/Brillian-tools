package com.example.ui.screens.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

enum class DrywallSheetPreset(
    val lengthFt: Double,
    val widthFt: Double,
    val areaSqFt: Double,
    val lengthMm: Double,
    val widthMm: Double,
    val areaSqM: Double,
    val label: String
) {
    SHEET_4X8(8.0, 4.0, 32.0, 2400.0, 1200.0, 2.88, "4' x 8' Standard (1/2\" or 5/8\")"),
    SHEET_4X10(10.0, 4.0, 40.0, 3000.0, 1200.0, 3.60, "4' x 10' Medium Sheet"),
    SHEET_4X12(12.0, 4.0, 48.0, 3600.0, 1200.0, 4.32, "4' x 12' Large Commercial"),
    SHEET_METRIC_2400(7.87, 3.94, 30.98, 2400.0, 1200.0, 2.88, "Metric 2400 x 1200 mm"),
    SHEET_METRIC_3000(9.84, 3.94, 38.75, 3000.0, 1200.0, 3.60, "Metric 3000 x 1200 mm")
}

enum class StudSpacing(val spacingInches: Double, val spacingMm: Double, val label: String) {
    SPACING_16_OC(16.0, 406.4, "16\" On-Center (Standard Structural)"),
    SPACING_24_OC(24.0, 609.6, "24\" On-Center (Advanced / Non-Bearing)"),
    SPACING_400_MM(15.75, 400.0, "400 mm On-Center (Metric Standard)"),
    SPACING_600_MM(23.62, 600.0, "600 mm On-Center (Metric Non-Bearing)")
}

data class DrywallStudUiState(
    val isMetric: Boolean = false,
    val sheetPreset: DrywallSheetPreset = DrywallSheetPreset.SHEET_4X8,
    val studSpacing: StudSpacing = StudSpacing.SPACING_16_OC,

    // Dimensions
    val wallRunLength: Double = 40.0, // ft or m (total linear perimeter or wall run)
    val wallHeight: Double = 8.0,     // ft or m
    val includeCeiling: Boolean = true,
    val roomWidthForCeiling: Double = 12.0, // ft or m (if ceiling included)

    // Openings & Details
    val doorOpenings: Int = 1,
    val windowOpenings: Int = 2,
    val cornersCount: Int = 4,
    val tJunctionsCount: Int = 1,
    val wastePercent: Double = 10.0,

    // Calculated Areas
    val totalWallAreaSqFt: Double = 320.0,
    val totalCeilingAreaSqFt: Double = 144.0,
    val combinedDrywallAreaSqFt: Double = 464.0,
    val combinedDrywallAreaSqM: Double = 43.11,

    // Drywall Takeoff
    val exactSheetsNeeded: Int = 15,
    val totalSheetsWithWaste: Int = 17,
    val screwsCount: Int = 544,
    val screwsLbs: Double = 1.81,
    val jointCompound4_5GalBuckets: Int = 2,
    val jointCompound1GalPails: Int = 1,
    val jointTapeRolls500Ft: Int = 1,
    val cornerBead8FtPieces: Int = 4,

    // Framing Studs Takeoff
    val fieldStudsCount: Int = 31,
    val cornerStudsCount: Int = 12,
    val openingStudsCount: Int = 12, // King + Jack studs
    val totalFramingStudsNeeded: Int = 62, // with 10% waste
    val plateLumberTotalLinearFt: Double = 120.0, // 3 plates (1 sole + 2 top)
    val plateLumberTotalLinearM: Double = 36.58,
    val plateBoards10FtCount: Int = 14
)

class DrywallStudViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrywallStudUiState())
    val uiState: StateFlow<DrywallStudUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                wallRunLength = if (metric) 12.0 else 40.0,
                wallHeight = if (metric) 2.5 else 8.0,
                roomWidthForCeiling = if (metric) 4.0 else 12.0,
                sheetPreset = if (metric) DrywallSheetPreset.SHEET_METRIC_2400 else DrywallSheetPreset.SHEET_4X8,
                studSpacing = if (metric) StudSpacing.SPACING_400_MM else StudSpacing.SPACING_16_OC
            )
            recalculate()
        }
    }

    fun setSheetPreset(preset: DrywallSheetPreset) {
        _uiState.value = _uiState.value.copy(sheetPreset = preset)
        recalculate()
    }

    fun setStudSpacing(spacing: StudSpacing) {
        _uiState.value = _uiState.value.copy(studSpacing = spacing)
        recalculate()
    }

    fun toggleCeiling() {
        _uiState.value = _uiState.value.copy(includeCeiling = !_uiState.value.includeCeiling)
        recalculate()
    }

    fun updateDimensions(
        wallRun: Double? = null,
        height: Double? = null,
        roomWidth: Double? = null,
        waste: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            wallRunLength = wallRun ?: _uiState.value.wallRunLength,
            wallHeight = height ?: _uiState.value.wallHeight,
            roomWidthForCeiling = roomWidth ?: _uiState.value.roomWidthForCeiling,
            wastePercent = waste ?: _uiState.value.wastePercent
        )
        recalculate()
    }

    fun updateOpenings(
        doors: Int? = null,
        windows: Int? = null,
        corners: Int? = null,
        tJunctions: Int? = null
    ) {
        _uiState.value = _uiState.value.copy(
            doorOpenings = (doors ?: _uiState.value.doorOpenings).coerceAtLeast(0),
            windowOpenings = (windows ?: _uiState.value.windowOpenings).coerceAtLeast(0),
            cornersCount = (corners ?: _uiState.value.cornersCount).coerceAtLeast(0),
            tJunctionsCount = (tJunctions ?: _uiState.value.tJunctionsCount).coerceAtLeast(0)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        val wallRunFt = if (isM) s.wallRunLength * 3.28084 else s.wallRunLength.coerceAtLeast(1.0)
        val wallHeightFt = if (isM) s.wallHeight * 3.28084 else s.wallHeight.coerceAtLeast(1.0)
        val roomWidthFt = if (isM) s.roomWidthForCeiling * 3.28084 else s.roomWidthForCeiling.coerceAtLeast(1.0)

        val wallAreaSqFt = wallRunFt * wallHeightFt
        val ceilingAreaSqFt = if (s.includeCeiling) (wallRunFt / 2.0) * roomWidthFt else 0.0
        val combinedSqFt = wallAreaSqFt + ceilingAreaSqFt
        val combinedSqM = combinedSqFt / 10.7639

        // Drywall Sheets
        val sheetArea = s.sheetPreset.areaSqFt
        val wasteFactor = 1.0 + (s.wastePercent / 100.0)
        val exactSheets = ceil(combinedSqFt / sheetArea).toInt()
        val totalSheets = ceil((combinedSqFt / sheetArea) * wasteFactor).toInt()

        // Drywall Fasteners & Mud
        // ~32 screws per 4x8 sheet (or ~1 lb per 300 sq ft)
        val screwsCount = totalSheets * 32
        val screwsLbs = screwsCount / 300.0

        // Mud: ~1 bucket (4.5 gal) per 450 sq ft of drywall across 3 coats
        val totalMudGal = combinedSqFt * 0.0105
        val mud4_5GalBuckets = ceil(totalMudGal / 4.5).toInt()
        val mud1GalPails = ceil(totalMudGal % 4.5).toInt()

        // Tape: ~0.35 ft tape per sq ft drywall
        val tapeFt = combinedSqFt * 0.35
        val tapeRolls500Ft = ceil(tapeFt / 500.0).toInt().coerceAtLeast(1)

        // Corner bead (for outside corners)
        val cornerBeads = s.cornersCount

        // Framing Studs Calculation
        // 1. Basic field studs along run
        val spacingInches = s.studSpacing.spacingInches
        val wallRunInches = wallRunFt * 12.0
        val fieldStuds = ceil(wallRunInches / spacingInches).toInt() + 1

        // 2. Corner studs: 3 studs per corner
        val cornerStuds = s.cornersCount * 3

        // 3. T-junction partition studs: 2 studs per T-post
        val tJunctionStuds = s.tJunctionsCount * 2

        // 4. Openings framing: 2 King + 2 Jack/Trimmer studs per opening = 4 studs each
        val openingStuds = (s.doorOpenings + s.windowOpenings) * 4

        val rawFramingStuds = fieldStuds + cornerStuds + tJunctionStuds + openingStuds
        val totalStudsWithWaste = ceil(rawFramingStuds * wasteFactor).toInt()

        // Plates: 1 bottom sole plate + 2 top plates = 3 x Wall Run
        val plateTotalLinearFt = wallRunFt * 3.0
        val plateTotalLinearM = plateTotalLinearFt * 0.3048
        val plate10FtBoards = ceil((plateTotalLinearFt / 10.0) * 1.10).toInt() // 10% plate cut waste

        _uiState.value = s.copy(
            totalWallAreaSqFt = wallAreaSqFt,
            totalCeilingAreaSqFt = ceilingAreaSqFt,
            combinedDrywallAreaSqFt = combinedSqFt,
            combinedDrywallAreaSqM = combinedSqM,
            exactSheetsNeeded = exactSheets,
            totalSheetsWithWaste = totalSheets,
            screwsCount = screwsCount,
            screwsLbs = screwsLbs,
            jointCompound4_5GalBuckets = mud4_5GalBuckets,
            jointCompound1GalPails = mud1GalPails,
            jointTapeRolls500Ft = tapeRolls500Ft,
            cornerBead8FtPieces = cornerBeads,
            fieldStudsCount = fieldStuds,
            cornerStudsCount = cornerStuds,
            openingStudsCount = openingStuds,
            totalFramingStudsNeeded = totalStudsWithWaste,
            plateLumberTotalLinearFt = plateTotalLinearFt,
            plateLumberTotalLinearM = plateTotalLinearM,
            plateBoards10FtCount = plate10FtBoards
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val areaText = if (s.isMetric) "${String.format("%.1f", s.combinedDrywallAreaSqM)} m²" else "${String.format("%.1f", s.combinedDrywallAreaSqFt)} sq.ft"
        val summary = "Drywall & Framing ($areaText): ${s.totalSheetsWithWaste} Sheets (${s.sheetPreset.label}), ${s.totalFramingStudsNeeded} Studs (${s.studSpacing.label}), ${s.jointCompound4_5GalBuckets} Mud Buckets (4.5 gal)"

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "CARPENTRY",
                title = "Drywall & Framing Studs",
                summary = summary,
                value = s.totalSheetsWithWaste.toDouble()
            )
        }
    }
}
