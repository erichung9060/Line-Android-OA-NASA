package com.example.nasacosmosmessenger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apod_cache")
data class ApodCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "explanation")
    val explanation: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "hd_url")
    val hdUrl: String?,

    @ColumnInfo(name = "media_type")
    val mediaType: String,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,

    @ColumnInfo(name = "copyright")
    val copyright: String?,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long
)
