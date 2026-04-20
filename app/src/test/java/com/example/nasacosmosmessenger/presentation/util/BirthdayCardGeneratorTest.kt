package com.example.nasacosmosmessenger.presentation.util

import android.content.Context
import coil.ImageLoader
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate

class BirthdayCardGeneratorTest {

    private lateinit var context: Context
    private lateinit var imageLoader: ImageLoader
    private lateinit var generator: BirthdayCardGenerator
    private lateinit var cacheDir: File

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        imageLoader = mockk(relaxed = true)
        cacheDir = File.createTempFile("test", "cache").parentFile!!
        every { context.cacheDir } returns cacheDir
        generator = BirthdayCardGenerator(context, imageLoader)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `generateCardForApod with image type returns card without source url`() = runTest {
        val apod = createImageApod()

        val result = generator.generateCardForApod(apod)

        // Image type: no source URL preserved
        assertThat(result.sourceUrl).isNull()
    }

    @Test
    fun `generateCardForApod with youtube video returns card without source url`() = runTest {
        val apod = createYouTubeVideoApod()

        val result = generator.generateCardForApod(apod)

        // YouTube video: thumbnail derived, no source URL needed
        assertThat(result.sourceUrl).isNull()
    }

    @Test
    fun `generateCardForApod with non-youtube video returns source url`() = runTest {
        val apod = createNonYouTubeVideoApod()

        val result = generator.generateCardForApod(apod)

        // Non-YouTube video: source URL preserved for fallback
        assertThat(result.sourceUrl).isEqualTo(apod.url)
    }

    @Test
    fun `generateCardForFavorite with non-youtube video returns source url`() = runTest {
        val favorite = createNonYouTubeVideoFavorite()

        val result = generator.generateCardForFavorite(favorite)

        assertThat(result.sourceUrl).isEqualTo(favorite.url)
    }

    @Test
    fun `extractYouTubeThumbnailUrl extracts correct thumbnail for youtube urls`() {
        val urls = listOf(
            "https://www.youtube.com/embed/dQw4w9WgXcQ" to "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
            "https://youtube.com/watch?v=dQw4w9WgXcQ" to "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
            "https://youtu.be/dQw4w9WgXcQ" to "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
        )

        urls.forEach { (input, expected) ->
            val result = generator.extractYouTubeThumbnailUrl(input)
            assertThat(result).isEqualTo(expected)
        }
    }

    @Test
    fun `extractYouTubeThumbnailUrl returns null for non-youtube urls`() {
        val nonYouTubeUrls = listOf(
            "https://vimeo.com/123456789",
            "https://example.com/video.mp4",
            "https://apod.nasa.gov/apod/image/video.mp4"
        )

        nonYouTubeUrls.forEach { url ->
            val result = generator.extractYouTubeThumbnailUrl(url)
            assertThat(result).isNull()
        }
    }

    private fun createImageApod() = Apod(
        date = LocalDate.of(2024, 3, 15),
        title = "Test Image APOD",
        explanation = "Test explanation",
        url = "https://apod.nasa.gov/apod/image/test.jpg",
        hdUrl = null,
        mediaType = MediaType.IMAGE,
        thumbnailUrl = null,
        copyright = null
    )

    private fun createYouTubeVideoApod() = Apod(
        date = LocalDate.of(2024, 3, 15),
        title = "Test YouTube APOD",
        explanation = "Test explanation",
        url = "https://www.youtube.com/embed/dQw4w9WgXcQ",
        hdUrl = null,
        mediaType = MediaType.VIDEO,
        thumbnailUrl = null,
        copyright = null
    )

    private fun createNonYouTubeVideoApod() = Apod(
        date = LocalDate.of(2024, 3, 15),
        title = "Test Vimeo APOD",
        explanation = "Test explanation",
        url = "https://vimeo.com/123456789",
        hdUrl = null,
        mediaType = MediaType.VIDEO,
        thumbnailUrl = null,
        copyright = null
    )

    private fun createNonYouTubeVideoFavorite() = Favorite(
        date = LocalDate.of(2024, 3, 15),
        title = "Test Vimeo Favorite",
        explanation = "Test explanation",
        url = "https://vimeo.com/123456789",
        hdUrl = null,
        mediaType = MediaType.VIDEO,
        thumbnailUrl = null,
        copyright = null,
        savedAt = Instant.now()
    )
}
