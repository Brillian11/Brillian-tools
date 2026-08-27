package com.example.domain.math

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.pow

enum class ConcreteStructureType { RECTANGULAR_SLAB, FOOTING_WALL, CYLINDRICAL_PIER }

enum class CementBagOption(val label: String, val weightKg: Double) {
    BAG_40KG("40 kg (Indonesian Standard / Semen Gresik / Tiga Roda)", 40.0),
    BAG_50KG("50 kg (Asian / European Standard)", 50.0),
    BAG_25KG("25 kg (Lightweight Bag)", 25.0),
    BAG_80LB("80 lb / 36.3 kg (US Standard)", 36.2874)
}

enum class BrickType(
    val label: String,
    val lengthMm: Double,
    val widthMm: Double,
    val heightMm: Double,
    val category: String
) {
    INDONESIAN_RED_BRICK("Bata Merah (Indonesian Red Brick - 20x10x5 cm)", 200.0, 100.0, 50.0, "Red Brick"),
    BATAKO_HOLLOW("Batako (Hollow Concrete Block - 40x20x10 cm)", 400.0, 100.0, 200.0, "Concrete Block"),
    HEBEL_AAC_75("Hebel AAC (Autoclaved Aerated Concrete - 60x20x7.5 cm)", 600.0, 75.0, 200.0, "AAC Block"),
    HEBEL_AAC_100("Hebel AAC (Autoclaved Aerated Concrete - 60x20x10 cm)", 600.0, 100.0, 200.0, "AAC Block"),
    US_RED_BRICK("Standard Red Brick (8\" x 3.6\" x 2.25\")", 203.0, 92.0, 57.0, "Red Brick"),
    CUSTOM_BRICK("Custom Brick Dimensions", 200.0, 100.0, 50.0, "Custom")
}

data class ConcreteInput(
    val structureType: ConcreteStructureType,
    val lengthMeters: Double = 3.0,
    val widthMeters: Double = 3.0,
    val thicknessMeters: Double = 0.1, // 10cm
    val radiusMeters: Double = 0.2, // for pier
    val quantity: Int = 1,
    val compactionWastagePercent: Double = 10.0, // +10% default
    val selectedBagOption: CementBagOption = CementBagOption.BAG_40KG
)

data class ConcreteResult(
    val netVolumeCubicMeters: Double,
    val grossVolumeCubicMeters: Double, // with compaction loss factor
    val grossVolumeCubicYards: Double,
    val totalCementBagsCount: Int,
    val cementWeightKg: Double, // 1:2:3 mix ratio approximation
    val sandWeightKg: Double,
    val gravelWeightKg: Double
)

data class BrickWallInput(
    val wallLengthMeters: Double = 5.0,
    val wallHeightMeters: Double = 3.0,
    val brickType: BrickType = BrickType.INDONESIAN_RED_BRICK,
    val customLengthMm: Double = 200.0,
    val customWidthMm: Double = 100.0,
    val customHeightMm: Double = 50.0,
    val mortarJointMm: Double = 10.0, // 10mm mortar joint
    val wastagePercent: Double = 5.0, // +5% brick waste
    val isDoubleWythe: Boolean = false, // single wythe (half brick) vs double wythe
    val selectedBagOption: CementBagOption = CementBagOption.BAG_40KG
)

data class BrickWallResult(
    val wallAreaM2: Double,
    val totalBricksNeeded: Int,
    val mortarVolumeM3: Double,
    val cementWeightKg: Double,
    val cementBagsCount: Int,
    val sandVolumeM3: Double,
    val sandWeightKg: Double
)

object ConcreteEngine {

