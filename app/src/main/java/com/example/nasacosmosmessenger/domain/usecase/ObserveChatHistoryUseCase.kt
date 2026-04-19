package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatHistoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> {
        return repository.observeChatHistory()
    }
}
