package com.example.nasacosmosmessenger.presentation.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.presentation.favorites.components.FavoriteCard
import com.example.nasacosmosmessenger.ui.theme.CosmosMessengerTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun favoritesGrid_showsFavoriteCards() {
        val favorites = listOf(
            createTestFavorite("2024-03-15", "Orion Nebula"),
            createTestFavorite("2024-03-14", "Andromeda Galaxy")
        )

        composeTestRule.setContent {
            CosmosMessengerTheme {
                FavoritesContent(
                    uiState = FavoritesUiState(favorites = favorites, isLoading = false),
                    onFavoriteClick = {},
                    onDeleteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Orion Nebula").assertIsDisplayed()
        composeTestRule.onNodeWithText("Andromeda Galaxy").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsWhenNoFavorites() {
        composeTestRule.setContent {
            CosmosMessengerTheme {
                FavoritesContent(
                    uiState = FavoritesUiState(favorites = emptyList(), isLoading = false),
                    onFavoriteClick = {},
                    onDeleteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No favorites yet!").assertIsDisplayed()
    }

    @Test
    fun favoriteCard_showsStarIndicator() {
        val favorite = createTestFavorite("2024-03-15", "Test APOD")

        composeTestRule.setContent {
            CosmosMessengerTheme {
                FavoriteCard(
                    favorite = favorite,
                    onClick = {},
                    onOverflowClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
    }

    @Test
    fun videoFavorite_showsPlayIndicator() {
        val videoFavorite = createTestFavorite(
            date = "2024-03-15",
            title = "Test Video",
            mediaType = MediaType.VIDEO
        )

        composeTestRule.setContent {
            CosmosMessengerTheme {
                FavoriteCard(
                    favorite = videoFavorite,
                    onClick = {},
                    onOverflowClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Video").assertIsDisplayed()
    }

    @Test
    fun favoriteCard_imageClickTriggersShareCallback() {
        var shareClicked = false
        val favorite = createTestFavorite("2024-03-15", "Test APOD")

        composeTestRule.setContent {
            CosmosMessengerTheme {
                FavoriteCard(
                    favorite = favorite,
                    onClick = { shareClicked = true },
                    onOverflowClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("${favorite.title} - tap to share")
            .performClick()

        assert(shareClicked)
    }

    private fun createTestFavorite(
        date: String,
        title: String,
        mediaType: MediaType = MediaType.IMAGE
    ) = Favorite(
        date = LocalDate.parse(date),
        title = title,
        explanation = "Test explanation",
        url = if (mediaType == MediaType.VIDEO)
            "https://www.youtube.com/embed/test"
        else
            "https://example.com/image.jpg",
        hdUrl = null,
        mediaType = mediaType,
        thumbnailUrl = if (mediaType == MediaType.VIDEO)
            "https://img.youtube.com/vi/test/maxresdefault.jpg"
        else
            null,
        copyright = null,
        savedAt = Instant.now()
    )
}
