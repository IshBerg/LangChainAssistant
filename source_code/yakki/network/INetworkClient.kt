/**
 * Project: Yakki
 * File: INetworkClient.kt
 * Version: 1.1 (Cleaned)
 * Last Updated: 22 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.network

import com.yakki.data.Language

/**
 * Контракт для компонента, отвечающего за все сетевые операции,
 * такие как распознавание речи и перевод текста.
 */
interface INetworkClient {

    /**
     * Отправляет аудиоданные на сервер для распознавания.
     * @param audioData Аудио в виде массива байт.
     * @param sourceLanguage Язык, на котором записано аудио.
     * @return Возвращает Result с распознанным текстом или ошибкой.
     */
    suspend fun transcribeAudio(audioData: ByteArray, sourceLanguage: Language): Result<String>

    /**
     * Отправляет текст на сервер для перевода.
     * @param text Исходный текст для перевода.
     * @param targetLanguage Язык, на который нужно перевести.
     * @return Возвращает Result с переведенным текстом или ошибкой.
     */
    suspend fun translateText(text: String, targetLanguage: Language): Result<String>
}
