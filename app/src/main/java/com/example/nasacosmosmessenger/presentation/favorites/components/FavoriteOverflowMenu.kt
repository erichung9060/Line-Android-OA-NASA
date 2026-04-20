package com.example.nasacosmosmessenger.presentation.favorites.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.nasacosmosmessenger.domain.model.Favorite

/**
 * Overflow menu for favorite cards.
 *
 * Per ARCHITECTURE.md Section 7.2:
 * - Delete favorite action (all items)
 * - Open source link action (video items only)
 */
@Composable
fun FavoriteOverflowMenu(
    favorite: Favorite,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    IconButton(
        onClick = { expanded = true },
        modifier = modifier.size(48.dp) // Minimum touch target per Material guidelines
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options for ${favorite.title}",
            tint = Color.White
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        if (favorite.isVideo) {
            DropdownMenuItem(
                text = { Text("Open source link") },
                onClick = {
                    expanded = false
                    uriHandler.openUri(favorite.sourceUrl)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null)
                }
            )
        }

        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                expanded = false
                onDeleteClick()
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        )
    }
}
