package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class PsychrometricViewModel(private val toolLogRepository: ToolLogRepository) : ViewModel() {

    private val _dryBulbTemp = MutableStateFlow("24.0")
    val dryBulbTemp: StateFlow<String> = _dryBulbTemp.asStateFlow()

    private val _pressure = MutableStateFlow("101.325") // kPa
    val pressure: StateFlow<String> = _pressure.asStateFlow()

    private val _humidityInputType = MutableStateFlow("RH") // "RH", "WB", "DP"
    val humidityInputType: StateFlow<String> = _humidityInputType.asStateFlow()

    private val _humidityValue = MutableStateFlow("50.0") // %, °C, or °C
    val humidityValue: StateFlow<String> = _humidityValue.asStateFlow()

    // Output States
    private val _relativeHumidity = MutableStateFlow(50.0)
    val relativeHumidity: StateFlow<Double> = _relativeHumidity.asStateFlow()

    private val _wetBulbTemp = MutableStateFlow(17.0)
    val wetBulbTemp: StateFlow<Double> = _wetBulbTemp.asStateFlow()

    private val _dewPointTemp = MutableStateFlow(12.9)
    val dewPointTemp: StateFlow<Double> = _dewPointTemp.asStateFlow()

    private val _enthalpy = MutableStateFlow(57.1) // kJ/kg
    val enthalpy: StateFlow<Double> = _enthalpy.asStateFlow()

    private val _humidityRatio = MutableStateFlow(9.2) // g/kg
    val humidityRatio: StateFlow<Double> = _humidityRatio.asStateFlow()

    private val _isImperial = MutableStateFlow(false)
    val isImperial: StateFlow<Boolean> = _isImperial.asStateFlow()

    fun setDryBulb(value: String) {
        _dryBulbTemp.value = value
        calculate()
    }

    fun setPressure(value: String) {
        _pressure.value = value
        calculate()
    }

    fun setHumidityInputType(value: String) {
        _humidityInputType.value = value
        // Set default values matching typical ambient conditions
        _humidityValue.value = when (value) {
            "RH" -> "50.0"
            "WB" -> if (_isImperial.value) "62.0" else "17.0"
            "DP" -> if (_isImperial.value) "55.0" else "13.0"
            else -> "50.0"
        }
        calculate()
    }

    fun setHumidityValue(value: String) {
        _humidityValue.value = value
        calculate()
    }

    fun toggleUnits() {
        val currentImperial = _isImperial.value
        _isImperial.value = !currentImperial

        // Convert input numbers
        val db = _dryBulbTemp.value.toDoubleOrNull() ?: 24.0
        val p = _pressure.value.toDoubleOrNull() ?: 101.325
        val hVal = _humidityValue.value.toDoubleOrNull() ?: 50.0

        if (!currentImperial) {
            // C -> F, kPa -> inHg
            _dryBulbTemp.value = "%.1f".format(db * 1.8 + 32.0)
            _pressure.value = "%.2f".format(p * 0.2953) // kPa to inHg
            if (_humidityInputType.value != "RH") {
                _humidityValue.value = "%.1f".format(hVal * 1.8 + 32.0)
            }
        } else {
            // F -> C, inHg -> kPa
            _dryBulbTemp.value = "%.1f".format((db - 32.0) / 1.8)
            _pressure.value = "%.3f".format(p / 0.2953) // inHg to kPa
            if (_humidityInputType.value != "RH") {
                _humidityValue.value = "%.1f".format((hVal - 32.0) / 1.8)
            }
        }
        calculate()
    }

    fun calculate() {
        val dbInput = _dryBulbTemp.value.toDoubleOrNull() ?: return
        val pInput = _pressure.value.toDoubleOrNull() ?: 101.325
        val hInput = _humidityValue.value.toDoubleOrNull() ?: 50.0

        val tdb = if (_isImperial.value) (dbInput - 32.0) / 1.8 else dbInput
        val pKpa = if (_isImperial.value) pInput / 0.2953 else pInput
        val inputType = _humidityInputType.value

        var rh = 50.0
        var tdp = 13.0
        var twb = 17.0

        try {
            when (inputType) {
                "RH" -> {
                    rh = hInput.coerceIn(1.0, 100.0)
                    val pws = satVaporPressure(tdb)
                    val pw = pws * (rh / 100.0)
                    tdp = dewPointFromVaporPressure(pw)
                    twb = solveWetBulb(tdb, rh, pKpa)
                }
                "WB" -> {
                    val twbRaw = if (_isImperial.value) (hInput - 32.0) / 1.8 else hInput
                    twb = twbRaw.coerceAtMost(tdb)
                    val pws_twb = satVaporPressure(twb)
                    // Carrier's Equation for wet bulb
                    val pw = pws_twb - 0.000662 * pKpa * (tdb - twb)
                    val pws_tdb = satVaporPressure(tdb)
                    rh = ((pw / pws_tdb) * 100.0).coerceIn(0.1, 100.0)
                    tdp = dewPointFromVaporPressure(pw)
                }
                "DP" -> {
                    val tdpRaw = if (_isImperial.value) (hInput - 32.0) / 1.8 else hInput
                    tdp = tdpRaw.coerceAtMost(tdb)
                    val pw = satVaporPressure(tdp)
                    val pws_tdb = satVaporPressure(tdb)
                    rh = ((pw / pws_tdb) * 100.0).coerceIn(0.1, 100.0)
                    twb = solveWetBulb(tdb, rh, pKpa)
                }
            }

            // Calculate Humidity Ratio (W) (g of water vapor / kg of dry air)
            val pw = satVaporPressure(tdb) * (rh / 100.0)
            val w = 0.621945 * pw / (pKpa - pw) // kg/kg
            val wGPerKg = w * 1000.0

            // Calculate Enthalpy (h) in kJ/kg
            val h = 1.006 * tdb + w * (2501.0 + 1.86 * tdb)

            _relativeHumidity.value = rh
            _wetBulbTemp.value = twb
            _dewPointTemp.value = tdp
            _enthalpy.value = h
            _humidityRatio.value = wGPerKg

        } catch (_: Exception) {}
    }

    // Saturation vapor pressure in kPa via Arden Buck equation
    private fun satVaporPressure(t: Double): Double {
        return 0.61121 * exp((18.678 - t / 234.5) * (t / (257.14 + t)))
    }

    private fun dewPointFromVaporPressure(pw: Double): Double {
        // Inverse Arden Buck equation
        val a = 18.678
        val b = 257.14
        val c = 234.5
        val d = 0.61121
        val y = ln(pw / d)
        return (b * y) / (a - y)
    }

    private fun solveWetBulb(tdb: Double, rh: Double, pKpa: Double): Double {
        val pw = satVaporPressure(tdb) * (rh / 100.0)
        var low = -40.0
        var high = tdb
        var twb = (low + high) / 2.0
        for (i in 0..40) {
            val pws_twb = satVaporPressure(twb)
            val pw_calc = pws_twb - 0.000662 * pKpa * (tdb - twb)
            if (pw_calc > pw) {
                high = twb
            } else {
                low = twb
            }
            twb = (low + high) / 2.0
        }
        return twb
    }

    fun logActivity() {
        viewModelScope.launch {
            val unitStr = if (_isImperial.value) "F" else "C"
            val db = _dryBulbTemp.value
            val rh = "%.1f".format(_relativeHumidity.value)
            val dp = "%.1f".format(if (_isImperial.value) _dewPointTemp.value * 1.8 + 32.0 else _dewPointTemp.value)
            val wb = "%.1f".format(if (_isImperial.value) _wetBulbTemp.value * 1.8 + 32.0 else _wetBulbTemp.value)
            val ent = "%.1f".format(if (_isImperial.value) _enthalpy.value * 0.4299 else _enthalpy.value) // kJ/kg to BTU/lb

            toolLogRepository.logToolActivity(
                toolType = "PSYCHROMETRIC",
                title = "Psychrometric Reading",
                summary = "Dry Bulb: $db°$unitStr | RH: $rh% | Dew Point: $dp°$unitStr | Wet Bulb: $wb°$unitStr | Enthalpy: $ent ${if (_isImperial.value) "BTU/lb" else "kJ/kg"}",
                value = _relativeHumidity.value
            )
        }
    }
}
