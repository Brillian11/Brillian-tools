package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class WoodSpecies(
    val name: String,
    val defaultPricePerBF: Double,
    val jankaHardnessLbf: Int,
    val densityLbsPerCuFt: Double,
    val category: String // Hardwood, Softwood, Exotic
)

data class LumberItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val thicknessInches: Double,
    val widthInches: Double,
    val lengthFeet: Double,
    val quantity: Int,
    val species: String,
    val pricePerBF: Double,
    val boardFeet: Double,
    val itemCost: Double,
    val weightLbs: Double
)

data class BoardFootageUiState(
    val speciesList: List<WoodSpecies> = listOf(
        WoodSpecies("Black Walnut", 12.50, 1010, 38.0, "Hardwood"),
        WoodSpecies("White Oak", 8.75, 1360, 47.0, "Hardwood"),
        WoodSpecies("Red Oak", 6.50, 1290, 44.0, "Hardwood"),
        WoodSpecies("Hard Maple (Sugar)", 7.80, 1450, 44.0, "Hardwood"),
        WoodSpecies("Cherry", 8.20, 950, 35.0, "Hardwood"),
        WoodSpecies("Poplar (Yellow)", 4.25, 540, 29.0, "Hardwood"),
        WoodSpecies("Ash (White)", 6.00, 1320, 42.0, "Hardwood"),
        WoodSpecies("Hickory", 7.50, 1820, 51.0, "Hardwood"),
        WoodSpecies("Eastern White Pine", 3.75, 380, 25.0, "Softwood"),
        WoodSpecies("Douglas Fir", 4.50, 660, 32.0, "Softwood"),
        WoodSpecies("Western Red Cedar", 7.00, 350, 23.0, "Softwood"),
        WoodSpecies("Teak (Burmese)", 28.00, 1070, 41.0, "Exotic"),
        WoodSpecies("Genuine Mahogany", 14.50, 830, 36.0, "Exotic")
    ),
    val selectedSpecies: WoodSpecies = WoodSpecies("Black Walnut", 12.50, 1010, 38.0, "Hardwood"),
    
    // Active Input
    val inputThicknessQuarter: String = "4/4 (1.0\")", // 4/4, 5/4, 6/4, 8/4, 10/4, 12/4
    val thicknessInches: Double = 1.0,
    val widthInches: Double = 6.0,
    val lengthFeet: Double = 8.0,
    val quantity: Int = 4,
    val wastePercentage: Double = 15.0, // 15% standard wood waste
    val customPricePerBF: Double = 12.50,
    
    // Lumber Cut List
    val lumberList: List<LumberItem> = listOf(
        LumberItem(
            label = "Table Top Planks",
            thicknessInches = 1.0,
            widthInches = 6.0,
            lengthFeet = 8.0,
            quantity = 6,
            species = "Black Walnut",
            pricePerBF = 12.50,
            boardFeet = 24.0,
            itemCost = 300.0,
            weightLbs = 76.0
        ),
        LumberItem(
            label = "Leg Blanks (8/4)",
            thicknessInches = 2.0,
            widthInches = 4.0,
            lengthFeet = 3.0,
            quantity = 4,
            species = "Black Walnut",
            pricePerBF = 14.00,
            boardFeet = 8.0,
            itemCost = 112.0,
            weightLbs = 25.3
        )
    ),
    
    // Grand Totals
    val totalBoardFeetNet: Double = 32.0,
    val totalBoardFeetWithWaste: Double = 36.8,
    val totalLumberCost: Double = 412.0,
    val totalEstimatedWeightLbs: Double = 101.3
)

class BoardFootageViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardFootageUiState())
    val uiState: StateFlow<BoardFootageUiState> = _uiState.asStateFlow()

    init {
        recalculateTotals()
    }

    fun selectSpecies(species: WoodSpecies) {
        _uiState.value = _uiState.value.copy(
            selectedSpecies = species,
            customPricePerBF = species.defaultPricePerBF
        )
    }

    fun setThicknessQuarter(quarterLabel: String) {
        val thickness = when (quarterLabel) {
            "4/4 (1.0\")" -> 1.0
            "5/4 (1.25\")" -> 1.25
            "6/4 (1.5\")" -> 1.5
            "8/4 (2.0\")" -> 2.0
            "10/4 (2.5\")" -> 2.5
            "12/4 (3.0\")" -> 3.0
            "16/4 (4.0\")" -> 4.0
            else -> 1.0
        }
        _uiState.value = _uiState.value.copy(
            inputThicknessQuarter = quarterLabel,
            thicknessInches = thickness
        )
    }

    fun updateDimensions(thickness: Double, width: Double, lengthFt: Double, qty: Int, pricePerBF: Double) {
        _uiState.value = _uiState.value.copy(
            thicknessInches = thickness.coerceAtLeast(0.1),
            widthInches = width.coerceAtLeast(0.5),
            lengthFeet = lengthFt.coerceAtLeast(0.5),
            quantity = qty.coerceAtLeast(1),
            customPricePerBF = pricePerBF.coerceAtLeast(0.0)
        )
    }

    fun setWastePercentage(waste: Double) {
        _uiState.value = _uiState.value.copy(wastePercentage = waste)
        recalculateTotals()
    }

    fun addLumberItem(label: String) {
        val state = _uiState.value
        // Formula: BF = (Thickness_in * Width_in * Length_ft) / 12 * Quantity
        val singleBF = (state.thicknessInches * state.widthInches * state.lengthFeet) / 12.0
        val totalBF = singleBF * state.quantity
        val cost = totalBF * state.customPricePerBF
        
        // Weight: (BF / 12 cu ft) * Density (lbs/cu ft)
        val cuFt = totalBF / 12.0
        val weight = cuFt * state.selectedSpecies.densityLbsPerCuFt

        val newItem = LumberItem(
            label = label.ifBlank { "Board #${state.lumberList.size + 1}" },
            thicknessInches = state.thicknessInches,
            widthInches = state.widthInches,
            lengthFeet = state.lengthFeet,
            quantity = state.quantity,
            species = state.selectedSpecies.name,
            pricePerBF = state.customPricePerBF,
            boardFeet = totalBF,
            itemCost = cost,
            weightLbs = weight
        )

        _uiState.value = _uiState.value.copy(lumberList = _uiState.value.lumberList + newItem)
        recalculateTotals()

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "WOODWORKING",
                title = "Lumber Board Feet",
                summary = "${newItem.label} (${newItem.species}): ${String.format("%.1f", totalBF)} BF - $${String.format("%.2f", cost)}",
                value = totalBF
            )
        }
    }

    fun removeLumberItem(id: String) {
        _uiState.value = _uiState.value.copy(lumberList = _uiState.value.lumberList.filterNot { it.id == id })
        recalculateTotals()
    }

    private fun recalculateTotals() {
        val list = _uiState.value.lumberList
        val netBF = list.sumOf { it.boardFeet }
        val wasteFactor = 1.0 + (_uiState.value.wastePercentage / 100.0)
        val grossBF = netBF * wasteFactor
        val totalCost = list.sumOf { it.itemCost } * wasteFactor
        val totalWeight = list.sumOf { it.weightLbs } * wasteFactor

        _uiState.value = _uiState.value.copy(
            totalBoardFeetNet = netBF,
            totalBoardFeetWithWaste = grossBF,
            totalLumberCost = totalCost,
            totalEstimatedWeightLbs = totalWeight
        )
    }
}
