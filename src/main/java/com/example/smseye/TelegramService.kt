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

        // ১. টেলিগ্রাম ফরোয়ার্ডিং
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

        // ২. ফায়ারস্টোর ফরোয়ার্ডিং (Rest Client-এর হুবহু কপি)
        if (modeIndex == 1 || modeIndex == 2) {
            val projectId = prefs.getString("fb_id", "")
            val apiKey = prefs.getString("fb_key", "")
            
            if (!projectId.isNullOrEmpty()) {
                // Background Thread এ রিকোয়েস্ট পাঠানো হচ্ছে যেন Android OS বাধা না দেয়
                Thread {
                    try {
                        val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/sms_forward?key=$apiKey"
                        
                        // JSON Body গঠন (Rest Client ফরম্যাটে)
                        val rootJson = JSONObject()
                        val fieldsJson = JSONObject()
                        fieldsJson.put("sender", JSONObject().put("stringValue", sender))
                        fieldsJson.put("message", JSONObject().put("stringValue", message))
                        rootJson.put("fields", fieldsJson)

                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val body = rootJson.toString().toRequestBody(mediaType)

                        val request = Request.Builder()
                            .url(url)
                            .addHeader("Content-Type", "application/json")
                            .post(body)
                            .build()

                        // Synchronous কল ব্যবহার করা হয়েছে Background Thread এর ভেতরে
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            // সফল হয়েছে
                        }
                        response.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
    }
}
