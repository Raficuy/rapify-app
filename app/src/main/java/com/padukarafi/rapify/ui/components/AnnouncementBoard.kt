package com.padukarafi.rapify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.padukarafi.rapify.R
import com.padukarafi.rapify.ui.theme.RapifyAppTheme
import com.padukarafi.rapify.ui.theme.Typography

/**
 * Papan pengumuman di Home: kartu kaca dengan judul, beberapa poin, dan tombol aksi opsional.
 * Dipakai untuk "Update tersedia" dan "Yang baru di versi ini". Tidak memblokir layar.
 */
@Composable
fun AnnouncementBoard(
    title: String,
    lines: List<String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = RapifyAppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(22.dp), colors.isDark, GlassLevel.Floating, LocalCoverTint.current)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.textPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            lines.forEach { line ->
                Text(
                    text = "\u2022 $line",
                    style = Typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            if (actionLabel != null && onAction != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.textPrimary)
                        .clickable(onClick = onAction)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = Typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.background
                    )
                }
            }
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.action_close),
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
