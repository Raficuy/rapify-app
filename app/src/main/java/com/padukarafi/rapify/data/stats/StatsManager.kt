package com.padukarafi.rapify.data.stats

import android.content.Context
import android.util.Log
import com.padukarafi.rapify.data.remote.HttpClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Penghitung total pengguna (anonim, tanpa login).
 *
 * - Setiap instalasi punya ID acak (UUID) yang dibuat lokal. Tidak terhubung ke akun Google,
 *   nomor HP, IMEI, atau data pribadi apa pun.
 * - Maksimal 1x ping per hari, lalu mengambil total pengguna untuk ditampilkan di Settings.
 * - Kalau STATS_BASE_URL kosong, fitur ini mati total (tidak ada request jaringan sama sekali).
 */
object StatsManager {
    // TODO: isi dengan URL Cloudflare Worker kamu setelah deploy, contoh:
    // "https://rapify-stats.namakamu.workers.dev"
    const val STATS_BASE_URL = ""

    private const val TAG = "RapifyStats"
    private const val PREFS = "rapify_stats"
    private const val KEY_ID = "install_id"
    private const val KEY_LAST_PING_DAY = "last_ping_day"
    private const val KEY_TOTAL = "cached_total"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _totalUsers = MutableStateFlow<Long?>(null)
    val totalUsers: StateFlow<Long?> = _totalUsers

    fun init(context: Context) {
        if (STATS_BASE_URL.isBlank()) return
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Tampilkan angka terakhir yang tersimpan dulu, biar footer langsung terisi
        if (prefs.contains(KEY_TOTAL)) _totalUsers.value = prefs.getLong(KEY_TOTAL, 0L)

        scope.launch {
            try {
                val id = prefs.getString(KEY_ID, null) ?: UUID.randomUUID().toString().also {
                    prefs.edit().putString(KEY_ID, it).apply()
                }
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                if (prefs.getString(KEY_LAST_PING_DAY, null) != today) {
                    if (ping(id)) prefs.edit().putString(KEY_LAST_PING_DAY, today).apply()
                }
                fetchTotal()?.let {
                    _totalUsers.value = it
                    prefs.edit().putLong(KEY_TOTAL, it).apply()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Stats sync skipped: ${e.message}")
            }
        }
    }

    private fun ping(id: String): Boolean {
        val body = JSONObject().put("id", id).toString()
            .toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("${STATS_BASE_URL.trimEnd('/')}/ping")
            .post(body)
            .build()
        HttpClientProvider.client.newCall(req).execute().use { return it.isSuccessful }
    }

    private fun fetchTotal(): Long? {
        val req = Request.Builder()
            .url("${STATS_BASE_URL.trimEnd('/')}/stats")
            .get()
            .build()
        HttpClientProvider.client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return null
            val json = JSONObject(res.body?.string() ?: return null)
            return json.optLong("total", -1L).takeIf { it >= 0 }
        }
    }
}
