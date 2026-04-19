package com.example.nasacosmosmessenger.data.remote.api

import com.example.nasacosmosmessenger.data.remote.dto.ApodResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaApodApi {

    @GET("planetary/apod")
    suspend fun getApod(
        @Query("date") date: String? = null
    ): ApodResponse

    companion object {
        const val BASE_URL = "https://api.nasa.gov/"
    }
}
