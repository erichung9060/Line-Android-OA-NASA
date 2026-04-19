package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Use case for saving an APOD to favorites.
 *
 * Per ARCHITECTURE.md Section 8.3:
 * - Converts Apod to Favorite with current timestamp
 * - Duplicates are overwritten (INSERT OR REPLACE)
 */
class SaveFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(apod: Apod) {
        favoriteRepository.saveFavoriteFromApod(apod)
    }
}
