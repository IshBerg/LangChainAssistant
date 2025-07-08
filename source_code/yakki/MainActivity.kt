/**
 * Проект: Yakki
 * Путь:   com.yakki.ui/
 * Файл:   MainActivity.kt
 * Версия: v4.1
 * Автор:  Gemini
 * Дата:   01-Jul-2025
 *
 * Что изменено:
 *   - Класс аннотирован @AndroidEntryPoint.
 *   - Удалена ручная ViewModelProvider.Factory.
 *   - ViewModel теперь получается через hiltViewModel().
 *   - Удалены все импорты и поля, связанные с ручной инициализацией.
 *
 * Почему:
 *   - Для завершения миграции на DI Hilt на уровне UI и устранения
 *     ручного создания зависимостей.
 */
package com.yakki.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yakki.BuildConfig
import com.yakki.ui.screens.MainScreen
import com.yakki.ui.theme.YakkiTheme
import com.yakki.viewmodel.YakkiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logKeys()

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                // TODO: Показать пользователю сообщение о необходимости разрешения
                println("Permission for RECORD_AUDIO was denied.")
            }
        }

        setContent {
            YakkiTheme {
                // ViewModel теперь получается от Hilt
                val viewModel: YakkiViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }

                MainScreen(
                    state                 = state,
                    onLanguageSelected    = viewModel::onLanguageSelected,
                    onStartStopPressed    = viewModel::onStartStopPressed,
                    onSwitchUserPressed   = viewModel::onSwitchUserPressed,
                    onInputDeviceSelected = viewModel::onInputDeviceSelected,
                    onOutputDeviceSelected= viewModel::onOutputDeviceSelected,
                    onDismissError        = viewModel::onDismissError
                )
            }
        }
    }

    /** Быстрый вывод ключей/URL-ов в Logcat для отладки */
    private fun logKeys() {
        println("OPENAI KEY     = ${BuildConfig.OPENAI_API_KEY.take(10)}…")
        println("GOOGLE KEY     = ${BuildConfig.GOOGLE_API_KEY.take(10)}…")
        println("AZURE  KEY     = ${BuildConfig.AZURE_API_KEY.take(10)}…")
        println("WHISPER URL    = ${BuildConfig.WHISPER_API_URL}")

}