package com.example

import com.example.domain.math.KerfBendSection
import com.example.domain.math.KerfBendingEngine
import com.example.domain.math.KerfBendingInput
import com.example.domain.math.MultiSectionKerfInput
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun kerfBending_calculatesInsideAndOutsideCurveLengths() {
        val input = KerfBendingInput(
            boardThicknessMm = 19.0,
            bladeKerfMm = 3.175,
            targetInsideRadiusMm = 295.0,
            bendAngleDegrees = 180.0,
            veneerAllowanceMm = 1.5
        )
        val result = KerfBendingEngine.calculate(input)

        val expectedInside = 2 * PI * 295.0 * (180.0 / 360.0)
        val expectedOutside = 2 * PI * (295.0 + 19.0) * (180.0 / 360.0)

        assertEquals(expectedInside, result.innerArcLengthMm, 0.01)
        assertEquals(expectedOutside, result.outerArcLengthMm, 0.01)
        assertEquals(expectedOutside - expectedInside, result.totalMaterialToRemoveMm, 0.01)
    }

    @Test
    fun kerfBendingMultiSection_totalsInsideAndOutsideCurveLengths() {
        val input = MultiSectionKerfInput(
            boardLengthMm = 1000.0,
            boardThicknessMm = 19.0,
            bladeKerfMm = 3.175,
            veneerAllowanceMm = 1.5,
            sections = listOf(
                KerfBendSection(1, 150.0, 90.0),
                KerfBendSection(2, 150.0, 90.0)
            )
        )
        val result = KerfBendingEngine.calculateMultiSection(input)

        val singleInside = 2 * PI * 150.0 * (90.0 / 360.0)
        val singleOutside = 2 * PI * (150.0 + 19.0) * (90.0 / 360.0)

        assertEquals(singleInside * 2, result.totalInnerCurveLengthMm, 0.01)
        assertEquals(singleOutside * 2, result.totalOuterCurveLengthMm, 0.01)
    }
}
