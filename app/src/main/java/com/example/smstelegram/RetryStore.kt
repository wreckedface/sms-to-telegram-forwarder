package com.example.smstelegram

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PendingMessage(val sender:String,val body:String,val time:String)

object RetryStore {
    private const val P="sms_tg_retry"
    fun add(c:Context,m:PendingMessage){
        val p=c.getSharedPreferences(P,0);val a=JSONArray(p.getString("items","[]")?:"[]")
        val out=JSONArray();out.put(JSONObject().put("sender",m.sender).put("body",m.body).put("time",m.time))
        for(i in 0 until minOf(a.length(),24))out.put(a.getJSONObject(i))
        p.edit().putString("items",out.toString()).apply()
    }
    fun all(c:Context):List<PendingMessage>{
        val a=JSONArray(c.getSharedPreferences(P,0).getString("items","[]")?:"[]")
        return (0 until a.length()).map{val o=a.getJSONObject(it);PendingMessage(o.getString("sender"),o.getString("body"),o.getString("time"))}
    }
    fun clear(c:Context)=c.getSharedPreferences(P,0).edit().clear().apply()
}