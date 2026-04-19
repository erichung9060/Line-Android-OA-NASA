package com.example.nasacosmosmessenger.data.repository

import com.example.nasacosmosmessenger.data.local.dao.ChatMessageDao
import com.example.nasacosmosmessenger.data.local.dao.ChatMessageWithApod
import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity
import com.example.nasacosmosmessenger.data.local.entity.ChatMessageEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ChatRepositoryTest {

    private val chatMessageDao = mockk<ChatMessageDao>(relaxed = true)
    private val repository = ChatRepositoryImpl(chatMessageDao)

    @Test
    fun `getChatHistory returns messages with rehydrated APOD`() = runTest {
        val messageEntity = ChatMessageEntity(
            id = "1",
            content = "Test",
            apodDate = "2024-01-15",
            isFromUser = false,
            timestamp = 1000L
        )
        val apodEntity = ApodCacheEntity(
            date = "2024-01-15",
            title = "APOD Title",
            explanation = "Explanation",
            url = "https://example.com/image.jpg",
            hdUrl = null,
            mediaType = "image",
            thumbnailUrl = null,
            copyright = null,
            cachedAt = System.currentTimeMillis()
        )

        coEvery { chatMessageDao.getAllWithApod() } returns listOf(
            ChatMessageWithApod(messageEntity, apodEntity)
        )

        val messages = repository.getChatHistory()

        assertThat(messages).hasSize(1)
        assertThat(messages[0].apod).isNotNull()
        assertThat(messages[0].apod?.title).isEqualTo("APOD Title")
    }

    @Test
    fun `message with missing APOD cache returns null apod`() = runTest {
        val messageEntity = ChatMessageEntity(
            id = "1",
            content = "Test",
            apodDate = "2024-01-15",
            isFromUser = false,
            timestamp = 1000L
        )

        coEvery { chatMessageDao.getAllWithApod() } returns listOf(
            ChatMessageWithApod(messageEntity, null)
        )

        val messages = repository.getChatHistory()

        assertThat(messages).hasSize(1)
        assertThat(messages[0].apod).isNull()
        assertThat(messages[0].content).isEqualTo("Test")
    }
}
