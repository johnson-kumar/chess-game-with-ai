package com.example.chess.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundEffects(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playMove(isCapture: Boolean, isCheck: Boolean, isVictory: Boolean) {
        scope.launch {
            when {
                isVictory -> playTone(frequency = 587.33, durationMs = 180, harmonics = true)
                isCheck -> playTone(frequency = 783.99, durationMs = 120, harmonics = false)
                isCapture -> playThud(durationMs = 90)
                else -> playWoodClick(durationMs = 50)
            }
        }
    }

    fun vibrateMove(isCapture: Boolean, isCheck: Boolean) {
        try {
            val duration = when {
                isCheck -> 60L
                isCapture -> 40L
                else -> 18L
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(duration, if (isCheck) 200 else 120)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(duration)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration errors if permission or hardware unavailable
        }
    }

    private fun playWoodClick(durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val decay = (1.0 - progress) * (1.0 - progress)
            val freq = 420.0 - progress * 180.0
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            val sample = sin(angle) * decay * 0.7
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playRawPcm(buffer, sampleRate)
    }

    private fun playThud(durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val decay = (1.0 - progress)
            val freq = 180.0 - progress * 90.0
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            val sample = sin(angle) * decay * 0.9
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playRawPcm(buffer, sampleRate)
    }

    private fun playTone(frequency: Double, durationMs: Int, harmonics: Boolean) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val decay = (1.0 - progress * 0.7)
            val angle = 2.0 * Math.PI * frequency * i / sampleRate
            var sample = sin(angle)
            if (harmonics) {
                sample = 0.7 * sample + 0.3 * sin(2.0 * angle)
            }
            sample *= decay * 0.8
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playRawPcm(buffer, sampleRate)
    }

    private fun playRawPcm(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    t?.release()
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
        } catch (_: Exception) {
            // Ignore audio initialization errors gracefully
        }
    }
}
