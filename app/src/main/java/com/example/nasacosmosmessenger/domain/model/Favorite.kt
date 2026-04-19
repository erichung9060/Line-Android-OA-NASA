package com.example.nasacosmosmessenger.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Domain model for a favorite APOD entry.
 *
 * Maps from/to FavoriteEntity for persistence.
 */
data class Favorite(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: MediaType,
    val thumbnailUrl: String?,
    val copyright: String?,
    val savedAt: Instant
) {
    /** Check if this favorite is a video type. */
    val isVideo: Boolean
        get() = mediaType == MediaType.VIDEO

    /** Display URL - for videos use thumbnailUrl if available, else url. */
    val displayUrl: String
        get() = if (isVideo) thumbnailUrl ?: url else url

    /** Source URL for videos (the actual video link). */
    val sourceUrl: String
        get() = url
}
