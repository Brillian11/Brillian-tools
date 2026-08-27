package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val isLightMode: Boolean = true,
    val highContrastOutdoor: Boolean = true,
    val unitSystem: String = "Imperial", // Imperial or Metric
    val weatherProvider: String = "Open-Meteo Site Live",
    val measurementPrecision: String = "1/64\"",
    val magneticDeclination: Float = 0.0f,
    val useGpsLocation: Boolean = true,
    val locationName: String = "Jobsite Yard A",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val altitudeMeters: Double = 45.2,
    val isFirstLaunch: Boolean = true,
    val workerProfile: String = "General",
    val laborCostPerHour: Double = 25.0,
    val currencyCode: String = "USD"
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_user_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            isLightMode = prefs.getBoolean("is_light_mode", true),
            highContrastOutdoor = prefs.getBoolean("high_contrast_outdoor", true),
            unitSystem = prefs.getString("unit_system", "Imperial") ?: "Imperial",
            weatherProvider = prefs.getString("weather_provider", "Open-Meteo Site Live") ?: "Open-Meteo Site Live",
            measurementPrecision = prefs.getString("measurement_precision", "1/64\"") ?: "1/64\"",
            magneticDeclination = prefs.getFloat("magnetic_declination", 0.0f),
            useGpsLocation = prefs.getBoolean("use_gps_location", true),
            locationName = prefs.getString("location_name", "Jobsite Yard A") ?: "Jobsite Yard A",
            latitude = prefs.getFloat("latitude", 37.7749f).toDouble(),
            longitude = prefs.getFloat("longitude", -122.4194f).toDouble(),
            altitudeMeters = prefs.getFloat("altitude_meters", 45.2f).toDouble(),
            isFirstLaunch = prefs.getBoolean("is_first_launch", true),
            workerProfile = prefs.getString("worker_profile", "General") ?: "General",
            laborCostPerHour = prefs.getFloat("labor_cost_per_hour", 25.0f).toDouble(),
            currencyCode = prefs.getString("currency_code", "USD") ?: "USD"
        )
    }

    fun completeOnboarding(profile: String, units: String) {
        prefs.edit()
            .putBoolean("is_first_launch", false)
            .putString("worker_profile", profile)
            .putString("unit_system", units)
            .apply()
        _settings.value = _settings.value.copy(
            isFirstLaunch = false,
            workerProfile = profile,
            unitSystem = units
        )
    }

    fun updateLightMode(isLight: Boolean) {
        prefs.edit().putBoolean("is_light_mode", isLight).apply()
        _settings.value = _settings.value.copy(isLightMode = isLight)
    }

    fun updateHighContrastOutdoor(enabled: Boolean) {
        prefs.edit().putBoolean("high_contrast_outdoor", enabled).apply()
        _settings.value = _settings.value.copy(highContrastOutdoor = enabled)
    }

    fun updateUnitSystem(system: String) {
        prefs.edit().putString("unit_system", system).apply()
        _settings.value = _settings.value.copy(unitSystem = system)
    }

    fun updateWeatherProvider(provider: String) {
        prefs.edit().putString("weather_provider", provider).apply()
        _settings.value = _settings.value.copy(weatherProvider = provider)
    }

    fun updateMeasurementPrecision(precision: String) {
        prefs.edit().putString("measurement_precision", precision).apply()
        _settings.value = _settings.value.copy(measurementPrecision = precision)
    }

    fun updateMagneticDeclination(declination: Float) {
        prefs.edit().putFloat("magnetic_declination", declination).apply()
        _settings.value = _settings.value.copy(magneticDeclination = declination)
    }

    fun updateUseGpsLocation(useGps: Boolean) {
        prefs.edit().putBoolean("use_gps_location", useGps).apply()
        _settings.value = _settings.value.copy(useGpsLocation = useGps)
    }

    fun updateLocationName(name: String) {
        prefs.edit().putString("location_name", name).apply()
        _settings.value = _settings.value.copy(locationName = name)
    }

    fun updateGpsCoordinates(lat: Double, lng: Double, alt: Double) {
        prefs.edit()
            .putFloat("latitude", lat.toFloat())
            .putFloat("longitude", lng.toFloat())
            .putFloat("altitude_meters", alt.toFloat())
            .apply()
        _settings.value = _settings.value.copy(latitude = lat, longitude = lng, altitudeMeters = alt)
    }

    fun updateLaborCost(cost: Double) {
        prefs.edit().putFloat("labor_cost_per_hour", cost.toFloat()).apply()
        _settings.value = _settings.value.copy(laborCostPerHour = cost)
    }

    fun updateCurrencyCode(code: String) {
        prefs.edit().putString("currency_code", code).apply()
        _settings.value = _settings.value.copy(currencyCode = code)
    }
}
