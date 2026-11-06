package com.example.domain.sensor

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.sin

/**
 * Universal sound & haptic feedback manager for sensor modules.
 * Uses hardware ToneGenerator with PCM AudioTrack fallback to ensure
 * robust sound across physical hardware and emulators.
 */
class SensorSoundManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var isToneGenFailed = false

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    init {
        initToneGenerator()
    }

    private fun initToneGenerator() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            isToneGenFailed = false
        } catch (_: Exception) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
                isToneGenFailed = false
            } catch (_: Exception) {
                toneGenerator = null
                isToneGenFailed = true
            }
        }
    }

    /**
     * Plays an audible beep. If ToneGenerator fails or is unavailable on the device/emulator,
     * seamlessly synthesizes a clean sine wave tone using AudioTrack.
     */
    fun playBeep(
        toneType: Int = ToneGenerator.TONE_PROP_BEEP,
        durationMs: Int = 45,
        frequencyHz: Int = 1200
    ) {
        var played = false
        if (!isToneGenFailed && toneGenerator != null) {
            try {
                toneGenerator?.startTone(toneType, durationMs)
                played = true
            } catch (_: Exception) {
                // If ToneGenerator threw an exception, fall back to AudioTrack
                isToneGenFailed = true
                played = false
            }
        }

        if (!played) {
            playSynthesizedTone(frequencyHz.coerceIn(300, 3500), durationMs.coerceIn(15, 600))
        }
    }

    /**
     * Synthesizes a pure sine wave audio buffer and plays it via AudioTrack.
     */
    private fun playSynthesizedTone(frequencyHz: Int, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            if (numSamples <= 0) return

            val buffer = ShortArray(numSamples)
            val radPerSample = 2.0 * Math.PI * frequencyHz / sampleRate
            val taperSamples = (numSamples / 10).coerceIn(15, 80)

            for (i in 0 until numSamples) {
                val envelope = when {
                    i < taperSamples -> i.toDouble() / taperSamples
                    i > numSamples - taperSamples -> (numSamples - i).toDouble() / taperSamples
                    else -> 1.0
                }
                buffer[i] = (sin(i * radPerSample) * 28000.0 * envelope).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Clean up static track after duration
            Thread {
                try {
                    Thread.sleep((durationMs + 60).toLong())
                    if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        audioTrack.stop()
                    }
                    audioTrack.release()
                } catch (_: Exception) {}
            }.start()
        } catch (_: Exception) {}
    }

    /**
     * Triggers a subtle tactile haptic vibration pulse if supported.
     */
    fun vibrate(durationMs: Long = 35) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
