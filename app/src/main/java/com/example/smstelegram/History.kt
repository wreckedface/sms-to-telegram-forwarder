package com.example.smstelegram
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
data class LogEntry(val sender:String,val body:String,val time:String,val ok:Boolean)
object History{
 private const val P="sms_tg_history"
 fun fingerprint(sender:String,body:String,time:String)="${sender}|${body}|${time.take(15)}".hashCode().toString()
 fun seen(c:Context,f:String)=c.getSharedPreferences(P,0).getStringSet("seen",emptySet())?.contains(f)==true
 fun mark(c:Context,f:String){val p=c.getSharedPreferences(P,0);val s=p.getStringSet("seen",emptySet())!!.toMutableSet();s.add(f);while(s.size>200)s.remove(s.first());p.edit().putStringSet("seen",s).apply()}
 fun add(c:Context,e:LogEntry){val p=c.getSharedPreferences(P,0);val old=JSONArray(p.getString("items","[]")?:"[]");val out=JSONArray();out.put(JSONObject().put("sender",e.sender).put("body",e.body).put("time",e.time).put("ok",e.ok));for(i in 0 until minOf(old.length(),49))out.put(old.getJSONObject(i));p.edit().putString("items",out.toString()).apply()}
 fun all(c:Context):List<LogEntry>{val a=JSONArray(c.getSharedPreferences(P,0).getString("items","[]")?:"[]");return (0 until a.length()).map{val o=a.getJSONObject(it);LogEntry(o.optString("sender"),o.optString("body"),o.optString("time"),o.optBoolean("ok"))}}
 fun clear(c:Context)=c.getSharedPreferences(P,0).edit().clear().apply()
}
