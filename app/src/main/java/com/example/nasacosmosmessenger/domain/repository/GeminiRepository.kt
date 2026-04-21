package com.example.nasacosmosmessenger.domain.repository

/**
 * Repository for interacting with Google Gemini API.
 */
interface GeminiRepository {

    /**
     * Extract a date from natural language text.
     *
     * @param message User's message containing potential date reference
     * @param todayDate Today's date in YYYY-MM-DD format for relative date calculation
     * @return Extracted date in YYYY-MM-DD format, or null if no date found
     */
    suspend fun extractDate(message: String, todayDate: String): String?

    /**
     * Generate a conversational response.
     *
     * @param message User's message to respond to
     * @return AI-generated response text
     */
    suspend fun generateResponse(message: String): String
}
