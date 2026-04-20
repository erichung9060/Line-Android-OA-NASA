package com.example.nasacosmosmessenger.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.nasacosmosmessenger.data.local.database.AppDatabase
import com.example.nasacosmosmessenger.data.local.entity.FavoriteEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        favoriteDao = database.favoriteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertFavoriteAndRetrieveReturnsSameData() = runTest {
        val favorite = createTestFavorite("2024-03-15")

        favoriteDao.insertFavorite(favorite)

        favoriteDao.getAllFavorites().test {
            val favorites = awaitItem()
            assertThat(favorites).hasSize(1)
            assertThat(favorites[0].date).isEqualTo("2024-03-15")
            assertThat(favorites[0].title).isEqualTo("Test Title")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertDuplicateDateReplacesExistingEntry() = runTest {
        val original = createTestFavorite("2024-03-15", title = "Original Title")
        val replacement = createTestFavorite("2024-03-15", title = "Replaced Title")

        favoriteDao.insertFavorite(original)
        favoriteDao.insertFavorite(replacement)

        favoriteDao.getAllFavorites().test {
            val favorites = awaitItem()
            assertThat(favorites).hasSize(1)
            assertThat(favorites[0].title).isEqualTo("Replaced Title")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteByDateRemovesEntry() = runTest {
        val favorite = createTestFavorite("2024-03-15")
        favoriteDao.insertFavorite(favorite)

        favoriteDao.deleteFavoriteByDate("2024-03-15")

        favoriteDao.getAllFavorites().test {
            val favorites = awaitItem()
            assertThat(favorites).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllFavoritesReturnsSortedBySavedAtDesc() = runTest {
        val older = createTestFavorite("2024-03-10", savedAt = 1000L)
        val newer = createTestFavorite("2024-03-15", savedAt = 2000L)
        val newest = createTestFavorite("2024-03-20", savedAt = 3000L)

        favoriteDao.insertFavorite(older)
        favoriteDao.insertFavorite(newest)
        favoriteDao.insertFavorite(newer)

        favoriteDao.getAllFavorites().test {
            val favorites = awaitItem()
            assertThat(favorites).hasSize(3)
            assertThat(favorites[0].date).isEqualTo("2024-03-20")
            assertThat(favorites[1].date).isEqualTo("2024-03-15")
            assertThat(favorites[2].date).isEqualTo("2024-03-10")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavoriteReturnsTrueForExistingDate() = runTest {
        val favorite = createTestFavorite("2024-03-15")
        favoriteDao.insertFavorite(favorite)

        val result = favoriteDao.isFavorite("2024-03-15")

        assertThat(result).isTrue()
    }

    @Test
    fun isFavoriteReturnsFalseForNonExistingDate() = runTest {
        val result = favoriteDao.isFavorite("2024-03-15")

        assertThat(result).isFalse()
    }

    @Test
    fun videoFavoritePreservesThumbnailAndMediaType() = runTest {
        val videoFavorite = FavoriteEntity(
            date = "2024-03-15",
            title = "Video Title",
            explanation = "Video explanation",
            url = "https://youtube.com/watch?v=xyz",
            hdUrl = null,
            mediaType = "video",
            thumbnailUrl = "https://img.youtube.com/vi/xyz/0.jpg",
            copyright = null,
            savedAt = System.currentTimeMillis()
        )

        favoriteDao.insertFavorite(videoFavorite)

        favoriteDao.getAllFavorites().test {
            val favorites = awaitItem()
            assertThat(favorites).hasSize(1)
            assertThat(favorites[0].mediaType).isEqualTo("video")
            assertThat(favorites[0].thumbnailUrl).isEqualTo("https://img.youtube.com/vi/xyz/0.jpg")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createTestFavorite(
        date: String,
        title: String = "Test Title",
        savedAt: Long = System.currentTimeMillis()
    ) = FavoriteEntity(
        date = date,
        title = title,
        explanation = "Test explanation",
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/image_hd.jpg",
        mediaType = "image",
        thumbnailUrl = null,
        copyright = "Test Copyright",
        savedAt = savedAt
    )
}
