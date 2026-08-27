package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class DuctSizerViewModel(private val toolLogRepository: ToolLogRepository) : ViewModel() {

    private val _cfm = MutableStateFlow("1200") // CFM Airflow
    val cfm: StateFlow<String> = _cfm.asStateFlow()

    private val _frictionRateInput = MutableStateFlow("0.10") // in. wg / 100 ft (Standard SMACNA Equal Friction)
    val frictionRateInput: StateFlow<String> = _frictionRateInput.asStateFlow()

    private val _rectangularWidth = MutableStateFlow("16") // inches, default width to find height
    val rectangularWidth: StateFlow<String> = _rectangularWidth.asStateFlow()

    // Calculated outputs
    private val _calculatedDiameter = MutableStateFlow(14.2) // Inches round
    val calculatedDiameter: StateFlow<Double> = _calculatedDiameter.asStateFlow()

    private val _calculatedVelocity = MutableStateFlow(1085.0) // FPM (Feet Per Minute)
    val calculatedVelocity: StateFlow<Double> = _calculatedVelocity.asStateFlow()

    private val _calculatedRectHeight = MutableStateFlow(11.0) // Inches rectangular height
    val calculatedRectHeight: StateFlow<Double> = _calculatedRectHeight.asStateFlow()

    private val _velocityPressure = MutableStateFlow(0.073) // in. wg
    val velocityPressure: StateFlow<Double> = _velocityPressure.asStateFlow()

    fun setCfm(value: String) {
        _cfm.value = value
        calculate()
    }

    fun setFrictionRate(value: String) {
        _frictionRateInput.value = value
        calculate()
    }

    fun setRectWidth(value: String) {
        _rectangularWidth.value = value
        calculate()
    }

    fun calculate() {
        val q = _cfm.value.toDoubleOrNull() ?: return
        val fTarget = _frictionRateInput.value.toDoubleOrNull() ?: 0.10
        val width = _rectangularWidth.value.toDoubleOrNull() ?: 16.0

        // Solve for diameter D using ASHRAE friction rate formula:
        // F = 0.10913 * (Q^1.9) / (D^5.02)
        // => D^5.02 = 0.10913 * (Q^1.9) / F
        // => D = (0.10913 * (Q^1.9) / F) ^ (1 / 5.02)
        try {
            val dPower = (0.10913 * q.pow(1.9)) / fTarget.coerceAtLeast(0.001)
            val d = dPower.pow(1.0 / 5.02)

            // Velocity (FPM) = Q / Area
            // Area (sq ft) = pi * (d / 24)^2 = (pi * d^2) / 576
            val areaSqFt = (Math.PI * d.pow(2)) / 576.0
            val velocity = q / areaSqFt

            // Velocity Pressure (VP) = (V / 4005)^2 (for standard air density)
            val vp = (velocity / 4005.0).pow(2)

            // Calculate rectangular equivalent height using Huebscher's formula via numerical solver
            val height = solveRectangularHeight(d, width)

            _calculatedDiameter.value = d
            _calculatedVelocity.value = velocity
            _velocityPressure.value = vp
            _calculatedRectHeight.value = height

        } catch (_: Exception) {}
    }

    private fun solveRectangularHeight(desiredRoundD: Double, w: Double): Double {
        var low = 1.0
        var high = 300.0
        var h = (low + high) / 2.0
        for (i in 0..40) {
            // Equivalent diameter formula: Deq = 1.30 * ((W * H)^0.625) / ((W + H)^0.25)
            val equivD = 1.30 * (w * h).pow(0.625) / (w + h).pow(0.25)
            if (equivD > desiredRoundD) {
                high = h
            } else {
                low = h
            }
            h = (low + high) / 2.0
        }
        return h
    }

    fun logActivity() {
        viewModelScope.launch {
            val cfmVal = _cfm.value
            val fricVal = _frictionRateInput.value
            val dVal = "%.1f".format(_calculatedDiameter.value)
            val vVal = "%.0f".format(_calculatedVelocity.value)
            val wVal = _rectangularWidth.value
            val hVal = "%.1f".format(_calculatedRectHeight.value)

            toolLogRepository.logToolActivity(
                toolType = "DUCT_SIZER",
                title = "Duct Design Sizing",
                summary = "Flow: $cfmVal CFM | Equal Friction: $fricVal\" wg/100ft | Round Dia: $dVal\" | Velocity: $vVal FPM | Rectangular Eq: $wVal\" x $hVal\"",
                value = _calculatedDiameter.value
            )
        }
    }
}
