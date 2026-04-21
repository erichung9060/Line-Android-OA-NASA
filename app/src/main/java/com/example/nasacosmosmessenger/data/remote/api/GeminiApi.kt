package com.example.nasacosmosmessenger.data.remote.api

import com.example.nasacosmosmessenger.data.remote.dto.GeminiRequestDto
import com.example.nasacosmosmessenger.data.remote.dto.GeminiResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApi {

    @POST("v1/publishers/google/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequestDto
    ): GeminiResponseDto

    companion object {
        const val BASE_URL = "https://aiplatform.googleapis.com/"
    }
}
