package com.example.nasacosmosmessenger.data.remote.dto

import kotlinx.serialization.Serializable

// ============ Request DTOs ============

@Serializable
data class GeminiRequestDto(
    val contents: List<GeminiContentDto>,
    val generationConfig: GeminiGenerationConfigDto? = null
)

@Serializable
data class GeminiContentDto(
    val role: String,
    val parts: List<GeminiPartDto>
)

@Serializable
data class GeminiPartDto(
    val text: String
)

@Serializable
data class GeminiGenerationConfigDto(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 1024
)

// ============ Response DTOs ============

@Serializable
data class GeminiResponseDto(
    val candidates: List<GeminiCandidateDto>? = null,
    val error: GeminiErrorDto? = null
)

@Serializable
data class GeminiCandidateDto(
    val content: GeminiContentDto? = null
)

@Serializable
data class GeminiErrorDto(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// ============ Helper Extensions ============

fun GeminiResponseDto.extractText(): String? {
    return candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
}

fun createGeminiRequest(prompt: String): GeminiRequestDto {
    return GeminiRequestDto(
        contents = listOf(
            GeminiContentDto(
                role = "user",
                parts = listOf(GeminiPartDto(text = prompt))
            )
        ),
        generationConfig = GeminiGenerationConfigDto(
            temperature = 0.7f,
            maxOutputTokens = 1024
        )
    )
}

fun createConversationRequest(prompt: String): GeminiRequestDto {
    return GeminiRequestDto(
        contents = listOf(
            GeminiContentDto(
                role = "user",
                parts = listOf(GeminiPartDto(text = prompt))
            )
        ),
        generationConfig = GeminiGenerationConfigDto(
            temperature = 0.7f,  // Higher for more creative responses
            maxOutputTokens = 1024
        )
    )
}
