package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class RetainingWallType(val label: String, val blockDensityPcf: Double) {
    SEGMENTAL_BLOCK("Segmental Modular Block (SRW)", 135.0),
    CONCRETE_CANTILEVER("Cast-in-Place Concrete Cantilever", 150.0),
    GRAVITY_BOULDER("Gravity Stone / Rockery", 140.0),
    TIMBER_CRIB("Timber / Treated Post Wall", 50.0)
}

enum class SoilBackfillType(val frictionAngleDeg: Double, val unitWeightPcf: Double, val label: String) {
    CLEAN_GRAVEL_SAND(36.0, 125.0, "Well-Graded Gravel & Sand (GW/SW)"),
    CRUSHED_STONE(38.0, 130.0, "Crushed Stone / Angular Aggregate"),
    MEDIUM_DENSE_SAND(32.0, 120.0, "Medium Dense Sand / Silty Sand"),
    CLAYEY_SILT(28.0, 115.0, "Clayey Silt / Common Earth (CL/ML)"),
    STIFF_CLAY(24.0, 110.0, "Stiff Lean Clay (CH)")
}

data class RetainingWallUiState(
    val isMetric: Boolean = false,
    val wallType: RetainingWallType = RetainingWallType.SEGMENTAL_BLOCK,
    val backfillType: SoilBackfillType = SoilBackfillType.CLEAN_GRAVEL_SAND,

    // Dimensions
    val wallHeight: Double = 8.0, // ft or m (Exposed height)
    val wallLength: Double = 50.0, // ft or m (Total wall running length)
    val embedmentDepth: Double = 1.0, // ft or m (Buried base course)
    val baseWidth: Double = 4.5, // ft or m (Base footing or block depth)
    val wallBatterDeg: Double = 4.0, // Wall setback / lean angle (deg)

    // Soil & Loading
    val soilFrictionAngleDeg: Double = 34.0, // phi (deg)
    val soilUnitWeight: Double = 120.0, // gamma (pcf or kN/m3)
    val backfillSlopeDeg: Double = 10.0, // beta (deg, e.g. 3:1 slope = 18.4°)
    val surchargeLoad: Double = 100.0, // q (psf or kPa, e.g. 250 psf for traffic)

    // Geogrid Reinforcement
    val blockUnitHeightInches: Double = 8.0, // 8 in typical SRW block
    val geogridTierSpacingInches: Double = 16.0, // Every 2 courses (16 in)
    val minGeogridLengthRatio: Double = 0.70, // 70% of total height

    // Calculated Earth Pressures & Results
    val kaRankine: Double = 0.0,
    val kpRankine: Double = 0.0,
    val totalActiveThrustPerFt: Double = 0.0, // lbs/ft or kN/m
    val soilThrustComponent: Double = 0.0,
    val surchargeThrustComponent: Double = 0.0,
    val thrustLineOfActionHeight: Double = 0.0, // ft above base

    // Stability Factors of Safety
    val totalWallWeightPerFt: Double = 0.0,
    val resistingMomentPerFt: Double = 0.0, // ft-lbs/ft
    val overturningMomentPerFt: Double = 0.0, // ft-lbs/ft
    val fsOverturning: Double = 0.0,
    val fsSliding: Double = 0.0,
    val baseEccentricity: Double = 0.0,
    val maxBearingPressure: Double = 0.0, // psf or kPa

    // Geogrid & Materials Quantities
    val numGeogridTiers: Int = 0,
    val geogridLengthPerTier: Double = 0.0, // ft or m
    val totalGeogridAreaSqYd: Double = 0.0, // sq yards or m2
    val drainageStoneVolumeCuYd: Double = 0.0, // 12 in stone chimney
    val drainageStoneTons: Double = 0.0,
    val wallFaceAreaSqFt: Double = 0.0,
    val totalBlockCount: Int = 0
)

class RetainingWallSizerViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RetainingWallUiState())
    val uiState: StateFlow<RetainingWallUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setMetric(metric: Boolean) {
        _uiState.value = _uiState.value.copy(isMetric = metric)
        recalculate()
    }

    fun setWallType(wallType: RetainingWallType) {
        _uiState.value = _uiState.value.copy(wallType = wallType)
        recalculate()
    }

    fun setBackfillType(backfill: SoilBackfillType) {
        _uiState.value = _uiState.value.copy(
            backfillType = backfill,
            soilFrictionAngleDeg = backfill.frictionAngleDeg,
            soilUnitWeight = if (_uiState.value.isMetric) (backfill.unitWeightPcf * 0.1571) else backfill.unitWeightPcf
        )
        recalculate()
    }

    fun updateInputs(
        wallHeight: Double? = null,
        wallLength: Double? = null,
        embedmentDepth: Double? = null,
        baseWidth: Double? = null,
        wallBatterDeg: Double? = null,
        soilFrictionAngleDeg: Double? = null,
        soilUnitWeight: Double? = null,
        backfillSlopeDeg: Double? = null,
        surchargeLoad: Double? = null,
        geogridTierSpacingInches: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            wallHeight = wallHeight ?: _uiState.value.wallHeight,
            wallLength = wallLength ?: _uiState.value.wallLength,
            embedmentDepth = embedmentDepth ?: _uiState.value.embedmentDepth,
            baseWidth = baseWidth ?: _uiState.value.baseWidth,
            wallBatterDeg = wallBatterDeg ?: _uiState.value.wallBatterDeg,
            soilFrictionAngleDeg = soilFrictionAngleDeg ?: _uiState.value.soilFrictionAngleDeg,
            soilUnitWeight = soilUnitWeight ?: _uiState.value.soilUnitWeight,
            backfillSlopeDeg = backfillSlopeDeg ?: _uiState.value.backfillSlopeDeg,
            surchargeLoad = surchargeLoad ?: _uiState.value.surchargeLoad,
            geogridTierSpacingInches = geogridTierSpacingInches ?: _uiState.value.geogridTierSpacingInches
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val totalH = s.wallHeight + s.embedmentDepth // Total structural height

        val phiRad = Math.toRadians(s.soilFrictionAngleDeg)
        val betaRad = Math.toRadians(s.backfillSlopeDeg.coerceAtMost(s.soilFrictionAngleDeg - 1.0))

        // Rankine Ka formula with backfill slope beta
        val cosBeta = cos(betaRad)
        val termInsideSqrt = (cosBeta.pow(2) - cos(phiRad).pow(2)).coerceAtLeast(0.0)
        val sqrtTerm = sqrt(termInsideSqrt)

        val ka = if (betaRad == 0.0) {
            tan(PI / 4.0 - phiRad / 2.0).pow(2)
        } else {
            cosBeta * ((cosBeta - sqrtTerm) / (cosBeta + sqrtTerm + 1e-6))
        }

        // Rankine Passive Kp
        val kp = tan(PI / 4.0 + phiRad / 2.0).pow(2)

        // Soil Active Lateral Thrust: Pa_soil = 0.5 * Ka * gamma * H^2
        val paSoil = 0.5 * ka * s.soilUnitWeight * totalH.pow(2)
        // Surcharge Thrust: Pa_surcharge = Ka * q * H
        val paSurcharge = ka * s.surchargeLoad * totalH
        val totalPa = paSoil + paSurcharge

        // Line of action height above base:
        // y_bar = [Pa_soil * (H / 3) + Pa_surcharge * (H / 2)] / Total_Pa
        val yBar = if (totalPa > 0) {
            (paSoil * (totalH / 3.0) + paSurcharge * (totalH / 2.0)) / totalPa
        } else {
            totalH / 3.0
        }

        // Overturning Moment about Toe
        val mot = totalPa * cos(betaRad) * yBar

        // Resisting Weight of Wall
        val wallDensity = if (s.isMetric) (s.wallType.blockDensityPcf * 0.1571) else s.wallType.blockDensityPcf
        val wallArea = totalH * s.baseWidth
        val wallWeight = wallArea * wallDensity
        val mr = wallWeight * (s.baseWidth / 2.0) + totalPa * sin(betaRad) * s.baseWidth

        val fsOt = if (mot > 0) (mr / mot) else 99.0

        // Sliding Factor of Safety (tan(delta) where base friction delta ≈ 2/3 * phi)
        val baseFriction = tan(2.0 / 3.0 * phiRad)
        val totalVerticalN = wallWeight + totalPa * sin(betaRad)
        val slidingResistance = totalVerticalN * baseFriction
        val horizontalSlidingForce = totalPa * cos(betaRad)
        val fsSlid = if (horizontalSlidingForce > 0) (slidingResistance / horizontalSlidingForce) else 99.0

        // Base Eccentricity & Bearing Pressure
        val xR = if (totalVerticalN > 0) (mr - mot) / totalVerticalN else s.baseWidth / 2.0
        val ecc = (s.baseWidth / 2.0) - xR
        val maxBearing = if (s.baseWidth > 0) {
            (totalVerticalN / s.baseWidth) * (1.0 + (6.0 * ecc / s.baseWidth).coerceAtLeast(0.0))
        } else 0.0

        // Geogrid Calculations
        val spacingFt = (s.geogridTierSpacingInches / 12.0)
        val numTiers = max(0, ceil((totalH - 1.5) / spacingFt).toInt())
        val gridLength = max(4.0, totalH * s.minGeogridLengthRatio) // minimum 4 ft or 0.7H
        val totalGridAreaSqFt = numTiers * gridLength * s.wallLength
        val totalGridSqYd = if (s.isMetric) (totalGridAreaSqFt * 0.0929) else (totalGridAreaSqFt / 9.0)

        // Drainage Stone Chimney (1.0 ft thick behind wall)
        val drainStoneVolCuFt = totalH * 1.0 * s.wallLength
        val drainStoneCuYd = drainStoneVolCuFt / 27.0
        val drainStoneTons = drainStoneCuYd * 1.4 // ~1.4 tons per cu yd for #57 stone

        // Wall Face Area & Blocks
        val faceAreaSqFt = s.wallHeight * s.wallLength
        // Typical SRW block is 8" high x 18" wide (1.0 sq ft per block)
        val blockCount = ceil(faceAreaSqFt * 1.10).toInt() // +10% waste/caps

        _uiState.value = s.copy(
            kaRankine = ka,
            kpRankine = kp,
            totalActiveThrustPerFt = totalPa,
            soilThrustComponent = paSoil,
            surchargeThrustComponent = paSurcharge,
            thrustLineOfActionHeight = yBar,
            totalWallWeightPerFt = wallWeight,
            resistingMomentPerFt = mr,
            overturningMomentPerFt = mot,
            fsOverturning = fsOt,
            fsSliding = fsSlid,
            baseEccentricity = ecc,
            maxBearingPressure = maxBearing,
            numGeogridTiers = numTiers,
            geogridLengthPerTier = gridLength,
            totalGeogridAreaSqYd = totalGridSqYd,
            drainageStoneVolumeCuYd = drainStoneCuYd,
            drainageStoneTons = drainStoneTons,
            wallFaceAreaSqFt = faceAreaSqFt,
            totalBlockCount = blockCount
        )
    }

    fun saveToLog() {
        val s = _uiState.value
        toolLogRepository?.let { repo ->
            viewModelScope.launch {
                val summary = "Retaining Wall H=${s.wallHeight}ft L=${s.wallLength}ft Ka=${String.format("%.3f", s.kaRankine)}, FS(OT)=${String.format("%.2f", s.fsOverturning)}, FS(Slide)=${String.format("%.2f", s.fsSliding)}, Geogrid: ${s.numGeogridTiers} tiers @ ${String.format("%.1f", s.geogridLengthPerTier)}ft"
                repo.logToolActivity(
                    toolType = "retaining_wall_sizer",
                    title = "Retaining Wall Sizer",
                    summary = summary,
                    value = s.fsOverturning
                )
            }
        }
    }
}
