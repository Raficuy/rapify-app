package com.padukarafi.rapify

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.padukarafi.rapify.data.auth.AuthManager
import com.padukarafi.rapify.data.remote.RapifyDownloader
import org.schabi.newpipe.extractor.NewPipe

class RapifyApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        AuthManager.init(this)
        val settings = com.padukarafi.rapify.data.settings.SettingsManager.init(this)
        try {
            val savedLang = settings.language.value
            if (savedLang.code.isNotEmpty()) {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(savedLang.code)
                )
            }
        } catch (_: Exception) {}
        val downloader = RapifyDownloader.init(this)
        NewPipe.init(downloader)
        com.padukarafi.rapify.data.stats.StatsManager.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(false)
            .respectCacheHeaders(false)
            .build()
    }
}
