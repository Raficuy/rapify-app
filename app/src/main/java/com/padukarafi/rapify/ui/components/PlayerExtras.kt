package com.padukarafi.rapify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.padukarafi.rapify.R
import com.padukarafi.rapify.data.model.Track
import com.padukarafi.rapify.service.SleepTimer
import com.padukarafi.rapify.ui.theme.RapifyAppTheme
import com.padukarafi.rapify.ui.theme.Typography
import com.padukarafi.rapify.viewmodel.MusicViewModel

/** Daftar antrean: lagu yang sedang diputar + yang berikutnya. Ketuk untuk langsung memutar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val colors = RapifyAppTheme.colors
    val current by viewModel.currentTrack.collectAsState()
    var version by remember { mutableStateOf(0) }

    val queue = remember(current?.id, version) { viewModel.queueSnapshot() }
    val index = remember(current?.id, version) { viewModel.queueIndex() }
    val upcoming = remember(queue, index) {
        if (index in queue.indices) queue.drop(index + 1) else queue.filter { it.id != current?.id }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.background
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.player_queue),
                style = Typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            current?.let { track ->
                Text(
                    text = stringResource(R.string.queue_now_playing),
                    style = Typography.labelMedium,
                    color = colors.textMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                QueueRow(
                    track = track,
                    highlighted = true,
                    onClick = {},
                    onPlayNext = null,
                    onRemove = null
                )
            }

            Text(
                text = stringResource(R.string.queue_up_next),
                style = Typography.labelMedium,
                color = colors.textMuted,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
            )

            if (upcoming.isEmpty()) {
                Text(
                    text = stringResource(R.string.queue_empty),
                    style = Typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    itemsIndexed(upcoming, key = { i, t -> "${t.id}#$i" }) { i, track ->
                        QueueRow(
                            track = track,
                            highlighted = false,
                            onClick = {
                                viewModel.playFromQueue(track.id)
                                version++
                            },
                            onPlayNext = if (i > 0) {
                                {
                                    viewModel.playNextInQueue(track.id)
                                    version++
                                }
                            } else null,
                            onRemove = {
                                viewModel.removeFromQueue(track.id)
                                version++
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    track: Track,
    highlighted: Boolean,
    onClick: () -> Unit,
    onPlayNext: (() -> Unit)?,
    onRemove: (() -> Unit)?
) {
    val colors = RapifyAppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (highlighted) Modifier.glass(RoundedCornerShape(12.dp), colors.isDark, GlassLevel.Subtle)
                else Modifier
            )
            .clickable(enabled = !highlighted, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.thumbnailUrlForDisplay(192),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = Typography.bodyMedium,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onPlayNext != null) {
            IconButton(onClick = onPlayNext, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.queue_play_next),
                    tint = colors.textMuted
                )
            }
        }
        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.queue_remove),
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Pilihan timer tidur. Timer-nya sendiri hidup di [SleepTimer] (proses service). */
@Composable
fun SleepTimerDialog(onDismiss: () -> Unit) {
    val colors = RapifyAppTheme.colors
    val endMs by SleepTimer.endMs.collectAsState()
    val remainingMin = if (endMs > 0L) {
        ((endMs - System.currentTimeMillis()) / 60_000L + 1).toInt().coerceAtLeast(1)
    } else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.background,
        title = {
            Text(
                text = stringResource(R.string.sleep_timer_title),
                style = Typography.titleLarge,
                color = colors.textPrimary
            )
        },
        text = {
            Column {
                if (remainingMin > 0) {
                    Text(
                        text = stringResource(R.string.sleep_timer_active, remainingMin),
                        style = Typography.bodyMedium,
                        color = colors.accent,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                listOf(15, 30, 45, 60, 90).forEach { minutes ->
                    Text(
                        text = stringResource(R.string.sleep_timer_minutes, minutes),
                        style = Typography.bodyLarge,
                        color = colors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                SleepTimer.start(minutes)
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (remainingMin > 0) {
                TextButton(onClick = {
                    SleepTimer.cancel()
                    onDismiss()
                }) {
                    Text(stringResource(R.string.sleep_timer_off), color = colors.textPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), color = colors.textMuted)
            }
        }
    )
}
