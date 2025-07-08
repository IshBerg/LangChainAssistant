/**
 * Project: Yakki
 * File: FakeLanguageRepository.kt
 * Version: 1.1.0 (Merged)
 * Last Updated: 11 июня 2025 г.
 */
package com.yakki.fakes

import com.yakki.data.Language
import com.yakki.repository.LanguageRepository // ИЗМЕНЕНО: Путь импорта соответствует новой архитектуре

/**
 * Фейковая реализация репозитория языков для использования в тестах.
 */
class FakeLanguageRepository : LanguageRepository {

    /**
     * Возвращает заранее определенный набор языков.
     * ИСПОЛЬЗОВАНО: Ваш набор данных с ивритом и точными кодами.
     */
    override fun getLanguages(): Map<String, Language> {
        return mapOf(
            "en" to Language(name = "English", code = "en-US", apiCode = "en"),
            "ru" to Language(name = "Русский", code = "ru-RU", apiCode = "ru"),
            "he" to Language(name = "עברית", code = "he-IL", apiCode = "he")
        )
    }
}