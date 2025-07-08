/**
 * Project: Yakki
 * File: ScreenState.kt
 * Version: 1.2
 * Last Updated: 11 июня 2025 г.
 */

package com.yakki.data

enum class User { USER_1, USER_2 }
enum class ButtonState { READY, RECORDING }

data class DialogueLine(
    val user: User,
    val text: String
)

/**
 * Основное состояние UI. Является "источником правды" для всего интерфейса.
 */
data class ScreenState(
    val activeUser: User = User.USER_1,

    val user1_selectedLanguage: Language? = null,
    val user2_selectedLanguage: Language? = null,

    val user1_selectedInput: AudioDevice? = null,
    val user1_selectedOutput: AudioDevice? = null,
    val user2_selectedInput: AudioDevice? = null,
    val user2_selectedOutput: AudioDevice? = null,

    val availableInputs: List<AudioDevice> = emptyList(),
    val availableOutputs: List<AudioDevice> = emptyList(),

    // --- ВОТ ЧТО НУЖНО ДОБАВИТЬ ---
    // Это поле необходимо для выпадающего списка выбора языков
    val availableLanguages: List<Language> = emptyList(),

    val dialogue: List<DialogueLine> = emptyList(),

    val user1_buttonState: ButtonState = ButtonState.READY,
    val user2_buttonState: ButtonState = ButtonState.READY,

    val user1_voiceAmplitude: Float = 0f,
    val user2_voiceAmplitude: Float = 0f,

    val errorMessage: String? = null
) {
    companion object {
        val initial = ScreenState()
    }
}