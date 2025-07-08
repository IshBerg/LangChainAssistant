/**
 * Проект: Yakki
 * Файл: AudioPlayerManagerImpl.kt
 * Версия: 1.0
 * Последнее обновление: 25 июня 2025 г.
 * Статус: ✅ Готово к интеграции
 */
package com.yakki.player

import android.content.Context
import android.media.MediaPlayer
import java.io.File

class AudioPlayerManagerImpl(private val context: Context) : IAudioPlayerManager {
    override fun play(file: File, onComplete: () -> Unit) {
        try {
            MediaPlayer().apply {
                setDataSource(file.absolutePath)
                // Устанавливаем слушателя, который вызовет onComplete после завершения
                setOnCompletionListener {
                    it.release()
                    println("PLAYER: MediaPlayer released on completion.")
                    onComplete()
                }
                // Добавляем слушателя ошибок для надежности
                setOnErrorListener { _, _, _ ->
                    release()
                    println("PLAYER: MediaPlayer released on error.")
                    onComplete() // Вызываем onComplete даже в случае ошибки
                    true
                }
                setOnPreparedListener { player ->
                    player.start()
                    println("PLAYER: Started playing ${file.name}")
                }
                prepareAsync()
                println("PLAYER: MediaPlayer preparing to play ${file.name}")
            }
        } catch (e: Exception) {
            println("PLAYER: Error setting up player: ${e.message}")
            e.printStackTrace()
            onComplete() // Гарантированно вызываем onComplete
        }
    }
}
