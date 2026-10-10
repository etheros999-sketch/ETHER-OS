package com.darkempire.ether

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object GeminiClient {
    private const val MODEL = "gemini-3.5-flash-lite"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    suspend fun generateReply(
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Add your Gemini API key in AI Setup first." }
        require(prompt.isNotBlank()) { "Enter a message first." }

        // Use the phone's current local time for every request so the model does not
        // have to guess whether it is morning, afternoon, or evening.
        val localNow = ZonedDateTime.now()
        val localDateTime = localNow.format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy, HH:mm", Locale.getDefault())
        )
        val localTimeZone = ZoneId.systemDefault().id

        val body = JSONObject()
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            "You are ETHER, a truthful, helpful personal AI assistant for Emperor Lucian, " +
                                "also known as Lucian and Alexander Ntow; these names refer to the same person. " +
                                "His organisation is Dark Empire Leadership. " +
                                "The user's current device-local date and time is $localDateTime " +
                                "in the $localTimeZone time zone. Use this exact local-time context for greetings " +
                                "and time-sensitive answers. Do not guess the time, use UTC as a substitute, " +
                                "or give a greeting inconsistent with the supplied local time. " +
                                "Be clear and practical. Never claim to have performed an action you did not perform. " +
                                "Never initiate purchases, payments, subscriptions, advertising spend, or inventory commitments; " +
                                "those always require explicit user approval. Keep answers readable on a phone."
                        )
                    )
                )
            )
            .put(
                "contents",
                JSONArray().apply {
                    history.takeLast(12).forEach { (role, text) ->
                        if ((role == "user" || role == "model") && text.isNotBlank()) {
                            put(
                                JSONObject()
                                    .put("role", role)
                                    .put("parts", JSONArray().put(JSONObject().put("text", text)))
                            )
                        }
                    }
                    put(
                        JSONObject()
                            .put("role", "user")
                            .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    )
                }
            )
            .put(
                "generationConfig",
                JSONObject().put("maxOutputTokens", 1200)
            )

        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 45000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-goog-api-key", apiKey)
        }

        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val responseText = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()

            if (status !in 200..299) {
                throw IllegalStateException(
                    when (status) {
                        400, 401 -> "Gemini rejected the request or API key. Check the key in AI Setup."
                        403 -> "Gemini access was denied. Check that the key is active and the Gemini API is enabled."
                        429 -> "The free usage limit may have been reached. ETHER has not switched to a paid provider."
                        in 500..599 -> "Gemini is temporarily unavailable. Please try again later."
                        else -> "Gemini request failed (HTTP $status). Please check the connection and API settings."
                    }
                )
            }

            val json = JSONObject(responseText)
            val candidates = json.optJSONArray("candidates")
            val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            val answer = buildString {
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.optJSONObject(i)
                        val text = part?.optString("text").orEmpty()
                        if (text.isNotBlank()) {
                            if (isNotEmpty()) append("\n")
                            append(text)
                        }
                    }
                }
            }.trim()
            if (answer.isBlank()) {
                throw IllegalStateException("Gemini returned no text. Try rephrasing your message.")
            }
            answer
        } finally {
            connection.disconnect()
        }
    }
}
