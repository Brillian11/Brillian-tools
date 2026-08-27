package com.example.domain.math

import kotlin.math.PI
import kotlin.math.pow

enum class LoadType { UNIFORM_DISTRIBUTED, POINT_LOAD_CENTER }
enum class BeamShape { RECTANGULAR, CIRCULAR }

data class MaterialPreset(
    val name: String,
    val elasticityModulusGpa: Double, // E in GPa
    val densityKgM3: Double,
    val category: String = "Wood"
) {
    companion object {
        val PRESETS = listOf(
            // Asian Timbers & Woods
            MaterialPreset("Kayu Jati / Teak Wood (Asian)", 12.5, 670.0, "Asian Timber"),
            MaterialPreset("Kayu Bangkirai / Yellow Balau", 18.0, 910.0, "Asian Timber"),
            MaterialPreset("Kayu Kamper / Camphorwood", 14.0, 800.0, "Asian Timber"),
            MaterialPreset("Kayu Meranti Red (SE Asian)", 11.5, 580.0, "Asian Timber"),
            MaterialPreset("Kayu Ulin / Ironwood (Indonesian)", 20.0, 1040.0, "Asian Timber"),
            MaterialPreset("Bamboo (Structural Dendrocalamus)", 18.5, 750.0, "Asian Timber"),
            MaterialPreset("Japanese Cedar (Sugi)", 8.0, 400.0, "Asian Timber"),
            MaterialPreset("Hinoki (Japanese Cypress)", 10.0, 440.0, "Asian Timber"),
            MaterialPreset("Chinese Fir (Shanmu)", 9.0, 420.0, "Asian Timber"),
            
            // Asian Structural Steel & Concrete
            MaterialPreset("SS400 Mild Steel (JIS G3101 Asian)", 205.0, 7850.0, "Asian Metals"),
            MaterialPreset("SM490 High-Strength Steel (JIS)", 206.0, 7850.0, "Asian Metals"),
            MaterialPreset("Reinforced Concrete K-300 (fc' 25 MPa)", 23.5, 2400.0, "Asian Concrete"),
            MaterialPreset("Reinforced Concrete K-350 (fc' 30 MPa)", 25.7, 2400.0, "Asian Concrete"),
            
            // International Standards
            MaterialPreset("Douglas Fir (Western Lumber)", 13.1, 530.0, "International"),
            MaterialPreset("Southern Yellow Pine", 12.0, 570.0, "International"),
            MaterialPreset("White Oak (Hardwood)", 12.3, 750.0, "International"),
            MaterialPreset("Structural Steel (A36 Standard)", 200.0, 7850.0, "International"),
            MaterialPreset("Aluminum 6061-T6", 68.9, 2700.0, "International")
        )
    }
}

data class BeamInput(
    val spanLengthMeters: Double,
    val loadType: LoadType,
    val uniformLoadKnM: Double = 0.0, // w in kN/m
    val pointLoadKn: Double = 0.0, // P in kN
    val shape: BeamShape,
    val widthMm: Double = 50.0, // b
    val heightMm: Double = 150.0, // h
    val radiusMm: Double = 50.0, // r
    val elasticityModulusGpa: Double = 12.0, // E
    val allowableDeflectionRatio: Double = 360.0 // e.g. L/360
)

data class BeamResult(
    val maxShearForceKn: Double,
    val maxBendingMomentKnm: Double,
    val momentOfInertiaMm4: Double,
    val maxDeflectionMm: Double,
    val allowableDeflectionMm: Double,
    val isDeflectionSafe: Boolean,
    val deflectionRatioActual: Double
)

object BeamDeflectionEngine {

    fun calculate(input: BeamInput): BeamResult {
        val L = input.spanLengthMeters // m
        val E_pa = input.elasticityModulusGpa * 1e9 // Pa = N/m^2

        // Calculate Moment of Inertia (I) in m^4 and mm^4
        val I_mm4 = when (input.shape) {
            BeamShape.RECTANGULAR -> (input.widthMm * input.heightMm.pow(3)) / 12.0
            BeamShape.CIRCULAR -> (PI * input.radiusMm.pow(4)) / 4.0
        }
        val I_m4 = I_mm4 * 1e-12

        var maxV = 0.0 // kN
        var maxM = 0.0 // kN*m
        var maxDeltaMeters = 0.0

        if (L > 0 && I_m4 > 0) {
            when (input.loadType) {
                LoadType.UNIFORM_DISTRIBUTED -> {
                    val w_N_m = input.uniformLoadKnM * 1000.0 // N/m
                    maxV = (w_N_m * L) / 2.0 / 1000.0 // kN
                    maxM = (w_N_m * L.pow(2)) / 8.0 / 1000.0 // kN*m
                    maxDeltaMeters = (5.0 * w_N_m * L.pow(4)) / (384.0 * E_pa * I_m4)
                }
                LoadType.POINT_LOAD_CENTER -> {
                    val P_N = input.pointLoadKn * 1000.0 // N
                    maxV = P_N / 2.0 / 1000.0 // kN
                    maxM = (P_N * L) / 4.0 / 1000.0 // kN*m
                    maxDeltaMeters = (P_N * L.pow(3)) / (48.0 * E_pa * I_m4)
                }
            }
        }

        val maxDeltaMm = maxDeltaMeters * 1000.0
        val allowableDeltaMm = (L * 1000.0) / input.allowableDeflectionRatio
        val actualRatio = if (maxDeltaMm > 0) (L * 1000.0) / maxDeltaMm else 9999.0

        val isSafe = maxDeltaMm <= allowableDeltaMm

        return BeamResult(
            maxShearForceKn = maxV,
            maxBendingMomentKnm = maxM,
            momentOfInertiaMm4 = I_mm4,
            maxDeflectionMm = maxDeltaMm,
            allowableDeflectionMm = allowableDeltaMm,
            isDeflectionSafe = isSafe,
            deflectionRatioActual = actualRatio
        )
    }
}
