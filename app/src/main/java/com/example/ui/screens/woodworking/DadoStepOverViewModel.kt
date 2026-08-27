package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import com.example.domain.math.DadoStepOverEngine
import com.example.domain.math.DadoStepOverInput
import com.example.domain.math.DadoStepOverResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DadoStepOverViewModel : ViewModel() {

    private val _jointWidth = MutableStateFlow("19.05") // 3/4 inch lap joint
    val jointWidth: StateFlow<String> = _jointWidth.asStateFlow()

    private val _bladeKerf = MutableStateFlow("3.175") // 1/8 inch kerf
    val bladeKerf: StateFlow<String> = _bladeKerf.asStateFlow()

    private val _stepOverOverlap = MutableStateFlow("0.5") // 0.5mm overlap
    val stepOverOverlap: StateFlow<String> = _stepOverOverlap.asStateFlow()

    private val _result = MutableStateFlow(calculateResult())
    val result: StateFlow<DadoStepOverResult> = _result.asStateFlow()

    fun updateJointWidth(value: String) {
        _jointWidth.value = value
        recalculate()
    }

    fun updateBladeKerf(value: String) {
        _bladeKerf.value = value
        recalculate()
    }

    fun updateStepOverOverlap(value: String) {
        _stepOverOverlap.value = value
        recalculate()
    }

    private fun recalculate() {
        val w = _jointWidth.value.toDoubleOrNull() ?: 19.05
        val k = _bladeKerf.value.toDoubleOrNull() ?: 3.175
        val o = _stepOverOverlap.value.toDoubleOrNull() ?: 0.5

        _result.value = DadoStepOverEngine.calculate(
            DadoStepOverInput(
                jointWidthMm = w,
                bladeKerfMm = k,
                stepOverOverlapMm = o
            )
        )
    }

    private fun calculateResult(): DadoStepOverResult {
        return DadoStepOverEngine.calculate(
            DadoStepOverInput(19.05, 3.175, 0.5)
        )
    }
}
