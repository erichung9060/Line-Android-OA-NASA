package com.example.nasacosmosmessenger.di

import android.content.Context
import androidx.room.Room
import com.example.nasacosmosmessenger.data.local.dao.ApodCacheDao
import com.example.nasacosmosmessenger.data.local.dao.ChatMessageDao
import com.example.nasacosmosmessenger.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideApodCacheDao(database: AppDatabase): ApodCacheDao {
        return database.apodCacheDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(database: AppDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }
}
