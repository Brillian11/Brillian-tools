package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class LaserMeasurementLog(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val distanceMeters: Double,
    val distanceFeetInches: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

data class LaserMeasureUiState(
    val isConnectedBLE: Boolean = false,
    val deviceName: String = "Bosch GLM 50 C Laser",
    val liveDistanceMeters: Double = 3.842,
    val logs: List<LaserMeasurementLog> = listOf(
        LaserMeasurementLog(label = "Living Room Length W1", distanceMeters = 5.240, distanceFeetInches = "17' 2-5/16\""),
        LaserMeasurementLog(label = "Living Room Width L1", distanceMeters = 3.842, distanceFeetInches = "12' 7-5/16\""),
        LaserMeasurementLog(label = "Ceiling Height H1", distanceMeters = 2.850, distanceFeetInches = "9' 4-3/16\"")
    ),
    
    // Room Area & Volume Calculations
    val calculatedRoomAreaSqM: Double = 20.13,
    val calculatedRoomVolumeCuM: Double = 57.38
)

class LaserMeasureViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaserMeasureUiState())
    val uiState: StateFlow<LaserMeasureUiState> = _uiState.asStateFlow()

    init {
        recalculateRoom()
    }

    fun toggleBLEConnection() {
        _uiState.value = _uiState.value.copy(isConnectedBLE = !_uiState.value.isConnectedBLE)
    }

    fun setLiveDistance(dist: Double) {
        _uiState.value = _uiState.value.copy(liveDistanceMeters = dist.coerceAtLeast(0.01))
    }

    fun addMeasurement(label: String) {
        val dist = _uiState.value.liveDistanceMeters
        val feet = (dist * 3.28084).toInt()
        val remainingInches = (dist * 3.28084 - feet) * 12.0
        val ftInStr = "${feet}' ${String.format("%.1f", remainingInches)}\""

        val newLog = LaserMeasurementLog(
            label = label.ifBlank { "Measure #${_uiState.value.logs.size + 1}" },
            distanceMeters = dist,
            distanceFeetInches = ftInStr
        )

        _uiState.value = _uiState.value.copy(logs = listOf(newLog) + _uiState.value.logs)
        recalculateRoom()

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "LASER",
                title = "Laser Distance Measure",
                summary = "${newLog.label}: ${dist}m ($ftInStr)",
                value = dist
            )
        }
    }

    fun removeMeasurement(id: String) {
        _uiState.value = _uiState.value.copy(logs = _uiState.value.logs.filterNot { it.id == id })
        recalculateRoom()
    }

    private fun recalculateRoom() {
        val logs = _uiState.value.logs
        if (logs.size >= 2) {
            val w = logs[0].distanceMeters
            val l = logs[1].distanceMeters
            val h = if (logs.size >= 3) logs[2].distanceMeters else 2.8

            val area = w * l
            val vol = area * h

            _uiState.value = _uiState.value.copy(
                calculatedRoomAreaSqM = area,
                calculatedRoomVolumeCuM = vol
            )
        }
    }
}
