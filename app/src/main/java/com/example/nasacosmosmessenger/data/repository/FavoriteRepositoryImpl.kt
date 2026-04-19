package com.example.nasacosmosmessenger.data.repository

import com.example.nasacosmosmessenger.data.local.dao.FavoriteDao
import com.example.nasacosmosmessenger.data.mapper.FavoriteMapper
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FavoriteRepository using Room database.
 *
 * Per ARCHITECTURE.md Section 8.3:
 * - Uses INSERT OR REPLACE via DAO (duplicates overwrite by date)
 * - Flow-based API for reactive UI updates
 */
@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val favoriteMapper: FavoriteMapper
) : FavoriteRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getAllFavorites(): Flow<List<Favorite>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { favoriteMapper.toDomain(it) }
        }
    }

    override suspend fun saveFavorite(favorite: Favorite) {
        favoriteDao.insertFavorite(favoriteMapper.toEntity(favorite))
    }

    override suspend fun saveFavoriteFromApod(apod: Apod) {
        val favorite = favoriteMapper.fromApod(apod)
        favoriteDao.insertFavorite(favoriteMapper.toEntity(favorite))
    }

    override suspend fun deleteFavorite(date: LocalDate) {
        favoriteDao.deleteFavoriteByDate(date.format(dateFormatter))
    }

    override suspend fun isFavorite(date: LocalDate): Boolean {
        return favoriteDao.isFavorite(date.format(dateFormatter))
    }

    override fun getFavoritesCount(): Flow<Int> {
        return favoriteDao.getFavoritesCount()
    }
}
