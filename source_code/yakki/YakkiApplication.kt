/**
 * Проект: Yakki
 * Путь:   com.yakki/
 * Файл:   YakkiApplication.kt
 * Версия: v1.2
 * Автор:  Gemini
 * Дата:   01-Jul-2025
 *
 * Что изменено:
 *   - (v1.2) Уточнен формат пути в шапке до полного пути пакета.
 *   - (v1.0) Создан класс Application, аннотированный @HiltAndroidApp.
 *
 * Почему:
 *   - Для максимальной точности и однозначности документации.
 *   - (v1.0) Это обязательное требование для инициализации графа
 *     зависимостей Dagger-Hilt в приложении.
 */
package com.yakki

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YakkiApplication : Application()