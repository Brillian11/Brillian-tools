package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class SlingAngleViewModel(private val toolLogRepository: ToolLogRepository) : ViewModel() {

    private val _loadWeight = MutableStateFlow("10000") // lbs or kg
    val loadWeight: StateFlow<String> = _loadWeight.asStateFlow()

    private val _numberOfLegs = MutableStateFlow(2) // 2, 3, or 4 effective legs
    val numberOfLegs: StateFlow<Int> = _numberOfLegs.asStateFlow()

    private val _slingAngle = MutableStateFlow("60.0") // Degrees from horizontal plane
    val slingAngle: StateFlow<String> = _slingAngle.asStateFlow()

    private val _isImperial = MutableStateFlow(true) // true for lbs, false for kg
    val isImperial: StateFlow<Boolean> = _isImperial.asStateFlow()

    // Calculated outputs
    private val _tensionMultiplier = MutableStateFlow(1.155) // L/H multiplier (1/sin(theta))
    val tensionMultiplier: StateFlow<Double> = _tensionMultiplier.asStateFlow()

    private val _tensionPerLeg = MutableStateFlow(5775.0) // Tension on each individual sling leg
    val tensionPerLeg: StateFlow<Double> = _tensionPerLeg.asStateFlow()

    private val _safetyStatus = MutableStateFlow("SAFE") // "SAFE", "WARNING", "CRITICAL"
    val safetyStatus: StateFlow<String> = _safetyStatus.asStateFlow()

    fun setLoadWeight(value: String) {
        _loadWeight.value = value
        calculate()
    }

    fun setNumberOfLegs(value: Int) {
        _numberOfLegs.value = value
        calculate()
    }

    fun setSlingAngle(value: String) {
        _slingAngle.value = value
        calculate()
    }

    fun toggleUnits() {
        val current = _isImperial.value
        _isImperial.value = !current
        val weightVal = _loadWeight.value.toDoubleOrNull() ?: 10000.0
        if (current) {
            // lbs to kg (lbs / 2.20462)
            _loadWeight.value = "%.0f".format(weightVal / 2.20462)
        } else {
            // kg to lbs (kg * 2.20462)
            _loadWeight.value = "%.0f".format(weightVal * 2.20462)
        }
        calculate()
    }

    fun calculate() {
        val weight = _loadWeight.value.toDoubleOrNull() ?: return
        val legs = _numberOfLegs.value
        val angleDeg = _slingAngle.value.toDoubleOrNull() ?: 60.0

        val angleRad = Math.toRadians(angleDeg.coerceIn(1.0, 90.0))
        val sinVal = sin(angleRad)

        // Tension Multiplier = 1 / sin(theta)
        val multiplier = 1.0 / sinVal

        // Tension per leg = (Weight / Legs) * Multiplier
        val legTension = (weight / legs) * multiplier

        val safety = when {
            angleDeg < 30.0 -> "CRITICAL"
            angleDeg < 45.0 -> "WARNING"
            else -> "SAFE"
        }

        _tensionMultiplier.value = multiplier
        _tensionPerLeg.value = legTension
        _safetyStatus.value = safety
    }

    fun logActivity() {
        viewModelScope.launch {
            val isImp = _isImperial.value
            val unitStr = if (isImp) "lbs" else "kg"
            val weightStr = _loadWeight.value
            val legsStr = _numberOfLegs.value
            val angleStr = _slingAngle.value
            val multStr = "%.3f".format(_tensionMultiplier.value)
            val tensStr = "%.0f".format(_tensionPerLeg.value)
            val safetyStr = _safetyStatus.value

            toolLogRepository.logToolActivity(
                toolType = "SLING_ANGLE",
                title = "Rigging Lifting Estimation",
                summary = "Load: $weightStr $unitStr | Legs: $legsStr | Sling Angle: $angleStr° | Load Factor: x$multStr | Tension/Leg: $tensStr $unitStr | Rating: $safetyStr",
                value = _tensionPerLeg.value
            )
        }
    }
}
