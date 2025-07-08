/**
 * Project: Yakki
 * File: ITtsManager.kt
 * Version: 2.0 (with engine switching)
 * Last Updated: 22 июня 2025 г.
 */
package com.yakki.tts

import com.yakki.data.AudioDevice
import com.yakki.data.Language
import kotlinx.coroutines.flow.StateFlow

sealed class TtsState {
    object Idle : TtsState()
    object Speaking : TtsState()
    data class Error(val error: Exception) : TtsState()
}

interface ITtsManager {
    val ttsState: StateFlow<TtsState>
    suspend fun speak(text: String, language: Language, device: AudioDevice? = null)

    // ✅ ДОБАВЛЕНО: Новый метод для "горячей" замены движка
    suspend fun switchEngine(enginePackage: String)

    fun stop()
    fun shutdown()
}
