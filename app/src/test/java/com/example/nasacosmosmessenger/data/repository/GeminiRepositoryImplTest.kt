package com.example.nasacosmosmessenger.data.repository

import com.example.nasacosmosmessenger.data.remote.api.GeminiApi
import com.example.nasacosmosmessenger.data.remote.dto.GeminiCandidateDto
import com.example.nasacosmosmessenger.data.remote.dto.GeminiContentDto
import com.example.nasacosmosmessenger.data.remote.dto.GeminiPartDto
import com.example.nasacosmosmessenger.data.remote.dto.GeminiResponseDto
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GeminiRepositoryImplTest {

    private lateinit var geminiApi: GeminiApi
    private lateinit var repository: GeminiRepositoryImpl

    @BeforeEach
    fun setup() {
        geminiApi = mockk()
        repository = GeminiRepositoryImpl(
            geminiApi = geminiApi,
            apiKey = "test-api-key",
            model = "gemini-2.0-flash"
        )
    }

    @Test
    fun `extractDate returns date when Gemini finds one`() = runTest {
        val response = GeminiResponseDto(
            candidates = listOf(
                GeminiCandidateDto(
                    content = GeminiContentDto(
                        role = "model",
                        parts = listOf(GeminiPartDto(text = "2026-04-18"))
                    )
                )
            )
        )
        coEvery { geminiApi.generateContent(any(), any(), any()) } returns response

        val result = repository.extractDate("給我上禮拜五的APOD", "2026-04-20")

        assertThat(result).isEqualTo("2026-04-18")
    }

    @Test
    fun `extractDate returns null when Gemini finds no date`() = runTest {
        val response = GeminiResponseDto(
            candidates = listOf(
                GeminiCandidateDto(
                    content = GeminiContentDto(
                        role = "model",
                        parts = listOf(GeminiPartDto(text = "NO_DATE"))
                    )
                )
            )
        )
        coEvery { geminiApi.generateContent(any(), any(), any()) } returns response

        val result = repository.extractDate("你好啊", "2026-04-20")

        assertThat(result).isNull()
    }

    @Test
    fun `generateResponse returns AI response text`() = runTest {
        val response = GeminiResponseDto(
            candidates = listOf(
                GeminiCandidateDto(
                    content = GeminiContentDto(
                        role = "model",
                        parts = listOf(GeminiPartDto(text = "Hello! I'm Nova, your cosmic guide."))
                    )
                )
            )
        )
        coEvery { geminiApi.generateContent(any(), any(), any()) } returns response

        val result = repository.generateResponse("Hello")

        assertThat(result).isEqualTo("Hello! I'm Nova, your cosmic guide.")
    }
}
