package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.ChatProcessingResult
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.domain.model.Resource
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProcessChatMessageUseCaseTest {

    private lateinit var parseDateUseCase: ParseDateUseCase
    private lateinit var extractDateWithGeminiUseCase: ExtractDateWithGeminiUseCase
    private lateinit var getTodayApodUseCase: GetTodayApodUseCase
    private lateinit var getApodByDateUseCase: GetApodByDateUseCase
    private lateinit var generateChatResponseUseCase: GenerateChatResponseUseCase
    private lateinit var useCase: ProcessChatMessageUseCase

    // Use correct Apod constructor: LocalDate date, hdUrl (nullable), MediaType enum, thumbnailUrl field
    private val testApod = Apod(
        date = LocalDate.of(2026, 4, 20),
        title = "Test APOD",
        explanation = "Test explanation",
        url = "https://example.com/image.jpg",
        hdUrl = null,
        mediaType = MediaType.IMAGE,
        thumbnailUrl = null,
        copyright = null
    )

    @BeforeEach
    fun setup() {
        parseDateUseCase = mockk()
        extractDateWithGeminiUseCase = mockk()
        getTodayApodUseCase = mockk()
        getApodByDateUseCase = mockk()
        generateChatResponseUseCase = mockk()

        useCase = ProcessChatMessageUseCase(
            parseDateUseCase = parseDateUseCase,
            extractDateWithGeminiUseCase = extractDateWithGeminiUseCase,
            getTodayApodUseCase = getTodayApodUseCase,
            getApodByDateUseCase = getApodByDateUseCase,
            generateChatResponseUseCase = generateChatResponseUseCase
        )
    }

    @Test
    fun `branch 1 - regex finds date, returns APOD`() = runTest {
        val date = LocalDate.of(2026, 4, 15)
        // ParseDateUseCase.invoke() is NOT suspend — use every, not coEvery
        every { parseDateUseCase(any()) } returns date
        coEvery { getApodByDateUseCase(date) } returns Resource.Success(testApod)

        val result = useCase("2026/04/15", hasSentTodayApod = false)

        assertThat(result).isInstanceOf(ChatProcessingResult.ApodFound::class.java)
        val apodResult = result as ChatProcessingResult.ApodFound
        assertThat(apodResult.wasDateExtractedByAi).isFalse()
    }

    @Test
    fun `branch 2 - regex fails, Gemini finds date, returns APOD`() = runTest {
        val date = LocalDate.of(2026, 4, 18)
        every { parseDateUseCase(any()) } returns null
        coEvery { extractDateWithGeminiUseCase(any()) } returns date
        coEvery { getApodByDateUseCase(date) } returns Resource.Success(testApod)

        val result = useCase("給我上禮拜五的APOD", hasSentTodayApod = false)

        assertThat(result).isInstanceOf(ChatProcessingResult.ApodFound::class.java)
        val apodResult = result as ChatProcessingResult.ApodFound
        assertThat(apodResult.wasDateExtractedByAi).isTrue()
    }

    @Test
    fun `branch 3 - no date found, today APOD not sent, returns APOD with conversation`() = runTest {
        every { parseDateUseCase(any()) } returns null
        coEvery { extractDateWithGeminiUseCase(any()) } returns null
        coEvery { getTodayApodUseCase() } returns Resource.Success(testApod)
        coEvery { generateChatResponseUseCase(any()) } returns "Hello! Here's today's cosmic view."

        val result = useCase("你好", hasSentTodayApod = false)

        assertThat(result).isInstanceOf(ChatProcessingResult.ApodWithConversation::class.java)
        val conversationResult = result as ChatProcessingResult.ApodWithConversation
        assertThat(conversationResult.apod).isEqualTo(testApod)
        assertThat(conversationResult.aiResponse).isEqualTo("Hello! Here's today's cosmic view.")
    }

    @Test
    fun `branch 4 - no date found, today APOD already sent, returns conversation only`() = runTest {
        every { parseDateUseCase(any()) } returns null
        coEvery { extractDateWithGeminiUseCase(any()) } returns null
        coEvery { generateChatResponseUseCase(any()) } returns "Mars is the fourth planet from the Sun."

        val result = useCase("告訴我火星的事", hasSentTodayApod = true)

        assertThat(result).isInstanceOf(ChatProcessingResult.ConversationOnly::class.java)
        val conversationResult = result as ChatProcessingResult.ConversationOnly
        assertThat(conversationResult.aiResponse).isEqualTo("Mars is the fourth planet from the Sun.")
    }

    @Test
    fun `returns error when APOD fetch fails`() = runTest {
        val date = LocalDate.of(2026, 4, 15)
        every { parseDateUseCase(any()) } returns date
        coEvery { getApodByDateUseCase(date) } returns Resource.Error("Network error")

        val result = useCase("2026/04/15", hasSentTodayApod = false)

        assertThat(result).isInstanceOf(ChatProcessingResult.Error::class.java)
    }
}
