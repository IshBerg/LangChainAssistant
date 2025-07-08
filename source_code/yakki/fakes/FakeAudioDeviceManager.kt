/**
 * Project: Yakki
 * File: FakeAudioDeviceManager.kt
 * Version: 1.1 (With Fake Data)
 * Last Updated: 21 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.fakes

import com.yakki.data.AudioDevice
import com.yakki.data.DeviceType
import com.yakki.device.IAudioDeviceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Фейковая реализация для тестов.
 * Предоставляет заранее заданный набор аудиоустройств.
 */
class FakeAudioDeviceManager : IAudioDeviceManager {

    // Создаем приватные, изменяемые потоки, чтобы мы могли менять выбранное устройство
    private val _selectedInput = MutableStateFlow<AudioDevice?>(null)
    private val _selectedOutput = MutableStateFlow<AudioDevice?>(null)

    // Определяем наши "игрушечные" устройства
    private val fakeInputs = listOf(
        AudioDevice(id = 1, name = "Встроенный микрофон", type = DeviceType.INTERNAL_MIC),
        AudioDevice(id = 2, name = "Микрофон гарнитуры", type = DeviceType.WIRED_HEADSET)
    )
    private val fakeOutputs = listOf(
        AudioDevice(id = 10, name = "Динамик телефона", type = DeviceType.PHONE_SPEAKER),
        AudioDevice(id = 11, name = "Наушники", type = DeviceType.WIRED_HEADSET)
    )

    // Предоставляем устройства наружу через StateFlow
    override val availableInputDevices: StateFlow<List<AudioDevice>> = MutableStateFlow(fakeInputs)
    override val availableOutputDevices: StateFlow<List<AudioDevice>> = MutableStateFlow(fakeOutputs)
    override val selectedInputDevice: StateFlow<AudioDevice?> = _selectedInput.asStateFlow()
    override val selectedOutputDevice: StateFlow<AudioDevice?> = _selectedOutput.asStateFlow()

    init {
        // При старте выбираем первое устройство в списке по умолчанию
        _selectedInput.value = fakeInputs.firstOrNull()
        _selectedOutput.value = fakeOutputs.firstOrNull()
    }

    override fun startMonitoring() {
        // В фейковой реализации ничего не делаем
    }

    override fun stopMonitoring() {
        // В фейковой реализации ничего не делаем
    }

    override suspend fun selectInputDevice(device: AudioDevice) {
        _selectedInput.value = device
    }

    override suspend fun selectOutputDevice(device: AudioDevice) {
        _selectedOutput.value = device
    }

    override suspend fun resetToDefaultInput() {
        _selectedInput.value = fakeInputs.firstOrNull()
    }

    override suspend fun resetToDefaultOutput() {
        _selectedOutput.value = fakeOutputs.firstOrNull()
    }
}
