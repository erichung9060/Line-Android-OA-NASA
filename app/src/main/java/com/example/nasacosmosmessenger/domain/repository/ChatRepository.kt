package com.example.nasacosmosmessenger.domain.repository

import com.example.nasacosmosmessenger.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /**
     * Observe chat history as a Flow.
     * Messages are ordered by timestamp ascending.
     * Used by ObserveChatHistoryUseCase for reactive updates.
     */
    fun observeChatHistory(): Flow<List<ChatMessage>>

    /**
     * Get all chat messages (one-shot).
     * Used by RestoreChatHistoryUseCase for initial load on app launch.
     */
    suspend fun getChatHistory(): List<ChatMessage>

    /**
     * Save a chat message.
     * Used by SaveChatMessageUseCase.
     */
    suspend fun saveMessage(message: ChatMessage)

    /**
     * Clear all chat history.
     */
    suspend fun clearHistory()

    /**
     * Get message count.
     */
    suspend fun getMessageCount(): Int
}
