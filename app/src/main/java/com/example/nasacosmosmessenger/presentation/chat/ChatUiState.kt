package com.example.nasacosmosmessenger.presentation.chat

import com.example.nasacosmosmessenger.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialized: Boolean = false
)
