package com.example.ui.screens.sensors

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.sensor.SensorSoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

data class StudDetectorUiState(
    val magneticFieldUT: Float = 45f,         // Magnetic flux density in microtesla (uT)
    val baselineUT: Float = 45f,              // Calibrated ambient baseline
    val signalDeltaUT: Float = 0f,            // Delta magnetic signal strength above baseline
    val signalPercent: Float = 0f,            // 0 - 100% proximity gauge
    val isStudDetected: Boolean = false,      // Signal above metal detection threshold
    val detectedMaterialType: String = "Clear Wall", // "Clear Wall", "Drywall Metal Screw", "Steel Rebar / Iron Pipe", "Electrical Conduit"
    val sensitivityThresholdUT: Float = 15f,   // Sensitivity slider threshold
    val isAudioEnabled: Boolean = true,       // Sound toggle (Audible Beeper)
    val isVibrationEnabled: Boolean = true,   // Haptic vibration feedback
    val isSensorAvailable: Boolean = true,    // Physical magnetometer sensor present
    val isSimulationMode: Boolean = false,    // Virtual sweep / test simulation
    val simulatedProximity: Float = 0f,       // 0 - 100% slider value
    val isAutoSweeping: Boolean = false       // Animated demo sweep active
)

class StudDetectorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as? SensorManager
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val soundManager = SensorSoundManager(application)

    private val _uiState = MutableStateFlow(
        StudDetectorUiState(
            isSensorAvailable = magnetometer != null
        )
    )
    val uiState: StateFlow<StudDetectorUiState> = _uiState.asStateFlow()

    private var beeperJob: Job? = null
    private var sweepJob: Job? = null

    init {
        if (magnetometer != null && sensorManager != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        } else {
            _uiState.value = _uiState.value.copy(
                isSensorAvailable = false,
                isSimulationMode = true
            )
        }
        startBeeperLoop()
    }

    /**
     * Loops in background to trigger audio beeps with variable frequency and pitch
     * matching the proximity to the detected metal object.
     */
    private fun startBeeperLoop() {
        beeperJob?.cancel()
        beeperJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val state = _uiState.value
                val isAudio = state.isAudioEnabled
                val isVibe = state.isVibrationEnabled
                val pct = state.signalPercent
                val isDetected = state.isStudDetected

                if (isAudio && (isDetected || pct >= 8f)) {
                    // Dynamic beep repetition interval:
                    // 10% signal -> ~750ms interval (slow pulses)
                    // 50% signal -> ~240ms interval (steady beeps)
                    // 100% signal -> ~50ms interval (rapid continuous pip / alarm)
                    val intervalMs = (750L - (pct * 7.0f).toLong()).coerceIn(45L, 800L)
                    val durationMs = if (pct > 80f) 50 else 35

                    // Frequency ramps from 850 Hz (approach) up to 2600 Hz (peak metal rebar)
                    val freqHz = (850 + (pct * 17.5f).toInt()).coerceIn(850, 2600)
                    val toneType = when {
                        pct >= 80f -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                        pct >= 45f -> ToneGenerator.TONE_PROP_BEEP2
                        else -> ToneGenerator.TONE_PROP_BEEP
                    }

                    soundManager.playBeep(
                        toneType = toneType,
                        durationMs = durationMs,
                        frequencyHz = freqHz
                    )

                    if (isVibe && isDetected) {
                        val vibeDuration = if (pct >= 80f) 30L else 18L
                        soundManager.vibrate(vibeDuration)
                    }

                    delay(intervalMs)
                } else {
                    // Idle check pause
                    delay(120L)
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // When active simulation is engaged and user is sliding or auto-sweeping, don't let static ambient override
        if (_uiState.value.isSimulationMode && (_uiState.value.simulatedProximity > 0f || _uiState.value.isAutoSweeping)) {
            return
        }

        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val totalUT = sqrt(x * x + y * y + z * z)
            val delta = abs(totalUT - _uiState.value.baselineUT)
            val thresh = _uiState.value.sensitivityThresholdUT

            val signalPct = (delta / (thresh * 2.5f) * 100f).coerceIn(0f, 100f)
            val isDetected = delta >= thresh

            val matType = when {
                delta >= thresh * 3.0f -> "Heavy Steel Rebar / Iron Pipe"
                delta >= thresh * 1.8f -> "Metal Framing Stud / Bracket"
                delta >= thresh -> "Drywall Screw / Small Fastener"
                else -> "Clear Wall Surface"
            }

            _uiState.value = _uiState.value.copy(
                magneticFieldUT = totalUT,
                signalDeltaUT = delta,
                signalPercent = signalPct,
                isStudDetected = isDetected,
                detectedMaterialType = matType,
                isSensorAvailable = true
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun toggleAudio() {
        val next = !_uiState.value.isAudioEnabled
        _uiState.value = _uiState.value.copy(isAudioEnabled = next)
        if (next) {
            // Immediate feedback chirp when enabling sound
            soundManager.playBeep(ToneGenerator.TONE_PROP_BEEP, 50, 1400)
        }
    }

    fun toggleVibration() {
        val next = !_uiState.value.isVibrationEnabled
        _uiState.value = _uiState.value.copy(isVibrationEnabled = next)
        if (next) {
            soundManager.vibrate(40)
        }
    }

    fun calibrateBaseline() {
        _uiState.value = _uiState.value.copy(
            baselineUT = _uiState.value.magneticFieldUT,
            signalDeltaUT = 0f,
            signalPercent = 0f,
            isStudDetected = false,
            simulatedProximity = 0f,
            isAutoSweeping = false
        )
        sweepJob?.cancel()
        soundManager.playBeep(ToneGenerator.TONE_PROP_ACK, 70, 1100)
    }

    fun setSensitivity(thresh: Float) {
        val newThresh = thresh.coerceIn(5f, 50f)
        _uiState.value = _uiState.value.copy(sensitivityThresholdUT = newThresh)
        if (_uiState.value.isSimulationMode && _uiState.value.simulatedProximity > 0f) {
            applySimulatedProximity(_uiState.value.simulatedProximity)
        }
    }

    fun setSimulatedProximity(percent: Float) {
        _uiState.value = _uiState.value.copy(isSimulationMode = true, isAutoSweeping = false)
        sweepJob?.cancel()
        applySimulatedProximity(percent)
    }

    fun toggleAutoSweep() {
        val willSweep = !_uiState.value.isAutoSweeping
        _uiState.value = _uiState.value.copy(isAutoSweeping = willSweep, isSimulationMode = true)
        sweepJob?.cancel()
        if (willSweep) {
            sweepJob = viewModelScope.launch {
                var step = 0.0
                while (isActive && _uiState.value.isAutoSweeping) {
                    delay(50)
                    step += 0.07
                    // Sine pulse simulating sweeping back and forth over a metal wall stud
                    val wave = (sin(step) + 1.0) / 2.0 // 0.0 to 1.0
                    val pct = (wave * 100.0).toFloat().coerceIn(0f, 100f)
                    applySimulatedProximity(pct)
                }
            }
        } else {
            applySimulatedProximity(0f)
        }
    }

    fun setLiveHardwareMode() {
        sweepJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isSimulationMode = false,
            isAutoSweeping = false,
            simulatedProximity = 0f
        )
    }

    private fun applySimulatedProximity(pct: Float) {
        val thresh = _uiState.value.sensitivityThresholdUT
        val maxDelta = thresh * 3.5f
        val delta = (pct / 100f) * maxDelta
        val totalUT = _uiState.value.baselineUT + delta
        val isDetected = delta >= thresh
        val matType = when {
            delta >= thresh * 3.0f -> "Heavy Steel Rebar / Iron Pipe"
            delta >= thresh * 1.8f -> "Metal Framing Stud / Bracket"
            delta >= thresh -> "Drywall Screw / Small Fastener"
            else -> "Clear Wall Surface"
        }

        _uiState.value = _uiState.value.copy(
            magneticFieldUT = totalUT,
            signalDeltaUT = delta,
            signalPercent = pct,
            isStudDetected = isDetected,
            detectedMaterialType = matType,
            simulatedProximity = pct
        )
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
        beeperJob?.cancel()
        sweepJob?.cancel()
        soundManager.release()
    }
}
