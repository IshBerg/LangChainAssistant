/**
 * Проект: Yakki
 * Файл: IAudioPlayerManager.kt
 * Версия: 1.0
 * Последнее обновление: 25 июня 2025 г.
 * Статус: ✅ Готово к интеграции
 */
package com.yakki.player

import java.io.File

/**
 * Контракт для компонента, который умеет воспроизводить аудиофайлы.
 */
interface IAudioPlayerManager {
    /**
     * Воспроизводит указанный аудиофайл.
     * @param file Файл для воспроизведения.
     * @param onComplete Коллбэк, который будет вызван по завершении воспроизведения (или в случае ошибки).
     */
    fun play(file: File, onComplete: () -> Unit)
}
