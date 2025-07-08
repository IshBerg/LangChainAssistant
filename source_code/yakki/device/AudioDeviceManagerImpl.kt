/**
 * Project: Yakki
 * File: AudioDeviceManagerImpl.kt
 * Version: 2.1 (Duplicate Fix)
 * Last Updated: 21 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.device

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.yakki.data.AudioDevice
import com.yakki.data.DeviceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioDeviceManagerImpl(
    private val context: Context
) : IAudioDeviceManager {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _availableInputDevices = MutableStateFlow<List<AudioDevice>>(emptyList())
    override val availableInputDevices: StateFlow<List<AudioDevice>> = _availableInputDevices.asStateFlow()

    private val _availableOutputDevices = MutableStateFlow<List<AudioDevice>>(emptyList())
    override val availableOutputDevices: StateFlow<List<AudioDevice>> = _availableOutputDevices.asStateFlow()

    private val _selectedInputDevice = MutableStateFlow<AudioDevice?>(null)
    override val selectedInputDevice: StateFlow<AudioDevice?> = _selectedInputDevice.asStateFlow()

    private val _selectedOutputDevice = MutableStateFlow<AudioDevice?>(null)
    override val selectedOutputDevice: StateFlow<AudioDevice?> = _selectedOutputDevice.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            refreshDeviceLists()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            refreshDeviceLists()
        }
    }

    override fun startMonitoring() {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        refreshDeviceLists()
    }

    override fun stopMonitoring() {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    private fun refreshDeviceLists() {
        // ✅ ИСПРАВЛЕНО: Добавлена фильтрация для удаления дубликатов по имени
        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .mapNotNull { it.toAudioDevice() }
            .distinctBy { it.name }

        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .mapNotNull { it.toAudioDevice() }
            .distinctBy { it.name }

        scope.launch {
            _availableInputDevices.value = inputs
            _availableOutputDevices.value = outputs

            if (_selectedInputDevice.value == null) {
                _selectedInputDevice.value = inputs.firstOrNull()
            }
            if (_selectedOutputDevice.value == null) {
                _selectedOutputDevice.value = outputs.firstOrNull { it.type == DeviceType.PHONE_SPEAKER }
                    ?: outputs.firstOrNull()
            }
        }
    }

    override suspend fun selectInputDevice(device: AudioDevice) {
        _selectedInputDevice.value = device
    }

    override suspend fun selectOutputDevice(device: AudioDevice) {
        _selectedOutputDevice.value = device
    }

    override suspend fun resetToDefaultInput() {
        _selectedInputDevice.value = availableInputDevices.value.firstOrNull()
    }

    override suspend fun resetToDefaultOutput() {
        _selectedOutputDevice.value = availableOutputDevices.value.firstOrNull()
    }

    private fun AudioDeviceInfo.toAudioDevice(): AudioDevice? {
        val deviceType = when (this.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> DeviceType.PHONE_SPEAKER
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> DeviceType.EARPIECE
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> DeviceType.BLUETOOTH_HEADSET
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> DeviceType.INTERNAL_MIC
            else -> return null // не используем неизвестные типы
        }

        return AudioDevice(
            id = this.id,
            name = this.productName?.toString() ?: "Unknown Device",
            type = deviceType
        )
    }
}
