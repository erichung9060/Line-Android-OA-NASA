package com.example.nasacosmosmessenger.presentation.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.ChatMessage
import com.example.nasacosmosmessenger.domain.model.MediaType
import com.example.nasacosmosmessenger.ui.theme.CosmosMessengerTheme
import java.time.Instant
import java.time.LocalDate

@Composable
fun ChatBubble(
    message: ChatMessage,
    onAddToFavorites: (Apod) -> Unit,
    onImageClick: (Apod) -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    val bubbleColor = if (message.isFromUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (message.isFromUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isFromUser) {
            NovaAvatar()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = if (message.isFromUser) 0.dp else 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                message.apod?.let { apod ->
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    ApodContent(
                        apod = apod,
                        onImageClick = { onImageClick(apod) },
                        onLongClick = { onAddToFavorites(apod) }
                    )
                }
            }
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Preview
@Composable
private fun ChatBubbleUserPreview() {
    CosmosMessengerTheme {
        ChatBubble(
            message = ChatMessage(
                id = "1",
                content = "Show me 1990/08/08",
                apod = null,
                isFromUser = true,
                timestamp = Instant.now()
            ),
            onAddToFavorites = {},
            onImageClick = {}
        )
    }
}

@Preview
@Composable
private fun ChatBubbleNovaPreview() {
    CosmosMessengerTheme {
        ChatBubble(
            message = ChatMessage(
                id = "2",
                content = "Here's what the cosmos looked like:",
                apod = Apod(
                    date = LocalDate.of(1990, 8, 8),
                    title = "Orion Nebula",
                    explanation = "A beautiful nebula...",
                    url = "https://example.com/image.jpg",
                    hdUrl = null,
                    mediaType = MediaType.IMAGE,
                    thumbnailUrl = null,
                    copyright = "NASA"
                ),
                isFromUser = false,
                timestamp = Instant.now()
            ),
            onAddToFavorites = {},
            onImageClick = {}
        )
    }
}
