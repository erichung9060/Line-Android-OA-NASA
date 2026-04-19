package com.example.nasacosmosmessenger.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Favorite
import com.example.nasacosmosmessenger.domain.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generator for birthday cosmos cards.
 *
 * Per ARCHITECTURE.md Section 10:
 * - Card dimensions: 1080x1350 (4:5 aspect ratio)
 * - Image cropped to square at top
 * - Text overlay with gradient background at bottom
 * - Saves to cache directory for sharing
 */
@Singleton
class BirthdayCardGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
) {

    companion object {
        private const val CARD_WIDTH = 1080
        private const val CARD_HEIGHT = 1350
        private const val IMAGE_SIZE = 1080
        private const val PADDING = 48
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    /**
     * Result of card generation, including optional source URL for non-YouTube video fallback.
     */
    data class CardGenerationResult(
        val cardFile: File?,
        val sourceUrl: String? = null
    )

    /**
     * Generate a birthday card for an Apod domain model.
     *
     * Per ARCHITECTURE.md Section 10:
     * - Images: use APOD image directly
     * - YouTube videos: derive thumbnail from video URL
     * - Other videos: text-only fallback and preserve source link
     */
    suspend fun generateCardForApod(apod: Apod): CardGenerationResult {
        val imageUrl = resolveImageUrl(
            isVideo = apod.mediaType == MediaType.VIDEO,
            url = apod.url,
            thumbnailUrl = apod.thumbnailUrl
        )

        return if (imageUrl != null) {
            CardGenerationResult(cardFile = generateCard(imageUrl, apod.title, apod.date))
        } else {
            CardGenerationResult(
                cardFile = generateTextOnlyCard(apod.title, apod.date),
                sourceUrl = apod.url
            )
        }
    }

    /**
     * Generate a birthday card for a Favorite domain model.
     */
    suspend fun generateCardForFavorite(favorite: Favorite): CardGenerationResult {
        val imageUrl = resolveImageUrl(
            isVideo = favorite.isVideo,
            url = favorite.url,
            thumbnailUrl = favorite.thumbnailUrl
        )

        return if (imageUrl != null) {
            CardGenerationResult(cardFile = generateCard(imageUrl, favorite.title, favorite.date))
        } else {
            CardGenerationResult(
                cardFile = generateTextOnlyCard(favorite.title, favorite.date),
                sourceUrl = favorite.url
            )
        }
    }

    private suspend fun generateCard(
        imageUrl: String,
        title: String,
        date: LocalDate
    ): File? = withContext(Dispatchers.IO) {
        try {
            val imageBitmap = loadBitmap(imageUrl) ?: return@withContext null

            val cardBitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(cardBitmap)

            canvas.drawColor(Color.parseColor("#0D1B2A"))

            drawCroppedImage(canvas, imageBitmap)
            drawTextOverlay(canvas, title, date)
            drawBranding(canvas)

            saveToCache(cardBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun generateTextOnlyCard(
        title: String,
        date: LocalDate
    ): File? = withContext(Dispatchers.IO) {
        try {
            val cardBitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(cardBitmap)

            val gradient = LinearGradient(
                0f, 0f, 0f, CARD_HEIGHT.toFloat(),
                Color.parseColor("#1B263B"),
                Color.parseColor("#0D1B2A"),
                Shader.TileMode.CLAMP
            )
            val paint = Paint().apply { shader = gradient }
            canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)

            drawCenteredText(canvas, title, date)
            drawBranding(canvas)

            saveToCache(cardBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(IMAGE_SIZE, IMAGE_SIZE)
            .allowHardware(false) // Required for Canvas operations
            .build()

        val result = imageLoader.execute(request)
        return if (result is SuccessResult) {
            (result.drawable as? BitmapDrawable)?.bitmap
        } else {
            null
        }
    }

    private fun drawCroppedImage(canvas: Canvas, bitmap: Bitmap) {
        val srcSize = minOf(bitmap.width, bitmap.height)
        val srcX = (bitmap.width - srcSize) / 2
        val srcY = (bitmap.height - srcSize) / 2

        val srcRect = Rect(srcX, srcY, srcX + srcSize, srcY + srcSize)
        val dstRect = RectF(0f, 0f, IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat())

        canvas.drawBitmap(bitmap, srcRect, dstRect, null)
    }

    private fun drawTextOverlay(canvas: Canvas, title: String, date: LocalDate) {
        val textAreaTop = IMAGE_SIZE.toFloat()

        val gradient = LinearGradient(
            0f, textAreaTop, 0f, CARD_HEIGHT.toFloat(),
            Color.parseColor("#1B263B"),
            Color.parseColor("#0D1B2A"),
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply { shader = gradient }
        canvas.drawRect(0f, textAreaTop, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        val headerPaint = Paint().apply {
            color = Color.parseColor("#E0E1DD")
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("🌌 Your Birthday Cosmos", PADDING.toFloat(), textAreaTop + 56f, headerPaint)

        val datePaint = Paint().apply {
            color = Color.parseColor("#778DA9")
            textSize = 28f
            isAntiAlias = true
        }
        canvas.drawText(
            "On ${date.format(dateFormatter)}, the universe looked like this:",
            PADDING.toFloat(), textAreaTop + 100f, datePaint
        )

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("\"$title\"", PADDING.toFloat(), textAreaTop + 160f, titlePaint)
    }

    private fun drawCenteredText(canvas: Canvas, title: String, date: LocalDate) {
        val centerY = CARD_HEIGHT / 2f

        val headerPaint = Paint().apply {
            color = Color.parseColor("#E0E1DD")
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🌌 Your Birthday Cosmos", CARD_WIDTH / 2f, centerY - 80f, headerPaint)

        val datePaint = Paint().apply {
            color = Color.parseColor("#778DA9")
            textSize = 32f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("On ${date.format(dateFormatter)},", CARD_WIDTH / 2f, centerY - 20f, datePaint)
        canvas.drawText("the universe looked like this:", CARD_WIDTH / 2f, centerY + 20f, datePaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("\"$title\"", CARD_WIDTH / 2f, centerY + 100f, titlePaint)
    }

    private fun drawBranding(canvas: Canvas) {
        val brandPaint = Paint().apply {
            color = Color.parseColor("#415A77")
            textSize = 24f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#415A77")
            strokeWidth = 2f
        }
        canvas.drawLine(
            PADDING.toFloat(), CARD_HEIGHT - 60f,
            CARD_WIDTH - PADDING.toFloat(), CARD_HEIGHT - 60f,
            linePaint
        )
        canvas.drawText("NASA Cosmos Messenger", PADDING.toFloat(), CARD_HEIGHT - 28f, brandPaint)
    }

    private fun saveToCache(bitmap: Bitmap): File {
        val cacheDir = File(context.cacheDir, "birthday_cards")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val file = File(cacheDir, "birthday_cosmos_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    private fun resolveImageUrl(isVideo: Boolean, url: String, thumbnailUrl: String?): String? {
        return when {
            !isVideo -> url
            thumbnailUrl != null -> thumbnailUrl
            isYouTubeUrl(url) -> extractYouTubeThumbnailUrl(url)
            else -> null
        }
    }

    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    /**
     * Extract YouTube thumbnail URL from a YouTube video URL.
     * Returns null for non-YouTube URLs.
     */
    internal fun extractYouTubeThumbnailUrl(url: String): String? {
        val videoId = extractYouTubeVideoId(url) ?: return null
        return "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val patterns = listOf(
            Regex("""youtube\.com/watch\?v=([a-zA-Z0-9_-]+)"""),
            Regex("""youtube\.com/embed/([a-zA-Z0-9_-]+)"""),
            Regex("""youtu\.be/([a-zA-Z0-9_-]+)""")
        )
        for (pattern in patterns) {
            pattern.find(url)?.groupValues?.get(1)?.let { return it }
        }
        return null
    }
}
