package com.example.nasacosmosmessenger.presentation.favorites

import app.cash.turbine.test
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.domain.usecase.DeleteFavoriteUseCase
import com.example.nasacosmosmessenger.domain.usecase.GetFavoritesUseCase
import com.example.nasacosmosmessenger.presentation.util.BirthdayCardGenerator
import com.example.nasacosmosmessenger.util.ShareUtils
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getFavoritesUseCase: GetFavoritesUseCase
    private lateinit var deleteFavoriteUseCase: DeleteFavoriteUseCase
    private lateinit var birthdayCardGenerator: BirthdayCardGenerator
    private lateinit var shareUtils: ShareUtils
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getFavoritesUseCase = mockk()
        deleteFavoriteUseCase = mockk(relaxed = true)
        birthdayCardGenerator = mockk(relaxed = true)
        shareUtils = mockk(relaxed = true)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows loading then favorites`() = runTest {
        val favorites = listOf(createTestFavorite(LocalDate.parse("2024-03-15")))
        every { getFavoritesUseCase() } returns flowOf(favorites)

        viewModel = FavoritesViewModel(
            getFavoritesUseCase,
            deleteFavoriteUseCase,
            birthdayCardGenerator,
            shareUtils
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.favorites).hasSize(1)
            assertThat(state.isEmpty).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty favorites shows isEmpty true`() = runTest {
        every { getFavoritesUseCase() } returns flowOf(emptyList())

        viewModel = FavoritesViewModel(
            getFavoritesUseCase,
            deleteFavoriteUseCase,
            birthdayCardGenerator,
            shareUtils
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isEmpty).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteFavorite calls use case and shows snackbar`() = runTest {
        val favorite = createTestFavorite(LocalDate.parse("2024-03-15"))
        every { getFavoritesUseCase() } returns flowOf(listOf(favorite))

        viewModel = FavoritesViewModel(
            getFavoritesUseCase,
            deleteFavoriteUseCase,
            birthdayCardGenerator,
            shareUtils
        )

        viewModel.deleteFavorite(favorite)

        coVerify { deleteFavoriteUseCase(LocalDate.parse("2024-03-15")) }

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.snackbarMessage).isEqualTo("Removed from favorites")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSnackbar clears message`() = runTest {
        val favorite = createTestFavorite(LocalDate.parse("2024-03-15"))
        every { getFavoritesUseCase() } returns flowOf(listOf(favorite))

        viewModel = FavoritesViewModel(
            getFavoritesUseCase,
            deleteFavoriteUseCase,
            birthdayCardGenerator,
            shareUtils
        )

        viewModel.deleteFavorite(favorite)
        viewModel.clearSnackbar()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.snackbarMessage).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createTestFavorite(date: LocalDate) = Favorite(
        date = date,
        title = "Test Title",
        explanation = "Test explanation",
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/image_hd.jpg",
        mediaType = MediaType.IMAGE,
        thumbnailUrl = null,
        copyright = "Test Copyright",
        savedAt = Instant.now()
    )
}
