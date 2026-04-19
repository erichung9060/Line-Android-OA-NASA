package com.example.nasacosmosmessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nasacosmosmessenger.data.local.entity.ApodCacheEntity

@Dao
interface ApodCacheDao {

    @Query("SELECT * FROM apod_cache WHERE date = :date")
    suspend fun getByDate(date: String): ApodCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(apod: ApodCacheEntity)

    @Query("DELETE FROM apod_cache WHERE cached_at < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("SELECT * FROM apod_cache ORDER BY cached_at DESC")
    suspend fun getAll(): List<ApodCacheEntity>
}
