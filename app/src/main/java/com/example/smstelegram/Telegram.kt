package com.example.smstelegram
import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

object Telegram {
 interface Callback{fun done(ok:Boolean,detail:String)}
 private fun call(c:Context,method:String,params:String="",cb:(Boolean,String,String?)->Unit){
  val token=Store.token(c);if(token.isBlank()){cb(false,"Bot token missing",null);return}
  thread{try{
   val con=URL("https://api.telegram.org/bot$token/$method").openConnection() as HttpURLConnection
   con.requestMethod="POST";con.doOutput=true;con.connectTimeout=10000;con.readTimeout=10000
   con.setRequestProperty("Content-Type","application/x-www-form-urlencoded")
   con.outputStream.use{it.write(params.toByteArray())}
   val ok=con.responseCode in 200..299
   val text=(if(ok)con.inputStream else con.errorStream).bufferedReader().readText();con.disconnect()
   cb(ok,if(ok)"OK" else "Telegram HTTP error",text)
  }catch(e:Exception){cb(false,e.message?:"Network error",null)}
  }
 }
 fun send(c:Context,message:String,cb:Callback?=null){
  val chat=Store.chatId(c);if(chat.isBlank()){cb?.done(false,"Chat ID missing");return}
  val p="chat_id=${URLEncoder.encode(chat,"UTF-8")}&text=${URLEncoder.encode(message,"UTF-8")}"
  call(c,"sendMessage",p){ok,d,_->cb?.done(ok,if(ok)"Delivered" else d)}
 }
 fun discoverChat(c:Context,cb:(String?,String)->Unit){
  call(c,"getUpdates"){ok,d,body->
   if(!ok||body==null){cb(null,d);return@call}
   try{
    val arr=JSONObject(body).getJSONArray("result")
    for(i in arr.length()-1 downTo 0){
     val u=arr.getJSONObject(i);val m=u.optJSONObject("message")?:continue;val ch=m.optJSONObject("chat")?:continue
     val id=ch.optLong("id",Long.MIN_VALUE);if(id!=Long.MIN_VALUE){cb(id.toString(),"Chat found");return@call}
    }
    cb(null,"No chat found. Open your bot in Telegram and send it a message first.")
   }catch(_:Exception){cb(null,"Could not parse Telegram response")}
  }
 }
}
