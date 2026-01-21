package com.example.smseye

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object TelegramService {
    private val client = OkHttpClient()

    fun forwardSms(context: Context, sender: String, message: String) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val mode = prefs.getString("forward_mode", "Telegram") ?: "Telegram"
        
        // ক্লিন মেসেজ টেমপ্লেট
        val cleanText = "$sender: $message"

        if (mode == "Telegram" || mode == "Both") {
            val token = prefs.getString("bot_token", "")
            val chatId = prefs.getString("chat_id", "")
            if (!token.isNullOrEmpty() && !chatId.isNullOrEmpty()) {
                val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&text=$cleanText"
                val request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }

        if (mode == "Firebase" || mode == "Both") {
            val projectId = "autopay-c8eea" // আপনার দেওয়া প্রজেক্ট আইডি
            val url = "https://$projectId-default-rtdb.firebaseio.com/sms_logs.json"
            
            val json = JSONObject().apply {
                put("sender", sender)
                put("message", message)
                put("timestamp", System.currentTimeMillis())
            }
            
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) { response.close() }
            })
        }
    }
}
