package com.example.nasacosmosmessenger.di

import com.example.nasacosmosmessenger.data.repository.ApodRepositoryImpl
import com.example.nasacosmosmessenger.data.repository.ChatRepositoryImpl
import com.example.nasacosmosmessenger.data.repository.FavoriteRepositoryImpl
import com.example.nasacosmosmessenger.domain.repository.ApodRepository
import com.example.nasacosmosmessenger.domain.repository.ChatRepository
import com.example.nasacosmosmessenger.domain.repository.FavoriteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindApodRepository(
        impl: ApodRepositoryImpl
    ): ApodRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        impl: FavoriteRepositoryImpl
    ): FavoriteRepository
}
