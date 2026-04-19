package com.example.nasacosmosmessenger.data.mapper

import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity
import com.example.nasacosmosmessenger.data.remote.dto.ApodResponse
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApodMapperTest {

    @Test
    fun `responseToDomain maps all fields correctly`() {
        val response = ApodResponse(
            date = "2024-01-15",
            title = "Test Title",
            explanation = "Test explanation",
            url = "https://example.com/image.jpg",
            hdUrl = "https://example.com/image_hd.jpg",
            mediaType = "image",
            copyright = "NASA",
            thumbnailUrl = null
        )

        val domain = ApodMapper.responseToDomain(response)

        assertThat(domain.date).isEqualTo(LocalDate.of(2024, 1, 15))
        assertThat(domain.title).isEqualTo("Test Title")
        assertThat(domain.mediaType).isEqualTo(MediaType.IMAGE)
        assertThat(domain.copyright).isEqualTo("NASA")
    }

    @Test
    fun `entityToDomain maps all fields correctly`() {
        val entity = ApodCacheEntity(
            date = "2024-01-15",
            title = "Cached Title",
            explanation = "Cached explanation",
            url = "https://example.com/cached.jpg",
            hdUrl = null,
            mediaType = "video",
            thumbnailUrl = "https://example.com/thumb.jpg",
            copyright = null,
            cachedAt = System.currentTimeMillis()
        )

        val domain = ApodMapper.entityToDomain(entity)

        assertThat(domain.date).isEqualTo(LocalDate.of(2024, 1, 15))
        assertThat(domain.mediaType).isEqualTo(MediaType.VIDEO)
        assertThat(domain.thumbnailUrl).isEqualTo("https://example.com/thumb.jpg")
    }

    @Test
    fun `video URL mapping derives YouTube thumbnail`() {
        val response = ApodResponse(
            date = "2024-01-15",
            title = "Video APOD",
            explanation = "Video explanation",
            url = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            hdUrl = null,
            mediaType = "video",
            copyright = null,
            thumbnailUrl = null
        )

        val domain = ApodMapper.responseToDomain(response)

        assertThat(domain.thumbnailUrl).isEqualTo("https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg")
    }

    @Test
    fun `non-YouTube video falls back safely`() {
        val response = ApodResponse(
            date = "2024-01-15",
            title = "Vimeo APOD",
            explanation = "Vimeo explanation",
            url = "https://vimeo.com/123456",
            hdUrl = null,
            mediaType = "video",
            copyright = null,
            thumbnailUrl = null
        )

        val domain = ApodMapper.responseToDomain(response)

        // Non-YouTube returns null thumbnail, UI handles fallback
        assertThat(domain.thumbnailUrl).isNull()
    }
}
