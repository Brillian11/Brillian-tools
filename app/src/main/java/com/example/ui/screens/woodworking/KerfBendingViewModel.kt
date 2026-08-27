package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import com.example.domain.math.KerfBendSection
import com.example.domain.math.KerfBendingEngine
import com.example.domain.math.KerfBendingInput
import com.example.domain.math.KerfBendingResult
import com.example.domain.math.MultiSectionKerfInput
import com.example.domain.math.MultiSectionKerfResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KerfBendingViewModel : ViewModel() {

    private val _isMultiSectionMode = MutableStateFlow(true)
    val isMultiSectionMode: StateFlow<Boolean> = _isMultiSectionMode.asStateFlow()

    private val _boardLength = MutableStateFlow("1000.0") // 1000mm = 1 meter board
    val boardLength: StateFlow<String> = _boardLength.asStateFlow()

    private val _sectionsList = MutableStateFlow(
        listOf(
            KerfBendSection(1, 150.0, 90.0),
            KerfBendSection(2, 150.0, 90.0)
        )
    )
    val sectionsList: StateFlow<List<KerfBendSection>> = _sectionsList.asStateFlow()

    private val _boardThickness = MutableStateFlow("19.0") // 3/4 inch = 19mm
    val boardThickness: StateFlow<String> = _boardThickness.asStateFlow()

    private val _bladeKerf = MutableStateFlow("3.175") // 1/8 inch = 3.175mm
    val bladeKerf: StateFlow<String> = _bladeKerf.asStateFlow()

    private val _targetRadius = MutableStateFlow("150.0") // 150mm radius
    val targetRadius: StateFlow<String> = _targetRadius.asStateFlow()

    private val _bendAngle = MutableStateFlow("90.0") // 90 degree bend
    val bendAngle: StateFlow<String> = _bendAngle.asStateFlow()

    private val _veneerAllowance = MutableStateFlow("1.5") // 1.5mm face veneer left
    val veneerAllowance: StateFlow<String> = _veneerAllowance.asStateFlow()

    private val _result = MutableStateFlow(calculateResult())
    val result: StateFlow<KerfBendingResult> = _result.asStateFlow()

    private val _multiResult = MutableStateFlow(calculateMultiResult())
    val multiResult: StateFlow<MultiSectionKerfResult> = _multiResult.asStateFlow()

    fun toggleMultiSectionMode(enabled: Boolean) {
        _isMultiSectionMode.value = enabled
        recalculate()
    }

    fun updateBoardLength(value: String) {
        _boardLength.value = value
        recalculate()
    }

    fun updateBoardThickness(value: String) {
        _boardThickness.value = value
        recalculate()
    }

    fun updateBladeKerf(value: String) {
        _bladeKerf.value = value
        recalculate()
    }

    fun updateTargetRadius(value: String) {
        _targetRadius.value = value
        recalculate()
    }

    fun updateBendAngle(value: String) {
        _bendAngle.value = value
        recalculate()
    }

    fun updateVeneerAllowance(value: String) {
        _veneerAllowance.value = value
        recalculate()
    }

    fun addKerfSection() {
        val current = _sectionsList.value
        val nextIdx = current.size + 1
        _sectionsList.value = current + KerfBendSection(nextIdx, 150.0, 90.0)
        recalculate()
    }

    fun removeKerfSection(index: Int) {
        val current = _sectionsList.value.toMutableList()
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
            // Reindex
            val reindexed = current.mapIndexed { idx, sec -> sec.copy(sectionIndex = idx + 1) }
            _sectionsList.value = reindexed
            recalculate()
        }
    }

    fun updateSectionParams(index: Int, radiusMm: Double, bendAngleDegrees: Double) {
        val current = _sectionsList.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(targetRadiusMm = radiusMm, bendAngleDegrees = bendAngleDegrees)
            _sectionsList.value = current
            recalculate()
        }
    }

    private fun recalculate() {
        _result.value = calculateResult()
        _multiResult.value = calculateMultiResult()
    }

    private fun calculateResult(): KerfBendingResult {
        val t = _boardThickness.value.toDoubleOrNull() ?: 19.0
        val k = _bladeKerf.value.toDoubleOrNull() ?: 3.175
        val r = _targetRadius.value.toDoubleOrNull() ?: 150.0
        val theta = _bendAngle.value.toDoubleOrNull() ?: 90.0
        val v = _veneerAllowance.value.toDoubleOrNull() ?: 1.5

        return KerfBendingEngine.calculate(
            KerfBendingInput(
                boardThicknessMm = t,
                bladeKerfMm = k,
                targetInsideRadiusMm = r,
                bendAngleDegrees = theta,
                veneerAllowanceMm = v
            )
        )
    }

    private fun calculateMultiResult(): MultiSectionKerfResult {
        val bLen = _boardLength.value.toDoubleOrNull() ?: 1000.0
        val t = _boardThickness.value.toDoubleOrNull() ?: 19.0
        val k = _bladeKerf.value.toDoubleOrNull() ?: 3.175
        val v = _veneerAllowance.value.toDoubleOrNull() ?: 1.5

        return KerfBendingEngine.calculateMultiSection(
            MultiSectionKerfInput(
                boardLengthMm = bLen,
                boardThicknessMm = t,
                bladeKerfMm = k,
                veneerAllowanceMm = v,
                sections = _sectionsList.value
            )
        )
    }
}
