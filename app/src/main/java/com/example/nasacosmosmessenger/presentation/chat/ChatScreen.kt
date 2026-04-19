package com.example.nasacosmosmessenger.presentation.chat

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.presentation.chat.components.ChatBubble
import com.example.nasacosmosmessenger.presentation.chat.components.MessageInput
import com.example.nasacosmosmessenger.presentation.common.TypingIndicator
import com.example.nasacosmosmessenger.ui.theme.CosmosMessengerTheme
import java.time.Instant
import java.time.LocalDate

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreenContent(
        uiState = uiState,
        onSendMessage = viewModel::sendMessage,
        onAddToFavorites = { _ ->
            // TODO: Implement in Phase 3
        },
        onImageClick = { _ ->
            // TODO: Implement share in Phase 3
        },
        modifier = modifier
    )
}

@Composable
private fun ChatScreenContent(
    uiState: ChatUiState,
    onSendMessage: (String) -> Unit,
    onAddToFavorites: (Apod) -> Unit,
    onImageClick: (Apod) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Auto-scroll when loading (typing indicator appears)
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading && uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show error toast
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            MessageInput(
                onSendMessage = onSendMessage,
                enabled = !uiState.isLoading,
                modifier = Modifier.imePadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    ChatBubble(
                        message = message,
                        onAddToFavorites = onAddToFavorites,
                        onImageClick = onImageClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Typing indicator
                if (uiState.isLoading) {
                    item {
                        TypingIndicator(
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    CosmosMessengerTheme {
        ChatScreenContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        content = "Hi! I'm Nova, your cosmic guide.",
                        apod = null,
                        isFromUser = false,
                        timestamp = Instant.now()
                    ),
                    ChatMessage(
                        id = "2",
                        content = "Show me 1990/08/08",
                        apod = null,
                        isFromUser = true,
                        timestamp = Instant.now()
                    ),
                    ChatMessage(
                        id = "3",
                        content = "Here's what the cosmos looked like:",
                        apod = Apod(
                            date = LocalDate.of(1990, 8, 8),
                            title = "Orion Nebula",
                            explanation = "A beautiful nebula in the night sky...",
                            url = "https://example.com/image.jpg",
                            hdUrl = null,
                            mediaType = MediaType.IMAGE,
                            thumbnailUrl = null,
                            copyright = "NASA"
                        ),
                        isFromUser = false,
                        timestamp = Instant.now()
                    )
                ),
                isLoading = false
            ),
            onSendMessage = {},
            onAddToFavorites = {},
            onImageClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenLoadingPreview() {
    CosmosMessengerTheme {
        ChatScreenContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        content = "Show me today",
                        apod = null,
                        isFromUser = true,
                        timestamp = Instant.now()
                    )
                ),
                isLoading = true
            ),
            onSendMessage = {},
            onAddToFavorites = {},
            onImageClick = {}
        )
    }
}
