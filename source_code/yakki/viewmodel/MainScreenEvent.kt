package com.yakki.viewmodel

import com.yakki.data.Language
import com.yakki.data.User

/**
 * Запечатанный класс, представляющий все возможные действия пользователя на главном экране.
 * UI создает один из этих объектов и отправляет его в ViewModel для обработки.
 */
sealed class MainScreenEvent {
    // Событие: Пользователь выбрал язык
    data class LanguageSelected(val user: User, val language: Language) : MainScreenEvent()

    // Событие: Пользователь нажал кнопку Start или Stop
    data class StartStopPressed(val user: User) : MainScreenEvent()

    // Событие: Пользователь нажал кнопку смены активного пользователя
    object SwitchUserPressed : MainScreenEvent()
}