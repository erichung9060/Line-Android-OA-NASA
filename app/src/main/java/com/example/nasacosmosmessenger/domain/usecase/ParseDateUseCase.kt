package com.example.nasacosmosmessenger.domain.usecase

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Parses date from user message text.
 *
 * Supported formats:
 * - yyyy/MM/dd (e.g., 1990/08/08)
 * - yyyy-MM-dd (e.g., 1990-08-08)
 *
 * Returns null if:
 * - No supported date format found in message
 * - Date is outside APOD range (1995-06-16 to today)
 */
class ParseDateUseCase @Inject constructor() {

    private val slashFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val dashFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

    // APOD started on June 16, 1995
    private val apodStartDate = LocalDate.of(1995, 6, 16)

    // Patterns to find dates in text
    private val slashPattern = Regex("""\d{4}/\d{2}/\d{2}""")
    private val dashPattern = Regex("""\d{4}-\d{2}-\d{2}""")

    /**
     * Extract and parse date from message text.
     * Returns null if no valid date in APOD range is found.
     */
    operator fun invoke(message: String): LocalDate? {
        // Try slash format first (yyyy/MM/dd)
        slashPattern.find(message)?.let { match ->
            val parsed = parseDate(match.value, slashFormatter)
            if (parsed != null && isInApodRange(parsed)) {
                return parsed
            }
        }

        // Try dash format (yyyy-MM-dd)
        dashPattern.find(message)?.let { match ->
            val parsed = parseDate(match.value, dashFormatter)
            if (parsed != null && isInApodRange(parsed)) {
                return parsed
            }
        }

        return null
    }

    private fun parseDate(text: String, formatter: DateTimeFormatter): LocalDate? {
        return try {
            LocalDate.parse(text, formatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun isInApodRange(date: LocalDate): Boolean {
        val today = LocalDate.now()
        return !date.isBefore(apodStartDate) && !date.isAfter(today)
    }
}
