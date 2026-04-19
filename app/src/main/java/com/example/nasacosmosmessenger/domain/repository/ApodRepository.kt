package com.example.nasacosmosmessenger.domain.repository

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Resource
import java.time.LocalDate

interface ApodRepository {

    /**
     * Get APOD for today's date.
     * Uses cache-first strategy: returns cached if available, otherwise fetches from network.
     */
    suspend fun getTodayApod(): Resource<Apod>

    /**
     * Get APOD for a specific date.
     * Uses cache-first strategy: returns cached if available, otherwise fetches from network.
     */
    suspend fun getApodByDate(date: LocalDate): Resource<Apod>

    /**
     * Get cached APOD for a specific date without network fallback.
     * Returns null if not cached. Used internally for rehydration.
     */
    suspend fun getCachedApod(date: LocalDate): Apod?
}
