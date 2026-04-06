package com.bariatric.assistant.features.chat.data

import com.bariatric.assistant.core.data.local.dao.ChatDao
import com.bariatric.assistant.core.data.local.entity.ChatMessageEntity
import com.bariatric.assistant.core.data.local.entity.MessageRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao
) {
    private var currentConversationId: String = generateConversationId()

    fun getCurrentConversationId(): String = currentConversationId

    fun getMessages(): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesByConversation(currentConversationId)
    }

    suspend fun getMessagesSync(): List<ChatMessageEntity> {
        return chatDao.getMessagesByConversationSync(currentConversationId)
    }

    suspend fun addUserMessage(content: String): ChatMessageEntity {
        val message = ChatMessageEntity(
            conversationId = currentConversationId,
            role = MessageRole.USER,
            content = content
        )
        val id = chatDao.insertMessage(message)
        return message.copy(id = id)
    }

    suspend fun addAssistantMessage(content: String): ChatMessageEntity {
        val message = ChatMessageEntity(
            conversationId = currentConversationId,
            role = MessageRole.ASSISTANT,
            content = content
        )
        val id = chatDao.insertMessage(message)
        return message.copy(id = id)
    }

    fun startNewConversation() {
        currentConversationId = generateConversationId()
    }

    suspend fun clearCurrentConversation() {
        chatDao.deleteConversation(currentConversationId)
    }

    private fun generateConversationId(): String = UUID.randomUUID().toString()
}
