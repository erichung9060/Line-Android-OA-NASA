package com.example.nasacosmosmessenger.data.remote.api

import com.example.nasacosmosmessenger.data.remote.dto.GeminiRequestDto
import com.example.nasacosmosmessenger.data.remote.dto.GeminiResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequestDto
    ): GeminiResponseDto

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
    }
}
