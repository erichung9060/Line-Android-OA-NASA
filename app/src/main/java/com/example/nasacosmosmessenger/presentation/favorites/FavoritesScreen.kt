package com.example.nasacosmosmessenger.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.presentation.common.EmptyState
import com.example.nasacosmosmessenger.presentation.favorites.components.FavoriteCard

/**
 * Favorites screen showing saved APOD entries in a 2-column grid.
 *
 * Per ARCHITECTURE.md Section 7.2:
 * - 2-column grid layout
 * - Empty state when no favorites
 * - Full-screen error state per ARCHITECTURE.md Section 11.2
 * - Tap image to share birthday card
 * - Overflow menu for delete and source link
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Favorites") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        FavoritesContent(
            uiState = uiState,
            onFavoriteClick = viewModel::shareFavorite,
            onDeleteClick = viewModel::deleteFavorite,
            onRetry = viewModel::retry,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
internal fun FavoritesContent(
    uiState: FavoritesUiState,
    onFavoriteClick: (Favorite) -> Unit,
    onDeleteClick: (Favorite) -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                // Full-screen error per ARCHITECTURE.md Section 11.2
                ErrorState(
                    message = uiState.error,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.isEmpty -> {
                EmptyState()
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.favorites,
                        key = { it.date.toString() }
                    ) { favorite ->
                        FavoriteCard(
                            favorite = favorite,
                            onClick = { onFavoriteClick(favorite) },
                            onOverflowClick = { onDeleteClick(favorite) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
