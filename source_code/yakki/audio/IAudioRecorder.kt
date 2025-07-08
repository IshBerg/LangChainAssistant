/**
 * Project: Yakki
 * File: IAudioRecorder.kt
 * Version: 2.2 (Release Method Added)
 * Last Updated: 27 июня 2025 г.
 */
package com.yakki.audio

import com.yakki.data.AudioDevice
import kotlinx.coroutines.flow.StateFlow
import java.io.File

enum class AudioEncoding { PCM_16BIT, PCM_8BIT }

data class RecorderConfig(
    val silenceDetectionEnabled: Boolean = true,
    val speechTimeoutMillis: Long = 2000L,
    val sampleRate: Int = 16000,
    val encoding: AudioEncoding = AudioEncoding.PCM_16BIT
)

sealed class RecordingResult {
    object Idle : RecordingResult()
    object Started : RecordingResult()
    data class Recording(val amplitude: Float, val normalizedLevel: Float) : RecordingResult()
    object Stopping : RecordingResult()
    data class Completed(
        val internalFile: File,   // WAV-файл во внутренней директории (cacheDir)
        val publicFile: File,     // Копия WAV-файла в Download/Yakki
        val duration: Long,       // Длительность записи (мс)
        val size: Long            // Размер файла в байтах
    ) : RecordingResult()
    data class Error(val error: Exception) : RecordingResult()
}

interface IAudioRecorder {
    val recordingState: StateFlow<RecordingResult>
    suspend fun startRecording(config: RecorderConfig, device: AudioDevice? = null)
    suspend fun stopRecording()
    fun release() // ✅ Новый метод для освобождения ресурсов
}
