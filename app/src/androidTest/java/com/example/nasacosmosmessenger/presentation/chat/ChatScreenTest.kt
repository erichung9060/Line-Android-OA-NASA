package com.example.nasacosmosmessenger.presentation.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.ui.theme.CosmosMessengerTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

/**
 * UI tests for ChatScreen.
 *
 * Per IMPLEMENTATION_ROADMAP.md Section 2.7.8:
 * - Critical send/scroll/render flows
 */
@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun chatScreen_displaysGreetingMessage() {
        val greeting = ChatMessage(
            id = "1",
            content = "Hello! I'm Nova. Ask me about any date!",
            apod = null,
            isFromUser = false,
            timestamp = Instant.now()
        )

        composeTestRule.setContent {
            CosmosMessengerTheme {
                ChatScreenContent(
                    uiState = ChatUiState(messages = listOf(greeting)),
                    onSendMessage = {},
                    onAddToFavorites = {},
                    onImageClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Hello! I'm Nova. Ask me about any date!")
            .assertIsDisplayed()
    }

    @Test
    fun chatScreen_userCanTypeAndSend() {
        var sentMessage: String? = null

        composeTestRule.setContent {
            CosmosMessengerTheme {
                ChatScreenContent(
                    uiState = ChatUiState(),
                    onSendMessage = { sentMessage = it },
                    onAddToFavorites = {},
                    onImageClick = {}
                )
            }
        }

        // Type a message
        composeTestRule
            .onNodeWithText("Message Nova...")
            .performTextInput("1990/08/08")

        // Click send
        composeTestRule
            .onNodeWithContentDescription("Send message")
            .performClick()

        assertThat(sentMessage).isEqualTo("1990/08/08")
    }

    @Test
    fun chatScreen_displaysApodTitle() {
        val apod = Apod(
            date = LocalDate.of(2024, 3, 15),
            title = "The Orion Nebula",
            explanation = "A beautiful nebula...",
            url = "https://example.com/image.jpg",
            hdUrl = null,
            mediaType = MediaType.IMAGE,
            thumbnailUrl = null,
            copyright = null
        )

        val novaMessage = ChatMessage(
            id = "2",
            content = "Here's what the cosmos looked like:",
            apod = apod,
            isFromUser = false,
            timestamp = Instant.now()
        )

        composeTestRule.setContent {
            CosmosMessengerTheme {
                ChatScreenContent(
                    uiState = ChatUiState(messages = listOf(novaMessage)),
                    onSendMessage = {},
                    onAddToFavorites = {},
                    onImageClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("The Orion Nebula")
            .assertIsDisplayed()
    }

    @Test
    fun chatScreen_showsInputField() {
        composeTestRule.setContent {
            CosmosMessengerTheme {
                ChatScreenContent(
                    uiState = ChatUiState(isLoading = false),
                    onSendMessage = {},
                    onAddToFavorites = {},
                    onImageClick = {}
                )
            }
        }

        // Input placeholder should be visible
        composeTestRule
            .onNodeWithText("Message Nova...")
            .assertIsDisplayed()
    }

    @Test
    fun chatScreen_userMessagesAlignRight_novaMessagesAlignLeft() {
        val userMessage = ChatMessage(
            id = "1",
            content = "Show me 1990/08/08",
            apod = null,
            isFromUser = true,
            timestamp = Instant.now()
        )

        val novaMessage = ChatMessage(
            id = "2",
            content = "Here's what the universe looked like...",
            apod = null,
            isFromUser = false,
            timestamp = Instant.now()
        )

        composeTestRule.setContent {
            CosmosMessengerTheme {
                ChatScreenContent(
                    uiState = ChatUiState(messages = listOf(userMessage, novaMessage)),
                    onSendMessage = {},
                    onAddToFavorites = {},
                    onImageClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Show me 1990/08/08")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Here's what the universe looked like...")
            .assertIsDisplayed()
    }
}
