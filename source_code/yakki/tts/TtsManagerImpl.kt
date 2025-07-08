/**
 * Project: Yakki
 * File: TtsManagerImpl.kt
 * Version: 2.0 (with Engine Switching)
 * Last Updated: 22 июня 2025 г.
 * Status: ✅ Готово к интеграции
 */
package com.yakki.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.yakki.data.AudioDevice
import com.yakki.data.Language
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TtsManagerImpl(private val context: Context) : ITtsManager, TextToSpeech.OnInitListener {

    private val _ttsState = MutableStateFlow<TtsState>(TtsState.Idle)
    override val ttsState = _ttsState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechContinuation: CancellableContinuation<Unit>? = null
    private var initContinuation: CancellableContinuation<Unit>? = null
    private var isInitialized = false

    init {
        // Инициализируем с движком по умолчанию
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _ttsState.value = TtsState.Speaking
                }

                override fun onDone(utteranceId: String?) {
                    speechContinuation?.resume(Unit)
                    speechContinuation = null
                    _ttsState.value = TtsState.Idle
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    speechContinuation?.resumeWithException(TtsException("TTS Error for utterance: $utteranceId"))
                    speechContinuation = null
                    _ttsState.value = TtsState.Idle
                }
            })
            // Если мы ждали завершения инициализации, сообщаем об успехе
            initContinuation?.resume(Unit)
            initContinuation = null
        } else {
            val error = TtsException("TTS Initialization Failed")
            _ttsState.value = TtsState.Error(error)
            // Если мы ждали завершения инициализации, сообщаем об ошибке
            initContinuation?.resumeWithException(error)
            initContinuation = null
        }
    }

    override suspend fun speak(text: String, language: Language, device: AudioDevice?) {
        if (!isInitialized) {
            val error = TtsException("TTS not initialized")
            _ttsState.value = TtsState.Error(error)
            throw error
        }

        return suspendCancellableCoroutine { cont ->
            speechContinuation = cont
            val locale = when (language.apiCode) {
                "ru" -> Locale("ru")
                "he" -> Locale("he")
                "en" -> Locale.ENGLISH
                else -> Locale.ENGLISH
            }
            tts?.language = locale

            val utteranceId = UUID.randomUUID().toString()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

            cont.invokeOnCancellation {
                tts?.stop()
            }
        }
    }

    override suspend fun switchEngine(enginePackage: String) {
        stop()
        tts?.shutdown()
        isInitialized = false
        _ttsState.value = TtsState.Idle

        return suspendCancellableCoroutine { cont ->
            initContinuation = cont
            // Создаем новый TTS с указанным пакетом и ждем завершения инициализации через onInit
            tts = TextToSpeech(context, this, enginePackage)
        }
    }

    override fun stop() {
        speechContinuation?.cancel()
        speechContinuation = null
        tts?.stop()
        _ttsState.value = TtsState.Idle
    }

    override fun shutdown() {
        tts?.shutdown()
    }
}

class TtsException(message: String) : Exception(message)
