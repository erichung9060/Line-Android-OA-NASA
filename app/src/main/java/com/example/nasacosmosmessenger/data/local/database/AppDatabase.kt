package com.example.nasacosmosmessenger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nasacosmosmessenger.data.local.dao.ApodCacheDao
import com.example.nasacosmosmessenger.data.local.dao.ChatMessageDao
import com.example.nasacosmosmessenger.data.local.dao.FavoriteDao
import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity
import com.example.nasacosmosmessenger.data.local.entity.ChatMessageEntity
import com.example.nasacosmosmessenger.data.local.entity.FavoriteEntity

/**
 * Room database for NASA Cosmos Messenger.
 *
 * Version History:
 * - v1: Initial schema with ApodCacheEntity and ChatMessageEntity
 * - v2: Added FavoriteEntity for favorites feature
 */
@Database(
    entities = [
        ApodCacheEntity::class,
        ChatMessageEntity::class,
        FavoriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun apodCacheDao(): ApodCacheDao

    abstract fun chatMessageDao(): ChatMessageDao

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "cosmos_messenger_db"
    }
}
