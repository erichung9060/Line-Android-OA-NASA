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
            Today is $todayDate.

            Task: Does the user's message refer to a specific date? If yes, calculate that exact calendar date and output ONLY the date in YYYY-MM-DD format. If no date reference exists, output ONLY: NO_DATE

            Rules:
            - You MUST calculate and output the actual YYYY-MM-DD date, not describe it in words.
            - Relative expressions must be resolved against today ($todayDate).
            - "上禮拜五" / "last Friday" → find the most recent Friday before today, output its date.
            - "昨天" / "yesterday" → today minus 1 day.
            - "前天" / "day before yesterday" → today minus 2 days.
            - "N天前" / "N days ago" → today minus N days.
            - "上個月" / "last month" → first day of last month.
            - A specific date like "2020/1/15", "1990-08-08", "January 15" → normalize to YYYY-MM-DD.
            - Output NOTHING except the date (YYYY-MM-DD) or NO_DATE. No explanation, no extra text.

            Examples (assuming today is 2026-04-22, Wednesday):
            - "給我上禮拜五的" → 2026-04-17
            - "show me yesterday's APOD" → 2026-04-21
            - "前天的星空" → 2026-04-20
            - "三天前" → 2026-04-19
            - "1990/08/08的照片" → 1990-08-08
            - "今天天氣真好" → NO_DATE
            - "你好" → NO_DATE

            User message: "$message"
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
