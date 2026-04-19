package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.repository.ChatRepository
import javax.inject.Inject

class RestoreChatHistoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): List<ChatMessage> {
        return repository.getChatHistory()
    }
}