    fun calculate(input: ConcreteInput): ConcreteResult {
        val qty = input.quantity.coerceAtLeast(1)
        val netVolOne = when (input.structureType) {
            ConcreteStructureType.RECTANGULAR_SLAB, ConcreteStructureType.FOOTING_WALL -> {
                input.lengthMeters * input.widthMeters * input.thicknessMeters
            }
            ConcreteStructureType.CYLINDRICAL_PIER -> {
                PI * input.radiusMeters.pow(2) * input.thicknessMeters
            }
        }

        val netVolTotal = netVolOne * qty
        val wastageFactor = 1.0 + (input.compactionWastagePercent / 100.0)
        val grossVolM3 = netVolTotal * wastageFactor
        val grossVolYards3 = grossVolM3 * 1.30795

        // Standard 1:2:3 mix ratio bulk density ~ 2400 kg/m^3
        // Cement = 400 kg/m^3
        // Sand = 800 kg/m^3
        // Gravel = 1200 kg/m^3
        val cementKg = grossVolM3 * 400.0
        val sandKg = grossVolM3 * 800.0
        val gravelKg = grossVolM3 * 1200.0

        val bagsCount = ceil(cementKg / input.selectedBagOption.weightKg).toInt()

        return ConcreteResult(
            netVolumeCubicMeters = netVolTotal,
            grossVolumeCubicMeters = grossVolM3,
            grossVolumeCubicYards = grossVolYards3,
            totalCementBagsCount = bagsCount,
            cementWeightKg = cementKg,
            sandWeightKg = sandKg,
            gravelWeightKg = gravelKg
        )
    }

    fun calculateBrickWall(input: BrickWallInput): BrickWallResult {
        val areaM2 = input.wallLengthMeters * input.wallHeightMeters

        val bLenM = if (input.brickType == BrickType.CUSTOM_BRICK) input.customLengthMm / 1000.0 else input.brickType.lengthMm / 1000.0
        val bWidthM = if (input.brickType == BrickType.CUSTOM_BRICK) input.customWidthMm / 1000.0 else input.brickType.widthMm / 1000.0
        val bHeightM = if (input.brickType == BrickType.CUSTOM_BRICK) input.customHeightMm / 1000.0 else input.brickType.heightMm / 1000.0

        val mortarM = input.mortarJointMm / 1000.0

        // Effective brick face area including mortar joint
        val effLengthM = bLenM + mortarM
        val effHeightM = bHeightM + mortarM
        val effFaceAreaM2 = effLengthM * effHeightM

        val rawBricksPerM2 = 1.0 / effFaceAreaM2
        var netBricks = rawBricksPerM2 * areaM2
        if (input.isDoubleWythe) netBricks *= 2.0

        val grossBricks = ceil(netBricks * (1.0 + input.wastagePercent / 100.0)).toInt()

        // Mortar volume calculation:
        // Total wall volume minus total volume occupied by raw bricks
        val wytheThicknessM = if (input.isDoubleWythe) (bWidthM * 2.0 + mortarM) else bWidthM
        val wallVolumeM3 = areaM2 * wytheThicknessM
        val singleBrickVolM3 = bLenM * bWidthM * bHeightM
        val totalBrickVolM3 = netBricks * singleBrickVolM3
        val mortarVolM3 = (wallVolumeM3 - totalBrickVolM3).coerceAtLeast(wallVolumeM3 * 0.15)

        // 1:4 Cement:Sand mortar mix proportion:
        // 1 m3 mortar requires ~ 320 kg cement and 1.15 m3 sand (~1600 kg)
        val cementKg = mortarVolM3 * 320.0
        val sandM3 = mortarVolM3 * 1.15
        val sandKg = sandM3 * 1400.0 // loose sand density ~ 1400 kg/m3

        val bagsCount = ceil(cementKg / input.selectedBagOption.weightKg).toInt()

        return BrickWallResult(
            wallAreaM2 = areaM2,
            totalBricksNeeded = grossBricks,
            mortarVolumeM3 = mortarVolM3,
            cementWeightKg = cementKg,
            cementBagsCount = bagsCount,
            sandVolumeM3 = sandM3,
            sandWeightKg = sandKg
        )
    }
}
