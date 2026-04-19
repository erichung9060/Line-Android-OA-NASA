package com.example.nasacosmosmessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nasacosmosmessenger.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for favorites table.
 *
 * Per ARCHITECTURE.md Section 8.3:
 * - INSERT OR REPLACE ensures duplicates overwrite by date
 * - Results sorted by savedAt descending (newest first)
 */
@Dao
interface FavoriteDao {

    /**
     * Insert or replace a favorite.
     * If a favorite with the same date exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    /**
     * Get all favorites sorted by savedAt descending.
     * Returns Flow for reactive UI updates.
     */
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    /**
     * Delete a favorite by date.
     */
    @Query("DELETE FROM favorites WHERE date = :date")
    suspend fun deleteFavoriteByDate(date: String)

    /**
     * Check if a favorite exists for a given date.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE date = :date)")
    suspend fun isFavorite(date: String): Boolean

    /**
     * Get total count of favorites as a reactive Flow.
     */
    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoritesCount(): Flow<Int>
}
