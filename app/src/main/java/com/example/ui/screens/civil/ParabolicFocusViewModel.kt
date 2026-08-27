package com.example.ui.screens.civil

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import com.example.domain.math.FrequencyBand
import com.example.domain.math.SatellitePointerResult
import com.example.domain.math.SatellitePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2

data class DeviceOrientationData(
    val azimuthDeg: Float = 0f, // 0 = North, 90 = East, 180 = South, 270 = West
    val pitchDeg: Float = 0f,   // 0 = Horizontal, 90 = Pointing straight up
    val rollDeg: Float = 0f,
    val rotationMatrix: FloatArray = FloatArray(9) { if (it % 4 == 0) 1f else 0f }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DeviceOrientationData
        return azimuthDeg == other.azimuthDeg && pitchDeg == other.pitchDeg && rollDeg == other.rollDeg
    }

    override fun hashCode(): Int {
        var result = azimuthDeg.hashCode()
        result = 31 * result + pitchDeg.hashCode()
        result = 31 * result + rollDeg.hashCode()
        return result
    }
}

class ParabolicFocusViewModel : ViewModel(), SensorEventListener, LocationListener {

    private val _result = MutableStateFlow(SatellitePointerResult())
    val result: StateFlow<SatellitePointerResult> = _result.asStateFlow()

    private val _deviceOrientation = MutableStateFlow(DeviceOrientationData())
    val deviceOrientation: StateFlow<DeviceOrientationData> = _deviceOrientation.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private var locationManager: LocationManager? = null

    // Rotation matrix buffers
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    fun startSensors(context: Context) {
        if (sensorManager == null) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        }
        rotationSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Try getting initial GPS location
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            gpsLoc?.let { updateLocation(it.latitude, it.longitude) }

            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, this)
            _isGpsActive.value = true
        } catch (_: SecurityException) {
            _isGpsActive.value = false
        } catch (_: Exception) {
            _isGpsActive.value = false
        }
    }

    fun stopSensors() {
        sensorManager?.unregisterListener(this)
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {}
        _isGpsActive.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            
            // Back camera points along -Z of device coordinate system.
            // In world coordinates (X=East, Y=North, Z=Up):
            val camEast = -rotationMatrix[2]
            val camNorth = -rotationMatrix[5]
            val camUp = -rotationMatrix[8]

            val horizDist = kotlin.math.sqrt(camEast * camEast + camNorth * camNorth)
            val pitch = Math.toDegrees(kotlin.math.atan2(camUp.toDouble(), horizDist.toDouble())).toFloat()

            var azimuth = Math.toDegrees(kotlin.math.atan2(camEast.toDouble(), camNorth.toDouble())).toFloat()
            if (azimuth < 0) azimuth += 360f

            val matrixCopy = rotationMatrix.clone()

            _deviceOrientation.value = DeviceOrientationData(
                azimuthDeg = azimuth,
                pitchDeg = pitch,
                rollDeg = 0f,
                rotationMatrix = matrixCopy
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onLocationChanged(location: Location) {
        updateLocation(location.latitude, location.longitude)
    }

    fun updateBand(band: FrequencyBand) {
        _result.value = _result.value.copy(
            frequencyGhz = band.defaultFreqGhz,
            selectedSat = _result.value.selectedSat.copy(band = band)
        )
    }

    fun updateFrequency(freqGhz: Double) {
        _result.value = _result.value.copy(frequencyGhz = freqGhz.coerceAtLeast(0.1))
    }

    fun selectSatellite(satellite: SatellitePreset) {
        _result.value = _result.value.copy(
            selectedSat = satellite,
            frequencyGhz = satellite.band.defaultFreqGhz
        )
    }

    fun updateCustomSatLongitude(lonDeg: Double) {
        _result.value = _result.value.copy(customSatLongitude = lonDeg.coerceIn(-180.0, 180.0))
    }

    fun updateLocation(lat: Double, lon: Double) {
        _result.value = _result.value.copy(
            userLatitude = lat.coerceIn(-90.0, 90.0),
            userLongitude = lon.coerceIn(-180.0, 180.0)
        )
    }

    fun updateDishDiameter(diameterCm: Double) {
        _result.value = _result.value.copy(dishDiameterCm = diameterCm.coerceAtLeast(10.0))
    }

    fun updateDishDepth(depthCm: Double) {
        _result.value = _result.value.copy(dishDepthCm = depthCm.coerceAtLeast(1.0))
    }

    fun updateEfficiency(efficiencyPercent: Double) {
        _result.value = _result.value.copy(efficiencyPercent = efficiencyPercent.coerceIn(10.0, 95.0))
    }

    fun updateLnbCount(count: Int) {
        _result.value = _result.value.copy(lnbCount = count.coerceIn(1, 4))
    }

    fun updateSecondarySatellite(index: Int, sat: SatellitePreset) {
        val currentList = _result.value.secondarySatellites.toMutableList()
        while (currentList.size <= index) {
            currentList.add(SatellitePreset.POPULAR_SATELLITES[0])
        }
        currentList[index] = sat
        _result.value = _result.value.copy(secondarySatellites = currentList)
    }
}
