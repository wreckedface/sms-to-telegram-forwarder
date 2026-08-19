package com.example.smstelegram

import android.content.Context

object Stats {
    private const val P = "sms_tg_stats"

    private fun p(c: Context) =
        c.getSharedPreferences(P, Context.MODE_PRIVATE)

    fun received(c: Context): Long =
        p(c).getLong("received", 0L)

    fun forwarded(c: Context): Long =
        p(c).getLong("forwarded", 0L)

    fun failed(c: Context): Long =
        p(c).getLong("failed", 0L)

    fun filtered(c: Context): Long =
        p(c).getLong("filtered", 0L)

    fun incrementReceived(c: Context, n: Int = 1) {
        p(c).edit()
            .putLong("received", received(c) + n)
            .apply()
    }

    fun incrementForwarded(c: Context) {
        p(c).edit()
            .putLong("forwarded", forwarded(c) + 1L)
            .apply()
    }

    fun incrementFailed(c: Context) {
        p(c).edit()
            .putLong("failed", failed(c) + 1L)
            .apply()
    }

    fun incrementFiltered(c: Context) {
        p(c).edit()
            .putLong("filtered", filtered(c) + 1L)
            .apply()
    }

    fun clear(c: Context) {
        p(c).edit().clear().apply()
    }
}
