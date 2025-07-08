/**
 * Project: Yakki
 * File: FakeTtsManager.kt
 * Version: 1.3 (SwitchEngine Fix)
 * Last Updated: 22 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.fakes

import com.yakki.data.AudioDevice
import com.yakki.data.Language
import com.yakki.tts.ITtsManager
import com.yakki.tts.TtsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTtsManager : ITtsManager {
    private val _ttsState = MutableStateFlow<TtsState>(TtsState.Idle)
    override val ttsState = _ttsState.asStateFlow()

    var speakCallCount = 0
    var lastSpokenText: String? = null
    var lastSpokenLanguage: Language? = null

    override suspend fun speak(text: String, language: Language, device: AudioDevice?) {
        speakCallCount++
        lastSpokenText = text
        lastSpokenLanguage = language

        println("--- FAKE TTS ---")
        println("SPEAKING: '$text'")
        println("LANGUAGE: ${language.name} (${language.apiCode})")
        println("DEVICE: ${device?.name ?: "Default"}")
        println("----------------")

        _ttsState.value = TtsState.Speaking
        delay(500)
        _ttsState.value = TtsState.Idle
    }

    // ✅ ИСПРАВЛЕНО: Добавлена пустая реализация для соответствия интерфейсу
    override suspend fun switchEngine(enginePackage: String) {
        println("FAKE TTS: SwitchEngine called for $enginePackage. Doing nothing.")
        // В фейковой реализации ничего не делаем
    }

    override fun stop() {
        _ttsState.value = TtsState.Idle
        println("--- FAKE TTS STOPPED ---")
    }

    override fun shutdown() { /* Ничего не делаем */ }
}
