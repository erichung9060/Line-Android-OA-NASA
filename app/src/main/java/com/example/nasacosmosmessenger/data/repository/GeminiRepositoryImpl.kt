package com.example.nasacosmosmessenger.data.repository

import com.example.nasacosmosmessenger.data.remote.api.GeminiApi
import com.example.nasacosmosmessenger.data.remote.dto.createConversationRequest
import com.example.nasacosmosmessenger.data.remote.dto.createGeminiRequest
import com.example.nasacosmosmessenger.data.remote.dto.extractText
import com.example.nasacosmosmessenger.domain.repository.GeminiRepository
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor(
    private val geminiApi: GeminiApi,
    private val apiKey: String,
    private val model: String
) : GeminiRepository {

    override suspend fun extractDate(message: String, todayDate: String): String? {
        val prompt = buildDateExtractionPrompt(message, todayDate)
        val request = createGeminiRequest(prompt)

        return try {
            val response = geminiApi.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )
            val text = response.extractText()?.trim()

            // Validate response is a date format or NO_DATE
            when {
                text == null -> null
                text == "NO_DATE" -> null
                text.matches(Regex("""\d{4}-\d{2}-\d{2}""")) -> text
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun generateResponse(message: String): String {
        val prompt = buildConversationPrompt(message)
        val request = createConversationRequest(prompt)

        return try {
            val response = geminiApi.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )
            response.extractText()?.trim() ?: DEFAULT_ERROR_RESPONSE
        } catch (e: Exception) {
            DEFAULT_ERROR_RESPONSE
        }
    }

    private fun buildDateExtractionPrompt(message: String, todayDate: String): String {
        return """
            You are a date extraction assistant. Given a user message and today's date, extract any date reference.

            Today's date: $todayDate

            User message: "$message"

            Rules:
            - "上禮拜五" or "last Friday" = last Friday relative to today
            - "昨天" or "yesterday" = yesterday
            - "前天" or "day before yesterday" = day before yesterday
            - "三天前" or "3 days ago" = 3 days ago
            - If no date reference found, respond with exactly: NO_DATE
            - If date found, respond with exactly the date in format: YYYY-MM-DD
            - Only respond with the date or NO_DATE, nothing else.
        """.trimIndent()
    }

    private fun buildConversationPrompt(message: String): String {
        return """
            You are NOVA, a friendly AI assistant in the NASA Cosmos Messenger app.
            You help users explore NASA's Astronomy Picture of the Day (APOD).

            Guidelines:
            - Be concise and friendly (2-3 sentences max)
            - If the user asks about space/astronomy, share interesting facts
            - If the user asks for an APOD, suggest they can say dates like "給我昨天的APOD" or "show me yesterday's APOD"
            - Respond in the same language the user uses

            User: $message
        """.trimIndent()
    }

    companion object {
        private const val DEFAULT_ERROR_RESPONSE = "Sorry, I'm having trouble responding right now. Please try again."
    }
}
