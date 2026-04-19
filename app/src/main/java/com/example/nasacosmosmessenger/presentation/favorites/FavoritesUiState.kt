package com.example.nasacosmosmessenger.presentation.favorites

import com.example.nasacosmosmessenger.domain.model.Favorite

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val snackbarMessage: String? = null
) {
    val isEmpty: Boolean
        get() = favorites.isEmpty() && !isLoading
}
