package com.example.nasacosmosmessenger.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.usecase.DeleteFavoriteUseCase
import com.example.nasacosmosmessenger.domain.usecase.GetFavoritesUseCase
import com.example.nasacosmosmessenger.presentation.util.BirthdayCardGenerator
import com.example.nasacosmosmessenger.util.ShareUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase,
    private val birthdayCardGenerator: BirthdayCardGenerator,
    private val shareUtils: ShareUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getFavoritesUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load favorites. Please try again."
                        )
                    }
                }
                .collect { favorites ->
                    _uiState.update { it.copy(favorites = favorites, isLoading = false, error = null) }
                }
        }
    }

    fun retry() {
        observeFavorites()
    }

    fun deleteFavorite(favorite: Favorite) {
        viewModelScope.launch {
            try {
                deleteFavoriteUseCase(favorite.date)
                _uiState.update { it.copy(snackbarMessage = "Removed from favorites") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Failed to delete favorite") }
            }
        }
    }

    fun shareFavorite(favorite: Favorite) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = birthdayCardGenerator.generateCardForFavorite(favorite)
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
}
