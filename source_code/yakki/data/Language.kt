/**
 * Проект : Yakki
 * Файл   : Language.kt
 * Версия : 1.0 (Room-Entity)
 * Дата   : 30 июня 2025 г.
 * Статус : ✅ Новый файл — готов к добавлению
 */
package com.yakki.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Справочник поддерживаемых языков
 *
 * @param apiCode     код для API (“en”, “ru”…)
 * @param displayName человекочитаемое имя (“English”, “Русский”…)
 */
@Entity(tableName = "languages")
data class Language(
    @PrimaryKey val apiCode: String,
    val displayName: String
)
