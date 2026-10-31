package com.padukarafi.rapify.data.update

import android.content.Context
import com.padukarafi.rapify.BuildConfig

/** Menandai apakah "Yang baru" untuk versi yang sedang terpasang sudah dilihat user. */
object WhatsNew {
    private const val PREFS = "rapify_whatsnew"
    private const val KEY_LAST_SEEN = "last_seen_version"

    fun shouldShow(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_SEEN, null) != BuildConfig.VERSION_NAME

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_SEEN, BuildConfig.VERSION_NAME)
            .apply()
    }
}
