package com.example.ui.screens.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ArPoint(
    val id: Int,
    val xMeters: Double,
    val yMeters: Double,
    val cameraXRatio: Float = 0.5f,
    val cameraYRatio: Float = 0.5f,
    val distanceMeters: Double = 0.0
)

enum class ArPaintColor(val title: String, val argbHex: Long, val coverageSqMetersPerLiter: Double = 10.0) {
    WHITE("Pure White", 0x88FFFFFF),
    WARM_CREAM("Warm Cream", 0x88FFF8DC),
    TERRACOTTA("Terracotta", 0x88E2725B),
    SAFETY_YELLOW("Safety Yellow", 0x88FFD600),
    OCEAN_BLUE("Ocean Blue", 0x882563EB),
    WALNUT_WOOD("Walnut Stain", 0x885C4033)
}

data class ArAreaState(
    val eyeHeightMeters: Double = 1.5,
    val currentPitchDegrees: Double = 45.0,
    val currentRollDegrees: Double = 0.0,
    val currentYawDegrees: Double = 0.0,
    val estimatedDistanceMeters: Double = 1.5,
    val points: List<ArPoint> = emptyList(),
    val areaSquareMeters: Double = 0.0,
    val perimeterMeters: Double = 0.0,
    val isMetric: Boolean = true,
    val isCameraPermissionGranted: Boolean = true,
    val selectedPaintColor: ArPaintColor = ArPaintColor.SAFETY_YELLOW,
    val paintCoats: Int = 2
) {
    val areaSquareFeet: Double get() = areaSquareMeters * 10.7639
    val areaSquareYards: Double get() = areaSquareMeters * 1.19599
    val areaAcres: Double get() = areaSquareMeters / 4046.8564224
    val perimeterFeet: Double get() = perimeterMeters * 3.28084

    val requiredPaintLiters: Double
        get() = (areaSquareMeters / selectedPaintColor.coverageSqMetersPerLiter) * paintCoats

    val requiredPaintGallons: Double
        get() = requiredPaintLiters * 0.264172
}

class ArAreaCalculatorViewModel(context: Context) : ViewModel(), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _state = MutableStateFlow(ArAreaState())
    val state: StateFlow<ArAreaState> = _state.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    fun startSensors() {
        sensorManager?.let { sm ->
            accelerometer?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    fun stopSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, 3)
                hasGravity = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                hasGeomagnetic = true
            }
        }

        if (hasGravity) {
            val ax = gravity[0].toDouble()
            val ay = gravity[1].toDouble()
            val az = gravity[2].toDouble()

            val pitch = Math.toDegrees(atan2(-ay, sqrt(ax * ax + az * az)))
            val roll = Math.toDegrees(atan2(ax, az))

            var yaw = 0.0
            if (hasGeomagnetic) {
                val R = FloatArray(9)
                val I = FloatArray(9)
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)
                    yaw = Math.toDegrees(orientation[0].toDouble())
                    if (yaw < 0) yaw += 360.0
                }
            }

            // Estimate ground distance based on camera tilt angle from horizontal
            // Pitch 0 = pointing straight ahead, pitch -90 = pointing straight down
            val tiltAngleFromHorizontal = abs(pitch).coerceIn(5.0, 85.0)
            val tiltRad = Math.toRadians(tiltAngleFromHorizontal)
            val eyeH = _state.value.eyeHeightMeters
            val estDist = (eyeH / sin(tiltRad)).coerceIn(0.2, 50.0)

            _state.update { old ->
                old.copy(
                    currentPitchDegrees = pitch,
                    currentRollDegrees = roll,
                    currentYawDegrees = yaw,
                    estimatedDistanceMeters = estDist
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setEyeHeight(heightMeters: Double) {
        val clamped = heightMeters.coerceIn(0.5, 3.0)
        _state.update { it.copy(eyeHeightMeters = clamped) }
        recalculateGeometry()
    }

    fun updatePaintColor(color: ArPaintColor) {
        _state.update { it.copy(selectedPaintColor = color) }
    }

    fun updatePaintCoats(coats: Int) {
        _state.update { it.copy(paintCoats = coats.coerceIn(1, 4)) }
    }

    fun addPointAtScreenTap(tapXRatio: Float, tapYRatio: Float) {
        val currentState = _state.value
        val dist = currentState.estimatedDistanceMeters
        val yawRad = Math.toRadians(currentState.currentYawDegrees + ((tapXRatio - 0.5f) * 40.0))

        val x = dist * sin(yawRad)
        val y = dist * cos(yawRad)

        val newPoint = ArPoint(
            id = (currentState.points.maxOfOrNull { it.id } ?: 0) + 1,
            xMeters = x,
            yMeters = y,
            cameraXRatio = tapXRatio,
            cameraYRatio = tapYRatio,
            distanceMeters = dist
        )

        val newPoints = currentState.points + newPoint
        _state.update { it.copy(points = newPoints) }
        recalculateGeometry()
    }

    fun addPointAtReticle() {
        val currentState = _state.value
        val dist = currentState.estimatedDistanceMeters
        val yawRad = Math.toRadians(currentState.currentYawDegrees)

        // Convert polar (distance, yaw) into 2D Cartesian floor plane coordinates (x, y)
        val x = dist * sin(yawRad)
        val y = dist * cos(yawRad)

        val newPoint = ArPoint(
            id = (currentState.points.maxOfOrNull { it.id } ?: 0) + 1,
            xMeters = x,
            yMeters = y,
            distanceMeters = dist
        )

        val newPoints = currentState.points + newPoint
        _state.update { it.copy(points = newPoints) }
        recalculateGeometry()
    }

    fun removeLastPoint() {
        val currentPoints = _state.value.points
        if (currentPoints.isNotEmpty()) {
            _state.update { it.copy(points = currentPoints.dropLast(1)) }
            recalculateGeometry()
        }
    }

    fun clearAllPoints() {
        _state.update { it.copy(points = emptyList(), areaSquareMeters = 0.0, perimeterMeters = 0.0) }
    }

    fun toggleUnitSystem() {
        _state.update { it.copy(isMetric = !it.isMetric) }
    }

    fun setUnitSystem(metric: Boolean) {
        _state.update { it.copy(isMetric = metric) }
    }

    private fun recalculateGeometry() {
        val pts = _state.value.points
        if (pts.size < 3) {
            val perim = computePerimeter(pts)
            _state.update { it.copy(areaSquareMeters = 0.0, perimeterMeters = perim) }
            return
        }

        // Calculate polygon area using Shoelace formula
        var sum = 0.0
        val n = pts.size
        for (i in 0 until n) {
            val p1 = pts[i]
            val p2 = pts[(i + 1) % n]
            sum += (p1.xMeters * p2.yMeters) - (p2.xMeters * p1.yMeters)
        }
        val area = abs(sum) / 2.0
        val perim = computePerimeter(pts)

        _state.update { it.copy(areaSquareMeters = area, perimeterMeters = perim) }
    }

    private fun computePerimeter(pts: List<ArPoint>): Double {
        if (pts.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until pts.size) {
            val p1 = pts[i]
            val p2 = pts[(i + 1) % pts.size]
            // Only add closing segment if 3 or more points
            if (i == pts.size - 1 && pts.size < 3) break
            val dx = p2.xMeters - p1.xMeters
            val dy = p2.yMeters - p1.yMeters
            total += sqrt(dx * dx + dy * dy)
        }
        return total
    }
}
