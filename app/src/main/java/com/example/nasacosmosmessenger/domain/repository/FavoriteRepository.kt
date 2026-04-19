package com.example.nasacosmosmessenger.domain.repository

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Favorite
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for favorites operations.
 *
 * Per ARCHITECTURE.md Section 8.3:
 * - saveFavorite uses INSERT OR REPLACE (duplicates overwrite)
 * - getAllFavorites returns Flow for reactive UI
 */
interface FavoriteRepository {

    /** Get all favorites as a reactive Flow, sorted by savedAt descending. */
    fun getAllFavorites(): Flow<List<Favorite>>

    /** Save a favorite. Duplicates are overwritten by date. */
    suspend fun saveFavorite(favorite: Favorite)

    /** Save an APOD as a favorite with current timestamp. */
    suspend fun saveFavoriteFromApod(apod: Apod)

    /** Delete a favorite by its date. */
    suspend fun deleteFavorite(date: LocalDate)

    /** Check if an APOD date is already in favorites. */
    suspend fun isFavorite(date: LocalDate): Boolean

    /** Get the count of favorites as a reactive Flow. */
    fun getFavoritesCount(): Flow<Int>
}
