package com.example.nasacosmosmessenger.domain.model

/**
 * Represents the result of processing a user chat message.
 */
sealed class ChatProcessingResult {
    /**
     * APOD was found for a specific date.
     * @param apod The astronomy picture of the day
     * @param wasDateExtractedByAi True if Gemini extracted the date (vs regex)
     */
    data class ApodFound(
        val apod: Apod,
        val wasDateExtractedByAi: Boolean
    ) : ChatProcessingResult()

    /**
     * APOD returned along with an AI conversation response.
     * Used when no date was requested but today's APOD hasn't been shown.
     */
    data class ApodWithConversation(
        val apod: Apod,
        val aiResponse: String
    ) : ChatProcessingResult()

    /**
     * Pure AI conversation response (no APOD).
     * Used when no date requested and today's APOD already shown.
     */
    data class ConversationOnly(
        val aiResponse: String
    ) : ChatProcessingResult()

    /**
     * Error occurred during processing.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ChatProcessingResult()
}
