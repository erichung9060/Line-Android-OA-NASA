package com.example.nasacosmosmessenger.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for sharing content.
 *
 * Per ARCHITECTURE.md Section 8.5:
 * - Uses FileProvider for secure file sharing
 * - Launches system share sheet
 */
@Singleton
class ShareUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Share a generated birthday card image.
     * For non-YouTube videos, optionally includes source URL text.
     *
     * @param cardFile The card image file to share
     * @param sourceUrl Optional source URL (for non-YouTube video fallback)
     */
    fun shareCard(cardFile: File, sourceUrl: String? = null) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cardFile
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            if (sourceUrl != null) {
                putExtra(Intent.EXTRA_TEXT, "Watch the video: $sourceUrl")
            }
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Birthday Cosmos").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }
}
