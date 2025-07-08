/**
 * Project: Yakki
 * File: FakeNetworkClient.kt
 * Version: 1.2 (Testable Version)
 * Last Updated: 12 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.fakes

import com.yakki.data.Language
import com.yakki.network.INetworkClient
import kotlinx.coroutines.delay

class FakeNetworkClient : INetworkClient {
    // ✅ ДОБАВЛЕНО: Свойства для управления ответами в тестах
    var transcriptionResponse: String = "Hello world"
    var translationResponse: String = "Привет, мир"
    var shouldSucceed: Boolean = true

    override suspend fun translateText(text: String, targetLanguage: Language): Result<String> {
        delay(300) // Имитация задержки сети
        println("FAKE NET: Translating '$text' to ${targetLanguage.apiCode}...")
        return if (shouldSucceed) {
            Result.success(translationResponse) // ✅ ИЗМЕНЕНО: Возвращаем тестовое свойство
        } else {
            Result.failure(RuntimeException("Fake network error: Translation failed"))
        }
    }

    override suspend fun transcribeAudio(audioData: ByteArray, sourceLanguage: Language): Result<String> {
        delay(500) // Имитация задержки распознавания
        println("FAKE NET: Transcribing audio from ${sourceLanguage.apiCode}...")
        return if (shouldSucceed) {
            Result.success(transcriptionResponse) // ✅ ИЗМЕНЕНО: Возвращаем тестовое свойство
        } else {
            Result.failure(RuntimeException("Fake network error: Transcription failed"))
        }
    }
}
