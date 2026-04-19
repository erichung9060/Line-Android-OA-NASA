package com.example.nasacosmosmessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity
import com.example.nasacosmosmessenger.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * POJO for LEFT JOIN result: ChatMessage with optional APOD data.
 * Per ARCHITECTURE.md section 8.2: "chat_messages LEFT JOIN apod_cache ON apod_date"
 *
 * Uses "cached_apod_" prefix for ApodCacheEntity to avoid column name collision with
 * ChatMessageEntity.apodDate (column: "apod_date").
 */
data class ChatMessageWithApod(
    @Embedded val message: ChatMessageEntity,
    @Embedded(prefix = "cached_apod_") val apod: ApodCacheEntity?
)

@Dao
interface ChatMessageDao {

    /**
     * Observe chat messages with APOD data via LEFT JOIN.
     * This is the primary query for displaying chat history with rehydrated APOD content.
     */
    @Query("""
        SELECT
            m.id, m.content, m.apod_date, m.is_from_user, m.timestamp,
            a.date AS cached_apod_date,
            a.title AS cached_apod_title,
            a.explanation AS cached_apod_explanation,
            a.url AS cached_apod_url,
            a.hd_url AS cached_apod_hd_url,
            a.media_type AS cached_apod_media_type,
            a.thumbnail_url AS cached_apod_thumbnail_url,
            a.copyright AS cached_apod_copyright,
            a.cached_at AS cached_apod_cached_at
        FROM chat_messages m
        LEFT JOIN apod_cache a ON m.apod_date = a.date
        ORDER BY m.timestamp ASC
    """)
    fun observeAllWithApod(): Flow<List<ChatMessageWithApod>>

    /**
     * Get all chat messages with APOD data (one-shot).
     * Used by RestoreChatHistoryUseCase for initial load.
     */
    @Query("""
        SELECT
            m.id, m.content, m.apod_date, m.is_from_user, m.timestamp,
            a.date AS cached_apod_date,
            a.title AS cached_apod_title,
            a.explanation AS cached_apod_explanation,
            a.url AS cached_apod_url,
            a.hd_url AS cached_apod_hd_url,
            a.media_type AS cached_apod_media_type,
            a.thumbnail_url AS cached_apod_thumbnail_url,
            a.copyright AS cached_apod_copyright,
            a.cached_at AS cached_apod_cached_at
        FROM chat_messages m
        LEFT JOIN apod_cache a ON m.apod_date = a.date
        ORDER BY m.timestamp ASC
    """)
    suspend fun getAllWithApod(): List<ChatMessageWithApod>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAll(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getCount(): Int
}
