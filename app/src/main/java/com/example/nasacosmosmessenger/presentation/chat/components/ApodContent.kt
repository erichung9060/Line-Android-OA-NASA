package com.example.nasacosmosmessenger.presentation.chat.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.ui.theme.CosmosMessengerTheme
import com.example.nasacosmosmessenger.util.DateFormatter
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApodContent(
    apod: Apod,
    onImageClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Image or Video Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onImageClick,
                    onLongClick = onLongClick
                )
        ) {
            val imageUrl = when (apod.mediaType) {
                MediaType.IMAGE -> apod.url
                MediaType.VIDEO -> apod.thumbnailUrl ?: apod.url
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = "APOD image: ${apod.title}, dated ${apod.date}",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            // Play icon overlay for video
            if (apod.mediaType == MediaType.VIDEO) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = "Video",
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = apod.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Date
        Text(
            text = DateFormatter.formatForDisplay(apod.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Explanation (truncated)
        Text(
            text = apod.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        // Copyright if available
        apod.copyright?.let { copyright ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "© $copyright",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun ApodContentPreview() {
    CosmosMessengerTheme {
        ApodContent(
            apod = Apod(
                date = LocalDate.of(2024, 1, 15),
                title = "Orion Nebula in Infrared",
                explanation = "The Great Nebula in Orion, also known as M42, is one of the most famous nebulae in the sky.",
                url = "https://example.com/image.jpg",
                hdUrl = null,
                mediaType = MediaType.IMAGE,
                thumbnailUrl = null,
                copyright = "NASA"
            ),
            onImageClick = {},
            onLongClick = {}
        )
    }
}
