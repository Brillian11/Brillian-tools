package com.example.ui.screens.sensors

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.sensor.InclinometerData
import com.example.domain.sensor.InclinometerSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DigitalLevelViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = InclinometerSensorManager(application)

    private val _sensorData = MutableStateFlow(
        InclinometerData(0f, 0f, 0f, 0f, isLevel = true, isTared = false)
    )
    val sensorData: StateFlow<InclinometerData> = _sensorData.asStateFlow()

    private val _isHold = MutableStateFlow(false)
    val isHold: StateFlow<Boolean> = _isHold.asStateFlow()

    init {
        viewModelScope.launch {
            sensorManager.getSensorFlow().collect { data ->
                if (!_isHold.value) {
                    _sensorData.value = data
                }
            }
        }
    }

    fun setTare() {
        sensorManager.setTare()
    }

    fun resetTare() {
        sensorManager.resetTare()
    }

    fun toggleHold() {
        _isHold.value = !_isHold.value
    }
}
