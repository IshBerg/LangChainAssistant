/**
 * Проект: Yakki
 * Файл: FakeAudioRecorder.kt
 * Версия: 2.0
 * Последнее обновление: 27 июня 2025 г.
 * Статус: ✅ Готово к интеграции
 */

package com.yakki.fakes

import com.yakki.audio.*
import com.yakki.data.AudioDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class FakeAudioRecorder : IAudioRecorder {

    private val _recordingState = MutableStateFlow<RecordingResult>(RecordingResult.Idle)
    override val recordingState: StateFlow<RecordingResult> = _recordingState.asStateFlow()

    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun startRecording(config: RecorderConfig, device: AudioDevice?) {
        recordingJob = scope.launch {
            delay(500) // имитируем задержку записи
            val fakeFile = File("fake_output.wav")

            _recordingState.value = RecordingResult.Completed(
                internalFile = fakeFile,
                publicFile = fakeFile,
                duration = 1200L,
                size = 1024L
            )
        }
    }

    override suspend fun stopRecording() {
        recordingJob?.cancel()
        _recordingState.value = RecordingResult.Idle
    }

    override fun release() {
        recordingJob?.cancel()
    }
}
