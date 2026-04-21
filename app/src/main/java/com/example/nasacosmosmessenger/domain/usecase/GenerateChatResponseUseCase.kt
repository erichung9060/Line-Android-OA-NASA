package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.repository.GeminiRepository
import javax.inject.Inject

/**
 * Generate a conversational AI response using Gemini.
 */
class GenerateChatResponseUseCase @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    suspend operator fun invoke(message: String): String {
        return geminiRepository.generateResponse(message)
    }
}
