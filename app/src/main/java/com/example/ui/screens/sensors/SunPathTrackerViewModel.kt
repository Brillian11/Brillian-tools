package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class SunPosition(
    val elevationDeg: Float,
    val azimuthDeg: Float,
    val isAboveHorizon: Boolean,
    val solarNoonElevationDeg: Float,
    val sunriseHour: Float,
    val sunsetHour: Float,
    val dayLengthHours: Float
)

data class SolarArcPoint(
    val hour: Float,
    val elevationDeg: Float,
    val azimuthDeg: Float
)

class SunPathTrackerViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _latitude = MutableStateFlow(37.77f) // Default San Francisco / Mid-latitude
    val latitude: StateFlow<Float> = _latitude.asStateFlow()

    private val _dayOfYear = MutableStateFlow(172) // Summer Solstice approx June 21
    val dayOfYear: StateFlow<Int> = _dayOfYear.asStateFlow()

    private val _solarHour = MutableStateFlow(12.0f) // Solar Noon
    val solarHour: StateFlow<Float> = _solarHour.asStateFlow()

    private val _sunPosition = MutableStateFlow(calculateSunPosition(37.77f, 172, 12.0f))
    val sunPosition: StateFlow<SunPosition> = _sunPosition.asStateFlow()

    private val _currentDayArc = MutableStateFlow<List<SolarArcPoint>>(emptyList())
    val currentDayArc: StateFlow<List<SolarArcPoint>> = _currentDayArc.asStateFlow()

    private val _summerSolsticeArc = MutableStateFlow<List<SolarArcPoint>>(emptyList())
    val summerSolsticeArc: StateFlow<List<SolarArcPoint>> = _summerSolsticeArc.asStateFlow()

    private val _winterSolsticeArc = MutableStateFlow<List<SolarArcPoint>>(emptyList())
    val winterSolsticeArc: StateFlow<List<SolarArcPoint>> = _winterSolsticeArc.asStateFlow()

    // Glazing & Overhang & Shadow inputs
    private val _windowHeightM = MutableStateFlow(1.5f)
    val windowHeightM: StateFlow<Float> = _windowHeightM.asStateFlow()

    private val _obstacleHeightM = MutableStateFlow(6.0f)
    val obstacleHeightM: StateFlow<Float> = _obstacleHeightM.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    init {
        recalculate()
    }

    fun setLatitude(lat: Float) {
        _latitude.value = lat.coerceIn(-90f, 90f)
        recalculate()
    }

    fun setDayOfYear(day: Int) {
        _dayOfYear.value = day.coerceIn(1, 365)
        recalculate()
    }

    fun setSolarHour(hour: Float) {
        _solarHour.value = hour.coerceIn(0f, 24f)
        recalculate()
    }

    fun setWindowHeight(h: Float) {
        _windowHeightM.value = h.coerceIn(0.5f, 10f)
    }

    fun setObstacleHeight(h: Float) {
        _obstacleHeightM.value = h.coerceIn(0.5f, 50f)
    }

    private fun recalculate() {
        val lat = _latitude.value
        val day = _dayOfYear.value
        val hour = _solarHour.value

        _sunPosition.value = calculateSunPosition(lat, day, hour)
        _currentDayArc.value = computeDayArc(lat, day)
        _summerSolsticeArc.value = computeDayArc(lat, 172)
        _winterSolsticeArc.value = computeDayArc(lat, 355)
    }

    private fun computeDayArc(lat: Float, day: Int): List<SolarArcPoint> {
        val points = ArrayList<SolarArcPoint>()
        var h = 4.0f
        while (h <= 20.0f) {
            val pos = calculateSunPosition(lat, day, h)
            if (pos.elevationDeg > -5f) {
                points.add(SolarArcPoint(h, pos.elevationDeg, pos.azimuthDeg))
            }
            h += 0.5f
        }
        return points
    }

    private fun calculateSunPosition(latDeg: Float, day: Int, hour: Float): SunPosition {
        val latRad = latDeg * (PI.toFloat() / 180f)

        // Declination angle delta
        val declinationDeg = 23.45f * sin((360f / 365f * (day - 81)) * (PI.toFloat() / 180f))
        val declinationRad = declinationDeg * (PI.toFloat() / 180f)

        // Hour angle (15 deg per hour from solar noon 12:00)
        val hourAngleDeg = (hour - 12.0f) * 15.0f
        val hourAngleRad = hourAngleDeg * (PI.toFloat() / 180f)

        // Elevation angle alpha: sin(alpha) = sin(lat)*sin(dec) + cos(lat)*cos(dec)*cos(H)
        val sinElevation = sin(latRad) * sin(declinationRad) + cos(latRad) * cos(declinationRad) * cos(hourAngleRad)
        val elevationRad = asin(sinElevation.coerceIn(-1f, 1f))
        val elevationDeg = elevationRad * (180f / PI.toFloat())

        // Azimuth angle gamma
        val cosAzimuth = (sin(declinationRad) - sin(latRad) * sin(elevationRad)) / (cos(latRad) * cos(elevationRad)).coerceAtLeast(0.0001f)
        var azimuthRad = kotlin.math.acos(cosAzimuth.coerceIn(-1f, 1f))
        var azimuthDeg = azimuthRad * (180f / PI.toFloat())
        if (sin(hourAngleRad) > 0) {
            azimuthDeg = 360f - azimuthDeg
        }

        // Solar Noon peak elevation: 90 - lat + declination
        val solarNoonElev = (90f - kotlin.math.abs(latDeg) + declinationDeg).coerceIn(0f, 90f)

        // Sunrise/Sunset approximation
        val cosHourAngleRise = (-tan(latRad) * tan(declinationRad)).coerceIn(-1f, 1f)
        val sunriseHourAngleDeg = kotlin.math.acos(cosHourAngleRise) * (180f / PI.toFloat())
        val sunriseHour = 12.0f - (sunriseHourAngleDeg / 15.0f)
        val sunsetHour = 12.0f + (sunriseHourAngleDeg / 15.0f)
        val dayLength = (sunsetHour - sunriseHour).coerceAtLeast(0f)

        return SunPosition(
            elevationDeg = elevationDeg,
            azimuthDeg = azimuthDeg,
            isAboveHorizon = elevationDeg > 0f,
            solarNoonElevationDeg = solarNoonElev,
            sunriseHour = sunriseHour,
            sunsetHour = sunsetHour,
            dayLengthHours = dayLength
        )
    }

    // Solar PV Tilt Optimization
    fun getOptimalPvTiltDeg(): Float {
        val lat = kotlin.math.abs(_latitude.value)
        return (lat * 0.87f).coerceIn(10f, 60f)
    }

    // Overhang Eave Projection Sizer: P = H / tan(Summer Noon Elevation)
    fun getOptimalOverhangDepthM(): Float {
        val summerNoonElev = 90f - kotlin.math.abs(_latitude.value) + 23.45f
        val rad = (summerNoonElev * PI.toFloat() / 180f).coerceAtLeast(0.1f)
        return (_windowHeightM.value / tan(rad)).coerceIn(0.2f, 3.0f)
    }

    // Shadow Length: L = H / tan(Elevation)
    fun getObstacleShadowLengthM(): Float {
        val elev = _sunPosition.value.elevationDeg.coerceAtLeast(1.0f)
        val rad = elev * PI.toFloat() / 180f
        return (_obstacleHeightM.value / tan(rad)).coerceIn(0.1f, 500f)
    }

    fun saveSolarAuditLog(locationName: String = "South Facade Glazing Audit") {
        viewModelScope.launch {
            val pos = _sunPosition.value
            val overhangM = getOptimalOverhangDepthM()
            val pvTilt = getOptimalPvTiltDeg()
            val shadowL = getObstacleShadowLengthM()

            toolLogRepository?.logToolActivity(
                toolType = "widget_sun_path_tracker",
                title = "Solar & Sun Path: $locationName",
                summary = "Elevation: ${String.format("%.1f°", pos.elevationDeg)}, Azimuth: ${String.format("%.1f°", pos.azimuthDeg)}, Noon Elev: ${String.format("%.1f°", pos.solarNoonElevationDeg)}, Overhang: ${String.format("%.2f m", overhangM)}, PV Tilt: ${String.format("%.1f°", pvTilt)}, Shadow: ${String.format("%.2f m", shadowL)}",
                value = pos.elevationDeg.toDouble()
            )
            _lastLogSaved.value = true
        }
    }
}
