/**
 * Project: Yakki
 * File: MainScreen.kt
 * Version: 1.8 (Device Selection UI)
 * Last Updated: 21 июня 2025 г.
 */
package com.yakki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yakki.data.*
import com.yakki.ui.theme.YakkiTheme

// ✅ ИЗМЕНЕНО: Добавлены обработчики для выбора устройств
@Composable
fun MainScreen(
    state: ScreenState,
    onLanguageSelected: (user: User, language: Language) -> Unit,
    onStartStopPressed: () -> Unit,
    onSwitchUserPressed: () -> Unit,
    onInputDeviceSelected: (user: User, device: AudioDevice) -> Unit,
    onOutputDeviceSelected: (user: User, device: AudioDevice) -> Unit,
    onDismissError: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isUser1Recording = state.user1_buttonState == ButtonState.RECORDING
            val isUser2Recording = state.user2_buttonState == ButtonState.RECORDING
            val isRecording = isUser1Recording || isUser2Recording

            // ✅ ИЗМЕНЕНО: Передаем в UserPanel новые данные и обработчики
            UserPanel(
                user = User.USER_1,
                isEnabled = !isRecording || isUser1Recording,
                isActiveUser = state.activeUser == User.USER_1,
                state = state,
                onLanguageSelected = { language -> onLanguageSelected(User.USER_1, language) },
                onStartStop = onStartStopPressed,
                onInputDeviceSelect = { device -> onInputDeviceSelected(User.USER_1, device) },
                onOutputDeviceSelect = { device -> onOutputDeviceSelected(User.USER_1, device) }
            )
            CentralControls(
                onSwitchUser = onSwitchUserPressed,
                isEnabled = !isRecording,
                onSttEngineSelect = { /* TODO */ },
                onTranslateEngineSelect = { /* TODO */ }
            )
            ChatHistory(
                modifier = Modifier.weight(1f),
                dialogue = state.dialogue
            )
            UserPanel(
                user = User.USER_2,
                isEnabled = !isRecording || isUser2Recording,
                isActiveUser = state.activeUser == User.USER_2,
                state = state,
                onLanguageSelected = { language -> onLanguageSelected(User.USER_2, language) },
                onStartStop = onStartStopPressed,
                onInputDeviceSelect = { device -> onInputDeviceSelected(User.USER_2, device) },
                onOutputDeviceSelect = { device -> onOutputDeviceSelected(User.USER_2, device) }
            )
        }
    }

    if (state.errorMessage != null) {
        ErrorAlertDialog(
            errorMessage = state.errorMessage,
            onDismiss = onDismissError
        )
    }
}


// ✅ ИЗМЕНЕНО: Сигнатура UserPanel расширена, добавлена логика DropdownMenu для устройств
@Composable
fun UserPanel(
    user: User,
    isEnabled: Boolean,
    isActiveUser: Boolean,
    state: ScreenState,
    onLanguageSelected: (Language) -> Unit,
    onStartStop: () -> Unit,
    onInputDeviceSelect: (AudioDevice) -> Unit,
    onOutputDeviceSelect: (AudioDevice) -> Unit
) {
    var langMenuExpanded by remember { mutableStateOf(false) }
    var inputMenuExpanded by remember { mutableStateOf(false) }
    var outputMenuExpanded by remember { mutableStateOf(false) }

    val isStartButtonEnabled = isEnabled && isActiveUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Button(onClick = { langMenuExpanded = true }, enabled = isEnabled) {
                Text(state.languageFor(user)?.name ?: "Язык")
            }
            DropdownMenu(
                expanded = langMenuExpanded,
                onDismissRequest = { langMenuExpanded = false }
            ) {
                state.availableLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language.name) },
                        onClick = {
                            onLanguageSelected(language)
                            langMenuExpanded = false
                        }
                    )
                }
            }
        }

        Button(onClick = onStartStop, enabled = isStartButtonEnabled) {
            val buttonState = state.buttonStateFor(user)
            Text(if (buttonState == ButtonState.READY) "START" else "STOP")
        }

        Row {
            Box {
                IconButton(onClick = { inputMenuExpanded = true }, enabled = isEnabled) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Выбор микрофона")
                }
                DropdownMenu(
                    expanded = inputMenuExpanded,
                    onDismissRequest = { inputMenuExpanded = false }
                ) {
                    state.availableInputs.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(device.name) },
                            onClick = {
                                onInputDeviceSelect(device)
                                inputMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { outputMenuExpanded = true }, enabled = isEnabled) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Выбор динамика")
                }
                DropdownMenu(
                    expanded = outputMenuExpanded,
                    onDismissRequest = { outputMenuExpanded = false }
                ) {
                    state.availableOutputs.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(device.name) },
                            onClick = {
                                onOutputDeviceSelect(device)
                                outputMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


// CentralControls, ChatHistory, ErrorAlertDialog и Preview остаются без изменений
@Composable
fun CentralControls(
    onSwitchUser: () -> Unit,
    isEnabled: Boolean,
    onSttEngineSelect: () -> Unit,
    onTranslateEngineSelect: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onSwitchUser, enabled = isEnabled) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Сменить пользователя", modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            OutlinedButton(onClick = onSttEngineSelect, enabled = isEnabled, modifier = Modifier.padding(horizontal = 4.dp)) { Text("STT: Cloud") }
            OutlinedButton(onClick = onTranslateEngineSelect, enabled =isEnabled, modifier = Modifier.padding(horizontal = 4.dp)) { Text("Translate: Cloud") }
        }
    }
}

@Composable
fun ChatHistory(
    dialogue: List<DialogueLine>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (dialogue.isEmpty()) {
            item {
                Text(
                    text = "...диалог появится здесь...",
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(dialogue) { line ->
                val alignment = if (line.user == User.USER_1) Alignment.Start else Alignment.End
                Text(
                    text = line.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = if (alignment == Alignment.Start) TextAlign.Left else TextAlign.Right
                )
            }
        }
    }
}


@Composable
fun ErrorAlertDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Произошла ошибка") },
        text = { Text(text = errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun MainScreenPreview() {
    YakkiTheme {
        val previewState = ScreenState()
        MainScreen(
            state = previewState,
            onLanguageSelected = { _, _ -> },
            onStartStopPressed = {},
            onSwitchUserPressed = {},
            onInputDeviceSelected = { _, _ -> },
            onOutputDeviceSelected = { _, _ -> },
            onDismissError = {}
        )
    }
}
