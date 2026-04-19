package com.example.nasacosmosmessenger.data.mapper

import com.example.nasacosmosmessenger.data.local.entity.FavoriteEntity
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.model.MediaType
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Mapper for converting between Favorite domain model, FavoriteEntity, and Apod.
 */
class FavoriteMapper @Inject constructor() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toDomain(entity: FavoriteEntity): Favorite {
        return Favorite(
            date = LocalDate.parse(entity.date, dateFormatter),
            title = entity.title,
            explanation = entity.explanation,
            url = entity.url,
            hdUrl = entity.hdUrl,
            mediaType = MediaType.fromString(entity.mediaType),
            thumbnailUrl = entity.thumbnailUrl,
            copyright = entity.copyright,
            savedAt = Instant.ofEpochMilli(entity.savedAt)
        )
    }

    fun toEntity(favorite: Favorite): FavoriteEntity {
        return FavoriteEntity(
            date = favorite.date.format(dateFormatter),
            title = favorite.title,
            explanation = favorite.explanation,
            url = favorite.url,
            hdUrl = favorite.hdUrl,
            mediaType = favorite.mediaType.name.lowercase(),
            thumbnailUrl = favorite.thumbnailUrl,
            copyright = favorite.copyright,
            savedAt = favorite.savedAt.toEpochMilli()
        )
    }

    fun fromApod(apod: Apod): Favorite {
        return Favorite(
            date = apod.date,
            title = apod.title,
            explanation = apod.explanation,
            url = apod.url,
            hdUrl = apod.hdUrl,
            mediaType = apod.mediaType,
            thumbnailUrl = apod.thumbnailUrl,
            copyright = apod.copyright,
            savedAt = Instant.now()
        )
    }
}
