package com.bariatric.assistant.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
