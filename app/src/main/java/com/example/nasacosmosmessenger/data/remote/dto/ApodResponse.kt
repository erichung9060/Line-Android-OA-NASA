package com.example.nasacosmosmessenger.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApodResponse(
    @SerialName("date")
    val date: String,

    @SerialName("title")
    val title: String,

    @SerialName("explanation")
    val explanation: String,

    @SerialName("url")
    val url: String,

    @SerialName("hdurl")
    val hdUrl: String? = null,

    @SerialName("media_type")
    val mediaType: String,

    @SerialName("copyright")
    val copyright: String? = null,

    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null
)
