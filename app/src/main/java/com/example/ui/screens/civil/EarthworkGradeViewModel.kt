package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import com.example.domain.math.CutFillInput
import com.example.domain.math.CutFillResult
import com.example.domain.math.EarthworkEngine
import com.example.domain.math.GradeConversionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EarthworkGradeViewModel : ViewModel() {

    private val _area1 = MutableStateFlow("15.0") // 15 m2
    val area1: StateFlow<String> = _area1.asStateFlow()

    private val _area2 = MutableStateFlow("22.0") // 22 m2
    val area2: StateFlow<String> = _area2.asStateFlow()

    private val _segmentLength = MutableStateFlow("50.0") // 50 meters
    val segmentLength: StateFlow<String> = _segmentLength.asStateFlow()

    private val _percentGrade = MutableStateFlow("4.5") // 4.5% grade
    val percentGrade: StateFlow<String> = _percentGrade.asStateFlow()

    private val _cutFillResult = MutableStateFlow(calculateCutFill())
    val cutFillResult: StateFlow<CutFillResult> = _cutFillResult.asStateFlow()

    private val _gradeResult = MutableStateFlow(calculateGrade())
    val gradeResult: StateFlow<GradeConversionResult> = _gradeResult.asStateFlow()

    fun updateArea1(value: String) {
        _area1.value = value
        recalculateCutFill()
    }

    fun updateArea2(value: String) {
        _area2.value = value
        recalculateCutFill()
    }

    fun updateSegmentLength(value: String) {
        _segmentLength.value = value
        recalculateCutFill()
    }

    fun updatePercentGrade(value: String) {
        _percentGrade.value = value
        _gradeResult.value = calculateGrade()
    }

    private fun recalculateCutFill() {
        _cutFillResult.value = calculateCutFill()
    }

    private fun calculateCutFill(): CutFillResult {
        val a1 = _area1.value.toDoubleOrNull() ?: 15.0
        val a2 = _area2.value.toDoubleOrNull() ?: 22.0
        val l = _segmentLength.value.toDoubleOrNull() ?: 50.0
        return EarthworkEngine.calculateCutFillVolume(CutFillInput(a1, a2, l))
    }

    private fun calculateGrade(): GradeConversionResult {
        val p = _percentGrade.value.toDoubleOrNull() ?: 4.5
        return EarthworkEngine.fromPercentGrade(p)
    }
}
