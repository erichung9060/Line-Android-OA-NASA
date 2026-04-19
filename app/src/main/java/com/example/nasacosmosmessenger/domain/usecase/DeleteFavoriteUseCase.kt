package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.repository.FavoriteRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for deleting a favorite by date.
 */
class DeleteFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(date: LocalDate) {
        favoriteRepository.deleteFavorite(date)
    }
}
