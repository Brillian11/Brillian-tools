package com.example.domain.math

import kotlin.math.PI
import kotlin.math.ceil

data class KerfBendingInput(
    val boardThicknessMm: Double,
    val bladeKerfMm: Double,
    val targetInsideRadiusMm: Double,
    val bendAngleDegrees: Double,
    val veneerAllowanceMm: Double = 1.5
)

data class KerfBendingResult(
    val innerArcLengthMm: Double,
    val outerArcLengthMm: Double,
    val totalMaterialToRemoveMm: Double,
    val totalKerfCuts: Int,
    val centerToCenterSpacingMm: Double,
    val bladeCutDepthMm: Double,
    val isFeasible: Boolean,
    val warningMessage: String? = null
)

data class KerfBendSection(
    val sectionIndex: Int,
    val targetRadiusMm: Double,
    val bendAngleDegrees: Double
)

data class MultiSectionKerfInput(
    val boardLengthMm: Double = 1000.0,
    val boardThicknessMm: Double = 19.0,
    val bladeKerfMm: Double = 3.175,
    val veneerAllowanceMm: Double = 1.5,
    val sections: List<KerfBendSection> = listOf(
        KerfBendSection(1, 150.0, 90.0),
        KerfBendSection(2, 150.0, 90.0)
    )
)

data class SectionCalculationResult(
    val sectionIndex: Int,
    val targetRadiusMm: Double,
    val bendAngleDegrees: Double,
    val innerArcLengthMm: Double,
    val outerArcLengthMm: Double,
    val totalKerfCuts: Int,
    val spacingMm: Double,
    val startPositionMm: Double,
    val endPositionMm: Double
)

data class MultiSectionKerfResult(
    val totalBoardLengthMm: Double,
    val totalCutsAcrossBoard: Int,
    val bladeCutDepthMm: Double,
    val sectionResults: List<SectionCalculationResult>,
    val totalKerfZoneLengthMm: Double,
    val remainingFlatLengthMm: Double,
    val isFeasible: Boolean,
    val warningMessage: String? = null
)

object KerfBendingEngine {

    fun calculate(input: KerfBendingInput): KerfBendingResult {
        val t = input.boardThicknessMm
        val k = input.bladeKerfMm
        val r = input.targetInsideRadiusMm
        val theta = input.bendAngleDegrees
        val veneer = input.veneerAllowanceMm

        if (t <= 0 || k <= 0 || r <= 0 || theta <= 0) {
            return KerfBendingResult(
                innerArcLengthMm = 0.0,
                outerArcLengthMm = 0.0,
                totalMaterialToRemoveMm = 0.0,
                totalKerfCuts = 0,
                centerToCenterSpacingMm = 0.0,
                bladeCutDepthMm = 0.0,
                isFeasible = false,
                warningMessage = "All input parameters must be greater than zero."
            )
        }

        val lInner = 2 * PI * r * (theta / 360.0)
        val lOuter = 2 * PI * (r + t) * (theta / 360.0)
        val deltaL = lOuter - lInner // = 2 * PI * t * (theta / 360.0)

        val cutsCount = ceil(deltaL / k).toInt()
        val spacing = if (cutsCount > 0) lOuter / cutsCount else 0.0
        val cutDepth = (t - veneer).coerceAtLeast(0.5)

        val isFeasible = spacing >= k
        val warning = when {
            spacing < k -> "Spacing between cuts (${String.format("%.1f", spacing)} mm) is less than blade kerf ($k mm). Cuts will overlap!"
            r < t * 2 -> "Inside bend radius is very tight relative to board thickness ($t mm). Risk of outer face cracking."
            cutDepth <= 0 -> "Veneer allowance exceeds board thickness!"
            else -> null
        }

        return KerfBendingResult(
            innerArcLengthMm = lInner,
            outerArcLengthMm = lOuter,
            totalMaterialToRemoveMm = deltaL,
            totalKerfCuts = cutsCount,
            centerToCenterSpacingMm = spacing,
            bladeCutDepthMm = cutDepth,
            isFeasible = isFeasible,
            warningMessage = warning
        )
    }

    fun calculateMultiSection(input: MultiSectionKerfInput): MultiSectionKerfResult {
        val boardLen = input.boardLengthMm
        val t = input.boardThicknessMm
        val k = input.bladeKerfMm
        val veneer = input.veneerAllowanceMm
        val cutDepth = (t - veneer).coerceAtLeast(0.5)

        if (boardLen <= 0 || t <= 0 || k <= 0 || input.sections.isEmpty()) {
            return MultiSectionKerfResult(
                totalBoardLengthMm = boardLen,
                totalCutsAcrossBoard = 0,
                bladeCutDepthMm = cutDepth,
                sectionResults = emptyList(),
                totalKerfZoneLengthMm = 0.0,
                remainingFlatLengthMm = boardLen,
                isFeasible = false,
                warningMessage = "Please specify valid board length and at least 1 bend section."
            )
        }

        var currentPos = 50.0 // 50mm lead-in margin from board start
        val sectionCalculations = mutableListOf<SectionCalculationResult>()
        var totalCuts = 0
        var totalKerfSpan = 0.0

        for (sec in input.sections) {
            val r = sec.targetRadiusMm
            val theta = sec.bendAngleDegrees
            val lInner = 2 * PI * r * (theta / 360.0)
            val lOuter = 2 * PI * (r + t) * (theta / 360.0)
            val deltaL = lOuter - lInner
            val cutsCount = ceil(deltaL / k).toInt()
            val spacing = if (cutsCount > 0) lOuter / cutsCount else 0.0
            val kerfZoneLength = lOuter

            val startPos = currentPos
            val endPos = startPos + kerfZoneLength

            sectionCalculations.add(
                SectionCalculationResult(
                    sectionIndex = sec.sectionIndex,
                    targetRadiusMm = r,
                    bendAngleDegrees = theta,
                    innerArcLengthMm = lInner,
                    outerArcLengthMm = lOuter,
                    totalKerfCuts = cutsCount,
                    spacingMm = spacing,
                    startPositionMm = startPos,
                    endPositionMm = endPos
                )
            )

            totalCuts += cutsCount
            totalKerfSpan += kerfZoneLength
            currentPos = endPos + 80.0 // 80mm un-kerfed flat spacer between bend sections
        }

        val remainingFlat = (boardLen - totalKerfSpan).coerceAtLeast(0.0)
        val fitsOnBoard = currentPos <= boardLen + 50.0

        val warning = when {
            !fitsOnBoard -> "Total bend sections (${String.format("%.1f", totalKerfSpan)} mm) exceed total board length (${String.format("%.1f", boardLen)} mm)!"
            remainingFlat <= 0 -> "Board length is too short for these bend sections."
            else -> null
        }

        return MultiSectionKerfResult(
            totalBoardLengthMm = boardLen,
            totalCutsAcrossBoard = totalCuts,
            bladeCutDepthMm = cutDepth,
            sectionResults = sectionCalculations,
            totalKerfZoneLengthMm = totalKerfSpan,
            remainingFlatLengthMm = remainingFlat,
            isFeasible = fitsOnBoard && warning == null,
            warningMessage = warning
        )
    }
}
