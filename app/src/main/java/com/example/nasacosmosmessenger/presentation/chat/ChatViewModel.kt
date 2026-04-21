package com.example.nasacosmosmessenger.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.model.ChatProcessingResult
import com.example.nasacosmosmessenger.domain.usecase.ObserveChatHistoryUseCase
import com.example.nasacosmosmessenger.domain.usecase.ProcessChatMessageUseCase
import com.example.nasacosmosmessenger.domain.usecase.RestoreChatHistoryUseCase
import com.example.nasacosmosmessenger.domain.usecase.SaveChatMessageUseCase
import com.example.nasacosmosmessenger.domain.usecase.SaveFavoriteUseCase
import com.example.nasacosmosmessenger.presentation.util.BirthdayCardGenerator
import com.example.nasacosmosmessenger.util.ShareUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val processChatMessageUseCase: ProcessChatMessageUseCase,
    private val saveChatMessageUseCase: SaveChatMessageUseCase,
    private val observeChatHistoryUseCase: ObserveChatHistoryUseCase,
    private val restoreChatHistoryUseCase: RestoreChatHistoryUseCase,
    private val saveFavoriteUseCase: SaveFavoriteUseCase,
    private val birthdayCardGenerator: BirthdayCardGenerator,
    private val shareUtils: ShareUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var hasShownGreeting = false
    private var hasSentTodayApod = false
    private var lastTodayApodDate: LocalDate? = null

    init {
        initializeChatHistory()
    }

    private fun initializeChatHistory() {
        viewModelScope.launch {
            val restoredHistory = restoreChatHistoryUseCase()

            _uiState.update { state ->
                state.copy(
                    messages = restoredHistory,
                    isInitialized = true
                )
            }

            if (restoredHistory.isEmpty() && !hasShownGreeting) {
                hasShownGreeting = true
                showGreeting()
            }

            observeChatHistory()
        }
    }

    private fun observeChatHistory() {
        viewModelScope.launch {
            observeChatHistoryUseCase().collect { messages ->
                _uiState.update { state ->
                    state.copy(messages = messages)
                }
            }
        }
    }

    private fun showGreeting() {
        viewModelScope.launch {
            val greetingMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Hi! I'm Nova, your cosmic guide. Tell me a date (like 1990/08/08 or \"last Friday\") and I'll show you what the universe looked like that day!",
                apod = null,
                isFromUser = false,
                timestamp = Instant.now()
            )
            saveChatMessageUseCase(greetingMessage)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Save user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = text.trim(),
                apod = null,
                isFromUser = true,
                timestamp = Instant.now()
            )
            saveChatMessageUseCase(userMessage)

            _uiState.update { it.copy(isLoading = true, error = null) }

            // Reset today's APOD flag if it's a new day
            val today = LocalDate.now()
            if (lastTodayApodDate != today) {
                hasSentTodayApod = false
                lastTodayApodDate = today
            }

            // Process the message
            val result = processChatMessageUseCase(text, hasSentTodayApod)

            when (result) {
                is ChatProcessingResult.ApodFound -> {
                    val responseText = if (result.wasDateExtractedByAi) {
                        "I understood you wanted ${result.apod.date}. Here's that day's cosmic view:"
                    } else {
                        "On ${result.apod.date}, the universe presented us with:"
                    }
                    saveNovaMessage(responseText, result.apod)
                    _uiState.update { it.copy(isLoading = false) }
                }

                is ChatProcessingResult.ApodWithConversation -> {
                    // Mark that we've sent today's APOD
                    hasSentTodayApod = true
                    lastTodayApodDate = today

                    // Send AI response first, then APOD
                    saveNovaMessage(result.aiResponse, null)
                    saveNovaMessage("Here's today's cosmic view:", result.apod)
                    _uiState.update { it.copy(isLoading = false) }
                }

                is ChatProcessingResult.ConversationOnly -> {
                    saveNovaMessage(result.aiResponse, null)
                    _uiState.update { it.copy(isLoading = false) }
                }

                is ChatProcessingResult.Error -> {
                    val errorContent = when {
                        result.cause is java.net.UnknownHostException ->
                            "Oops! I couldn't reach the server right now. Please check your connection and try again."
                        result.cause is java.net.SocketTimeoutException ->
                            "The connection timed out. Please try again."
                        result.message.contains("429") ->
                            "The server is busy. Please try again in a moment."
                        else ->
                            "Oops! Something went wrong. Please try again."
                    }
                    saveNovaMessage(errorContent, null)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            lastFailedMessage = text.trim()
                        )
                    }
                }
            }
        }
    }

    private suspend fun saveNovaMessage(content: String, apod: Apod?) {
        saveChatMessageUseCase(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                content = content,
                apod = apod,
                isFromUser = false,
                timestamp = Instant.now()
            )
        )
    }

    fun saveToFavorites(apod: Apod) {
        viewModelScope.launch {
            try {
                saveFavoriteUseCase(apod)
                _uiState.update { it.copy(snackbarMessage = "Added to favorites!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Failed to save favorite") }
            }
        }
    }

    fun shareApod(apod: Apod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = birthdayCardGenerator.generateCardForApod(apod)
                if (result.cardFile != null) {
                    shareUtils.shareCard(cardFile = result.cardFile, sourceUrl = result.sourceUrl)
                } else {
                    _uiState.update { it.copy(snackbarMessage = "Failed to generate card") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Failed to share: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun retryLastMessage() {
        val lastMessage = _uiState.value.lastFailedMessage ?: return
        _uiState.update { it.copy(lastFailedMessage = null) }
        sendMessage(lastMessage)
    }

    fun clearRetryState() {
        _uiState.update { it.copy(lastFailedMessage = null) }
    }
}
