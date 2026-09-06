package com.aster.mobileagent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Native Android integration of the MobileAgent/Mota chat protocol.
 *
 * The implementation intentionally keeps Aster's existing networking and device
 * control stack untouched. It only adds an OpenAI-compatible streaming chat layer
 * for the user-facing Russian assistant.
 */
class MobileAgentChatClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun stream(
        baseUrl: String,
        model: String,
        apiKey: String,
        messages: List<Message>,
        onText: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val url = baseUrl.trimEnd('/') + if (baseUrl.endsWith("/chat/completions")) "" else "/chat/completions"
        val payload = JSONObject().apply {
            put("model", model)
            put("stream", true)
            put("messages", JSONArray().apply {
                messages.forEach { message ->
                    put(JSONObject().apply {
                        put("role", message.role)
                        put("content", message.content)
                    })
                }
            })
            if (baseUrl.contains("moonshot", ignoreCase = true)) {
                put("thinking", JSONObject().put("type", "disabled"))
            } else {
                put("temperature", 0.7)
            }
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "text/event-stream")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val body = response.body?.string().orEmpty().take(500)
                throw IOException("Ошибка AI (${response.code}): ${body.ifBlank { "сервер не вернул описание ошибки" }}")
            }

            val source = response.body?.source() ?: throw IOException("Пустой ответ AI")
            var accumulated = ""
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                val trimmed = line.trim()
                if (!trimmed.startsWith("data:")) continue
                val data = trimmed.removePrefix("data:").trim()
                if (data == "[DONE]" || data.isBlank()) continue

                val delta = runCatching {
                    val root = JSONObject(data)
                    val choices = root.optJSONArray("choices") ?: return@runCatching ""
                    if (choices.length() == 0) return@runCatching ""
                    choices.optJSONObject(0)
                        ?.optJSONObject("delta")
                        ?.optString("content", "")
                        .orEmpty()
                }.getOrDefault("")

                if (delta.isNotEmpty()) {
                    accumulated += delta
                    onText(accumulated)
                }
            }

            if (accumulated.isBlank()) {
                throw IOException("AI вернул пустой ответ")
            }
        }
    }

    data class Message(val role: String, val content: String)
}
