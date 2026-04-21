package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.repository.GeminiRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Extract date from natural language using Gemini AI.
 * Falls back to null if:
 * - Gemini doesn't find a date
 * - Extracted date is outside APOD range (1995-06-16 to today)
 *
 * Note on Clock injection: LocalDate.now() is used directly here, consistent with the
 * existing ParseDateUseCase pattern. Injecting a Clock would improve testability for
 * boundary cases but would be inconsistent with the rest of the codebase. This is a
 * known limitation — if Clock injection is adopted project-wide, update this class too.
 */
class ExtractDateWithGeminiUseCase @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    private val apodStartDate = LocalDate.of(1995, 6, 16)

    suspend operator fun invoke(message: String): LocalDate? {
        val todayDate = LocalDate.now().toString()
        val extractedDate = geminiRepository.extractDate(message, todayDate) ?: return null

        return try {
            val date = LocalDate.parse(extractedDate)
            if (isInApodRange(date)) date else null
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun isInApodRange(date: LocalDate): Boolean {
        val today = LocalDate.now()
        return !date.isBefore(apodStartDate) && !date.isAfter(today)
    }
}
