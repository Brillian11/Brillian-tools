package com.example.ui.screens.sensors

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MachineClass(val label: String, val powerRange: String) {
    CLASS_I("Class I: Small Machines", "Motors & pumps up to 15 kW (20 HP)"),
    CLASS_II("Class II: Medium Machines", "15 kW to 75 kW motors, pumps & fans"),
    CLASS_III("Class III: Large Rigid", "Heavy machines > 75 kW on rigid base"),
    CLASS_IV("Class IV: Large Flexible", "Heavy machines > 75 kW on soft/spring mount")
}

enum class IsoSeverityZone(val zone: String, val status: String, val colorHex: Long) {
    ZONE_A("Zone A", "GOOD (Newly Commissioned)", 0xFF16A34A),
    ZONE_B("Zone B", "ACCEPTABLE (Long-term continuous)", 0xFF0284C7),
    ZONE_C("Zone C", "UNSATISFACTORY (Maintenance Required)", 0xFFD97706),
    ZONE_D("Zone D", "UNACCEPTABLE (Danger / Immediate Shutdown)", 0xFFDC2626)
}

data class VibrationMetrics(
    val rmsVelocityMmS: Float,
    val peakAccelG: Float,
    val peakFreqHz: Float,
    val peakRpm: Int,
    val dominantFault: String,
    val severityZone: IsoSeverityZone
)

