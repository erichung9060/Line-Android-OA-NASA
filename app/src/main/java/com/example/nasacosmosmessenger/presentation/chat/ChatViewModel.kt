package com.example.nasacosmosmessenger.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.model.Resource
import com.example.nasacosmosmessenger.domain.usecase.GetApodByDateUseCase
import com.example.nasacosmosmessenger.domain.usecase.GetTodayApodUseCase
import com.example.nasacosmosmessenger.domain.usecase.ObserveChatHistoryUseCase
import com.example.nasacosmosmessenger.domain.usecase.ParseDateUseCase
import com.example.nasacosmosmessenger.domain.usecase.RestoreChatHistoryUseCase
import com.example.nasacosmosmessenger.domain.usecase.SaveChatMessageUseCase
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
    private val restoreChatHistoryUseCase: RestoreChatHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Flag to prevent greeting from being shown multiple times
    private var hasShownGreeting = false

    init {
        initializeChatHistory()
    }

    /**
     * Initialize chat history following ARCHITECTURE.md section 8.2:
     * 1. Use RestoreChatHistoryUseCase for initial load
     * 2. Show greeting if history is empty (only once)
     * 3. Start observing for reactive updates
     */
    private fun initializeChatHistory() {
        viewModelScope.launch {
            // Step 1: Restore existing history (one-shot)
            val restoredHistory = restoreChatHistoryUseCase()

            _uiState.update { state ->
                state.copy(
                    messages = restoredHistory,
                    isInitialized = true
                )
            }

            // Step 2: Show greeting if no history exists (only once)
            if (restoredHistory.isEmpty() && !hasShownGreeting) {
                hasShownGreeting = true
                showGreeting()
            }

            // Step 3: Start observing for reactive updates
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
            // 1. Create and save user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = text.trim(),
                apod = null,
                isFromUser = true,
                timestamp = Instant.now()
            )
            saveChatMessageUseCase(userMessage)

            // 2. Set loading state
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 3. Parse date from message
            val parsedDate = parseDateUseCase(text)

            // 4. Fetch APOD (by date or today)
            val result = if (parsedDate != null) {
                getApodByDateUseCase(parsedDate)
            } else {
                getTodayApodUseCase()
            }

            // 5. Handle result
            when (result) {
                is Resource.Success -> {
                    val apod = result.data
                    val responseText = if (parsedDate != null) {
                        "On ${apod.date}, the universe presented us with:"
                    } else {
                        "Here's today's cosmic view:"
                    }

                    val novaMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = responseText,
                        apod = apod,
                        isFromUser = false,
                        timestamp = Instant.now()
                    )
                    saveChatMessageUseCase(novaMessage)
                    _uiState.update { it.copy(isLoading = false) }
                }

                is Resource.Error -> {
                    val errorMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = "Oops! I couldn't reach NASA right now. ${result.message}",
                        apod = null,
                        isFromUser = false,
                        timestamp = Instant.now()
                    )
                    saveChatMessageUseCase(errorMessage)
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }

                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
