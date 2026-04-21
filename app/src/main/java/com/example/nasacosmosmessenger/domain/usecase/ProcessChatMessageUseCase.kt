package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.ChatProcessingResult
import com.example.nasacosmosmessenger.domain.model.Resource
import java.time.LocalDate
import javax.inject.Inject

/**
 * Orchestrates the chat message processing flow:
 * 1. Try regex date parsing
 * 2. Try Gemini date extraction
 * 3. If no date: return today's APOD (if not sent) + conversation, or conversation only
 */
class ProcessChatMessageUseCase @Inject constructor(
    private val parseDateUseCase: ParseDateUseCase,
    private val extractDateWithGeminiUseCase: ExtractDateWithGeminiUseCase,
    private val getTodayApodUseCase: GetTodayApodUseCase,
    private val getApodByDateUseCase: GetApodByDateUseCase,
    private val generateChatResponseUseCase: GenerateChatResponseUseCase
) {
    suspend operator fun invoke(
        message: String,
        hasSentTodayApod: Boolean
    ): ChatProcessingResult {
        // Branch 1: Try regex date parsing
        val regexDate = parseDateUseCase(message)
        if (regexDate != null) {
            return fetchApodForDate(regexDate, wasDateExtractedByAi = false)
        }

        // Branch 2: Try Gemini date extraction
        val geminiDate = extractDateWithGeminiUseCase(message)
        if (geminiDate != null) {
            return fetchApodForDate(geminiDate, wasDateExtractedByAi = true)
        }

        // Branch 3 & 4: No date found
        return if (!hasSentTodayApod) {
            // Branch 3: Send today's APOD with conversation
            fetchTodayApodWithConversation(message)
        } else {
            // Branch 4: Conversation only
            val aiResponse = generateChatResponseUseCase(message)
            ChatProcessingResult.ConversationOnly(aiResponse)
        }
    }

    private suspend fun fetchApodForDate(
        date: LocalDate,
        wasDateExtractedByAi: Boolean
    ): ChatProcessingResult {
        return when (val result = getApodByDateUseCase(date)) {
            is Resource.Success -> ChatProcessingResult.ApodFound(
                apod = result.data,
                wasDateExtractedByAi = wasDateExtractedByAi
            )
            is Resource.Error -> ChatProcessingResult.Error(
                message = result.message ?: "Failed to fetch APOD",
                cause = result.cause
            )
            is Resource.Loading -> ChatProcessingResult.Error("Unexpected loading state")
        }
    }

    private suspend fun fetchTodayApodWithConversation(message: String): ChatProcessingResult {
        return when (val result = getTodayApodUseCase()) {
            is Resource.Success -> {
                val aiResponse = generateChatResponseUseCase(message)
                ChatProcessingResult.ApodWithConversation(
                    apod = result.data,
                    aiResponse = aiResponse
                )
            }
            is Resource.Error -> ChatProcessingResult.Error(
                message = result.message ?: "Failed to fetch today's APOD",
                cause = result.cause
            )
            is Resource.Loading -> ChatProcessingResult.Error("Unexpected loading state")
        }
    }
}
