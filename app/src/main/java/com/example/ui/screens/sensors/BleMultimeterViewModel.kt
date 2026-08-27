package com.example.ui.screens.sensors

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class MeterFunction(val label: String, val unit: String, val isArcFlashHazard: Boolean) {
    VOLTAGE_AC("AC Voltage", "V AC", true),
    VOLTAGE_DC("DC Voltage", "V DC", true),
    CURRENT_AC("AC Current (Clamp)", "A AC", true),
    CURRENT_DC("DC Current", "A DC", false),
    RESISTANCE("Resistance", "Ω", false),
    CONTINUITY("Continuity", "Beep / Ω", false),
    CAPACITANCE("Capacitance", "μF", false),
    FREQUENCY("Frequency", "Hz", false)
}

data class MeterReading(
    val value: Float,
    val unit: String,
    val function: MeterFunction,
    val isAutoRanging: Boolean = true,
    val isHoldActive: Boolean = false,
    val minVal: Float,
    val maxVal: Float,
    val avgVal: Float
)

class BleMultimeterViewModel(
    application: Application,
    private val toolLogRepository: ToolLogRepository? = null
) : AndroidViewModel(application) {

    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _deviceName = MutableStateFlow("Fluke 376 FC Clamp / BLE")
    val deviceName: StateFlow<String> = _deviceName.asStateFlow()

    private val _function = MutableStateFlow(MeterFunction.VOLTAGE_AC)
    val function: StateFlow<MeterFunction> = _function.asStateFlow()

    private val _isHold = MutableStateFlow(false)
    val isHold: StateFlow<Boolean> = _isHold.asStateFlow()

    private val _reading = MutableStateFlow(
        MeterReading(
            value = 120.4f,
            unit = "V AC",
            function = MeterFunction.VOLTAGE_AC,
            minVal = 119.8f,
            maxVal = 121.2f,
            avgVal = 120.3f
        )
    )
    val reading: StateFlow<MeterReading> = _reading.asStateFlow()

    private val _history = MutableStateFlow<List<Float>>(emptyList())
    val history: StateFlow<List<Float>> = _history.asStateFlow()

    private val _alarmThreshold = MutableStateFlow(240f)
    val alarmThreshold: StateFlow<Float> = _alarmThreshold.asStateFlow()

    private val _isAlarmTriggered = MutableStateFlow(false)
    val isAlarmTriggered: StateFlow<Boolean> = _isAlarmTriggered.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    init {
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            var step = 0f
            val historyList = ArrayList<Float>()
            var minV = Float.MAX_VALUE
            var maxV = Float.MIN_VALUE
            var sumV = 0.0
            var count = 0

            while (true) {
                kotlinx.coroutines.delay(100)
                if (!_isHold.value && _isConnected.value) {
                    step += 0.15f
                    val currentFunc = _function.value
                    val rawVal = when (currentFunc) {
                        MeterFunction.VOLTAGE_AC -> 120.2f + sin(step) * 1.5f + (sin(step * 3f) * 0.4f).toFloat()
                        MeterFunction.VOLTAGE_DC -> 24.05f + sin(step * 0.5f) * 0.2f
                        MeterFunction.CURRENT_AC -> 14.8f + sin(step * 0.8f) * 1.2f
                        MeterFunction.CURRENT_DC -> 3.2f + sin(step) * 0.1f
                        MeterFunction.RESISTANCE -> 470.2f + sin(step * 0.3f) * 2.0f
                        MeterFunction.CONTINUITY -> if (sin(step) > 0) 0.3f else 999.0f
                        MeterFunction.CAPACITANCE -> 47.5f + sin(step * 0.2f) * 0.5f
                        MeterFunction.FREQUENCY -> 60.02f + sin(step * 0.4f) * 0.05f
                    }

                    if (rawVal < minV) minV = rawVal
                    if (rawVal > maxV) maxV = rawVal
                    sumV += rawVal
                    count++
                    val avgV = (sumV / count).toFloat()

                    historyList.add(rawVal)
                    if (historyList.size > 50) historyList.removeAt(0)
                    _history.value = ArrayList(historyList)

                    _reading.value = MeterReading(
                        value = rawVal,
                        unit = currentFunc.unit,
                        function = currentFunc,
                        isHoldActive = _isHold.value,
                        minVal = minV,
                        maxVal = maxV,
                        avgVal = avgV
                    )

                    _isAlarmTriggered.value = rawVal >= _alarmThreshold.value
                }
            }
        }
    }

    fun setFunction(newFunc: MeterFunction) {
        _function.value = newFunc
        _history.value = emptyList()
        when (newFunc) {
            MeterFunction.VOLTAGE_AC -> _alarmThreshold.value = 240f
            MeterFunction.VOLTAGE_DC -> _alarmThreshold.value = 50f
            MeterFunction.CURRENT_AC -> _alarmThreshold.value = 20f
            MeterFunction.CURRENT_DC -> _alarmThreshold.value = 10f
            MeterFunction.RESISTANCE -> _alarmThreshold.value = 1000f
            MeterFunction.CONTINUITY -> _alarmThreshold.value = 50f
            MeterFunction.CAPACITANCE -> _alarmThreshold.value = 100f
            MeterFunction.FREQUENCY -> _alarmThreshold.value = 65f
        }
    }

    fun toggleHold() {
        _isHold.value = !_isHold.value
    }

    fun setAlarmThreshold(thresh: Float) {
        _alarmThreshold.value = thresh
    }

    fun toggleConnection() {
        _isConnected.value = !_isConnected.value
    }

    fun saveMultimeterLog(circuitNote: String = "Panel A Feeder Breaker") {
        viewModelScope.launch {
            val r = _reading.value
            toolLogRepository?.logToolActivity(
                toolType = "widget_ble_multimeter",
                title = "BLE Meter: ${r.function.label} ($circuitNote)",
                summary = "Live: ${String.format("%.2f %s", r.value, r.unit)}, Min/Max/Avg: ${String.format("%.2f/%.2f/%.2f", r.minVal, r.maxVal, r.avgVal)}, Safe Distance: ${if (r.function.isArcFlashHazard) "30+ ft Wireless Range" else "Standard Safe"}, Alarm: ${if (_isAlarmTriggered.value) "THRESHOLD EXCEEDED" else "NORMAL"}",
                value = r.value.toDouble()
            )
            _lastLogSaved.value = true
        }
    }
}
