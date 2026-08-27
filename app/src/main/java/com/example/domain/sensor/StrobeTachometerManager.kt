package com.example.domain.sensor

import android.content.Context
import android.hardware.camera2.CameraManager

class StrobeTachometerManager(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null

    @Volatile
    private var isStrobeRunning = false
    private var strobeThread: Thread? = null
    
    @Volatile
    private var targetRpm: Int = 1200 // default 1200 RPM

    init {
        try {
            val ids = cameraManager?.cameraIdList
            if (!ids.isNullOrEmpty()) {
                cameraId = ids[0]
            }
        } catch (e: Exception) {
            cameraId = null
        }
    }

    fun startStrobe(initialRpm: Int, onRpmChanged: (Int) -> Unit) {
        targetRpm = initialRpm
        if (isStrobeRunning) return
        isStrobeRunning = true

        strobeThread = Thread {
            try {
                while (isStrobeRunning && !Thread.currentThread().isInterrupted) {
                    val currentRpm = targetRpm.coerceIn(100, 12000)
                    
                    // Hardware camera torch PWM rate limiting to avoid Android CameraService Binder flooding
                    // Max flash frequency ~20 Hz (1200 RPM) for physical camera torch, otherwise sleep smoothly
                    val periodMs = (60.0 / currentRpm * 1000.0).toLong().coerceAtLeast(20L)
                    val pulseOnMs = (periodMs * 0.15).toLong().coerceIn(2L, 10L)
                    val pulseOffMs = (periodMs - pulseOnMs).coerceAtLeast(10L)

                    toggleTorch(true)
                    Thread.sleep(pulseOnMs)
                    toggleTorch(false)
                    Thread.sleep(pulseOffMs)
                }
            } catch (e: InterruptedException) {
                // Thread interrupted on stop
            } catch (e: Exception) {
                // Catch any runtime hardware exception
            } finally {
                toggleTorch(false)
            }
        }
        strobeThread?.start()
    }

    fun updateRpm(rpm: Int) {
        targetRpm = rpm.coerceIn(100, 12000)
    }

    fun stopStrobe() {
        isStrobeRunning = false
        try {
            strobeThread?.interrupt()
            strobeThread = null
        } catch (e: Exception) {
            // Ignore interruption exception
        }
        toggleTorch(false)
    }

    private fun toggleTorch(enabled: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager?.setTorchMode(id, enabled)
        } catch (e: Exception) {
            // Flash not supported, permission denied, or camera busy
        }
    }
}
