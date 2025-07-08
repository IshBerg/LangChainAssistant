/**
 * Project: Yakki
 * File: ScreenStateExtensions.kt
 * Version: 1.0
 * Last Updated: 11 июня 2025 г.
 */
package com.yakki.data

fun User.opposite(): User =
    if (this == User.USER_1) User.USER_2 else User.USER_1

fun ScreenState.withLanguage(user: User, language: Language): ScreenState =
    if (user == User.USER_1)
        this.copy(user1_selectedLanguage = language)
    else
        this.copy(user2_selectedLanguage = language)

fun ScreenState.languageFor(user: User): Language? =
    if (user == User.USER_1)
        this.user1_selectedLanguage
    else
        this.user2_selectedLanguage

fun ScreenState.inputDeviceFor(user: User): AudioDevice? =
    if (user == User.USER_1)
        this.user1_selectedInput
    else
        this.user2_selectedInput

fun ScreenState.outputDeviceFor(user: User): AudioDevice? =
    if (user == User.USER_1)
        this.user1_selectedOutput
    else
        this.user2_selectedOutput

fun ScreenState.withInputDevice(user: User, device: AudioDevice): ScreenState =
    if (user == User.USER_1)
        this.copy(user1_selectedInput = device)
    else
        this.copy(user2_selectedInput = device)

fun ScreenState.withOutputDevice(user: User, device: AudioDevice): ScreenState =
    if (user == User.USER_1)
        this.copy(user1_selectedOutput = device)
    else
        this.copy(user2_selectedOutput = device)

fun ScreenState.buttonStateFor(user: User): ButtonState =
    if (user == User.USER_1)
        this.user1_buttonState
    else
        this.user2_buttonState

fun ScreenState.withButtonState(user: User, state: ButtonState): ScreenState =
    if (user == User.USER_1)
        this.copy(user1_buttonState = state)
    else
        this.copy(user2_buttonState = state)

fun ScreenState.withNewLine(user: User, text: String): ScreenState =
    this.copy(dialogue = this.dialogue + DialogueLine(user, text))