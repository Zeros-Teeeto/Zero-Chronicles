package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val text: String, val activeKeyIndex: Int) : GeminiResult()
    data class KeyWarning(val message: String, val currentKeyIndex: Int, val nextKeyIndex: Int) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
}

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun resolveModel(rawModel: String): String {
        val trimmed = rawModel.trim()
        return if (trimmed.isNotBlank()) trimmed else "gemini-2.5-flash"
    }

    suspend fun generateContent(
        modelName: String,
        keysPool: List<String>,
        startKeyIndex: Int,
        chatHistory: List<ChatMessage>,
        onStatusUpdate: ((String) -> Unit)? = null
    ): GeminiResult = withContext(Dispatchers.IO) {
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) { "" }

        // Collect all non-blank keys from user pool
        val validKeys = mutableListOf<Pair<Int, String>>()

        // 1. Add keys from user pool
        keysPool.forEachIndexed { index, k ->
            val trimmed = k.trim()
            if (trimmed.isNotBlank() && trimmed != "MY_GEMINI_API_KEY") {
                if (validKeys.none { it.second == trimmed }) {
                    validKeys.add(Pair(validKeys.size, trimmed))
                }
            }
        }

        // 3. Always include buildKey if valid and not already in pool
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            if (validKeys.none { it.second == buildKey }) {
                validKeys.add(Pair(validKeys.size, buildKey))
            }
        }

        if (validKeys.isEmpty()) {
            return@withContext GeminiResult.Error(
                "Список API-ключей Gemini пуст! Добавьте ваш ключ в настройках."
            )
        }

        // Rotate order based on startKeyIndex
        var startPos = 0
        for (i in validKeys.indices) {
            if (validKeys[i].first >= startKeyIndex) {
                startPos = i
                break
            }
        }
        val orderedKeys = validKeys.subList(startPos, validKeys.size) + validKeys.subList(0, startPos)

        val sanitizedContents = preparePayloadContents(chatHistory)
        if (sanitizedContents.length() == 0) {
            return@withContext GeminiResult.Error("Ошибка: пустая история диалога.")
        }

        val primaryModel = resolveModel(modelName)
        // Candidate models list: start with primary model (e.g. gemini-2.5-flash),
        // followed by ultra-fast and reliable models
        val candidateModels = listOf(
            primaryModel,
            "gemini-3.5-flash-lite",
            "gemini-3.5-flash",
            "gemini-3.8-flash"
        ).distinct()

        for (activeModel in candidateModels) {
            for (keyPos in orderedKeys.indices) {
                val (idx, key) = orderedKeys[keyPos]
                val keyNum = idx + 1

                var retry503Count = 0
                while (true) {
                    try {
                        val url = "https://generativelanguage.googleapis.com/v1beta/models/$activeModel:generateContent?key=$key"

                        val payload = JSONObject().apply {
                            put("contents", sanitizedContents)
                            val genConfig = JSONObject().apply {
                                put("temperature", 0.78)
                                put("maxOutputTokens", 4096)
                                // NOTE: Do NOT pass thinkingConfig here as it returns 400 Invalid Argument on most flash endpoints
                            }
                            put("generationConfig", genConfig)
                            val safetyList = JSONArray().apply {
                                put(JSONObject().put("category", "HARM_CATEGORY_HARASSMENT").put("threshold", "BLOCK_NONE"))
                                put(JSONObject().put("category", "HARM_CATEGORY_HATE_SPEECH").put("threshold", "BLOCK_NONE"))
                                put(JSONObject().put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT").put("threshold", "BLOCK_NONE"))
                                put(JSONObject().put("category", "HARM_CATEGORY_DANGEROUS_CONTENT").put("threshold", "BLOCK_NONE"))
                            }
                            put("safetySettings", safetyList)
                        }

                        val request = Request.Builder()
                            .url(url)
                            .post(payload.toString().toRequestBody(jsonMediaType))
                            .build()

                        val response = client.newCall(request).execute()
                        val responseBody = response.body?.string().orEmpty()
                        val responseCode = response.code

                        var errObj: JSONObject? = null
                        var rootJson: JSONObject? = null
                        try {
                            rootJson = JSONObject(responseBody)
                            if (rootJson.has("error")) {
                                errObj = rootJson.getJSONObject("error")
                            }
                        } catch (_: Exception) {}

                        val errMsg = errObj?.optString("message", "").orEmpty().lowercase()
                        val errStatus = errObj?.optString("status", "").orEmpty().uppercase()

                        val is503 = responseCode == 503 ||
                                errObj?.optInt("code") == 503 ||
                                errMsg.contains("overloaded") ||
                                errStatus == "UNAVAILABLE"

                        if (is503) {
                            retry503Count++
                            onStatusUpdate?.invoke("Модель $activeModel перегружена (503). Попытка #$retry503Count...")
                            if (retry503Count <= 2) {
                                delay(2000)
                                continue
                            } else {
                                break // try next model or key
                            }
                        }

                        if (rootJson != null && rootJson.has("candidates")) {
                            val candidates = rootJson.getJSONArray("candidates")
                            if (candidates.length() > 0) {
                                val candidate = candidates.getJSONObject(0)
                                val finishReason = candidate.optString("finishReason", "")

                                if (finishReason in listOf("SAFETY", "BLOCKLIST", "PROHIBITED_CONTENT")) {
                                    val reason = "фильтр безопасности ($finishReason)"
                                    onStatusUpdate?.invoke("Ключ #$keyNum: $reason. Переключение...")
                                    break // try next key
                                }

                                val content = candidate.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val candidateText = parts.getJSONObject(0).optString("text", "")
                                    if (candidateText.isNotBlank()) {
                                        return@withContext GeminiResult.Success(candidateText, idx)
                                    }
                                }
                            }
                        }

                        // Model 404 (e.g. if 2.5-flash is not available for new users) -> smoothly continue to next model
                        if (responseCode == 404 || errMsg.contains("not found") || errMsg.contains("no longer available")) {
                            onStatusUpdate?.invoke("Модель $activeModel недоступна для этого ключа. Переход на резервную модель...")
                            break // try next candidate model
                        }

                        val reason = formatErrorReason(responseCode, errMsg, errStatus)
                        onStatusUpdate?.invoke("Ключ #$keyNum ($activeModel): $reason. Переключение...")
                        break // try next key or model
                    } catch (e: Exception) {
                        onStatusUpdate?.invoke("Ошибка соединения (${e.javaClass.simpleName}). Пробуем дальше...")
                        break // try next key or model
                    }
                }
            }
        }

        return@withContext GeminiResult.Error(
            "Все попытки генерации завершились ошибкой. Проверьте соединение с интернетом или ключ."
        )
    }

    private fun preparePayloadContents(history: List<ChatMessage>): JSONArray {
        val jsonArray = JSONArray()
        val merged = mutableListOf<ChatMessage>()

        for (msg in history) {
            val text = msg.text.trim()
            if (text.isEmpty()) continue
            val role = if (msg.role == "model") "model" else "user"

            if (merged.isNotEmpty() && merged.last().role == role) {
                val last = merged.removeAt(merged.size - 1)
                merged.add(ChatMessage(role, "${last.text}\n\n$text"))
            } else {
                merged.add(ChatMessage(role, text))
            }
        }

        // Must start and end with user
        while (merged.isNotEmpty() && merged.first().role != "user") {
            merged.removeAt(0)
        }
        while (merged.isNotEmpty() && merged.last().role != "user") {
            merged.removeAt(merged.size - 1)
        }

        for (msg in merged) {
            val contentObj = JSONObject().apply {
                put("role", msg.role)
                val partsArr = JSONArray().apply {
                    put(JSONObject().put("text", msg.text))
                }
                put("parts", partsArr)
            }
            jsonArray.put(contentObj)
        }

        return jsonArray
    }

    private fun formatErrorReason(statusCode: Int, msg: String, status: String): String {
        return when {
            statusCode == 429 || msg.contains("quota") || status == "RESOURCE_EXHAUSTED" ->
                "исчерпана квота (429 Rate Limit)"
            statusCode == 404 ->
                "модель недоступна (404 Not Found)"
            statusCode == 400 && (msg.contains("role") || msg.contains("consecutive")) ->
                "ошибка очерёдности сообщений (400)"
            statusCode == 400 ->
                "неверный аргумент (400)"
            statusCode == 403 || status == "PERMISSION_DENIED" ->
                "доступ запрещён (403 Forbidden)"
            statusCode == 503 || msg.contains("overloaded") || status == "UNAVAILABLE" ->
                "модель перегружена (503 Overloaded)"
            msg.isNotBlank() ->
                "код $statusCode (${msg.take(45)})"
            else ->
                "код HTTP $statusCode"
        }
    }
}
