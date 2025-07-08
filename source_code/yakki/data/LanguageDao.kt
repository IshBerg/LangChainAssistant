/**
 * Проект : Yakki
 * Файл   : LanguageDao.kt
 * Версия : 1.2 (fix column name)
 * Дата   : 02 июл 2025 г.
 *
 * Изменено v1.2
 *   • Запрос getById теперь использует колонку apiCode
 *     (соответствует полю в Entity).
 */
package com.yakki.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {

    @Query("SELECT * FROM languages ORDER BY displayName")
    fun getAllFlow(): Flow<List<Language>>

    @Query("SELECT * FROM languages ORDER BY displayName")
    suspend fun getAll(): List<Language>

    @Query("SELECT * FROM languages WHERE apiCode = :code LIMIT 1")   // 🔄 исправлен столбец
    suspend fun getById(code: String): Language?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: Language)

    @Query("DELETE FROM languages")
    suspend fun deleteAll()
}
