package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.ln

data class WeatherStationData(
    val locationName: String = "Main Jobsite Yard",
    val tempC: Double = 24.5,
    val humidityPercent: Int = 62,
    val pressureHpa: Double = 1013.2,
    val windKmH: Double = 12.0,
    val windDirection: String = "ENE",
    val aqiIndex: Int = 42,
    val conditionText: String = "Partly Cloudy"
) {
    val tempF: Double get() = tempC * 1.8 + 32.0
    val pressureInHg: Double get() = pressureHpa * 0.02953
    val windMph: Double get() = windKmH * 0.621371

    // Magnus formula approximation for dew point °C
    val dewPointC: Double
        get() {
            val a = 17.27
            val b = 237.7
            val alpha = ((a * tempC) / (b + tempC)) + ln(humidityPercent.toDouble() / 100.0)
            return (b * alpha) / (a - alpha)
        }

    val dewPointF: Double get() = dewPointC * 1.8 + 32.0

    // Concrete Evaporation Rate (Nomograph formula approximation lb/ft²/hr)
    val concreteEvaporationRate: Double
        get() {
            val v = windMph
            val r = humidityPercent
            val tc = tempC
            val rate = (tc * tc * 0.0001) + (v * 0.01) - (r * 0.001)
            return rate.coerceAtLeast(0.01)
        }

    val aqiStatus: String
        get() = when {
            aqiIndex <= 50 -> "Good (Clean Air)"
            aqiIndex <= 100 -> "Moderate"
            aqiIndex <= 150 -> "Unhealthy for Sensitive Groups"
            else -> "Unhealthy Dust/Smog"
        }
}

class MeteorologyViewModel : ViewModel() {

    private val _weatherState = MutableStateFlow(WeatherStationData())
    val weatherState: StateFlow<WeatherStationData> = _weatherState.asStateFlow()

    private val _selectedPresetIndex = MutableStateFlow(0)
    val selectedPresetIndex: StateFlow<Int> = _selectedPresetIndex.asStateFlow()

    private val presets = listOf(
        WeatherStationData("Main Jobsite Yard", 24.5, 62, 1013.2, 12.0, "ENE", 42, "Partly Cloudy"),
        WeatherStationData("Coastal Pier Project", 19.0, 85, 1008.5, 28.0, "SW", 25, "Breezy & Humid"),
        WeatherStationData("Quarry Field Station", 32.0, 28, 1016.0, 8.0, "N", 88, "Hot & Dry Dust"),
        WeatherStationData("High Altitude Timber Yard", 12.5, 45, 985.0, 18.0, "WNW", 15, "Cool & Crisp")
    )

    fun selectPreset(index: Int) {
        if (index in presets.indices) {
            _selectedPresetIndex.value = index
            _weatherState.value = presets[index]
        }
    }

    fun updateTemperature(tempC: Double) {
        _weatherState.value = _weatherState.value.copy(tempC = tempC)
    }

    fun updateHumidity(humidity: Int) {
        _weatherState.value = _weatherState.value.copy(humidityPercent = humidity.coerceIn(5, 100))
    }

    fun updateWindSpeed(windKmH: Double) {
        _weatherState.value = _weatherState.value.copy(windKmH = windKmH.coerceAtLeast(0.0))
    }
}
