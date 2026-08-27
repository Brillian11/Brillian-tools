package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.sensor.AudioDecibelData
import com.example.domain.sensor.AudioDecibelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DecibelMeterViewModel : ViewModel() {

    private val audioManager = AudioDecibelManager()

    private val _dbData = MutableStateFlow(AudioDecibelData(35f, 35f, false))
    val dbData: StateFlow<AudioDecibelData> = _dbData.asStateFlow()

    init {
        viewModelScope.launch {
            audioManager.getDecibelFlow().collect { data ->
                _dbData.value = data
            }
        }
    }
}
