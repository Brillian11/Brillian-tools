package com.example.domain.math

import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.sqrt

enum class StudSpacingOption(val labelMetric: String, val labelImperial: String, val inches: Double, val spacingMm: Double) {
    SPACING_40CM("400 mm (40 cm)", "16\" On-Center", 15.748, 400.0),
    SPACING_60CM("600 mm (60 cm)", "24\" On-Center", 23.622, 600.0)
}

data class WallFramingInput(
    val wallLengthMeters: Double = 6.0,
    val wallHeightMeters: Double = 2.44, // 2.44m = 8 ft standard
    val studSpacing: StudSpacingOption = StudSpacingOption.SPACING_40CM,
    val numberOfDoors: Int = 1,
    val numberOfWindows: Int = 2
)

data class WallFramingResult(
    val totalStuds: Int,
    val topPlatesCount: Int,
    val bottomPlatesCount: Int,
    val headerBoardsCount: Int,
    val drywallSheetsMetric12x24Count: Int,
    val totalWallAreaM2: Double
)

data class RoofingInput(
    val runMeters: Double = 4.0, // horizontal run in meters
    val riseMeters: Double = 1.5, // vertical rise in meters
    val overhangMeters: Double = 0.4,
    val roofLengthMeters: Double = 10.0,
    val rafterSpacingMm: Double = 600.0 // 600mm metric standard spacing
)

data class RoofingResult(
    val rafterLengthMeters: Double,
    val pitchAngleDegrees: Double,
    val pitchSlopePercentage: Double, // % slope
    val pitchRatioInches: Double, // e.g. 4.5/12
    val totalRaftersCount: Int,
    val totalRoofAreaM2: Double,
    val plywoodSheathingSheetsMetric: Int
)

object FramingEngine {

    fun calculateWallFraming(input: WallFramingInput): WallFramingResult {
        val spacingMeters = input.studSpacing.spacingMm / 1000.0

        // Base stud count along wall = (Length / Spacing) + 1
        val baseStuds = ceil(input.wallLengthMeters / spacingMeters).toInt() + 1

        // Add studs for corners (2 corners = 4 studs), extra jacks & king studs for openings (4 per window, 4 per door)
        val openingExtraStuds = (input.numberOfDoors * 4) + (input.numberOfWindows * 4)
        val totalStuds = baseStuds + 4 + openingExtraStuds

        // Double top plate + single bottom plate (3 plates along length)
        val standardBoardLenM = 3.66 // 3.66m standard lumber length
        val platesPerLine = ceil(input.wallLengthMeters / standardBoardLenM).toInt()
        val topPlates = platesPerLine * 2
        val bottomPlates = platesPerLine * 1

        // Headers for doors + windows (2 boards per header)
        val headers = (input.numberOfDoors + input.numberOfWindows) * 2

        // Drywall sheets standard metric size 1.2m x 2.4m (2.88 m2 per sheet) for both sides of wall
        val wallAreaM2 = input.wallLengthMeters * input.wallHeightMeters
        val sheetAreaM2 = 1.2 * 2.4 // 2.88 m2
        val drywallSheets = ceil((wallAreaM2 * 2.0) / sheetAreaM2).toInt()

        return WallFramingResult(
            totalStuds = totalStuds,
            topPlatesCount = topPlates,
            bottomPlatesCount = bottomPlates,
            headerBoardsCount = headers,
            drywallSheetsMetric12x24Count = drywallSheets,
            totalWallAreaM2 = wallAreaM2
        )
    }

    fun calculateRoofing(input: RoofingInput): RoofingResult {
        val run = input.runMeters
        val rise = input.riseMeters
        val overhang = input.overhangMeters

        val rafterLen = sqrt((run * run) + (rise * rise)) + overhang
        val angleRad = atan(rise / run)
        val angleDeg = Math.toDegrees(angleRad)
        val slopePercent = (rise / run) * 100.0
        val pitch12 = (rise / run) * 12.0 // e.g. 4.5 in 12

        val spacingM = input.rafterSpacingMm / 1000.0
        val raftersPerSlope = ceil(input.roofLengthMeters / spacingM).toInt() + 1
        val totalRafters = raftersPerSlope * 2 // gable roof 2 slopes

        val areaSlope = rafterLen * input.roofLengthMeters
        val totalArea = areaSlope * 2.0

        val sheetAreaM2 = 1.2 * 2.4 // 2.88 m2 standard metric sheathing sheet
        val sheathingSheets = ceil((totalArea * 1.1) / sheetAreaM2).toInt() // +10% waste

        return RoofingResult(
            rafterLengthMeters = rafterLen,
            pitchAngleDegrees = angleDeg,
            pitchSlopePercentage = slopePercent,
            pitchRatioInches = pitch12,
            totalRaftersCount = totalRafters,
            totalRoofAreaM2 = totalArea,
            plywoodSheathingSheetsMetric = sheathingSheets
        )
    }
}
