package com.example.nasacosmosmessenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing favorite APOD entries.
 *
 * Schema defined in ARCHITECTURE.md Section 3.2.
 * Uses date as primary key - duplicate saves overwrite existing entry.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: String,
    val thumbnailUrl: String?,
    val copyright: String?,
    val savedAt: Long
)
