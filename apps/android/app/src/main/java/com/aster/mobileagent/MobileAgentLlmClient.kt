package com.aster.mobileagent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Native Android port of MobileAgent/MotaAI's LLM chat transport.
 * The original project uses OpenAI-compatible Chat Completions + SSE streaming.
 * No API key is ever sent to the Aster/MCP server.
 */
class MobileAgentLlmClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun streamChat(
        baseUrl: String,
        model: String,
        apiKey: String,
        messages: List<Message>,
        onText: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val endpoint = buildEndpoint(baseUrl)
        val body = buildRequestBody(model, messages)
        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "text/event-stream")
            .post(body.toRequestBody(mediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val error = response.body?.string()?.take(500).orEmpty()
                throw IOException("Запрос к ИИ завершился с кодом ${response.code}: $error")
            }

            val source = response.body?.charStream()
                ?: throw IOException("ИИ не вернул поток ответа")
            val accumulated = StringBuilder()
            source.forEachLine { line ->
                val delta = parseDelta(line) ?: return@forEachLine
                accumulated.append(delta)
                onText(accumulated.toString())
            }

            if (accumulated.isEmpty()) {
                throw IOException("ИИ вернул пустой ответ")
            }
        }
    }

    private fun buildEndpoint(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        return if (normalized.endsWith("/chat/completions")) {
            normalized
        } else {
            "$normalized/chat/completions"
        }
    }

    private fun buildRequestBody(model: String, messages: List<Message>): String {
        val escapedMessages = messages.joinToString(",") { message ->
            "{\"role\":\"${escape(message.role)}\",\"content\":\"${escape(message.content)}\"}"
        }
        return "{\"model\":\"${escape(model)}\",\"messages\":[$escapedMessages],\"stream\":true}"
    }

    private fun parseDelta(line: String): String? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("data:")) return null
        val data = trimmed.removePrefix("data:").trim()
        if (data.isEmpty() || data == "[DONE]") return null

        return runCatching {
            val root: JsonObject = json.parseToJsonElement(data).jsonObject
            root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("delta")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
        }.getOrNull()
    }

    private fun escape(value: String): String = buildString(value.length + 8) {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }

    data class Message(val role: String, val content: String)
}
