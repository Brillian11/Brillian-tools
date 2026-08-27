package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class RefrigerantViewModel(private val toolLogRepository: ToolLogRepository) : ViewModel() {

    private val _selectedRefrigerant = MutableStateFlow("R410A") // R410A, R134a, R32, R22
    val selectedRefrigerant: StateFlow<String> = _selectedRefrigerant.asStateFlow()

    private val _pressureInput = MutableStateFlow("118.0") // in psig by default for R410A
    val pressureInput: StateFlow<String> = _pressureInput.asStateFlow()

    private val _isPressureImperial = MutableStateFlow(true) // true for psig, false for kPa
    val isPressureImperial: StateFlow<Boolean> = _isPressureImperial.asStateFlow()

    private val _lineTempInput = MutableStateFlow("55.0") // °F or °C
    val lineTempInput: StateFlow<String> = _lineTempInput.asStateFlow()

    private val _isTempImperial = MutableStateFlow(true) // true for °F, false for °C
    val isTempImperial: StateFlow<Boolean> = _isTempImperial.asStateFlow()

    private val _mode = MutableStateFlow("Superheat") // "Superheat" or "Subcooling"
    val mode: StateFlow<String> = _mode.asStateFlow()

    // Calculated States
    private val _saturationTemp = MutableStateFlow(40.0) // °F or °C matching setting
    val saturationTemp: StateFlow<Double> = _saturationTemp.asStateFlow()

    private val _targetValue = MutableStateFlow(15.0) // Superheat or Subcooling value
    val targetValue: StateFlow<Double> = _targetValue.asStateFlow()

    fun selectRefrigerant(refrigerant: String) {
        _selectedRefrigerant.value = refrigerant
        // Set typical pressures for each refrigerant to provide a realistic default
        _pressureInput.value = when (refrigerant) {
            "R410A" -> if (_isPressureImperial.value) "118.0" else "815.0"
            "R134a" -> if (_isPressureImperial.value) "35.0" else "240.0"
            "R32" -> if (_isPressureImperial.value) "122.0" else "840.0"
            "R22" -> if (_isPressureImperial.value) "68.0" else "470.0"
            else -> "100.0"
        }
        calculate()
    }

    fun setPressureInput(value: String) {
        _pressureInput.value = value
        calculate()
    }

    fun setLineTempInput(value: String) {
        _lineTempInput.value = value
        calculate()
    }

    fun setMode(value: String) {
        _mode.value = value
        calculate()
    }

    fun togglePressureUnits() {
        val current = _isPressureImperial.value
        _isPressureImperial.value = !current
        val press = _pressureInput.value.toDoubleOrNull() ?: return
        if (current) {
            // psig to kPa gauge (kPa_gauge = psig * 6.89476)
            _pressureInput.value = "%.1f".format(press * 6.89476)
        } else {
            // kPa gauge to psig (psig = kPa / 6.89476)
            _pressureInput.value = "%.1f".format(press / 6.89476)
        }
        calculate()
    }

    fun toggleTempUnits() {
        val current = _isTempImperial.value
        _isTempImperial.value = !current
        val temp = _lineTempInput.value.toDoubleOrNull() ?: return
        if (current) {
            // F to C
            _lineTempInput.value = "%.1f".format((temp - 32.0) / 1.8)
        } else {
            // C to F
            _lineTempInput.value = "%.1f".format(temp * 1.8 + 32.0)
        }
        calculate()
    }

    fun calculate() {
        val pRaw = _pressureInput.value.toDoubleOrNull() ?: return
        val tLineRaw = _lineTempInput.value.toDoubleOrNull() ?: return

        // Convert pressure to absolute kPa for saturation formulas
        val pAbsKpa = if (_isPressureImperial.value) {
            // psig to kPa gauge, then add atmospheric 101.325 kPa
            (pRaw * 6.89476) + 101.325
        } else {
            // kPa gauge to kPa absolute
            pRaw + 101.325
        }

        val tSatC = getSatTempC(_selectedRefrigerant.value, pAbsKpa)

        // Line Temp in C
        val tLineC = if (_isTempImperial.value) {
            (tLineRaw - 32.0) / 1.8
        } else {
            tLineRaw
        }

        // Saturation Temp in matching display unit
        val tSatDisplay = if (_isTempImperial.value) {
            tSatC * 1.8 + 32.0
        } else {
            tSatC
        }

        // Superheat = Line Temp - Saturation Temp (suction side)
        // Subcooling = Saturation Temp - Line Temp (liquid side)
        val targetVal = if (_mode.value == "Superheat") {
            tLineRaw - tSatDisplay
        } else {
            tSatDisplay - tLineRaw
        }

        _saturationTemp.value = tSatDisplay
        _targetValue.value = targetVal
    }

    // Antoine Saturation Temperature calculation
    private fun getSatTempC(refrigerant: String, pAbsKpa: Double): Double {
        val p = pAbsKpa.coerceAtLeast(1.0)
        val tKelvin = when (refrigerant) {
            "R134a" -> 2430.2 / (14.3686 - ln(p)) + 47.15
            "R22" -> 2173.5 / (14.186 - ln(p)) + 34.1
            "R410A" -> 2127.3 / (14.453 - ln(p)) + 45.4
            "R32" -> 2084.1 / (14.281 - ln(p)) + 49.3
            else -> 2430.2 / (14.3686 - ln(p)) + 47.15
        }
        return tKelvin - 273.15
    }

    fun logActivity() {
        viewModelScope.launch {
            val isTempImp = _isTempImperial.value
            val unitStr = if (isTempImp) "F" else "C"
            val modeStr = _mode.value
            val pUnit = if (_isPressureImperial.value) "psig" else "kPa"
            val pVal = _pressureInput.value
            val tSat = "%.1f".format(_saturationTemp.value)
            val tLine = _lineTempInput.value
            val finalVal = "%.1f".format(_targetValue.value)

            toolLogRepository.logToolActivity(
                toolType = "REFRIGERANT",
                title = "Refrigerant Saturation Calculation",
                summary = "${_selectedRefrigerant.value} | Press: $pVal $pUnit | Sat Temp: $tSat°$unitStr | Line Temp: $tLine°$unitStr | Calculated $modeStr: $finalVal°$unitStr",
                value = _targetValue.value
            )
        }
    }
}
