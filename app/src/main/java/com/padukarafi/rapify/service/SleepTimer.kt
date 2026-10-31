package com.padukarafi.rapify.service

import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Timer tidur yang hidup di proses service, bukan di ViewModel, supaya tetap jalan
 * walaupun Activity ditutup selama musik masih diputar.
 */
object SleepTimer {
    private val handler = Handler(Looper.getMainLooper())
    private val _endMs = MutableStateFlow(0L)

    /** Waktu (epoch ms) timer berakhir, atau 0 kalau tidak aktif. */
    val endMs: StateFlow<Long> = _endMs.asStateFlow()

    @Volatile
    var player: Player? = null

    private val fire = Runnable {
        player?.pause()
        _endMs.value = 0L
    }

    fun start(minutes: Int) {
        handler.removeCallbacks(fire)
        if (minutes <= 0) {
            _endMs.value = 0L
            return
        }
        val delayMs = minutes * 60_000L
        _endMs.value = System.currentTimeMillis() + delayMs
        handler.postDelayed(fire, delayMs)
    }

    fun cancel() = start(0)
}
