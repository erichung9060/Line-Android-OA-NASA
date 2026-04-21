package com.example.nasacosmosmessenger.presentation.chat

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.ChatProcessingResult
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.domain.usecase.ObserveChatHistoryUseCase
import com.example.nasacosmosmessenger.domain.usecase.ProcessChatMessageUseCase
import com.example.nasacosmosmessenger.domain.usecase.RestoreChatHistoryUseCase
import com.example.nasacosmosmessenger.domain.usecase.SaveChatMessageUseCase
import com.example.nasacosmosmessenger.domain.usecase.SaveFavoriteUseCase
import com.example.nasacosmosmessenger.presentation.util.BirthdayCardGenerator
import com.example.nasacosmosmessenger.util.ShareUtils
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val processChatMessageUseCase = mockk<ProcessChatMessageUseCase>()
    private val saveChatMessageUseCase = mockk<SaveChatMessageUseCase>(relaxed = true)
    private val observeChatHistoryUseCase = mockk<ObserveChatHistoryUseCase>()
    private val restoreChatHistoryUseCase = mockk<RestoreChatHistoryUseCase>()
    private val saveFavoriteUseCase = mockk<SaveFavoriteUseCase>(relaxed = true)
    private val birthdayCardGenerator = mockk<BirthdayCardGenerator>(relaxed = true)
    private val shareUtils = mockk<ShareUtils>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

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

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ChatViewModel {
        every { observeChatHistoryUseCase() } returns flowOf(emptyList())
        coEvery { restoreChatHistoryUseCase() } returns emptyList()

        return ChatViewModel(
            processChatMessageUseCase,
            saveChatMessageUseCase,
            observeChatHistoryUseCase,
            restoreChatHistoryUseCase,
            saveFavoriteUseCase,
            birthdayCardGenerator,
            shareUtils
        )
    }

    @Test
    fun `initial state is correct`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `send message with date result saves nova message`() = runTest {
        val viewModel = createViewModel()
        coEvery { processChatMessageUseCase("Show me 2020/01/15", any()) } returns
            ChatProcessingResult.ApodFound(apod = sampleApod, wasDateExtractedByAi = false)

        viewModel.sendMessage("Show me 2020/01/15")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `send message without date shows apod and conversation`() = runTest {
        val viewModel = createViewModel()
        coEvery { processChatMessageUseCase("Hello Nova", any()) } returns
            ChatProcessingResult.ApodWithConversation(
                apod = sampleApod,
                aiResponse = "Hello! Here's today's cosmos."
            )

        viewModel.sendMessage("Hello Nova")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `error result updates error state`() = runTest {
        val viewModel = createViewModel()
        coEvery { processChatMessageUseCase("test", any()) } returns
            ChatProcessingResult.Error("Network error")

        viewModel.sendMessage("test")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("Network error")
    }
}
