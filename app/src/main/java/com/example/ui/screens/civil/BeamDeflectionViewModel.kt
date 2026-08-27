package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import com.example.domain.math.BeamDeflectionEngine
import com.example.domain.math.BeamInput
import com.example.domain.math.BeamResult
import com.example.domain.math.BeamShape
import com.example.domain.math.LoadType
import com.example.domain.math.MaterialPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BeamDeflectionViewModel : ViewModel() {

    private val _spanLength = MutableStateFlow("3.6") // 3.6m span
    val spanLength: StateFlow<String> = _spanLength.asStateFlow()

    private val _loadType = MutableStateFlow(LoadType.UNIFORM_DISTRIBUTED)
    val loadType: StateFlow<LoadType> = _loadType.asStateFlow()

    private val _uniformLoad = MutableStateFlow("2.5") // 2.5 kN/m
    val uniformLoad: StateFlow<String> = _uniformLoad.asStateFlow()

    private val _pointLoad = MutableStateFlow("5.0") // 5.0 kN
    val pointLoad: StateFlow<String> = _pointLoad.asStateFlow()

    private val _beamShape = MutableStateFlow(BeamShape.RECTANGULAR)
    val beamShape: StateFlow<BeamShape> = _beamShape.asStateFlow()

    private val _beamWidth = MutableStateFlow("89.0") // 2x4 / 2x6 nominal width ~ 89mm
    val beamWidth: StateFlow<String> = _beamWidth.asStateFlow()

    private val _beamHeight = MutableStateFlow("184.0") // 2x8 nominal height ~ 184mm
    val beamHeight: StateFlow<String> = _beamHeight.asStateFlow()

    private val _selectedMaterial = MutableStateFlow(MaterialPreset.PRESETS[0]) // Douglas Fir
    val selectedMaterial: StateFlow<MaterialPreset> = _selectedMaterial.asStateFlow()

    private val _result = MutableStateFlow(calculateResult())
    val result: StateFlow<BeamResult> = _result.asStateFlow()

    fun updateSpanLength(value: String) {
        _spanLength.value = value
        recalculate()
    }

    fun updateLoadType(type: LoadType) {
        _loadType.value = type
        recalculate()
    }

    fun updateUniformLoad(value: String) {
        _uniformLoad.value = value
        recalculate()
    }

    fun updatePointLoad(value: String) {
        _pointLoad.value = value
        recalculate()
    }

    fun updateBeamShape(shape: BeamShape) {
        _beamShape.value = shape
        recalculate()
    }

    fun updateBeamWidth(value: String) {
        _beamWidth.value = value
        recalculate()
    }

    fun updateBeamHeight(value: String) {
        _beamHeight.value = value
        recalculate()
    }

    fun selectMaterial(preset: MaterialPreset) {
        _selectedMaterial.value = preset
        recalculate()
    }

    private fun recalculate() {
        _result.value = calculateResult()
    }

    private fun calculateResult(): BeamResult {
        val l = _spanLength.value.toDoubleOrNull() ?: 3.6
        val w = _uniformLoad.value.toDoubleOrNull() ?: 2.5
        val p = _pointLoad.value.toDoubleOrNull() ?: 5.0
        val bw = _beamWidth.value.toDoubleOrNull() ?: 89.0
        val bh = _beamHeight.value.toDoubleOrNull() ?: 184.0

        return BeamDeflectionEngine.calculate(
            BeamInput(
                spanLengthMeters = l,
                loadType = _loadType.value,
                uniformLoadKnM = w,
                pointLoadKn = p,
                shape = _beamShape.value,
                widthMm = bw,
                heightMm = bh,
                elasticityModulusGpa = _selectedMaterial.value.elasticityModulusGpa,
                allowableDeflectionRatio = 360.0
            )
        )
    }
}
