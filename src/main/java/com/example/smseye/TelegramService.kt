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

        // --- ১. টেলিগ্রাম ফরোয়ার্ডিং ---
        if (modeIndex == 0 || modeIndex == 2) {
            val token = prefs.getString("bot_token", "")
            val chat = prefs.getString("chat_id", "")
            if (!token.isNullOrEmpty()) {
                val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chat&text=${URLEncoder.encode("$sender: $message", "UTF-8")}"
                val request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }

        // --- ২. ফায়ারস্টোর ফরোয়ার্ডিং (Rest Client-এর হুবহু কপি) ---
        if (modeIndex == 1 || modeIndex == 2) {
            val projectId = prefs.getString("fb_id", "")
            val apiKey = prefs.getString("fb_key", "")
            
            if (!projectId.isNullOrEmpty()) {
                // Rest Client-এর সেই সফল URL
                val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/sms_forward?key=$apiKey"
                
                // Rest Client-এ আপনি যে বডি পাঠিয়েছেন ঠিক সেটিই এখানে তৈরি হচ্ছে
                val rootJson = JSONObject()
                val fieldsJson = JSONObject()
                
                fieldsJson.put("sender", JSONObject().put("stringValue", sender))
                fieldsJson.put("message", JSONObject().put("stringValue", message))
                rootJson.put("fields", fieldsJson)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = rootJson.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json") // হেডার নিশ্চিত করা
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // নেটওয়ার্ক ফেইল করলে এখানে আসবে
                    }
                    override fun onResponse(call: Call, response: Response) {
                        // সফল হলে বা কোনো এরর কোড (যেমন 400, 403) আসলে এখানে আসবে
                        response.close()
                    }
                })
            }
        }
    }
}
