package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SyncStatusBadge(
    isOnline: Boolean,
    pendingCount: Int,
    isSyncing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeBgColor by animateColorAsState(
        targetValue = when {
            isSyncing -> MaterialTheme.colorScheme.primaryContainer
            !isOnline -> MaterialTheme.colorScheme.errorContainer
            pendingCount > 0 -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        },
        label = "syncBadgeColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSyncing -> MaterialTheme.colorScheme.onPrimaryContainer
            !isOnline -> MaterialTheme.colorScheme.onErrorContainer
            pendingCount > 0 -> MaterialTheme.colorScheme.onTertiaryContainer
            else -> MaterialTheme.colorScheme.onSecondaryContainer
        },
        label = "syncBadgeContentColor"
    )

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("sync_status_badge"),
        color = badgeBgColor,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isSyncing -> Icons.Default.Sync
                    !isOnline -> Icons.Default.CloudOff
                    else -> Icons.Default.CloudDone
                },
                contentDescription = "Sync Status",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    isSyncing -> "Syncing..."
                    !isOnline -> "Offline ($pendingCount)"
                    pendingCount > 0 -> "Pending ($pendingCount)"
                    else -> "Synced"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = contentColor
            )
        }
    }
}
