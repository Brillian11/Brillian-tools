package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UserSettings
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settings

    fun setLightMode(enabled: Boolean) {
        settingsRepository.updateLightMode(enabled)
    }

    fun setHighContrastOutdoor(enabled: Boolean) {
        settingsRepository.updateHighContrastOutdoor(enabled)
    }

    fun setUnitSystem(system: String) {
        settingsRepository.updateUnitSystem(system)
    }

    fun setWeatherProvider(provider: String) {
        settingsRepository.updateWeatherProvider(provider)
    }

    fun setMeasurementPrecision(precision: String) {
        settingsRepository.updateMeasurementPrecision(precision)
    }

    fun setMagneticDeclination(declination: Float) {
        settingsRepository.updateMagneticDeclination(declination)
    }

    fun setUseGpsLocation(enabled: Boolean) {
        settingsRepository.updateUseGpsLocation(enabled)
    }

    fun setLocationName(name: String) {
        settingsRepository.updateLocationName(name)
    }

    fun setGpsCoordinates(lat: Double, lng: Double, alt: Double) {
        settingsRepository.updateGpsCoordinates(lat, lng, alt)
    }

    fun setLaborCost(cost: Double) {
        settingsRepository.updateLaborCost(cost)
    }

    fun setCurrencyCode(code: String) {
        settingsRepository.updateCurrencyCode(code)
    }

    fun updateAiEnabled(enabled: Boolean) {
        settingsRepository.updateAiEnabled(enabled)
    }

    fun updateAiProvider(provider: String) {
        settingsRepository.updateAiProvider(provider)
    }

    fun updateAiApiKey(apiKey: String) {
        settingsRepository.updateAiApiKey(apiKey)
    }

    fun updateAiModel(model: String) {
        settingsRepository.updateAiModel(model)
    }

    fun setLanguageCode(code: String) {
        settingsRepository.updateLanguageCode(code)
    }
}
