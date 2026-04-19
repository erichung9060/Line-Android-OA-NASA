package com.example.nasacosmosmessenger.data.repository

import com.example.nasacosmosmessenger.data.local.dao.ChatMessageDao
import com.example.nasacosmosmessenger.data.local.dao.ChatMessageWithApod
import com.example.nasacosmosmessenger.data.local.entity.ChatMessageEntity
import com.example.nasacosmosmessenger.data.mapper.ApodMapper
import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Observe chat history with APOD rehydration via LEFT JOIN.
     * Per ARCHITECTURE.md section 8.2: Messages are rehydrated with cached APOD data.
     * If APOD is missing from cache, message is preserved with apod = null.
     */
    override fun observeChatHistory(): Flow<List<ChatMessage>> {
        return chatMessageDao.observeAllWithApod().map { entities ->
            entities.map { withApod -> joinResultToDomain(withApod) }
        }
    }

    /**
     * Get chat history one-shot with APOD rehydration.
     * Used by RestoreChatHistoryUseCase for initial load.
     */
    override suspend fun getChatHistory(): List<ChatMessage> {
        return chatMessageDao.getAllWithApod().map { withApod -> joinResultToDomain(withApod) }
    }

    override suspend fun saveMessage(message: ChatMessage) {
        val entity = domainToEntity(message)
        chatMessageDao.insert(entity)
    }

    override suspend fun clearHistory() {
        chatMessageDao.deleteAll()
    }

    override suspend fun getMessageCount(): Int {
        return chatMessageDao.getCount()
    }

    /**
     * Convert LEFT JOIN result to domain model.
     * APOD is rehydrated from cache if available, otherwise null.
     */
    private fun joinResultToDomain(withApod: ChatMessageWithApod): ChatMessage {
        val apod = withApod.apod?.let { ApodMapper.entityToDomain(it) }

        return ChatMessage(
            id = withApod.message.id,
            content = withApod.message.content,
            apod = apod,
            isFromUser = withApod.message.isFromUser,
            timestamp = Instant.ofEpochMilli(withApod.message.timestamp)
        )
    }

    private fun domainToEntity(message: ChatMessage): ChatMessageEntity {
        return ChatMessageEntity(
            id = message.id,
            content = message.content,
            apodDate = message.apod?.date?.format(dateFormatter),
            isFromUser = message.isFromUser,
            timestamp = message.timestamp.toEpochMilli()
        )
    }
}
