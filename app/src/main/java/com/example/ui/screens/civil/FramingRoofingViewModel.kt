package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import com.example.domain.math.FramingEngine
import com.example.domain.math.RoofingInput
import com.example.domain.math.RoofingResult
import com.example.domain.math.StudSpacingOption
import com.example.domain.math.WallFramingInput
import com.example.domain.math.WallFramingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FramingRoofingViewModel : ViewModel() {

    private val _wallLength = MutableStateFlow("6.0") // 6 meters
    val wallLength: StateFlow<String> = _wallLength.asStateFlow()

    private val _wallHeight = MutableStateFlow("2.44") // 2.44m
    val wallHeight: StateFlow<String> = _wallHeight.asStateFlow()

    private val _studSpacing = MutableStateFlow(StudSpacingOption.SPACING_40CM)
    val studSpacing: StateFlow<StudSpacingOption> = _studSpacing.asStateFlow()

    private val _doors = MutableStateFlow("1")
    val doors: StateFlow<String> = _doors.asStateFlow()

    private val _windows = MutableStateFlow("2")
    val windows: StateFlow<String> = _windows.asStateFlow()

    private val _roofRun = MutableStateFlow("4.0") // 4m run
    val roofRun: StateFlow<String> = _roofRun.asStateFlow()

    private val _roofRise = MutableStateFlow("1.5") // 1.5m rise
    val roofRise: StateFlow<String> = _roofRise.asStateFlow()

    private val _roofOverhang = MutableStateFlow("0.4") // 0.4m overhang
    val roofOverhang: StateFlow<String> = _roofOverhang.asStateFlow()

    private val _roofLength = MutableStateFlow("10.0") // 10m building length
    val roofLength: StateFlow<String> = _roofLength.asStateFlow()

    private val _wallResult = MutableStateFlow(calculateWall())
    val wallResult: StateFlow<WallFramingResult> = _wallResult.asStateFlow()

    private val _roofResult = MutableStateFlow(calculateRoof())
    val roofResult: StateFlow<RoofingResult> = _roofResult.asStateFlow()

    fun updateWallLength(value: String) {
        _wallLength.value = value
        recalculateWall()
    }

    fun updateWallHeight(value: String) {
        _wallHeight.value = value
        recalculateWall()
    }

    fun updateStudSpacing(spacing: StudSpacingOption) {
        _studSpacing.value = spacing
        recalculateWall()
    }

    fun updateDoors(value: String) {
        _doors.value = value
        recalculateWall()
    }

    fun updateWindows(value: String) {
        _windows.value = value
        recalculateWall()
    }

    fun updateRoofRun(value: String) {
        _roofRun.value = value
        recalculateRoof()
    }

    fun updateRoofRise(value: String) {
        _roofRise.value = value
        recalculateRoof()
    }

    fun updateRoofOverhang(value: String) {
        _roofOverhang.value = value
        recalculateRoof()
    }

    fun updateRoofLength(value: String) {
        _roofLength.value = value
        recalculateRoof()
    }

    private fun recalculateWall() {
        _wallResult.value = calculateWall()
    }

    private fun recalculateRoof() {
        _roofResult.value = calculateRoof()
    }

    private fun calculateWall(): WallFramingResult {
        val l = _wallLength.value.toDoubleOrNull() ?: 6.0
        val h = _wallHeight.value.toDoubleOrNull() ?: 2.44
        val d = _doors.value.toIntOrNull() ?: 1
        val w = _windows.value.toIntOrNull() ?: 2

        return FramingEngine.calculateWallFraming(
            WallFramingInput(l, h, _studSpacing.value, d, w)
        )
    }

    private fun calculateRoof(): RoofingResult {
        val run = _roofRun.value.toDoubleOrNull() ?: 4.0
        val rise = _roofRise.value.toDoubleOrNull() ?: 1.5
        val overhang = _roofOverhang.value.toDoubleOrNull() ?: 0.4
        val len = _roofLength.value.toDoubleOrNull() ?: 10.0

        return FramingEngine.calculateRoofing(
            RoofingInput(run, rise, overhang, len, 600.0)
        )
    }
}
