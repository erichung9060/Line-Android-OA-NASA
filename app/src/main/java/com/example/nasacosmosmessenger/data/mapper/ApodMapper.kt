package com.example.nasacosmosmessenger.data.mapper

import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity
import com.example.nasacosmosmessenger.data.remote.dto.ApodResponse
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.MediaType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ApodMapper {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun responseToDomain(response: ApodResponse): Apod {
        val thumbnailUrl = deriveThumbnailUrl(response)
        return Apod(
            date = LocalDate.parse(response.date, dateFormatter),
            title = response.title,
            explanation = response.explanation,
            url = response.url,
            hdUrl = response.hdUrl,
            mediaType = MediaType.fromString(response.mediaType),
            thumbnailUrl = thumbnailUrl,
            copyright = response.copyright
        )
    }

    fun responseToEntity(response: ApodResponse): ApodCacheEntity {
        val thumbnailUrl = deriveThumbnailUrl(response)
        return ApodCacheEntity(
            date = response.date,
            title = response.title,
            explanation = response.explanation,
            url = response.url,
            hdUrl = response.hdUrl,
            mediaType = response.mediaType,
            thumbnailUrl = thumbnailUrl,
            copyright = response.copyright,
            cachedAt = System.currentTimeMillis()
        )
    }

    fun entityToDomain(entity: ApodCacheEntity): Apod {
        return Apod(
            date = LocalDate.parse(entity.date, dateFormatter),
            title = entity.title,
            explanation = entity.explanation,
            url = entity.url,
            hdUrl = entity.hdUrl,
            mediaType = MediaType.fromString(entity.mediaType),
            thumbnailUrl = entity.thumbnailUrl,
            copyright = entity.copyright
        )
    }

    fun domainToEntity(apod: Apod): ApodCacheEntity {
        return ApodCacheEntity(
            date = apod.date.format(dateFormatter),
            title = apod.title,
            explanation = apod.explanation,
            url = apod.url,
            hdUrl = apod.hdUrl,
            mediaType = apod.mediaType.name.lowercase(),
            thumbnailUrl = apod.thumbnailUrl,
            copyright = apod.copyright,
            cachedAt = System.currentTimeMillis()
        )
    }

    /**
     * Derive thumbnail URL for video APODs.
     * Per ARCHITECTURE.md section 10.2: Only YouTube thumbnails are derived.
     * Non-YouTube videos fall back to text-only card generation.
     * Returns null for unsupported video sources.
     */
    private fun deriveThumbnailUrl(response: ApodResponse): String? {
        // If API provides thumbnail, use it
        if (!response.thumbnailUrl.isNullOrBlank()) {
            return response.thumbnailUrl
        }

        // Only derive for video type
        if (response.mediaType.lowercase() != "video") {
            return null
        }

        val url = response.url

        // YouTube: extract video ID and construct thumbnail URL
        val youtubeId = extractYouTubeId(url)
        if (youtubeId != null) {
            return "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg"
        }

        // Non-YouTube videos (Vimeo, others): return null
        // UI will use text-only fallback with source link
        return null
    }

    private fun extractYouTubeId(url: String): String? {
        // Patterns:
        // https://www.youtube.com/watch?v=VIDEO_ID
        // https://youtu.be/VIDEO_ID
        // https://www.youtube.com/embed/VIDEO_ID
        val patterns = listOf(
            Regex("""youtube\.com/watch\?v=([a-zA-Z0-9_-]+)"""),
            Regex("""youtu\.be/([a-zA-Z0-9_-]+)"""),
            Regex("""youtube\.com/embed/([a-zA-Z0-9_-]+)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        return null
    }
}
