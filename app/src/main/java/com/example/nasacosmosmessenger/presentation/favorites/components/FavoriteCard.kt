package com.example.nasacosmosmessenger.presentation.favorites.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.ui.theme.CosmosMessengerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Card component for displaying a favorite in the grid.
 *
 * Per ARCHITECTURE.md Section 7.2:
 * - Shows image or thumbnail
 * - Title and date
 * - Star indicator
 * - Play indicator for videos
 */
@Composable
fun FavoriteCard(
    favorite: Favorite,
    onClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .clickable(onClick = onClick)
            ) {
                AsyncImage(
                    model = favorite.displayUrl,
                    contentDescription = "${favorite.title} - tap to share",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Star indicator (top-left)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(20.dp)
                )

                // Play indicator for videos (center)
                if (favorite.isVideo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Overflow menu (top-right)
                FavoriteOverflowMenu(
                    favorite = favorite,
                    onDeleteClick = onOverflowClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = favorite.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = favorite.date.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteCardPreview() {
    CosmosMessengerTheme {
        FavoriteCard(
            favorite = Favorite(
                date = LocalDate.of(2024, 3, 15),
                title = "The Orion Nebula in Infrared",
                explanation = "A beautiful nebula...",
                url = "https://example.com/image.jpg",
                hdUrl = null,
                mediaType = MediaType.IMAGE,
                thumbnailUrl = null,
                copyright = "NASA",
                savedAt = Instant.now()
            ),
            onClick = {},
            onOverflowClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteCardVideoPreview() {
    CosmosMessengerTheme {
        FavoriteCard(
            favorite = Favorite(
                date = LocalDate.of(2024, 3, 15),
                title = "Perseverance Rover Landing Video",
                explanation = "Watch the landing...",
                url = "https://youtube.com/watch?v=xyz",
                hdUrl = null,
                mediaType = MediaType.VIDEO,
                thumbnailUrl = "https://img.youtube.com/vi/xyz/0.jpg",
                copyright = null,
                savedAt = Instant.now()
            ),
            onClick = {},
            onOverflowClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
