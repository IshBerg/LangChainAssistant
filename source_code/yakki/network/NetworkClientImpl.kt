//
// Проект : Yakki
// Файл   : NetworkClientImpl.kt
// Версия : 1.6 (rename ctor params + вернуть реализацию)
// Дата   : 30‑06‑2025
// Статус : ✅ Синхронизировано
//
package com.yakki.network

import com.yakki.data.Language
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class NetworkClientImpl(
    private val whisperApiKey: String,
    private val translateApiKey: String,
    private val baseWhisperUrl: String,
    private val baseTranslateUrl: String
) : INetworkClient {

    private val client = OkHttpClient()

    // ─────────────────────────── Whisper ────────────────────────────
    override suspend fun transcribeAudio(
        audioData: ByteArray,
        sourceLanguage: Language
    ): Result<String> = runCatching {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "audio.wav",
                audioData.toRequestBody("audio/wav".toMediaType())
            )
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart("language", sourceLanguage.apiCode)
            .build()

        val request = Request.Builder()
            .url(baseWhisperUrl)
            .header("Authorization", "Bearer $whisperApiKey")
            .post(body)
            .build()

        client.newCall(request).execute().use { resp ->
            val json = resp.body?.string() ?: ""
            if (!resp.isSuccessful) error("Whisper error ${resp.code}: $json")
            JSONObject(json).optString("text", "")
                .ifBlank { error("Whisper returned empty text") }
        }
    }

    // ───────────────────────── Google Translate ─────────────────────
    override suspend fun translateText(
        text: String,
        targetLanguage: Language
    ): Result<String> = runCatching {
        val payload = JSONObject()
            .put("q", text)
            .put("target", targetLanguage.apiCode)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseTranslateUrl?key=$translateApiKey")
            .post(payload)
            .build()

        client.newCall(request).execute().use { resp ->
            val json = resp.body?.string() ?: ""
            if (!resp.isSuccessful) error("Translate error ${resp.code}: $json")

            JSONObject(json)
                .getJSONObject("data")
                .getJSONArray("translations")
                .getJSONObject(0)
                .getString("translatedText")
                .ifBlank { error("Translate returned empty text") }
        }
    }
}
