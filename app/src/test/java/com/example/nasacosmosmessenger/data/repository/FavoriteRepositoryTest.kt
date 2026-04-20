package com.example.nasacosmosmessenger.data.repository

import app.cash.turbine.test
import com.example.nasacosmosmessenger.data.local.dao.FavoriteDao
import com.example.nasacosmosmessenger.data.local.entity.FavoriteEntity
import com.example.nasacosmosmessenger.data.mapper.FavoriteMapper
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class FavoriteRepositoryTest {

    private lateinit var favoriteDao: FavoriteDao
    private lateinit var favoriteMapper: FavoriteMapper
    private lateinit var repository: FavoriteRepositoryImpl

    @Before
    fun setup() {
        favoriteDao = mockk(relaxed = true)
        favoriteMapper = FavoriteMapper()
        repository = FavoriteRepositoryImpl(favoriteDao, favoriteMapper)
    }

    @Test
    fun `getAllFavorites maps entities to domain models`() = runTest {
        val entity = createTestEntity("2024-03-15")
        every { favoriteDao.getAllFavorites() } returns flowOf(listOf(entity))

        repository.getAllFavorites().test {
            val favorites = awaitItem()
            assertThat(favorites).hasSize(1)
            assertThat(favorites[0].date).isEqualTo(LocalDate.parse("2024-03-15"))
            assertThat(favorites[0].title).isEqualTo("Test Title")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveFavorite converts domain to entity and inserts`() = runTest {
        val entitySlot = slot<FavoriteEntity>()
        coEvery { favoriteDao.insertFavorite(capture(entitySlot)) } returns Unit

        val favorite = createTestFavorite(LocalDate.parse("2024-03-15"))
        repository.saveFavorite(favorite)

        coVerify { favoriteDao.insertFavorite(any()) }
        assertThat(entitySlot.captured.date).isEqualTo("2024-03-15")
        assertThat(entitySlot.captured.title).isEqualTo("Test Title")
    }

    @Test
    fun `deleteFavorite calls dao with formatted date`() = runTest {
        repository.deleteFavorite(LocalDate.parse("2024-03-15"))

        coVerify { favoriteDao.deleteFavoriteByDate("2024-03-15") }
    }

    @Test
    fun `isFavorite returns dao result`() = runTest {
        coEvery { favoriteDao.isFavorite("2024-03-15") } returns true

        val result = repository.isFavorite(LocalDate.parse("2024-03-15"))

        assertThat(result).isTrue()
    }

    @Test
    fun `video favorite preserves metadata through mapping`() = runTest {
        val videoEntity = FavoriteEntity(
            date = "2024-03-15",
            title = "Video Title",
            explanation = "Video explanation",
            url = "https://youtube.com/watch?v=xyz",
            hdUrl = null,
            mediaType = "video",
            thumbnailUrl = "https://img.youtube.com/vi/xyz/0.jpg",
            copyright = null,
            savedAt = 1000L
        )
        every { favoriteDao.getAllFavorites() } returns flowOf(listOf(videoEntity))

        repository.getAllFavorites().test {
            val favorites = awaitItem()
            assertThat(favorites).hasSize(1)
            assertThat(favorites[0].mediaType).isEqualTo(MediaType.VIDEO)
            assertThat(favorites[0].thumbnailUrl).isEqualTo("https://img.youtube.com/vi/xyz/0.jpg")
            assertThat(favorites[0].isVideo).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createTestEntity(date: String) = FavoriteEntity(
        date = date,
        title = "Test Title",
        explanation = "Test explanation",
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/image_hd.jpg",
        mediaType = "image",
        thumbnailUrl = null,
        copyright = "Test Copyright",
        savedAt = 1000L
    )

    private fun createTestFavorite(date: LocalDate) = Favorite(
        date = date,
        title = "Test Title",
        explanation = "Test explanation",
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/image_hd.jpg",
        mediaType = MediaType.IMAGE,
        thumbnailUrl = null,
        copyright = "Test Copyright",
        savedAt = Instant.ofEpochMilli(1000L)
    )
}