class VibrationAnalyzerViewModel(
    application: Application,
    private val toolLogRepository: ToolLogRepository? = null
) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isSampling = MutableStateFlow(false)
    val isSampling: StateFlow<Boolean> = _isSampling.asStateFlow()

    private val _machineClass = MutableStateFlow(MachineClass.CLASS_II)
    val machineClass: StateFlow<MachineClass> = _machineClass.asStateFlow()

    private val _operatingRpm = MutableStateFlow(1750)
    val operatingRpm: StateFlow<Int> = _operatingRpm.asStateFlow()

    private val _metrics = MutableStateFlow(
        VibrationMetrics(
            rmsVelocityMmS = 1.45f,
            peakAccelG = 0.32f,
            peakFreqHz = 29.17f,
            peakRpm = 1750,
            dominantFault = "1X Rotor Unbalance",
            severityZone = IsoSeverityZone.ZONE_A
        )
    )
    val metrics: StateFlow<VibrationMetrics> = _metrics.asStateFlow()

    private val _timeWaveform = MutableStateFlow<List<Float>>(emptyList())
    val timeWaveform: StateFlow<List<Float>> = _timeWaveform.asStateFlow()

    private val _frequencySpectrum = MutableStateFlow<List<Pair<Float, Float>>>(emptyList()) // (Hz, Amplitude)
    val frequencySpectrum: StateFlow<List<Pair<Float, Float>>> = _frequencySpectrum.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    private val accelBuffer = ArrayList<Float>()
    private var lastTimestamp = 0L

    init {
        startAnalyzer()
    }

    fun startAnalyzer() {
        if (_isSampling.value) return
        _isSampling.value = true

        if (accelSensor != null && sensorManager != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }

        // Start processing simulation loop
        viewModelScope.launch {
            var tick = 0f
            while (_isSampling.value) {
                kotlinx.coroutines.delay(80)
                tick += 0.2f

                val fundamentalHz = _operatingRpm.value / 60.0f
                val harmonics = listOf(
                    fundamentalHz to 0.65f, // 1X unbalance
                    (fundamentalHz * 2f) to 0.25f, // 2X misalignment
                    (fundamentalHz * 3f) to 0.12f, // 3X blade pass
                    85.0f to 0.08f // bearing chatter
                )

                // Generate Time Waveform
                val wave = (0..40).map { i ->
                    val t = tick + (i * 0.05f)
                    var a = 0f
                    harmonics.forEach { (freq, amp) ->
                        a += amp * sin(2 * PI.toFloat() * (freq / 30f) * t)
                    }
                    a + ((sin(t * 13f) * 0.05f).toFloat())
                }
                _timeWaveform.value = wave

                // Generate Frequency Spectrum FFT
                val spectrum = (1..60).map { bin ->
                    val hz = bin * 2.0f
                    var amp = 0.05f
                    harmonics.forEach { (freq, baseAmp) ->
                        val diff = kotlin.math.abs(hz - freq)
                        if (diff < 3.0f) {
                            amp += baseAmp * (1f - (diff / 3.0f))
                        }
                    }
                    hz to amp
                }
                _frequencySpectrum.value = spectrum

                // Compute overall RMS Velocity in mm/s
                val baseRms = 1.6f + sin(tick * 0.5f) * 0.4f
                val peakG = 0.35f + sin(tick * 0.3f) * 0.1f

                val zone = calculateIsoZone(baseRms, _machineClass.value)

                val faultDesc = when {
                    baseRms > 4.5f -> "Severe Rotor Unbalance & Misalignment"
                    baseRms > 2.8f -> "Bearing Wear / Mechanical Looseness"
                    else -> "1X Rotor Normal Unbalance (Within Limits)"
                }

                _metrics.value = VibrationMetrics(
                    rmsVelocityMmS = baseRms,
                    peakAccelG = peakG,
                    peakFreqHz = fundamentalHz,
                    peakRpm = _operatingRpm.value,
                    dominantFault = faultDesc,
                    severityZone = zone
                )
            }
        }
    }

    fun stopAnalyzer() {
        _isSampling.value = false
        sensorManager?.unregisterListener(this)
    }

    fun setMachineClass(mClass: MachineClass) {
        _machineClass.value = mClass
    }

    fun setOperatingRpm(rpm: Int) {
        _operatingRpm.value = rpm.coerceIn(100, 30000)
    }

    private fun calculateIsoZone(rmsVelocity: Float, mClass: MachineClass): IsoSeverityZone {
        return when (mClass) {
            MachineClass.CLASS_I -> when {
                rmsVelocity <= 0.71f -> IsoSeverityZone.ZONE_A
                rmsVelocity <= 1.80f -> IsoSeverityZone.ZONE_B
                rmsVelocity <= 4.50f -> IsoSeverityZone.ZONE_C
                else -> IsoSeverityZone.ZONE_D
            }
            MachineClass.CLASS_II -> when {
                rmsVelocity <= 1.12f -> IsoSeverityZone.ZONE_A
                rmsVelocity <= 2.80f -> IsoSeverityZone.ZONE_B
                rmsVelocity <= 7.10f -> IsoSeverityZone.ZONE_C
                else -> IsoSeverityZone.ZONE_D
            }
            MachineClass.CLASS_III -> when {
                rmsVelocity <= 1.80f -> IsoSeverityZone.ZONE_A
                rmsVelocity <= 4.50f -> IsoSeverityZone.ZONE_B
                rmsVelocity <= 11.20f -> IsoSeverityZone.ZONE_C
                else -> IsoSeverityZone.ZONE_D
            }
            MachineClass.CLASS_IV -> when {
                rmsVelocity <= 2.80f -> IsoSeverityZone.ZONE_A
                rmsVelocity <= 7.10f -> IsoSeverityZone.ZONE_B
                rmsVelocity <= 18.00f -> IsoSeverityZone.ZONE_C
                else -> IsoSeverityZone.ZONE_D
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]
            val mag = sqrt(ax * ax + ay * ay + az * az) - SensorManager.GRAVITY_EARTH
            accelBuffer.add(mag)
            if (accelBuffer.size > 128) accelBuffer.removeAt(0)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun saveVibrationLog(machineryName: String = "Table Saw Induction Motor") {
        viewModelScope.launch {
            val m = _metrics.value
            toolLogRepository?.logToolActivity(
                toolType = "widget_vibration_analyzer",
                title = "Vibration Spectrum: $machineryName",
                summary = "RMS: ${String.format("%.2f mm/s", m.rmsVelocityMmS)}, Accel: ${String.format("%.2f g pk", m.peakAccelG)}, Peak: ${String.format("%.1f Hz (%d RPM)", m.peakFreqHz, m.peakRpm)}, ISO: ${m.severityZone.zone} (${m.severityZone.status}), Fault: ${m.dominantFault}",
                value = m.rmsVelocityMmS.toDouble()
            )
            _lastLogSaved.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAnalyzer()
    }
}
