/**
 * Проект : Yakki
 * Файл   : LanguageRepository.kt
 * Версия : 1.0 (initial)
 * Дата   : 30 июня 2025 г.
 * Статус : ✅ Готово к интеграции
 */
package com.yakki.repository

import com.yakki.data.Language
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    /** Горячий Flow со всеми языками, отсортированными по названию */
    fun getAll(): Flow<List<Language>>

    /** Сохранить/обновить список языков единым батчем */
    suspend fun upsertAll(languages: List<Language>)
}
