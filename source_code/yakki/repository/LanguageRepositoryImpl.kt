/**
 * Проект : Yakki
 * Файл   : LanguageRepositoryImpl.kt
 * Версия : 1.0 (initial)
 * Дата   : 30 июня 2025 г.
 * Статус : ✅ Готово к интеграции
 */
package com.yakki.repository

import com.yakki.data.Language
import com.yakki.data.LanguageDao
import kotlinx.coroutines.flow.Flow

class LanguageRepositoryImpl(
    private val dao: LanguageDao,
) : LanguageRepository {

    override fun getAll(): Flow<List<Language>> = dao.getAll()

    override suspend fun upsertAll(languages: List<Language>) {
        dao.upsertAll(languages)
    }
}
