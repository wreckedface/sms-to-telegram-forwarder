package com.example.smstelegram

import android.content.Context

object Stats {
    private const val P="sms_tg_stats"
    private fun p(c:Context)=c.getSharedPreferences(P,0)
    fun received(c:Context)=p(c).getLong("received",0)
    fun forwarded(c:Context)=p(c).getLong("forwarded",0)
    fun failed(c:Context)=p(c).getLong("failed",0)
    fun filtered(c:Context)=p(c).getLong("filtered",0)
    fun received(c:Context,n:Int=1){p(c).edit().putLong("received",received(c)+n).apply()}
    fun forwarded(c:Context){p(c).edit().putLong("forwarded",forwarded(c)+1).apply()}
    fun failed(c:Context){p(c).edit().putLong("failed",failed(c)+1).apply()}
    fun filtered(c:Context){p(c).edit().putLong("filtered",filtered(c)+1).apply()}
    fun clear(c:Context){p(c).edit().clear().apply()}
}