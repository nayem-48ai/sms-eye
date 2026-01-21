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

        // 1. Telegram
        if (modeIndex == 0 || modeIndex == 2) {
            val token = prefs.getString("bot_token", "")
            val chat = prefs.getString("chat_id", "")
            if (!token.isNullOrEmpty()) {
                val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chat&text=${URLEncoder.encode("$sender: $message", "UTF-8")}"
                client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }

        // 2. Firestore REST API Integration
        if (modeIndex == 1 || modeIndex == 2) {
            val projectId = prefs.getString("fb_id", "")
            if (!projectId.isNullOrEmpty()) {
                // Firestore REST API URL
                val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/sms_forward"
                
                // Firestore JSON format requires specific wrapping
                val json = JSONObject("""
                    {
                      "fields": {
                        "sender": {"stringValue": "$sender"},
                        "message": {"stringValue": "$message"},
                        "timestamp": {"timestampValue": "${java.time.Instant.now()}"}
                      }
                    }
                """.trimIndent())

                val body = json.toString().toRequestBody("application/json".toMediaType())
                client.newCall(Request.Builder().url(url).post(body).build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }
    }
}
