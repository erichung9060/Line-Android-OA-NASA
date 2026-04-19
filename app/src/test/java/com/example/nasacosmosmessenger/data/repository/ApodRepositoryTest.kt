package com.example.nasacosmosmessenger.data.repository

import com.example.nasacosmosmessenger.data.local.dao.ApodCacheDao
import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity
import com.example.nasacosmosmessenger.data.remote.api.NasaApodApi
import com.example.nasacosmosmessenger.data.remote.dto.ApodResponse
import com.example.nasacosmosmessenger.domain.model.Resource
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApodRepositoryTest {

    private val api = mockk<NasaApodApi>()
    private val cacheDao = mockk<ApodCacheDao>(relaxed = true)
    private val repository = ApodRepositoryImpl(api, cacheDao)

    private val testDate = LocalDate.of(2024, 1, 15)
    private val dateString = "2024-01-15"

    private val cachedEntity = ApodCacheEntity(
        date = dateString,
        title = "Cached APOD",
        explanation = "Cached explanation",
        url = "https://example.com/cached.jpg",
        hdUrl = null,
        mediaType = "image",
        thumbnailUrl = null,
        copyright = null,
        cachedAt = System.currentTimeMillis()
    )

    private val apiResponse = ApodResponse(
        date = dateString,
        title = "API APOD",
        explanation = "API explanation",
        url = "https://example.com/api.jpg",
        hdUrl = null,
        mediaType = "image",
        copyright = null,
        thumbnailUrl = null
    )

    @Test
    fun `cache hit returns cached data without network call`() = runTest {
        coEvery { cacheDao.getByDate(dateString) } returns cachedEntity

        val result = repository.getApodByDate(testDate)

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat((result as Resource.Success).data.title).isEqualTo("Cached APOD")
        coVerify(exactly = 0) { api.getApod(any()) }
    }

    @Test
    fun `cache miss fetches network and caches result`() = runTest {
        coEvery { cacheDao.getByDate(dateString) } returns null
        coEvery { api.getApod(dateString) } returns apiResponse

        val result = repository.getApodByDate(testDate)

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat((result as Resource.Success).data.title).isEqualTo("API APOD")
        coVerify { cacheDao.insert(any()) }
    }

    @Test
    fun `network failure with cache returns cached data`() = runTest {
        coEvery { cacheDao.getByDate(dateString) } returns null andThen cachedEntity
        coEvery { api.getApod(dateString) } throws RuntimeException("Network error")

        val result = repository.getApodByDate(testDate)

        assertThat(result).isInstanceOf(Resource.Success::class.java)
    }

    @Test
    fun `network failure without cache returns error`() = runTest {
        coEvery { cacheDao.getByDate(dateString) } returns null
        coEvery { api.getApod(dateString) } throws RuntimeException("Network error")

        val result = repository.getApodByDate(testDate)

        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }
}
