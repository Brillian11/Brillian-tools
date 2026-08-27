package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.MaterialEntity
import com.example.data.repository.MaterialRepository
import com.example.domain.math.CutPiece
import com.example.domain.math.CutlistOptimizationResult
import com.example.domain.math.CutlistOptimizerEngine
import com.example.domain.math.MaterialType
import com.example.domain.math.StockBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class CutlistOptimizerViewModel(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    val availableMaterials: StateFlow<List<MaterialEntity>> = materialRepository.allMaterials
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _materialType = MutableStateFlow(MaterialType.TIMBER_BOARD)
    val materialType: StateFlow<MaterialType> = _materialType.asStateFlow()

    private val _stockBoardLength = MutableStateFlow("2438.4") // 8 ft stock
    val stockBoardLength: StateFlow<String> = _stockBoardLength.asStateFlow()

    private val _stockBoardWidth = MutableStateFlow("1219.2") // 4 ft width for Plywood
    val stockBoardWidth: StateFlow<String> = _stockBoardWidth.asStateFlow()

    private val _bladeKerf = MutableStateFlow("3.175") // 1/8 inch
    val bladeKerf: StateFlow<String> = _bladeKerf.asStateFlow()

    private val _trimMargin = MutableStateFlow("12.7") // 1/2 inch trim margin
    val trimMargin: StateFlow<String> = _trimMargin.asStateFlow()

    private val colors = CutlistOptimizerEngine.DEFAULT_CUT_COLORS

    private val _requestedCuts = MutableStateFlow(
        listOf(
            CutPiece("1", "Table Top Rails", 1200.0, 150.0, 2, colors[0]),
            CutPiece("2", "Table Stretcher", 950.0, 120.0, 1, colors[1]),
            CutPiece("3", "Leg Aprons", 450.0, 90.0, 4, colors[2]),
            CutPiece("4", "Corner Blocks", 150.0, 90.0, 4, colors[3])
        )
    )
    val requestedCuts: StateFlow<List<CutPiece>> = _requestedCuts.asStateFlow()

    private val _optimizationResult = MutableStateFlow(calculateOptimization())
    val optimizationResult: StateFlow<CutlistOptimizationResult> = _optimizationResult.asStateFlow()

    fun updateMaterialType(type: MaterialType) {
        _materialType.value = type
        if (type == MaterialType.PLYWOOD_SHEET) {
            _stockBoardLength.value = "2438.4" // 8 ft
            _stockBoardWidth.value = "1219.2"  // 4 ft
        } else {
            _stockBoardLength.value = "2438.4"
            _stockBoardWidth.value = "89.0"    // 2x4 nominal board width
        }
        recalculate()
    }

    fun updateStockLength(value: String) {
        _stockBoardLength.value = value
        recalculate()
    }

    fun updateStockWidth(value: String) {
        _stockBoardWidth.value = value
        recalculate()
    }

    fun updateBladeKerf(value: String) {
        _bladeKerf.value = value
        recalculate()
    }

    fun updateTrimMargin(value: String) {
        _trimMargin.value = value
        recalculate()
    }

    fun addCutPiece(label: String, lengthMm: Double, widthMm: Double, quantity: Int) {
        val colorIdx = _requestedCuts.value.size % colors.size
        val newPiece = CutPiece(
            id = System.currentTimeMillis().toString(),
            label = label.ifEmpty { "Cut Piece" },
            lengthMm = lengthMm,
            widthMm = widthMm,
            quantity = quantity,
            colorHex = colors[colorIdx]
        )
        _requestedCuts.value = _requestedCuts.value + newPiece
        recalculate()
    }

    fun removeCutPiece(id: String) {
        _requestedCuts.value = _requestedCuts.value.filterNot { it.id == id }
        recalculate()
    }

    private fun recalculate() {
        _optimizationResult.value = calculateOptimization()
    }

    private fun calculateOptimization(): CutlistOptimizationResult {
        val stockLen = _stockBoardLength.value.toDoubleOrNull() ?: 2438.4
        val stockWid = _stockBoardWidth.value.toDoubleOrNull() ?: 1219.2
        val kerf = _bladeKerf.value.toDoubleOrNull() ?: 3.175
        val trim = _trimMargin.value.toDoubleOrNull() ?: 12.7

        val stockName = if (_materialType.value == MaterialType.PLYWOOD_SHEET) {
            "Plywood Sheet (${stockLen}mm x ${stockWid}mm)"
        } else {
            "Timber Board (${stockLen}mm x ${stockWid}mm)"
        }

        val stocks = listOf(
            StockBoard(
                id = 1,
                name = stockName,
                type = _materialType.value,
                lengthMm = stockLen,
                widthMm = stockWid,
                costPerUnit = 28.50
            )
        )

        return CutlistOptimizerEngine.optimize(
            stockBoards = stocks,
            requestedCuts = _requestedCuts.value,
            bladeKerfMm = kerf,
            trimMarginMm = trim
        )
    }
}

