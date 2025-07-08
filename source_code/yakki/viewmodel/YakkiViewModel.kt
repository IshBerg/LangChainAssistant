/**
 * Проект: Yakki
 * Путь:   com.yakki.viewmodel/
 * Файл:   YakkiViewModel.kt
 * Версия: v5.2
 * Автор:  Gemini
 * Дата:   01-Jul-2025
 *
 * Что изменено:
 *   - Восстановлен вызов сервиса перевода (networkClient.translateText)
 *     в методе handleTranscriptionResult.
 *
 * Почему:
 *   - Для исправления логической ошибки, обнаруженной в ходе ревью v5.1,
 *     и восстановления полного функционального цикла STT -> Translate -> TTS.
 */
package com.yakki.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yakki.audio.IAudioRecorder
import com.yakki.audio.RecorderConfig
import com.yakki.audio.RecordingResult
import com.yakki.data.*
import com.yakki.device.IAudioDeviceManager
import com.yakki.network.INetworkClient
import com.yakki.player.IAudioPlayerManager
import com.yakki.repository.LanguageRepository
import com.yakki.tts.ITtsManager
import com.yakki.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class YakkiViewModel @Inject constructor(
    private val networkClient: INetworkClient,
    private val audioRecorder: IAudioRecorder,
    private val audioDeviceManager: IAudioDeviceManager,
    private val ttsManager: ITtsManager,
    private val languageRepository: LanguageRepository,
    private val playerManager: IAudioPlayerManager,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenState.initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        loadAvailableLanguagesAndSetDefaults()
        audioDeviceManager.startMonitoring()

        viewModelScope.launch {
            audioRecorder.recordingState.collect(::handleRecordingResult)
        }
        viewModelScope.launch {
            ttsManager.ttsState.collect {
                if (it is TtsState.Error) {
                    updateErrorMessage(it.error.message)
                }
            }
        }
        viewModelScope.launch {
            combine(
                audioDeviceManager.availableInputDevices,
                audioDeviceManager.availableOutputDevices
            ) { inputs, outputs ->
                _state.update { currentState ->
                    currentState.copy(
                        availableInputs = inputs,
                        availableOutputs = outputs
                    )
                }
            }.collect()
        }
    }

    internal fun handleRecordingResult(result: RecordingResult) {
        when (result) {
            is RecordingResult.Completed -> {
                println("VIEWMODEL: Recording completed. Processing file...")
                val fileToProcess = result.publicFile
                handleSuccessfulRecording(fileToProcess)
            }
            is RecordingResult.Recording -> {
                _state.update { currentState ->
                    if (currentState.activeUser == User.USER_1) {
                        currentState.copy(user1_voiceAmplitude = result.normalizedLevel)
                    } else {
                        currentState.copy(user2_voiceAmplitude = result.normalizedLevel)
                    }
                }
            }
            is RecordingResult.Error -> {
                updateErrorMessage(result.error.message)
                stopAllButtonStates()
            }
            is RecordingResult.Idle -> {
                stopAllButtonStates()
            }
            is RecordingResult.Started, is RecordingResult.Stopping -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioDeviceManager.stopMonitoring()
        audioRecorder.release()
        println("VIEWMODEL: Cleared and resources released.")
    }

    private fun loadAvailableLanguagesAndSetDefaults() {
        viewModelScope.launch {
            val languagesMap = withContext(ioDispatcher) { languageRepository.getLanguages() }
            _state.update {
                it.copy(
                    availableLanguages = languagesMap.values.toList(),
                    user1_selectedLanguage = languagesMap["en"],
                    user2_selectedLanguage = languagesMap["ru"]
                )
            }
        }
    }

    fun onSwitchUserPressed() {
        if (_state.value.user1_buttonState == ButtonState.RECORDING || _state.value.user2_buttonState == ButtonState.RECORDING) return
        _state.update { it.copy(activeUser = it.activeUser.opposite()) }
    }

    fun onLanguageSelected(user: User, language: Language) {
        _state.update { it.withLanguage(user, language) }
    }

    fun onInputDeviceSelected(user: User, device: AudioDevice) {
        viewModelScope.launch { audioDeviceManager.selectInputDevice(device) }
        _state.update { it.withInputDevice(user, device) }
    }

    fun onOutputDeviceSelected(user: User, device: AudioDevice) {
        viewModelScope.launch { audioDeviceManager.selectOutputDevice(device) }
        _state.update { it.withOutputDevice(user, device) }
    }

    fun onStartStopPressed() {
        val activeUser = _state.value.activeUser
        val buttonState = _state.value.buttonStateFor(activeUser)

        if (buttonState == ButtonState.READY) {
            onStartRecording(activeUser)
        } else {
            onStopRecording()
        }
    }

    fun onDismissError() {
        updateErrorMessage(null)
    }

    private fun updateErrorMessage(message: String?) {
        _state.update { it.copy(errorMessage = message) }
    }

    private fun onStartRecording(user: User) {
        if (_state.value.user1_buttonState == ButtonState.RECORDING || _state.value.user2_buttonState == ButtonState.RECORDING) return

        val language = _state.value.languageFor(user) ?: return
        val device = _state.value.inputDeviceFor(user)

        _state.update { it.withButtonState(user, ButtonState.RECORDING) }

        viewModelScope.launch {
            audioRecorder.startRecording(RecorderConfig(), device)
        }
    }

    private fun onStopRecording() {
        viewModelScope.launch {
            audioRecorder.stopRecording()
        }
    }

    private fun stopAllButtonStates() {
        _state.update {
            it.copy(
                user1_buttonState = ButtonState.READY,
                user2_buttonState = ButtonState.READY,
                user1_voiceAmplitude = 0f,
                user2_voiceAmplitude = 0f
            )
        }
    }

    private fun handleSuccessfulRecording(file: File) {
        val currentState = _state.value
        val sourceUser = currentState.activeUser
        val targetUser = sourceUser.opposite()

        val sourceLang = currentState.languageFor(sourceUser) ?: return
        val targetLang = currentState.languageFor(targetUser) ?: return
        val outputDevice = currentState.outputDeviceFor(targetUser)

        viewModelScope.launch {
            try {
                val fileBytes = withContext(ioDispatcher) { file.readBytes() }
                val sttResult = withContext(ioDispatcher) {
                    networkClient.transcribeAudio(fileBytes, sourceLang)
                }
                handleTranscriptionResult(sttResult, sourceUser, targetUser, sourceLang, targetLang, outputDevice)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    updateErrorMessage(e.message)
                }
                stopAllButtonStates() // Убедимся, что кнопки сбрасываются даже при ошибке чтения файла
            }
        }
    }

    private suspend fun handleTranscriptionResult(
        result: kotlin.Result<String>,
        sourceUser: User,
        targetUser: User,
        sourceLang: Language,
        targetLang: Language,
        outputDevice: AudioDevice?
    ) {
        result.fold(
            onSuccess = { recognized ->
                println("VIEWMODEL: 📝 Распознанный текст: $recognized")
                _state.update { it.withNewLine(sourceUser, recognized) }

                // 🔄 ВОССТАНОВЛЕН ВЫЗОВ ПЕРЕВОДА
                val translateResult = withContext(ioDispatcher) {
                    networkClient.translateText(recognized, sourceLang, targetLang)
                }
                handleTranslationResult(translateResult, targetUser, targetLang, outputDevice)
            },
            onFailure = { e ->
                if (e !is CancellationException) {
                    updateErrorMessage(e.message)
                }
                stopAllButtonStates()
            }
        )
    }

    private suspend fun handleTranslationResult(
        result: kotlin.Result<String>,
        targetUser: User,
        targetLang: Language,
        device: AudioDevice?
    ) {
        result.fold(
            onSuccess = { translated ->
                _state.update { it.withNewLine(targetUser, translated) }
                ttsManager.speak(translated, targetLang, device)
                // Сброс кнопок происходит после успешной озвучки
                stopAllButtonStates()
            },
            onFailure = { e ->
                if (e !is CancellationException) {
                    updateErrorMessage(e.message)
                }
                // Сброс кнопок также происходит при ошибке перевода/озвучки
                stopAllButtonStates()
            }
        )
    }
}