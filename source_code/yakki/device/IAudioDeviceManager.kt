/**
 * Project: Yakki
 * File: IAudioDeviceManager.kt
 * Version: 2.0 (DeviceType model)
 * Last Updated: 21 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.device

import com.yakki.data.AudioDevice
import kotlinx.coroutines.flow.StateFlow

/**
 * Контракт для управления доступными аудиоустройствами, выбором микрофона и динамика.
 */
interface IAudioDeviceManager {

    /** Поток доступных устройств ввода (микрофонов). */
    val availableInputDevices: StateFlow<List<AudioDevice>>

    /** Поток доступных устройств вывода (динамиков, наушников). */
    val availableOutputDevices: StateFlow<List<AudioDevice>>

    /** Поток текущего выбранного микрофона. */
    val selectedInputDevice: StateFlow<AudioDevice?>

    /** Поток текущего выбранного динамика. */
    val selectedOutputDevice: StateFlow<AudioDevice?>

    /** Запустить мониторинг изменений списка устройств. */
    fun startMonitoring()

    /** Остановить мониторинг изменений. */
    fun stopMonitoring()

    /** Установить выбранное устройство ввода. */
    suspend fun selectInputDevice(device: AudioDevice)

    /** Установить выбранное устройство вывода. */
    suspend fun selectOutputDevice(device: AudioDevice)

    /** Сбросить выбор микрофона к значению по умолчанию. */
    suspend fun resetToDefaultInput()

    /** Сбросить выбор динамика к значению по умолчанию. */
    suspend fun resetToDefaultOutput()
}
