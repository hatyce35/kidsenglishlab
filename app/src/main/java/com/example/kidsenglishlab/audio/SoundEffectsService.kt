package com.example.kidsenglishlab.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundEffectsService {
    private var job = Job()
    private var scope = CoroutineScope(Dispatchers.Default + job)
    var isSoundEnabled: Boolean = true

    private fun playPcmTones(tones: List<Pair<Double, Int>>) {
        if (!isSoundEnabled) return
        scope.launch {
            var audioTrack: AudioTrack? = null
            try {
                val sampleRate = 44100
                var totalDurationMs = 0
                tones.forEach { totalDurationMs += it.second }
                val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt()
                val buffer = ShortArray(totalSamples)

                var sampleOffset = 0
                for ((freq, durationMs) in tones) {
                    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val envelope = if (i < numSamples * 0.1) {
                            i.toDouble() / (numSamples * 0.1)
                        } else {
                            (numSamples - i).toDouble() / (numSamples * 0.9)
                        }
                        val sample = sin(2.0 * Math.PI * freq * t) * envelope
                        val clamped = (sample * 24000).toInt().coerceIn(-32767, 32767)
                        val index = sampleOffset + i
                        if (index < buffer.size) {
                            buffer[index] = clamped.toShort()
                        }
                    }
                    sampleOffset += numSamples
                }

                audioTrack = AudioTrack.Builder()
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

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                kotlinx.coroutines.delay(totalDurationMs.toLong() + 30)
            } catch (_: Exception) {
                // Ignore audio errors gracefully
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun playPopTone() {
        playPcmTones(listOf(Pair(587.33, 60), Pair(880.0, 80))) // D5 -> A5
    }

    fun playSuccessTone() {
        playPcmTones(listOf(Pair(523.25, 90), Pair(659.25, 90), Pair(783.99, 110), Pair(1046.50, 160))) // C5, E5, G5, C6
    }

    fun playWrongTone() {
        playPcmTones(listOf(Pair(220.0, 90), Pair(180.0, 140))) // Low buzzing
    }

    fun playHintTone() {
        playPcmTones(listOf(Pair(880.0, 80), Pair(1174.66, 80), Pair(1396.91, 140))) // A5, D6, F6
    }

    fun playPaintSplashTone() {
        playPcmTones(listOf(Pair(440.0, 40), Pair(660.0, 70), Pair(550.0, 50)))
    }

    fun playFanfare() {
        playPcmTones(
            listOf(
                Pair(523.25, 120),
                Pair(659.25, 120),
                Pair(783.99, 120),
                Pair(1046.50, 250),
                Pair(783.99, 100),
                Pair(1046.50, 400)
            )
        )
    }

    fun release() {
        try {
            scope.cancel()
        } catch (_: Exception) {}
    }
}
