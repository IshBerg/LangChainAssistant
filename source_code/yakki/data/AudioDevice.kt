/**
 * Project: Yakki
 * File: AudioDevice.kt
 * Version: 3.0 (with DeviceType enum)
 * Last Updated: 21 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.data

/**
 * Перечисление, описывающее базовые типы аудиоустройств для упрощения логики.
 */
enum class DeviceType {
    PHONE_SPEAKER,
    EARPIECE,
    WIRED_HEADSET,
    BLUETOOTH_HEADSET,
    INTERNAL_MIC
}

/**
 * Упрощенная модель данных для аудиоустройства, используемая в UI и ViewModel.
 */
data class AudioDevice(
    val id: Int,
    val name: String,
    val type: DeviceType
)
