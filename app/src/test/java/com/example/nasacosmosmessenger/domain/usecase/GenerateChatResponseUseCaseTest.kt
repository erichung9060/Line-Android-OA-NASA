package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.repository.GeminiRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateChatResponseUseCaseTest {

    private lateinit var geminiRepository: GeminiRepository
    private lateinit var useCase: GenerateChatResponseUseCase

    @BeforeEach
    fun setup() {
        geminiRepository = mockk()
        useCase = GenerateChatResponseUseCase(geminiRepository)
    }

    @Test
    fun `returns AI response from repository`() = runTest {
        val expectedResponse = "Hello! I'm Nova, your cosmic guide."
        coEvery { geminiRepository.generateResponse(any()) } returns expectedResponse

        val result = useCase("Hello")

        assertThat(result).isEqualTo(expectedResponse)
    }

    @Test
    fun `passes message to repository correctly`() = runTest {
        coEvery { geminiRepository.generateResponse("Tell me about Mars") } returns "Mars is fascinating!"

        val result = useCase("Tell me about Mars")

        assertThat(result).isEqualTo("Mars is fascinating!")
    }
}
