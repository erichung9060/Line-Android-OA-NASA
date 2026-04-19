package com.example.nasacosmosmessenger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nasacosmosmessenger.data.local.dao.ApodCacheDao
import com.example.nasacosmosmessenger.data.local.dao.ChatMessageDao
import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity
import com.example.nasacosmosmessenger.data.local.entity.ChatMessageEntity

@Database(
    entities = [
        ApodCacheEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun apodCacheDao(): ApodCacheDao

    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        const val DATABASE_NAME = "cosmos_messenger_db"
    }
}
