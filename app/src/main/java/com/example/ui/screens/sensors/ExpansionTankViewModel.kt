package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class ExpansionTankViewModel(private val toolLogRepository: ToolLogRepository) : ViewModel() {

    private val _systemVolume = MutableStateFlow("150.0") // Gallons or Liters
    val systemVolume: StateFlow<String> = _systemVolume.asStateFlow()

    private val _fillTemp = MutableStateFlow("50.0") // °F or °C
    val fillTemp: StateFlow<String> = _fillTemp.asStateFlow()

    private val _designTemp = MutableStateFlow("180.0") // °F or °C
    val designTemp: StateFlow<String> = _designTemp.asStateFlow()

    private val _fillPressure = MutableStateFlow("12.0") // psig (Initial cold fill pressure)
    val fillPressure: StateFlow<String> = _fillPressure.asStateFlow()

    private val _reliefPressure = MutableStateFlow("30.0") // psig (Boiler relief valve rating)
    val reliefPressure: StateFlow<String> = _reliefPressure.asStateFlow()

    private val _glycolPercent = MutableStateFlow(0.0) // 0% to 50%
    val glycolPercent: StateFlow<Double> = _glycolPercent.asStateFlow()

    private val _isImperial = MutableStateFlow(true) // true for Gallons/°F/psig, false for Liters/°C/kPa
    val isImperial: StateFlow<Boolean> = _isImperial.asStateFlow()

    // Calculated outputs
    private val _expansionFactor = MutableStateFlow(0.0258) // Net expansion percentage
    val expansionFactor: StateFlow<Double> = _expansionFactor.asStateFlow()

    private val _requiredVolume = MutableStateFlow(9.7) // Gallons or Liters matching unit
    val requiredVolume: StateFlow<Double> = _requiredVolume.asStateFlow()

    private val _acceptanceVolume = MutableStateFlow(3.87) // Boiler acceptance volume
    val acceptanceVolume: StateFlow<Double> = _acceptanceVolume.asStateFlow()

    fun setSystemVolume(value: String) {
        _systemVolume.value = value
        calculate()
    }

    fun setFillTemp(value: String) {
        _fillTemp.value = value
        calculate()
    }

    fun setDesignTemp(value: String) {
        _designTemp.value = value
        calculate()
    }

    fun setFillPressure(value: String) {
        _fillPressure.value = value
        calculate()
    }

    fun setReliefPressure(value: String) {
        _reliefPressure.value = value
        calculate()
    }

    fun setGlycolPercent(value: Double) {
        _glycolPercent.value = value
        calculate()
    }

    fun toggleUnits() {
        val current = _isImperial.value
        _isImperial.value = !current

        val v = _systemVolume.value.toDoubleOrNull() ?: 150.0
        val t1 = _fillTemp.value.toDoubleOrNull() ?: 50.0
        val t2 = _designTemp.value.toDoubleOrNull() ?: 180.0
        val p1 = _fillPressure.value.toDoubleOrNull() ?: 12.0
        val p2 = _reliefPressure.value.toDoubleOrNull() ?: 30.0

        if (current) {
            // Imperial to Metric: gal -> L, °F -> °C, psig -> kPa
            _systemVolume.value = "%.1f".format(v * 3.78541)
            _fillTemp.value = "%.1f".format((t1 - 32.0) / 1.8)
            _designTemp.value = "%.1f".format((t2 - 32.0) / 1.8)
            _fillPressure.value = "%.1f".format(p1 * 6.89476)
            _reliefPressure.value = "%.1f".format(p2 * 6.89476)
        } else {
            // Metric to Imperial: L -> gal, °C -> °F, kPa -> psig
            _systemVolume.value = "%.1f".format(v / 3.78541)
            _fillTemp.value = "%.1f".format(t1 * 1.8 + 32.0)
            _designTemp.value = "%.1f".format(t2 * 1.8 + 32.0)
            _fillPressure.value = "%.1f".format(p1 / 6.89476)
            _reliefPressure.value = "%.1f".format(p2 / 6.89476)
        }
        calculate()
    }

    fun calculate() {
        val vSystem = _systemVolume.value.toDoubleOrNull() ?: return
        val t1Raw = _fillTemp.value.toDoubleOrNull() ?: return
        val t2Raw = _designTemp.value.toDoubleOrNull() ?: return
        val pFillRaw = _fillPressure.value.toDoubleOrNull() ?: return
        val pReliefRaw = _reliefPressure.value.toDoubleOrNull() ?: return
        val glycol = _glycolPercent.value

        // Convert temperatures to Celsius for Specific Volume calculations
        val t1C = if (_isImperial.value) (t1Raw - 32.0) / 1.8 else t1Raw
        val t2C = if (_isImperial.value) (t2Raw - 32.0) / 1.8 else t2Raw

        // Convert pressures to absolute psig (psi absolute)
        val pFillAbs = if (_isImperial.value) {
            pFillRaw + 14.7
        } else {
            // kPa to psi absolute: (kPa * 0.145038) + 14.7
            (pFillRaw * 0.145038) + 14.7
        }

        val pReliefAbs = if (_isImperial.value) {
            pReliefRaw + 14.7
        } else {
            (pReliefRaw * 0.145038) + 14.7
        }

        // Standard safety margin: Max operating pressure is 10% below relief setting OR 5 psi below (whichever is more conservative)
        // Let's use standard ASME rule: P_max = Relief Valve Pressure - 5 psi (or -34 kPa metric)
        val pMaxAbs = (pReliefAbs - 5.0).coerceAtLeast(pFillAbs + 2.0)

        try {
            // Water specific volumes in ml/g (L/kg)
            val v1 = waterSpecificVolume(t1C)
            val v2 = waterSpecificVolume(t2C)

            // Pure water net expansion
            var eps = (v2 / v1) - 1.0

            // Adjust for glycol (glycol increases density expansion factor)
            // 30% Glycol adds ~15% to expansion, 50% Glycol adds ~30%
            eps *= (1.0 + 0.006 * glycol)

            // ASME Diaphragm/Bladder sizing formula:
            // Vt = Vs * eps / (1 - P_fill_abs / P_max_abs)
            val pressureFactor = 1.0 - (pFillAbs / pMaxAbs)
            val requiredVol = (vSystem * eps) / pressureFactor.coerceAtLeast(0.01)

            _expansionFactor.value = eps
            _requiredVolume.value = requiredVol
            _acceptanceVolume.value = vSystem * eps

        } catch (_: Exception) {}
    }

    private fun waterSpecificVolume(tC: Double): Double {
        // Density of water polynomial
        val t = tC.coerceIn(0.0, 120.0)
        val density = 1.0 - ((t - 3.98).pow(2) / 508929.2) * ((t + 288.94) / (t + 68.12))
        return 1.0 / density
    }

    fun logActivity() {
        viewModelScope.launch {
            val isImp = _isImperial.value
            val volUnit = if (isImp) "gal" else "L"
            val tempUnit = if (isImp) "F" else "C"
            val pressUnit = if (isImp) "psig" else "kPa"

            val sysVol = _systemVolume.value
            val fT = _fillTemp.value
            val dT = _designTemp.value
            val fp = _fillPressure.value
            val rp = _reliefPressure.value
            val gly = "%.0f".format(_glycolPercent.value)
            val req = "%.1f".format(_requiredVolume.value)

            toolLogRepository.logToolActivity(
                toolType = "EXPANSION_TANK",
                title = "Boiler Expansion Sizing",
                summary = "System: $sysVol $volUnit | Temps: $fT°$tempUnit to $dT°$tempUnit | Fill: $fp $pressUnit | Relief: $rp $pressUnit | Glycol: $gly% | Req Tank Size: $req $volUnit",
                value = _requiredVolume.value
            )
        }
    }
}
