package com.example.domain.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.PI
import kotlin.math.atan2

data class InclinometerData(
    val pitchDegrees: Float,
    val rollDegrees: Float,
    val rawPitchDegrees: Float,
    val rawRollDegrees: Float,
    val isLevel: Boolean,
    val isTared: Boolean
)

class ButterworthFilter(
    private val cutoffHz: Float = 4.0f,
    private val sampleRateHz: Float = 50.0f
) {
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f

    private var b0 = 0f
    private var b1 = 0f
    private var b2 = 0f
    private var a1 = 0f
    private var a2 = 0f

    init {
        val omega = (2.0f * PI.toFloat() * cutoffHz) / sampleRateHz
        val cosOmega = kotlin.math.cos(omega)
        val sinOmega = kotlin.math.sin(omega)
        val alpha = sinOmega / (2.0f * 0.70710678f) // Q = 1/sqrt(2)

        val a0 = 1.0f + alpha
        b0 = ((1.0f - cosOmega) / 2.0f) / a0
        b1 = (1.0f - cosOmega) / a0
        b2 = ((1.0f - cosOmega) / 2.0f) / a0
        a1 = (-2.0f * cosOmega) / a0
        a2 = (1.0f - alpha) / a0
    }

    fun filter(input: Float): Float {
        val output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1
        x1 = input
        y2 = y1
        y1 = output
        return output
    }

    fun reset() {
        x1 = 0f; x2 = 0f; y1 = 0f; y2 = 0f
    }
}

class InclinometerSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val pitchFilter = ButterworthFilter(cutoffHz = 3.5f, sampleRateHz = 50.0f)
    private val rollFilter = ButterworthFilter(cutoffHz = 3.5f, sampleRateHz = 50.0f)

    private var tarePitch = 0f
    private var tareRoll = 0f
    private var isTared = false

    fun setTare() {
        isTared = true
    }

    fun resetTare() {
        isTared = false
        tarePitch = 0f
        tareRoll = 0f
    }

    fun getSensorFlow(): Flow<InclinometerData> = callbackFlow {
        if (rotationSensor == null) {
            trySend(InclinometerData(0f, 0f, 0f, 0f, isLevel = true, isTared = false))
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val rotationMatrix = FloatArray(9)
                val orientation = FloatArray(3)

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val ax = event.values[0]
                    val ay = event.values[1]
                    val az = event.values[2]
                    orientation[1] = atan2(-ax.toDouble(), kotlin.math.sqrt((ay * ay + az * az).toDouble())).toFloat()
                    orientation[2] = atan2(ay.toDouble(), az.toDouble()).toFloat()
                }

                // Pitch and Roll in degrees
                val rawPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                // Filter signal
                val filteredPitch = pitchFilter.filter(rawPitch)
                val filteredRoll = rollFilter.filter(rawRoll)

                if (isTared && tarePitch == 0f && tareRoll == 0f) {
                    tarePitch = filteredPitch
                    tareRoll = filteredRoll
                }

                val finalPitch = if (isTared) filteredPitch - tarePitch else filteredPitch
                val finalRoll = if (isTared) filteredRoll - tareRoll else filteredRoll

                val isLevel = kotlin.math.abs(finalPitch) < 0.2f && kotlin.math.abs(finalRoll) < 0.2f

                trySend(
                    InclinometerData(
                        pitchDegrees = finalPitch,
                        rollDegrees = finalRoll,
                        rawPitchDegrees = rawPitch,
                        rawRollDegrees = rawRoll,
                        isLevel = isLevel,
                        isTared = isTared
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
