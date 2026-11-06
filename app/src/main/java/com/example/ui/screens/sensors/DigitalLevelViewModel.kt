package com.example.ui.screens.sensors

import android.app.Application
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.sensor.InclinometerData
import com.example.domain.sensor.InclinometerSensorManager
import com.example.domain.sensor.SensorSoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DigitalLevelViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = InclinometerSensorManager(application)
    private val soundManager = SensorSoundManager(application)

    private val _sensorData = MutableStateFlow(
        InclinometerData(0f, 0f, 0f, 0f, isLevel = true, isTared = false)
    )
    val sensorData: StateFlow<InclinometerData> = _sensorData.asStateFlow()

    private val _isHold = MutableStateFlow(false)
    val isHold: StateFlow<Boolean> = _isHold.asStateFlow()

    private val _isBuzzerEnabled = MutableStateFlow(true)
    val isBuzzerEnabled: StateFlow<Boolean> = _isBuzzerEnabled.asStateFlow()

    private var lastLevelBeepTime = 0L

    init {
        viewModelScope.launch {
            sensorManager.getSensorFlow().collect { data ->
                if (!_isHold.value) {
                    _sensorData.value = data

                    // Level confirmation tone
                    if (data.isLevel && _isBuzzerEnabled.value) {
                        val now = System.currentTimeMillis()
                        if (now - lastLevelBeepTime > 350L) {
                            lastLevelBeepTime = now
                            soundManager.playBeep(ToneGenerator.TONE_PROP_ACK, 60, 1500)
                            soundManager.vibrate(25)
                        }
                    }
                }
            }
        }
    }

    fun setTare() {
        sensorManager.setTare()
        if (_isBuzzerEnabled.value) {
            soundManager.playBeep(ToneGenerator.TONE_PROP_BEEP, 40, 1100)
        }
    }

    fun resetTare() {
        sensorManager.resetTare()
        if (_isBuzzerEnabled.value) {
            soundManager.playBeep(ToneGenerator.TONE_PROP_BEEP, 40, 900)
        }
    }

    fun toggleHold() {
        _isHold.value = !_isHold.value
    }

    fun toggleBuzzer() {
        val next = !_isBuzzerEnabled.value
        _isBuzzerEnabled.value = next
        if (next) {
            soundManager.playBeep(ToneGenerator.TONE_PROP_BEEP2, 50, 1600)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
