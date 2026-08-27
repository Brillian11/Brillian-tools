package com.example.ui.screens.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt

enum class PipeMaterial(
    val cFactor: Double,
    val label: String,
    val roughnessDesc: String
) {
    COPPER_TYPE_L(150.0, "Copper (Type L - Standard)", "Smooth (C=150)"),
    COPPER_TYPE_K(150.0, "Copper (Type K - Thick Wall)", "Smooth (C=150)"),
    COPPER_TYPE_M(150.0, "Copper (Type M - Thin Wall)", "Smooth (C=150)"),
    PEX_CTS(150.0, "PEX (Cross-Linked Polyethylene)", "Ultra-smooth plastic (C=150)"),
    PVC_SCH40(150.0, "PVC (Schedule 40)", "Smooth rigid plastic (C=150)"),
    PVC_SCH80(150.0, "PVC (Schedule 80)", "Heavy wall plastic (C=150)"),
    CPVC(150.0, "CPVC (Chlorinated Polyvinyl)", "Smooth hot/cold plastic (C=150)"),
    GALVANIZED_STEEL(120.0, "Galvanized Steel", "Moderate roughness (C=120)")
}

enum class NominalPipeSize(
    val nominalStr: String,
    // Approximate Inner Diameter in inches for each material class
    val idCopperL: Double,
    val idPex: Double,
    val idPvcSch40: Double,
    val idPvcSch80: Double,
    val idSteel: Double
) {
    SIZE_1_2("1/2\"", 0.545, 0.475, 0.622, 0.546, 0.622),
    SIZE_3_4("3/4\"", 0.785, 0.671, 0.824, 0.742, 0.824),
    SIZE_1("1\"", 1.025, 0.862, 1.049, 0.957, 1.049),
    SIZE_1_1_4("1-1/4\"", 1.265, 1.054, 1.380, 1.278, 1.380),
    SIZE_1_1_2("1-1/2\"", 1.505, 1.244, 1.610, 1.500, 1.610),
    SIZE_2("2\"", 1.985, 1.629, 2.067, 1.939, 2.067),
    SIZE_2_1_2("2-1/2\"", 2.465, 2.015, 2.469, 2.323, 2.469),
    SIZE_3("3\"", 2.945, 2.408, 3.068, 2.900, 3.068),
    SIZE_4("4\"", 3.935, 3.190, 4.026, 3.826, 4.026);

    fun getInnerDiameterInches(material: PipeMaterial): Double {
        return when (material) {
            PipeMaterial.COPPER_TYPE_L, PipeMaterial.COPPER_TYPE_M -> idCopperL
            PipeMaterial.COPPER_TYPE_K -> idCopperL * 0.98
            PipeMaterial.PEX_CTS -> idPex
            PipeMaterial.PVC_SCH40, PipeMaterial.CPVC -> idPvcSch40
            PipeMaterial.PVC_SCH80 -> idPvcSch80
            PipeMaterial.GALVANIZED_STEEL -> idSteel
        }
    }
}

enum class VelocityStatus(val label: String, val isWarning: Boolean) {
    SLUGGISH("Low Velocity (< 2.0 ft/s) - Risk of particulate settling", false),
    OPTIMAL_DOMESTIC("Optimal Velocity (2.0 - 5.0 ft/s) - Quiet & Efficient", false),
    ACCEPTABLE_COMMERCIAL("Acceptable Commercial (5.0 - 8.0 ft/s) - Moderate Noise", false),
    EXCESSIVE_EROSION("High Velocity (> 8.0 ft/s) - Warning: Pipe Erosion & Water Hammer!", true)
}

data class PipeSizingUiState(
    val isMetric: Boolean = false,
    val material: PipeMaterial = PipeMaterial.COPPER_TYPE_L,
    val nominalSize: NominalPipeSize = NominalPipeSize.SIZE_3_4,
    
    // Inputs
    val flowRateGpmOrLpm: Double = 8.0, // GPM (or L/min in metric)
    val pipeLengthFtOrM: Double = 50.0, // Length (ft or m)
    val staticSupplyPressurePsiOrBar: Double = 60.0, // Supply pressure (psi or bar)
    
    // Fittings count for equivalent length
    val elbow90Count: Int = 4,
    val elbow45Count: Int = 2,
    val teeFlowThroughCount: Int = 2,
    val teeBranchCount: Int = 1,
    val ballValveCount: Int = 2,
    val checkValveCount: Int = 0,

    // Calculated values
    val innerDiameterInches: Double = 0.785,
    val innerDiameterMm: Double = 19.94,
    val actualFlowGpm: Double = 8.0,
    val actualFlowLpm: Double = 30.28,
    val velocityFps: Double = 5.29,
    val velocityMps: Double = 1.61,
    val velocityStatus: VelocityStatus = VelocityStatus.ACCEPTABLE_COMMERCIAL,
    
    // Friction & Pressure Drop
    val equivalentFittingsLengthFt: Double = 18.5,
    val totalEquivalentLengthFt: Double = 68.5,
    val totalEquivalentLengthM: Double = 20.88,
    val headLossPer100Ft: Double = 6.45, // ft head per 100 ft
    val totalHeadLossFt: Double = 4.42, // ft head
    val pressureDropPsi: Double = 1.92, // psi
    val pressureDropBar: Double = 0.132, // bar
    val pressureDropKpa: Double = 13.2, // kPa
    val residualPressurePsi: Double = 58.08,
    val residualPressureBar: Double = 4.00,
    
    // Sizing recommendation
    val recommendedNominalSize: String = "3/4\"",
    val maxRecommendedGpm: Double = 11.0
)

class PipeSizingViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PipeSizingUiState())
    val uiState: StateFlow<PipeSizingUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                flowRateGpmOrLpm = if (metric) 30.0 else 8.0, // 30 L/min vs 8 GPM
                pipeLengthFtOrM = if (metric) 15.0 else 50.0, // 15 m vs 50 ft
                staticSupplyPressurePsiOrBar = if (metric) 4.0 else 60.0 // 4 bar vs 60 psi
            )
            recalculate()
        }
    }

    fun setMaterial(mat: PipeMaterial) {
        _uiState.value = _uiState.value.copy(material = mat)
        recalculate()
    }

    fun setNominalSize(size: NominalPipeSize) {
        _uiState.value = _uiState.value.copy(nominalSize = size)
        recalculate()
    }

    fun updateInputs(
        flowRate: Double? = null,
        length: Double? = null,
        supplyPressure: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            flowRateGpmOrLpm = flowRate ?: _uiState.value.flowRateGpmOrLpm,
            pipeLengthFtOrM = length ?: _uiState.value.pipeLengthFtOrM,
            staticSupplyPressurePsiOrBar = supplyPressure ?: _uiState.value.staticSupplyPressurePsiOrBar
        )
        recalculate()
    }

    fun updateFittings(
        elbow90: Int? = null,
        elbow45: Int? = null,
        teeFlow: Int? = null,
        teeBranch: Int? = null,
        ballValve: Int? = null,
        checkValve: Int? = null
    ) {
        _uiState.value = _uiState.value.copy(
            elbow90Count = (elbow90 ?: _uiState.value.elbow90Count).coerceAtLeast(0),
            elbow45Count = (elbow45 ?: _uiState.value.elbow45Count).coerceAtLeast(0),
            teeFlowThroughCount = (teeFlow ?: _uiState.value.teeFlowThroughCount).coerceAtLeast(0),
            teeBranchCount = (teeBranch ?: _uiState.value.teeBranchCount).coerceAtLeast(0),
            ballValveCount = (ballValve ?: _uiState.value.ballValveCount).coerceAtLeast(0),
            checkValveCount = (checkValve ?: _uiState.value.checkValveCount).coerceAtLeast(0)
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        // Standardize flow to US GPM and length to US Feet
        val flowGpm = if (isM) (s.flowRateGpmOrLpm / 3.78541).coerceAtLeast(0.1) else s.flowRateGpmOrLpm.coerceAtLeast(0.1)
        val flowLpm = flowGpm * 3.78541
        val straightLengthFt = if (isM) s.pipeLengthFtOrM * 3.28084 else s.pipeLengthFtOrM.coerceAtLeast(1.0)
        val supplyPressurePsi = if (isM) s.staticSupplyPressurePsiOrBar * 14.5038 else s.staticSupplyPressurePsiOrBar.coerceAtLeast(5.0)

        // Inner diameter in inches & mm
        val idInches = s.nominalSize.getInnerDiameterInches(s.material)
        val idMm = idInches * 25.4

        // Flow velocity: v (ft/s) = (0.4085 * GPM) / (d^2 in inches)
        val areaSqIn = PI * (idInches / 2.0).pow(2)
        val velocityFps = (0.4085 * flowGpm) / (idInches.pow(2))
        val velocityMps = velocityFps * 0.3048

        val velStatus = when {
            velocityFps < 2.0 -> VelocityStatus.SLUGGISH
            velocityFps <= 5.0 -> VelocityStatus.OPTIMAL_DOMESTIC
            velocityFps <= 8.0 -> VelocityStatus.ACCEPTABLE_COMMERCIAL
            else -> VelocityStatus.EXCESSIVE_EROSION
        }

        // Equivalent fitting lengths (multiplier of pipe ID in ft, rule of thumb: 90 elbow ~ 30*D, tee branch ~ 60*D)
        val eq90Ft = s.elbow90Count * (idInches * 2.5) // ~2.5 ft for 1" pipe
        val eq45Ft = s.elbow45Count * (idInches * 1.2)
        val eqTeeFlowFt = s.teeFlowThroughCount * (idInches * 1.5)
        val eqTeeBranchFt = s.teeBranchCount * (idInches * 5.0)
        val eqBallValveFt = s.ballValveCount * (idInches * 0.8)
        val eqCheckValveFt = s.checkValveCount * (idInches * 8.0)

        val fittingsEqLengthFt = eq90Ft + eq45Ft + eqTeeFlowFt + eqTeeBranchFt + eqBallValveFt + eqCheckValveFt
        val totalLengthFt = straightLengthFt + fittingsEqLengthFt
        val totalLengthM = totalLengthFt * 0.3048

        // Hazen-Williams Friction Loss Equation:
        // h_f (ft head per 100 ft) = 0.2083 * (100 / C)^1.852 * (Q^1.852 / d^4.8655)
        val c = s.material.cFactor
        val hfPer100Ft = 0.2083 * (100.0 / c).pow(1.852) * (flowGpm.pow(1.852) / idInches.pow(4.8655))
        val totalHeadLossFt = hfPer100Ft * (totalLengthFt / 100.0)

        // Convert head loss (ft) to pressure drop (psi): 1 ft of water head = 0.4335 psi
        val deltaPsi = totalHeadLossFt * 0.4335
        val deltaBar = deltaPsi / 14.5038
        val deltaKpa = deltaPsi * 6.89476

        val residualPsi = (supplyPressurePsi - deltaPsi).coerceAtLeast(0.0)
        val residualBar = residualPsi / 14.5038

        // Sizing Recommendation for flow rate keeping v <= 6.0 ft/s
        val recommendedSize = when {
            flowGpm <= 4.0 -> "1/2\""
            flowGpm <= 9.0 -> "3/4\""
            flowGpm <= 16.0 -> "1\""
            flowGpm <= 26.0 -> "1-1/4\""
            flowGpm <= 40.0 -> "1-1/2\""
            flowGpm <= 70.0 -> "2\""
            flowGpm <= 120.0 -> "2-1/2\""
            flowGpm <= 190.0 -> "3\""
            else -> "4\""
        }

        // Max recommended GPM for current pipe size at 6 ft/s limit: GPM = (v * d^2) / 0.4085
        val maxGpmAt6Fps = (6.0 * idInches.pow(2)) / 0.4085

        _uiState.value = s.copy(
            innerDiameterInches = idInches,
            innerDiameterMm = idMm,
            actualFlowGpm = flowGpm,
            actualFlowLpm = flowLpm,
            velocityFps = velocityFps,
            velocityMps = velocityMps,
            velocityStatus = velStatus,
            equivalentFittingsLengthFt = fittingsEqLengthFt,
            totalEquivalentLengthFt = totalLengthFt,
            totalEquivalentLengthM = totalLengthM,
            headLossPer100Ft = hfPer100Ft,
            totalHeadLossFt = totalHeadLossFt,
            pressureDropPsi = deltaPsi,
            pressureDropBar = deltaBar,
            pressureDropKpa = deltaKpa,
            residualPressurePsi = residualPsi,
            residualPressureBar = residualBar,
            recommendedNominalSize = recommendedSize,
            maxRecommendedGpm = maxGpmAt6Fps
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val flowText = if (s.isMetric) "${String.format("%.1f", s.actualFlowLpm)} L/min" else "${String.format("%.1f", s.actualFlowGpm)} GPM"
        val dropText = if (s.isMetric) "${String.format("%.2f", s.pressureDropBar)} bar" else "${String.format("%.2f", s.pressureDropPsi)} psi"
        val velText = if (s.isMetric) "${String.format("%.2f", s.velocityMps)} m/s" else "${String.format("%.2f", s.velocityFps)} ft/s"
        val summary = "${s.nominalSize.nominalStr} ${s.material.label}: Flow $flowText -> Vel $velText, ΔP $dropText (Total ${String.format("%.1f", if (s.isMetric) s.totalEquivalentLengthM else s.totalEquivalentLengthFt)} ${if (s.isMetric) "m" else "ft"} run)"

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "PLUMBING",
                title = "Pipe Sizing & Friction Loss",
                summary = summary,
                value = s.pressureDropPsi
            )
        }
    }
}
