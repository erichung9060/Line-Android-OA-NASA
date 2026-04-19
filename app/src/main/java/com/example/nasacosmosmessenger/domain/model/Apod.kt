package com.example.nasacosmosmessenger.domain.model

import java.time.LocalDate

/**
 * Domain model for NASA Astronomy Picture of the Day.
 */
data class Apod(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: MediaType,
    val thumbnailUrl: String?,
    val copyright: String?
)

/**
 * Media type for APOD content.
 * Per architecture spec section 6.1: enum MediaType { IMAGE, VIDEO }
 */
enum class MediaType {
    IMAGE,
    VIDEO;

    companion object {
        fun fromString(value: String): MediaType {
            return when (value.lowercase()) {
                "image" -> IMAGE
                "video" -> VIDEO
                else -> IMAGE
            }
        }
    }
}
