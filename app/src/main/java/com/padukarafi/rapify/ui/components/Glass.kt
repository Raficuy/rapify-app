package com.padukarafi.rapify.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawOutline
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.padukarafi.rapify.ui.theme.RapifyAppTheme

/*
 * RapiFy "liquid glass" — aturan mainnya:
 *
 *  1. Glass hanya untuk permukaan yang MELAYANG di atas konten (nav bar, mini player,
 *     chip, kartu, tombol di atas artwork). Baris list biasa tetap polos.
 *  2. Tetap monokrom. Warna hanya datang dari artwork lagu yang sedang diputar,
 *     itupun lewat AmbientBackdrop dengan intensitas rendah.
 *  3. Tidak ada blur real-time di atas list yang di-scroll (jaga 120 FPS & baterai).
 *     Kesan "kaca" dibuat dari: isi gradien tipis + highlight spekular + rim light 0.75dp.
 *  4. Teks selalu high-contrast. Kalau ragu, kurangi alpha, jangan tambah efek.
 */

enum class GlassLevel {
    /** Baris/kartu yang cuma perlu dibedakan halus dari latar. */
    Subtle,

    /** Kartu, chip, input. Default. */
    Regular,

    /** Elemen melayang: nav bar, mini player, tombol di atas artwork. */
    Floating
}

/**
 * Permukaan kaca: isi gradien translusen + sheen di pojok kiri-atas + rim light gradien.
 * Sudah termasuk clip ke [shape], jadi tidak perlu `.clip()` terpisah.
 */
fun Modifier.glass(
    shape: Shape,
    isDark: Boolean,
    level: GlassLevel = GlassLevel.Regular,
    tint: Color = Color.White
): Modifier = this
    .clip(shape)
    .drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)

        val fillTop: Color
        val fillBottom: Color
        val sheen: Color
        val rimHi: Color
        val rimMid: Color
        val rimLo: Color

        if (isDark) {
            val white = lerp(Color.White, tint, 0.55f)
            val rimBase = lerp(Color.White, tint, 0.30f)
            when (level) {
                GlassLevel.Subtle -> {
                    fillTop = white.copy(alpha = 0.09f); fillBottom = white.copy(alpha = 0.045f)
                    sheen = white.copy(alpha = 0.05f)
                    rimHi = rimBase.copy(alpha = 0.22f); rimMid = rimBase.copy(alpha = 0.05f); rimLo = rimBase.copy(alpha = 0.10f)
                }
                GlassLevel.Regular -> {
                    fillTop = white.copy(alpha = 0.16f); fillBottom = white.copy(alpha = 0.08f)
                    sheen = white.copy(alpha = 0.10f)
                    rimHi = rimBase.copy(alpha = 0.38f); rimMid = rimBase.copy(alpha = 0.07f); rimLo = rimBase.copy(alpha = 0.18f)
                }
                GlassLevel.Floating -> {
                    fillTop = white.copy(alpha = 0.22f); fillBottom = white.copy(alpha = 0.11f)
                    sheen = white.copy(alpha = 0.14f)
                    rimHi = rimBase.copy(alpha = 0.48f); rimMid = rimBase.copy(alpha = 0.08f); rimLo = rimBase.copy(alpha = 0.22f)
                }
            }
        } else {
            val black = Color.Black
            when (level) {
                GlassLevel.Subtle -> {
                    fillTop = black.copy(alpha = 0.035f); fillBottom = black.copy(alpha = 0.02f)
                    sheen = Color.White.copy(alpha = 0.20f)
                    rimHi = black.copy(alpha = 0.07f); rimMid = black.copy(alpha = 0.02f); rimLo = black.copy(alpha = 0.04f)
                }
                GlassLevel.Regular -> {
                    fillTop = black.copy(alpha = 0.05f); fillBottom = black.copy(alpha = 0.03f)
                    sheen = Color.White.copy(alpha = 0.30f)
                    rimHi = black.copy(alpha = 0.10f); rimMid = black.copy(alpha = 0.03f); rimLo = black.copy(alpha = 0.06f)
                }
                GlassLevel.Floating -> {
                    fillTop = Color.White.copy(alpha = 0.72f); fillBottom = Color.White.copy(alpha = 0.50f)
                    sheen = Color.White.copy(alpha = 0.40f)
                    rimHi = black.copy(alpha = 0.12f); rimMid = black.copy(alpha = 0.04f); rimLo = black.copy(alpha = 0.08f)
                }
            }
        }

        val fillBrush = Brush.verticalGradient(
            colors = listOf(fillTop, fillBottom),
            startY = 0f,
            endY = size.height
        )
        val sheenBrush = Brush.radialGradient(
            colors = listOf(sheen, Color.Transparent),
            center = Offset(size.width * 0.18f, 0f),
            radius = size.width.coerceAtLeast(1f) * 0.75f
        )
        val rimBrush = Brush.linearGradient(
            colorStops = arrayOf(0f to rimHi, 0.5f to rimMid, 1f to rimLo),
            start = Offset.Zero,
            end = Offset(size.width, size.height)
        )
        // Digambar 2x lebih tebal karena separuhnya terpotong clip -> terlihat 0.75dp.
        val rimStroke = Stroke(width = 1.5.dp.toPx())

        onDrawBehind {
            drawOutline(outline, brush = fillBrush)
            if (level != GlassLevel.Subtle) {
                drawOutline(outline, brush = sheenBrush)
            }
            drawOutline(outline, brush = rimBrush, style = rimStroke)
        }
    }

/**
 * Latar ambient: artwork yang sangat kabur, didesaturasi sedikit, lalu dipudarkan ke hitam
 * di bagian bawah. Ini yang memberi "kedalaman" di belakang elemen kaca.
 *
 * Murah: bitmap didecode 64px lalu di-upscale (sudah halus sendiri); Modifier.blur hanya
 * aktif di Android 12+ dan tidak membahayakan versi lama (no-op).
 */
@Composable
fun AmbientBackdrop(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    intensity: Float = 0.50f
) {
    val colors = RapifyAppTheme.colors
    val context = LocalContext.current
    val alpha = if (colors.isDark) intensity else intensity * 0.55f
    val softSaturation = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.85f) })
    }

    Box(modifier = modifier.background(colors.background)) {
        Crossfade(
            targetState = imageUrl,
            animationSpec = tween(650),
            label = "ambient_crossfade"
        ) { url ->
            if (url != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url)
                        .size(64)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = alpha,
                    colorFilter = softSaturation,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(40.dp, BlurredEdgeTreatment.Unbounded)
                )
            }
        }

        // Peredam tipis supaya teks putih tetap terbaca di atas cover yang terang.
        if (colors.isDark) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f))
            )
        }

        // Glow memudar ke warna latar di bagian bawah -> nav bar duduk di atas hitam bersih.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to colors.background.copy(alpha = 0.55f),
                        1f to colors.background
                    )
                )
        )
    }
}

/** Tombol ikon bulat berbahan kaca, untuk dipakai di atas artwork. */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color.Black.copy(alpha = 0.22f), CircleShape)
            .glass(CircleShape, isDark = true, level = GlassLevel.Floating)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
