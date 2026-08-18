package com.example.smstelegram

import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object Store {
    private const val PREF="sms_tg"; private const val KEY="telegram_token"; private const val ALIAS="sms_tg_key"
    private fun prefs(c:Context)=c.getSharedPreferences(PREF,0)
    private fun key():SecretKey{
        val ks=KeyStore.getInstance("AndroidKeyStore").apply{load(null)}
        (ks.getEntry(ALIAS,null) as? KeyStore.SecretKeyEntry)?.let{return it.secretKey}
        val kg=KeyGenerator.getInstance("AES","AndroidKeyStore")
        kg.init(android.security.keystore.KeyGenParameterSpec.Builder(ALIAS,3)
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return kg.generateKey()
    }
    private fun enc(v:String):String{if(v.isBlank())return "";val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(1,key());return Base64.encodeToString(c.iv+c.doFinal(v.toByteArray()),2)}
    private fun dec(v:String):String{if(v.isBlank())return "";return try{val r=Base64.decode(v,2);val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(2,key(),javax.crypto.spec.GCMParameterSpec(128,r.copyOfRange(0,12)));String(c.doFinal(r.copyOfRange(12,r.size)))}catch(_:Exception){""}}
    fun token(c:Context)=dec(prefs(c).getString(KEY,"")?:"")
    fun chatId(c:Context)=prefs(c).getString("chat","")?:""
    fun enabled(c:Context)=prefs(c).getBoolean("enabled",false)
    fun whitelist(c:Context)=prefs(c).getString("whitelist","")?:""
    fun keywords(c:Context)=prefs(c).getString("keywords","")?:""
    fun dedupe(c:Context)=prefs(c).getBoolean("dedupe",true)
    fun redact(c:Context)=prefs(c).getBoolean("redact",false)
    fun save(c:Context,t:String,chat:String,on:Boolean,wl:String,kw:String,d:Boolean,r:Boolean)=prefs(c).edit()
      .putString(KEY,enc(t)).putString("chat",chat).putBoolean("enabled",on).putString("whitelist",wl)
      .putString("keywords",kw).putBoolean("dedupe",d).putBoolean("redact",r).apply()
}
