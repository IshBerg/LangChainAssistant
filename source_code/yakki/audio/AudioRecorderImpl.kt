/**
 * Проект: Yakki
 * Файл: AudioRecorderImpl.kt
 * Версия: 4.4.2 (Architecture Fix)
 * Последнее обновление: 27 июня 2025 г.
 * Статус: 🟢 Готово к интеграции
 */
package com.yakki.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import com.yakki.data.AudioDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

class AudioRecorderImpl(private val context: Context) : IAudioRecorder {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _recordingState = MutableStateFlow<RecordingResult>(RecordingResult.Idle)
    override val recordingState = _recordingState.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private var lastOutputFile: File? = null
    private var totalBytesRead: Long = 0L

    // ✅ ИСПРАВЛЕНО v4.4.2: Храним активную конфигурацию здесь, а не в ViewModel
    private var activeConfig: RecorderConfig = RecorderConfig()

    @SuppressLint("MissingPermission")
    override suspend fun startRecording(config: RecorderConfig, device: AudioDevice?) {
        if (_recordingState.value !is RecordingResult.Idle) {
            println("RECORDER_DEBUG: State is not Idle. Stopping previous recording first.")
            stopRecording()
        }

        try {
            // ✅ ИСПРАВЛЕНО v4.4.2: Сохраняем конфигурацию для последующего использования
            this.activeConfig = config

            val audioFormat = if (config.encoding == AudioEncoding.PCM_16BIT)
                AudioFormat.ENCODING_PCM_16BIT
            else
                AudioFormat.ENCODING_PCM_8BIT

            val bufferSize = AudioRecord.getMinBufferSize(
                config.sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                audioFormat
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                config.sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                audioFormat,
                bufferSize
            )

            totalBytesRead = 0L
            lastOutputFile = File(context.cacheDir, "yakki_record_${System.currentTimeMillis()}.wav")

            audioRecord?.startRecording()
            _recordingState.value = RecordingResult.Started

            recordingJob = coroutineScope.launch {
                writeAudioDataToFile(lastOutputFile!!, bufferSize)
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                _recordingState.value = RecordingResult.Error(error = e)
            }
        }
    }

    private suspend fun writeAudioDataToFile(file: File, bufferSize: Int) {
        val buffer = ByteArray(bufferSize)
        try {
            FileOutputStream(file).use { fos ->
                while (coroutineContext.isActive) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readSize > 0) {
                        fos.write(buffer, 0, readSize)
                        totalBytesRead += readSize
                        val normalizedLevel = calculateAmplitude(buffer, readSize)
                        _recordingState.value = RecordingResult.Recording(normalizedLevel, normalizedLevel)
                    }
                }
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                _recordingState.value = RecordingResult.Error(error = e)
            }
        }
    }

    override suspend fun stopRecording() {
        if (recordingJob?.isActive == true) {
            _recordingState.value = RecordingResult.Stopping
            recordingJob?.cancelAndJoin()
        }

        if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord?.stop()
        }
        audioRecord?.release()
        audioRecord = null

        lastOutputFile?.let { file ->
            if (file.exists() && totalBytesRead > 0) {
                println("RECORDER_DEBUG: Finalizing file. Total bytes: $totalBytesRead")

                // ✅ ИСПРАВЛЕНО v4.4.2: Используем сохраненный `activeConfig`
                writeWavHeader(file, activeConfig.sampleRate, totalBytesRead)
                val duration = (totalBytesRead * 1000) / (activeConfig.sampleRate * 2)

                val copiedFile = copyFileToDownloads(file)
                if (copiedFile != null) {
                    println("RECORDER_DEBUG: Emitting Completed state with public file path.")
                    _recordingState.value = RecordingResult.Completed(file, copiedFile, duration, totalBytesRead)
                } else {
                    _recordingState.value = RecordingResult.Error(error = Exception("Failed to copy file."))
                }
            }
        }

        delay(100)
        println("RECORDER_DEBUG: Emitting Idle state.")
        _recordingState.value = RecordingResult.Idle
    }

    override fun release() {
        println("RECORDER_DEBUG: Releasing AudioRecorderImpl resources.")
        coroutineScope.cancel()

        if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord?.stop()
        }
        audioRecord?.release()
        audioRecord = null
    }

    private fun copyFileToDownloads(sourceFile: File): File? {
        // ... (код без изменений)
        return try {
            val targetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Yakki")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val targetFile = File(targetDir, sourceFile.name)
            sourceFile.copyTo(targetFile, overwrite = true)
            println("RECORDER_DEBUG: File copied to ${targetFile.absolutePath}")
            targetFile
        } catch (e: Exception) {
            println("RECORDER_ERROR: Failed to copy file. Error: ${e.message}")
            null
        }
    }

    private fun calculateAmplitude(buffer: ByteArray, size: Int): Float {
        // ... (код без изменений)
        var sum = 0.0
        for (i in 0 until size step 2) {
            if (i + 1 < size) {
                val sample = ((buffer[i+1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                sum += sample * sample
            }
        }
        val rms = sqrt(sum / (size / 2))
        return (rms / 32767.0).toFloat()
    }

    private suspend fun writeWavHeader(file: File, sampleRate: Int, dataSize: Long) = withContext(Dispatchers.IO) {
        // ... (код с исправлением reverseBytes, без изменений)
        RandomAccessFile(file, "rw").use { raf ->
            val channels = 1
            val bitsPerSample = 16
            val byteRate = sampleRate * channels * bitsPerSample / 8
            val blockAlign = (channels * bitsPerSample / 8).toShort()
            raf.seek(0)
            raf.write(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()))
            raf.writeInt(java.lang.Integer.reverseBytes((dataSize + 36).toInt()))
            raf.write(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()))
            raf.write(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()))
            raf.writeInt(java.lang.Integer.reverseBytes(16))
            raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
            raf.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt())
            raf.writeInt(java.lang.Integer.reverseBytes(sampleRate))
            raf.writeInt(java.lang.Integer.reverseBytes(byteRate))
            raf.writeShort(java.lang.Short.reverseBytes(blockAlign).toInt())
            raf.writeShort(java.lang.Short.reverseBytes(bitsPerSample.toShort()).toInt())
            raf.write(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()))
            raf.writeInt(java.lang.Integer.reverseBytes(dataSize.toInt()))
        }
    }
}