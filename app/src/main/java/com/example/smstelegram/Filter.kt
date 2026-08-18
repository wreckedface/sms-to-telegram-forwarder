package com.example.smstelegram

object Filter {
    fun allow(context:android.content.Context, sender:String, body:String):Boolean {
        val wl=Store.whitelist(context).split(",","\n").map{it.trim()}.filter{it.isNotEmpty()}
        if(wl.isNotEmpty() && wl.none{sender.contains(it,true)}) return false
        val keys=Store.keywords(context).split(",","\n").map{it.trim()}.filter{it.isNotEmpty()}
        if(keys.isNotEmpty() && keys.none{body.contains(it,true)}) return false
        return true
    }
}
