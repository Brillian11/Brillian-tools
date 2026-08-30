package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WoodShrinkageSpecies(
    val name: String,
    val totalTangentialPct: Double,
    val totalRadialPct: Double,
    val fspPct: Double = 28.0,
    val trRatio: Double,
    val stabilityRating: String
)

enum class GrainOrientation {
    FLATSAWN,
    QUARTERSAWN,
    RIFTSAWN
}

data class WoodMoistureUiState(
    val isMetric: Boolean = false,
    val speciesList: List<WoodShrinkageSpecies> = listOf(
        WoodShrinkageSpecies("White Oak", 10.5, 5.6, 28.0, 1.88, "Good"),
        WoodShrinkageSpecies("Red Oak (Northern)", 8.6, 4.0, 28.0, 2.15, "Moderate"),
        WoodShrinkageSpecies("Black Walnut", 7.8, 5.5, 28.0, 1.42, "Excellent"),
        WoodShrinkageSpecies("Hard Maple (Sugar)", 9.9, 4.8, 28.0, 2.06, "Moderate"),
        WoodShrinkageSpecies("Black Cherry", 7.1, 3.7, 28.0, 1.92, "Good"),
        WoodShrinkageSpecies("Poplar (Yellow)", 8.2, 4.6, 28.0, 1.78, "Good"),
        WoodShrinkageSpecies("Eastern White Pine", 6.1, 2.3, 28.0, 2.65, "Good"),
        WoodShrinkageSpecies("Douglas Fir", 7.6, 4.8, 28.0, 1.58, "Excellent"),
        WoodShrinkageSpecies("Western Red Cedar", 5.0, 2.4, 28.0, 2.08, "Excellent"),
        WoodShrinkageSpecies("Teak (Burmese)", 5.8, 2.5, 28.0, 2.32, "Excellent"),
        WoodShrinkageSpecies("Genuine Mahogany", 4.1, 3.0, 28.0, 1.37, "Excellent"),
        WoodShrinkageSpecies("Hickory", 10.5, 7.0, 28.0, 1.50, "Moderate")
    ),
    val selectedSpecies: WoodShrinkageSpecies = WoodShrinkageSpecies("White Oak", 10.5, 5.6, 28.0, 1.88, "Good"),

    val initialMoisturePct: Double = 14.0,
    val targetMoisturePct: Double = 8.0,
    val initialWidthInches: Double = 8.0,
    val initialThicknessInches: Double = 1.0,
    val grainOrientation: GrainOrientation = GrainOrientation.FLATSAWN,

    val moistureDeltaPct: Double = 6.0,
    val effectiveShrinkagePct: Double = 2.25,
    val widthChangeInches: Double = -0.180,
    val finalWidthInches: Double = 7.820,
    val finalWidthMm: Double = 198.6,
    val widthChangeDisplay: String = "-0.180\" (-4.6 mm)",
    val finalWidthDisplay: String = "7.820\" (198.6 mm)",
    val thicknessChangeInches: Double = -0.012,
    val finalThicknessInches: Double = 0.988,
    val warpingRiskAssessment: String = "Low cupping risk under steady interior climate"
)

class WoodMoistureViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WoodMoistureUiState())
    val uiState: StateFlow<WoodMoistureUiState> = _uiState.asStateFlow()

    init {
        recalculateShrinkage()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(isMetric = metric)
            recalculateShrinkage()
        }
    }

    fun selectSpecies(species: WoodShrinkageSpecies) {
        _uiState.value = _uiState.value.copy(selectedSpecies = species)
        recalculateShrinkage()
    }

    fun setGrainOrientation(grain: GrainOrientation) {
        _uiState.value = _uiState.value.copy(grainOrientation = grain)
        recalculateShrinkage()
    }

    fun updateInputs(initialMC: Double, targetMC: Double, width: Double, thickness: Double) {
        val wIn = if (_uiState.value.isMetric) width / 25.4 else width
        val tIn = if (_uiState.value.isMetric) thickness / 25.4 else thickness

        _uiState.value = _uiState.value.copy(
            initialMoisturePct = initialMC.coerceIn(2.0, 60.0),
            targetMoisturePct = targetMC.coerceIn(2.0, 60.0),
            initialWidthInches = wIn.coerceAtLeast(0.01),
            initialThicknessInches = tIn.coerceAtLeast(0.01)
        )
        recalculateShrinkage()
    }

    private fun recalculateShrinkage() {
        val s = _uiState.value
        val fsp = s.selectedSpecies.fspPct
        val mInitial = s.initialMoisturePct.coerceAtMost(fsp)
        val mTarget = s.targetMoisturePct.coerceAtMost(fsp)
        val deltaMC = mInitial - mTarget

        val tangShrinkPct = s.selectedSpecies.totalTangentialPct * (deltaMC / fsp)
        val radShrinkPct = s.selectedSpecies.totalRadialPct * (deltaMC / fsp)

        val (widthShrinkPct, thickShrinkPct) = when (s.grainOrientation) {
            GrainOrientation.FLATSAWN -> Pair(tangShrinkPct, radShrinkPct)
            GrainOrientation.QUARTERSAWN -> Pair(radShrinkPct, tangShrinkPct)
            GrainOrientation.RIFTSAWN -> Pair((tangShrinkPct + radShrinkPct) / 2.0, (tangShrinkPct + radShrinkPct) / 2.0)
        }

        val deltaWidthIn = -(s.initialWidthInches * (widthShrinkPct / 100.0))
        val finalWidthIn = s.initialWidthInches + deltaWidthIn
        val deltaThickIn = -(s.initialThicknessInches * (thickShrinkPct / 100.0))
        val finalThickIn = s.initialThicknessInches + deltaThickIn

        val widthChangeDisp = if (s.isMetric) "%.1f mm".format(deltaWidthIn * 25.4) else "%.3f\"".format(deltaWidthIn)
        val finalWidthDisp = if (s.isMetric) "%.1f mm".format(finalWidthIn * 25.4) else "%.3f\"".format(finalWidthIn)

        val risk = when {
            s.selectedSpecies.trRatio > 2.0 && s.grainOrientation == GrainOrientation.FLATSAWN ->
                "High Cupping Risk: Wide flatsawn board with T/R ratio > 2.0."
            s.grainOrientation == GrainOrientation.QUARTERSAWN ->
                "Minimal Movement: Quartersawn orientation provides maximum dimensional stability."
            else ->
                "Moderate Movement: Standard furniture allowance for seasonal expansion/contraction required."
        }

        _uiState.value = s.copy(
            moistureDeltaPct = deltaMC,
            effectiveShrinkagePct = widthShrinkPct,
            widthChangeInches = deltaWidthIn,
            finalWidthInches = finalWidthIn,
            finalWidthMm = finalWidthIn * 25.4,
            widthChangeDisplay = widthChangeDisp,
            finalWidthDisplay = finalWidthDisp,
            thicknessChangeInches = deltaThickIn,
            finalThicknessInches = finalThickIn,
            warpingRiskAssessment = risk
        )
    }
}
