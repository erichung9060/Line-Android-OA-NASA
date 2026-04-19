package com.example.nasacosmosmessenger.domain.model

import java.time.Instant
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val apod: Apod? = null,
    val isFromUser: Boolean,
    val timestamp: Instant = Instant.now()
)
