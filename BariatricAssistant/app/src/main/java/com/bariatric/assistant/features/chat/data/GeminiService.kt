package com.bariatric.assistant.features.chat.data

import com.bariatric.assistant.BuildConfig
import com.bariatric.assistant.core.data.local.entity.ChatMessageEntity
import com.bariatric.assistant.core.data.local.entity.MessageRole
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val modelName = "gemini-2.0-flash"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1/models/$modelName:generateContent"

    suspend fun sendMessage(
        systemPrompt: String,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext "⚠️ Cheia API Gemini nu este configurată.\n\nAdăugați în fișierul `local.properties`:\n```\nGEMINI_API_KEY=cheia_voastră_aici\n```\nObțineți o cheie gratuită de la aistudio.google.com"
            }

            val contents = buildContents(systemPrompt, chatHistory, userMessage)

            val requestBody = mapOf(
                "contents" to contents,
                "generationConfig" to mapOf(
                    "maxOutputTokens" to 1024,
                    "temperature" to 0.75,
                    "topP" to 0.95
                )
            )

            val json = gson.toJson(requestBody)
            val request = Request.Builder()
                .url("$apiUrl?key=$apiKey")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Timber.e("Gemini API error ${response.code}: $responseBody")
                return@withContext parseErrorMessage(response.code, responseBody)
            }

            parseSuccessResponse(responseBody)

        } catch (e: Exception) {
            Timber.e(e, "Eroare conexiune Gemini API")
            when {
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                    "⚠️ Nu există conexiune la internet. Verificați rețeaua și încercați din nou."
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "⚠️ Serverul nu răspunde (timeout). Încercați din nou."
                else ->
                    "⚠️ Eroare de conexiune: ${e.localizedMessage}. Vă rog încercați din nou."
            }
        }
    }

    private fun buildContents(
        systemPrompt: String,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String
    ): List<Map<String, Any>> {
        val contents = mutableListOf<Map<String, Any>>()

        // Injectăm system prompt ca primul schimb user/model
        contents.add(mapOf(
            "role" to "user",
            "parts" to listOf(mapOf("text" to systemPrompt))
        ))
        contents.add(mapOf(
            "role" to "model",
            "parts" to listOf(mapOf("text" to "Am înțeles rolul meu. Sunt gata să ofer suport nutrițional personalizat pacienților bariatrici."))
        ))

        // Adăugăm istoricul conversației
        for (msg in chatHistory) {
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "model"
                MessageRole.SYSTEM -> continue
            }
            contents.add(mapOf(
                "role" to role,
                "parts" to listOf(mapOf("text" to msg.content))
            ))
        }

        // Adăugăm mesajul curent
        contents.add(mapOf(
            "role" to "user",
            "parts" to listOf(mapOf("text" to userMessage))
        ))

        return contents
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseSuccessResponse(responseBody: String): String {
        return try {
            val result = gson.fromJson(responseBody, Map::class.java) as Map<String, Any>
            val candidates = result["candidates"] as? List<Map<String, Any>>
                ?: return "⚠️ Răspuns invalid de la server."
            val content = candidates.firstOrNull()?.get("content") as? Map<String, Any>
                ?: return "⚠️ Conținut lipsă în răspuns."
            val parts = content["parts"] as? List<Map<String, Any>>
                ?: return "⚠️ Format răspuns neașteptat."
            parts.firstOrNull()?.get("text") as? String
                ?: "⚠️ Text lipsă în răspuns."
        } catch (e: Exception) {
            Timber.e(e, "Eroare parsare răspuns: $responseBody")
            "⚠️ Eroare la procesarea răspunsului. Vă rog încercați din nou."
        }
    }

    private fun parseErrorMessage(code: Int, body: String): String {
        return when (code) {
            400 -> "⚠️ Cerere invalidă (400). Verificați cheia API."
            401, 403 -> "⚠️ Cheie API invalidă sau expirată (${code}). Verificați cheia în local.properties."
            404 -> "⚠️ Modelul Gemini nu a fost găsit (404). Verificați că aveți acces la Gemini API."
            429 -> "⚠️ Limita de cereri depășită (429). Așteptați câteva secunde și încercați din nou."
            500, 503 -> "⚠️ Serverul Gemini are probleme temporare (${code}). Încercați din nou mai târziu."
            else -> "⚠️ Eroare API (${code}). Vă rog încercați din nou."
        }
    }
}
