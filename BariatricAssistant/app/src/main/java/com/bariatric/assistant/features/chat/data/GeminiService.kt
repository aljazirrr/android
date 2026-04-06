package com.bariatric.assistant.features.chat.data

import com.bariatric.assistant.core.data.local.entity.ChatMessageEntity
import com.bariatric.assistant.core.data.local.entity.MessageRole
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val generativeModel: GenerativeModel
) {

    suspend fun sendMessage(
        systemPrompt: String,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String
    ): String {
        return try {
            val history = chatHistory.mapNotNull { msg ->
                when (msg.role) {
                    MessageRole.USER -> content(role = "user") { text(msg.content) }
                    MessageRole.ASSISTANT -> content(role = "model") { text(msg.content) }
                    MessageRole.SYSTEM -> null
                }
            }

            val chat = generativeModel.startChat(
                history = listOf(
                    content(role = "user") { text(systemPrompt) },
                    content(role = "model") { text("Am înțeles. Sunt gata să ofer suport nutrițional personalizat. Cum te pot ajuta?") }
                ) + history
            )

            val response = chat.sendMessage(userMessage)
            response.text ?: "Ne pare rău, nu am putut genera un răspuns. Vă rog încercați din nou."
        } catch (e: Exception) {
            Timber.e(e, "Eroare la comunicarea cu Gemini AI")
            when {
                e.message?.contains("API key", ignoreCase = true) == true ->
                    "⚠️ Cheia API Gemini nu este configurată. Adăugați GEMINI_API_KEY în local.properties."
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("connect", ignoreCase = true) == true ->
                    "⚠️ Nu am putut contacta serverul. Verificați conexiunea la internet și încercați din nou."
                else ->
                    "⚠️ A apărut o eroare: ${e.localizedMessage ?: "Necunoscută"}. Vă rog încercați din nou."
            }
        }
    }
}
