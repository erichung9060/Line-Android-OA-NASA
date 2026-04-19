package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.domain.model.Resource
import com.example.nasacosmosmessenger.domain.repository.ApodRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GetApodUseCaseTest {

    private val repository = mockk<ApodRepository>()
    private val getTodayUseCase = GetTodayApodUseCase(repository)
    private val getByDateUseCase = GetApodByDateUseCase(repository)

    private val sampleApod = Apod(
        date = LocalDate.of(2024, 1, 15),
        title = "Test APOD",
        explanation = "Test explanation",
        url = "https://example.com/image.jpg",
        hdUrl = null,
        mediaType = MediaType.IMAGE,
        thumbnailUrl = null,
        copyright = null
    )

    @Test
    fun `get today use case delegates to repository without explicit date`() = runTest {
        coEvery { repository.getTodayApod() } returns Resource.Success(sampleApod)

        val result = getTodayUseCase()

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        coVerify { repository.getTodayApod() }
    }

    @Test
    fun `get by date use case delegates to repository with requested date`() = runTest {
        val date = LocalDate.of(2024, 1, 15)
        coEvery { repository.getApodByDate(date) } returns Resource.Success(sampleApod)

        val result = getByDateUseCase(date)

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        coVerify { repository.getApodByDate(date) }
    }
}
