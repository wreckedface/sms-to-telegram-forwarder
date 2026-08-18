package com.example.smstelegram

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RetryWorker(app:Context,params:WorkerParameters):Worker(app,params){
 override fun doWork():Result{
  val c=applicationContext
  if(!Store.enabled(c)) return Result.success()
  val pending=RetryStore.all(c)
  if(pending.isEmpty()) return Result.success()
  var remaining=false
  pending.reversed().forEach{
   val text="📩 New SMS\nFrom: ${it.sender}\n\n${it.body}\n\n🕒 ${it.time}"
   Telegram.send(c,text,object:Telegram.Callback{
    override fun done(ok:Boolean,detail:String){}
   })
  }
  RetryStore.clear(c)
  return if(remaining) Result.retry() else Result.success()
 }
}