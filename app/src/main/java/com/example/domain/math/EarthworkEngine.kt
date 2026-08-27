package com.example.domain.math

import kotlin.math.atan
import kotlin.math.tan

data class CutFillInput(
    val startCrossSectionAreaM2: Double, // A1
    val endCrossSectionAreaM2: Double, // A2
    val segmentLengthMeters: Double // L
)

data class CutFillResult(
    val volumeCubicMeters: Double,
    val volumeCubicYards: Double
)

data class GradeConversionResult(
    val percentGrade: Double,
    val angleDegrees: Double,
    val ratioX: Double // 1:X
)

object EarthworkEngine {

    fun calculateCutFillVolume(input: CutFillInput): CutFillResult {
        val a1 = input.startCrossSectionAreaM2.coerceAtLeast(0.0)
        val a2 = input.endCrossSectionAreaM2.coerceAtLeast(0.0)
        val l = input.segmentLengthMeters.coerceAtLeast(0.0)

        val volM3 = ((a1 + a2) / 2.0) * l
        val volYards3 = volM3 * 1.30795

        return CutFillResult(
            volumeCubicMeters = volM3,
            volumeCubicYards = volYards3
        )
    }

    fun fromPercentGrade(percent: Double): GradeConversionResult {
        val p = percent.coerceAtLeast(0.0001)
        val angleRad = atan(p / 100.0)
        val angleDeg = Math.toDegrees(angleRad)
        val x = 100.0 / p
        return GradeConversionResult(percentGrade = percent, angleDegrees = angleDeg, ratioX = x)
    }

    fun fromAngleDegrees(angleDeg: Double): GradeConversionResult {
        val deg = angleDeg.coerceIn(0.0001, 89.99)
        val angleRad = Math.toRadians(deg)
        val percent = tan(angleRad) * 100.0
        val x = 1.0 / tan(angleRad)
        return GradeConversionResult(percentGrade = percent, angleDegrees = deg, ratioX = x)
    }

    fun fromRatio(ratioX: Double): GradeConversionResult {
        val x = ratioX.coerceAtLeast(0.0001)
        val percent = 100.0 / x
        return fromPercentGrade(percent)
    }
}
