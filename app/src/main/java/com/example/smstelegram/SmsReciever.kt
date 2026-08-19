package com.example.smstelegram

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SmsReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,i:Intent){
  if(i.action!=Telephony.Sms.Intents.SMS_RECEIVED_ACTION||!Store.enabled(c))return
  val ms=Telephony.Sms.Intents.getMessagesFromIntent(i);if(ms.isEmpty())return
  val sender=ms.first().displayOriginatingAddress?:"Unknown";val body=ms.joinToString(""){it.messageBody?:""}
  Stats.received(c)
  if(!Filter.allow(c,sender,body)){Stats.filtered(c);return}
  val time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(Date())
  val fp=History.fingerprint(sender,body,time);if(Store.dedupe(c)&&History.seen(c,fp))return;History.mark(c,fp)
  val safe=if(Store.redact(c))body.replace(Regex("\\b\\d{4,8}\\b"),"••••") else body
  val msg="📩 New SMS\nFrom: $sender\n\n$safe\n\n🕒 $time"
  Telegram.send(c,msg,object:Telegram.Callback{
   override fun done(ok:Boolean,detail:String){
    if(ok){Stats.forwarded(c);History.add(c,LogEntry(sender,if(Store.redact(c))"[redacted]" else body,time,true))}
    else{Stats.failed(c);RetryStore.add(c,PendingMessage(sender,safe,time));History.add(c,LogEntry(sender,if(Store.redact(c))"[redacted]" else body,time,false));WorkManager.getInstance(c).enqueue(OneTimeWorkRequestBuilder<RetryWorker>().build());Log.w("SmsTelegram",detail)}
   }
  })
 }
}
