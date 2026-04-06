package com.bariatric.assistant.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bariatric.assistant.core.data.local.entity.ChatMessageEntity
import com.bariatric.assistant.core.data.preferences.UserPreferences
import com.bariatric.assistant.core.data.preferences.UserProfile
import com.bariatric.assistant.features.chat.data.ChatRepository
import com.bariatric.assistant.features.chat.data.GeminiService
import com.bariatric.assistant.features.chat.data.SystemPromptBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile = UserProfile()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val geminiService: GeminiService,
    private val systemPromptBuilder: SystemPromptBuilder,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        loadUserProfile()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages().collect { messages ->
                _uiState.update { it.copy(messages = messages) }

                if (messages.isEmpty()) {
                    sendWelcomeMessage()
                }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userPreferences.userProfile.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    private suspend fun sendWelcomeMessage() {
        val profile = userPreferences.userProfile.first()
        val welcomeText = buildString {
            append("Bună, ${profile.userName}! 👋\n\n")
            append("Sunt asistentul tău dietetician specializat în chirurgie bariatrică. ")
            append("Sunt aici să te ajut cu:\n\n")
            append("• 🥗 Sfaturi nutriționale personalizate\n")
            append("• 💧 Recomandări de hidratare\n")
            append("• 💪 Încurajare și suport\n")
            append("• ❓ Răspunsuri la întrebările tale despre alimentație\n\n")
            append("Care este cel mai mare obstacol nutrițional pe care îl întâmpini în acest moment?")
        }
        chatRepository.addAssistantMessage(welcomeText)
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val messageText = _uiState.value.inputText.trim()
        if (messageText.isBlank() || _uiState.value.isLoading) return

        _uiState.update { it.copy(inputText = "", isLoading = true, error = null) }

        viewModelScope.launch {
            chatRepository.addUserMessage(messageText)

            val profile = userPreferences.userProfile.first()
            val systemPrompt = systemPromptBuilder.buildSystemPrompt(profile)
            val history = chatRepository.getMessagesSync()

            val response = geminiService.sendMessage(
                systemPrompt = systemPrompt,
                chatHistory = history,
                userMessage = messageText
            )

            chatRepository.addAssistantMessage(response)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            chatRepository.clearCurrentConversation()
            chatRepository.startNewConversation()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
