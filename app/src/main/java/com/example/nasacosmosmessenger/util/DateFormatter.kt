package com.example.nasacosmosmessenger.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {

    private val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
    private val shortFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Format date for display: "January 15, 2024"
     */
    fun formatForDisplay(date: LocalDate): String {
        return date.format(displayFormatter)
    }

    /**
     * Format date short: "Jan 15, 2024"
     */
    fun formatShort(date: LocalDate): String {
        return date.format(shortFormatter)
    }

    /**
     * Format date as ISO: "2024-01-15"
     */
    fun formatIso(date: LocalDate): String {
        return date.format(isoFormatter)
    }
}
