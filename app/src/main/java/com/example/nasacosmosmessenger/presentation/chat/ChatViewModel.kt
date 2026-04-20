package com.example.nasacosmosmessenger.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.model.Resource
import com.example.nasacosmosmessenger.domain.usecase.GetApodByDateUseCase
import com.example.nasacosmosmessenger.domain.usecase.GetTodayApodUseCase
import com.example.nasacosmosmessenger.domain.usecase.ObserveChatHistoryUseCase
import com.example.nasacosmosmessenger.domain.usecase.ParseDateUseCase
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getTodayApodUseCase: GetTodayApodUseCase,
    private val getApodByDateUseCase: GetApodByDateUseCase,
    private val parseDateUseCase: ParseDateUseCase,
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
                content = "Hi! I'm Nova, your cosmic guide. Tell me a date (like 1990/08/08 or 1990-08-08) and I'll show you what the universe looked like that day!",
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
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = text.trim(),
                apod = null,
                isFromUser = true,
                timestamp = Instant.now()
            )
            saveChatMessageUseCase(userMessage)

            _uiState.update { it.copy(isLoading = true, error = null) }

            val parsedDate = parseDateUseCase(text)

            val result = if (parsedDate != null) {
                getApodByDateUseCase(parsedDate)
            } else {
                getTodayApodUseCase()
            }

            when (result) {
                is Resource.Success -> {
                    val apod = result.data
                    val responseText = if (parsedDate != null) {
                        "On ${apod.date}, the universe presented us with:"
                    } else {
                        "Here's today's cosmic view:"
                    }

                    saveChatMessageUseCase(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            content = responseText,
                            apod = apod,
                            isFromUser = false,
                            timestamp = Instant.now()
                        )
                    )
                    _uiState.update { it.copy(isLoading = false) }
                }

                is Resource.Error -> {
                    val errorContent = when {
                        result.cause is java.net.UnknownHostException ->
                            "Oops! I couldn't reach NASA right now. Please check your connection and try again."
                        result.cause is java.net.SocketTimeoutException ->
                            "The connection timed out. Please try again."
                        result.message?.contains("429") == true ->
                            "NASA's servers are busy. Please try again in a moment."
                        else ->
                            "Oops! Something went wrong. Please try again."
                    }
                    saveChatMessageUseCase(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            content = errorContent,
                            apod = null,
                            isFromUser = false,
                            timestamp = Instant.now()
                        )
                    )
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }

                is Resource.Loading -> {}
            }
        }
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
}
