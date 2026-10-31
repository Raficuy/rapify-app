package com.padukarafi.rapify.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Warna dominan cover lagu yang sedang diputar. Putih = netral (tanpa tint). */
val LocalCoverTint = staticCompositionLocalOf { Color.White }

/**
 * Ambil warna rata-rata cover (tanpa library Palette), naikkan saturasinya sedikit,
 * jaga kecerahannya, lalu animasikan perpindahan antar lagu.
 */
@Composable
fun rememberCoverTint(imageUrl: String?): Color {
    val context = LocalContext.current
    var target by remember { mutableStateOf(Color.White) }

    LaunchedEffect(imageUrl) {
        target = if (imageUrl == null) {
            Color.White
        } else {
            withContext(Dispatchers.IO) { extractTint(context, imageUrl) } ?: Color.White
        }
    }

    val animated by animateColorAsState(
        targetValue = target,
        animationSpec = tween(600),
        label = "cover_tint"
    )
    return animated
}

private suspend fun extractTint(context: Context, url: String): Color? {
    return try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(32)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request) as? SuccessResult ?: return null
        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return null
        val pixel = Bitmap.createScaledBitmap(bitmap, 1, 1, true).getPixel(0, 0)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(pixel, hsv)
        hsv[1] = (hsv[1] * 1.25f).coerceIn(0f, 0.85f)
        hsv[2] = hsv[2].coerceIn(0.6f, 0.95f)
        Color(android.graphics.Color.HSVToColor(hsv))
    } catch (_: Exception) {
        null
    }
}
