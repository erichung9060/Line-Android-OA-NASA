package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.repository.GeminiRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ExtractDateWithGeminiUseCaseTest {

    private lateinit var geminiRepository: GeminiRepository
    private lateinit var useCase: ExtractDateWithGeminiUseCase

    @BeforeEach
    fun setup() {
        geminiRepository = mockk()
        useCase = ExtractDateWithGeminiUseCase(geminiRepository)
    }

    @Test
    fun `returns LocalDate when Gemini extracts valid date`() = runTest {
        coEvery { geminiRepository.extractDate(any(), any()) } returns "2026-04-18"

        val result = useCase("給我上禮拜五的APOD")

        assertThat(result).isEqualTo(LocalDate.of(2026, 4, 18))
    }

    @Test
    fun `returns null when Gemini finds no date`() = runTest {
        coEvery { geminiRepository.extractDate(any(), any()) } returns null

        val result = useCase("你好")

        assertThat(result).isNull()
    }

    @Test
    fun `returns null for date before APOD start`() = runTest {
        coEvery { geminiRepository.extractDate(any(), any()) } returns "1990-01-01"

        val result = useCase("給我1990年的APOD")

        assertThat(result).isNull()
    }

    @Test
    fun `returns null for future date`() = runTest {
        val futureDate = LocalDate.now().plusDays(10).toString()
        coEvery { geminiRepository.extractDate(any(), any()) } returns futureDate

        val result = useCase("給我下個月的APOD")

        assertThat(result).isNull()
    }
}
