package com.example.domain.math

import kotlin.math.ceil

data class DadoStepOverInput(
    val jointWidthMm: Double,
    val bladeKerfMm: Double,
    val stepOverOverlapMm: Double = 0.5,
    val initialFenceOffsetMm: Double = 0.0
)

data class PassStep(
    val passNumber: Int,
    val fencePositionMm: Double,
    val cutWidthMm: Double
)

data class DadoStepOverResult(
    val totalPasses: Int,
    val passSteps: List<PassStep>,
    val effectiveStepOverMm: Double,
    val totalJointWidthMm: Double
)

object DadoStepOverEngine {

    fun calculate(input: DadoStepOverInput): DadoStepOverResult {
        val width = input.jointWidthMm
        val kerf = input.bladeKerfMm
        val overlap = input.stepOverOverlapMm
        val startOffset = input.initialFenceOffsetMm

        if (width <= 0 || kerf <= 0) {
            return DadoStepOverResult(0, emptyList(), 0.0, 0.0)
        }

        if (width <= kerf) {
            val pass = PassStep(1, startOffset, width)
            return DadoStepOverResult(1, listOf(pass), kerf, width)
        }

        val advancePerPass = (kerf - overlap).coerceAtLeast(0.1)
        val remainingWidth = width - kerf
        val additionalPasses = ceil(remainingWidth / advancePerPass).toInt()
        val totalPasses = 1 + additionalPasses

        val actualStepOver = remainingWidth / additionalPasses

        val passes = mutableListOf<PassStep>()
        // First pass at shoulder 1
        passes.add(PassStep(1, startOffset, kerf))

        // Intermediate passes
        for (i in 1 until additionalPasses) {
            val fencePos = startOffset + (i * actualStepOver)
            passes.add(PassStep(i + 1, fencePos, kerf))
        }

        // Final pass at far shoulder
        val finalFencePos = startOffset + remainingWidth
        passes.add(PassStep(totalPasses, finalFencePos, kerf))

        return DadoStepOverResult(
            totalPasses = totalPasses,
            passSteps = passes,
            effectiveStepOverMm = actualStepOver,
            totalJointWidthMm = width
        )
    }
}
