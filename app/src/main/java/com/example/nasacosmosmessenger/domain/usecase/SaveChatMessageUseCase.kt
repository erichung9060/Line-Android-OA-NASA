package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.repository.ChatRepository
import javax.inject.Inject

class SaveChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: ChatMessage) {
        repository.saveMessage(message)
    }
}
