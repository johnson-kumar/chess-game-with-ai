package com.example.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun ControlBar(
    canUndo: Boolean,
    onUndoClick: () -> Unit,
    onHintClick: () -> Unit,
    onFlipClick: () -> Unit,
    onMovesSheetClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo Button
        FilledTonalIconButton(
            onClick = onUndoClick,
            enabled = canUndo,
            modifier = Modifier.size(50.dp).testTag("button_undo"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo Move",
                modifier = Modifier.size(24.dp)
            )
        }

        // Hint Button
        FilledTonalIconButton(
            onClick = onHintClick,
            modifier = Modifier.size(50.dp).testTag("button_hint"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Get Move Hint",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Flip Board Button
        FilledTonalIconButton(
            onClick = onFlipClick,
            modifier = Modifier.size(50.dp).testTag("button_flip"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = "Flip Board Perspective",
                modifier = Modifier.size(24.dp)
            )
        }

        // Move History Button
        FilledTonalIconButton(
            onClick = onMovesSheetClick,
            modifier = Modifier.size(50.dp).testTag("button_history"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.FormatListNumbered,
                contentDescription = "View Move History",
                modifier = Modifier.size(24.dp)
            )
        }

        // Settings / New Game Button
        FilledTonalIconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(50.dp).testTag("button_settings"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Game Settings & Modes",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
