package com.example.smseye

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.io.IOException

object TelegramService {
    private val client = OkHttpClient()

    fun forwardSms(context: Context, sender: String, message: String) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("is_active", true)) return

        val modeIndex = prefs.getInt("mode_index", 0)

        // 1. Telegram Forwarding
        if (modeIndex == 0 || modeIndex == 2) {
            val token = prefs.getString("bot_token", "")
            val chat = prefs.getString("chat_id", "")
            if (!token.isNullOrEmpty()) {
                val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chat&text=${URLEncoder.encode("$sender: $message", "UTF-8")}"
                client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }

        // 2. Firestore Forwarding (Rest Client Style)
        if (modeIndex == 1 || modeIndex == 2) {
            val projectId = prefs.getString("fb_id", "")
            val apiKey = prefs.getString("fb_key", "")
            
            if (!projectId.isNullOrEmpty()) {
                // Rest Client এ আপনি যেভাবে URL দিয়েছিলেন
                val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/sms_forward?key=$apiKey"
                
                // Firestore JSON Structure
                val jsonBody = JSONObject().apply {
                    val fields = JSONObject().apply {
                        put("sender", JSONObject().put("stringValue", sender))
                        put("message", JSONObject().put("stringValue", message))
                    }
                    put("fields", fields)
                }

                val body = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        // সফল হোক বা ব্যর্থ, কানেকশন ক্লোজ করা জরুরি
                        response.close()
                    }
                })
            }
        }
    }
}
