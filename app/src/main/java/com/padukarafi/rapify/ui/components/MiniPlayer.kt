package com.padukarafi.rapify.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.padukarafi.rapify.R
import com.padukarafi.rapify.data.model.Track
import com.padukarafi.rapify.ui.theme.DarkBorder
import com.padukarafi.rapify.ui.theme.DarkCard
import com.padukarafi.rapify.ui.theme.MutedText
import com.padukarafi.rapify.ui.theme.PureWhite
import com.padukarafi.rapify.ui.theme.Typography

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = com.padukarafi.rapify.ui.theme.RapifyAppTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(22.dp), colors.isDark, GlassLevel.Floating, LocalCoverTint.current, frost = 0.93f)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = track,
                    transitionSpec = { androidx.compose.animation.fadeIn(tween(210)) togetherWith androidx.compose.animation.fadeOut(tween(170)) },
                    label = "mini_artwork"
                ) { animatedTrack ->
                    AsyncImage(
                        model = animatedTrack.thumbnailUrlForDisplay(192),
                        contentDescription = animatedTrack.title,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedContent(
                    targetState = track,
                    transitionSpec = { androidx.compose.animation.fadeIn(tween(210)) togetherWith androidx.compose.animation.fadeOut(tween(170)) },
                    label = "mini_metadata"
                ) { animatedTrack ->
                    Column {
                        Text(animatedTrack.title, style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(animatedTrack.artist, style = Typography.bodyMedium, fontWeight = FontWeight.Normal, color = colors.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.textPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                val playScale by animateFloatAsState(
                    targetValue = if (isPlaying) 0.94f else 1f,
                    animationSpec = tween(durationMillis = 140),
                    label = "mini_play_scale"
                )
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.ic_rapify_pause) else painterResource(R.drawable.ic_rapify_play),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(26.dp).graphicsLayer { scaleX = playScale; scaleY = playScale }
                    )
                }
            }

            IconButton(onClick = onSkipNextClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_rapify_fast_forward),
                    contentDescription = "Next",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(2.dp)
                .clip(CircleShape)
                .background(colors.textPrimary.copy(alpha = 0.14f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(colors.textPrimary.copy(alpha = 0.85f))
            )
        }
    }
}
