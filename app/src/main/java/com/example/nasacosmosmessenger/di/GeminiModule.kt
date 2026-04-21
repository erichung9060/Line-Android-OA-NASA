package com.example.nasacosmosmessenger.di

import com.example.nasacosmosmessenger.BuildConfig
import com.example.nasacosmosmessenger.data.remote.api.GeminiApi
import com.example.nasacosmosmessenger.data.repository.GeminiRepositoryImpl
import com.example.nasacosmosmessenger.domain.repository.GeminiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiClient

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    @GeminiClient
    fun provideGeminiOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApi(
        @GeminiClient okHttpClient: OkHttpClient,
        json: Json
    ): GeminiApi {
        return Retrofit.Builder()
            .baseUrl(GeminiApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiRepository(
        geminiApi: GeminiApi
    ): GeminiRepository {
        return GeminiRepositoryImpl(
            geminiApi = geminiApi,
            apiKey = BuildConfig.GEMINI_API_KEY,
            model = BuildConfig.GEMINI_MODEL
        )
    }
}
